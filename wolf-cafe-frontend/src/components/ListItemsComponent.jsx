import React, { useEffect, useState } from 'react';
import OrderService from '../services/OrderService'; 
import unsplashClient from "../services/UnsplashClient"
import '../styles/Menu.css';
import '../styles/Cart.css';
import {getTaxRate} from '../services/TaxRateService';
import { isStaffUser } from '../services/AuthService'; 

//GENERATIVE AI Used
/**
 * ListItemsComponent: A React component to display a menu of items, manage a shopping cart,
 * and place an order.
 */
const ListItemsComponent = () => {
  const [items, setItems] = useState([]); // List of menu items
  const [cartItems, setCartItems] = useState([]); // Store items added to the cart
  const [images, setImages] = useState({});// Store images fetched for items
  const [tip, setTip] = useState(0);// Store the selected tip amount
  const [rate, setRate] = useState(0); // Store the tax rate
  const [customTip, setCustomTip] = useState(0);// Store the custom tip percentage
  const [isCustomTip, setIsCustomTip] = useState(false);// Flag to determine if custom tip is used


  const isStaff = isStaffUser(); // Check if the current user is a staff member
  const subtotal = cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0); // Calculate subtotal
  const tax = subtotal * rate; // Calculate tax based on subtotal

  useEffect(() => {
    // Fetch items from backend
    fetchItems();
    fetchTaxRate();
  }, []);

  /**
   * Fetches menu items from the backend and their corresponding images.
   */
  const fetchItems = async () => {
    try {
      const response = await OrderService.getItems(); // Use OrderService to fetch items
      setItems(response.data);
      response.data.forEach((item) => {
        fetchImage(item.name);
      });
    } catch (error) {
      console.error("Error fetching items:", error);
    }
  };


  /**
   * Fetches the tax rate from the backend.
   */
  const fetchTaxRate = async () => {
    try {
      const response = await getTaxRate();
      setRate(response.data.rate);
    } catch (error) {
      console.error("Error fetching tax rate:", error);
    }
  };

    /**
   * Fetches an image for a given item name from Unsplash API.
   * @param query - The name of the item.
   */
  const fetchImage = async (query) => {
    try {
      const response = await unsplashClient.get("/search/photos", {
        params: { query, per_page: 1 },
      });
      const imageUrl = response.data.results[0]?.urls?.small || "";
      setImages((prevImages) => ({
        ...prevImages,
        [query]: imageUrl,
      }));
    } catch (error) {
      console.error(`Error fetching image for ${query}:`, error.response?.data || error.message);
    }
  };
  
    /**
   * Adds an item to the cart or increases its quantity if it already exists.
   * @param item - The item to add to the cart.
   */
  const handleAddToCart = (item) => {
    setCartItems((prevCartItems) => {
      const existingItem = prevCartItems.find((cartItem) => cartItem.id === item.id);
      if (existingItem) {
        return prevCartItems.map((cartItem) =>
          cartItem.id === item.id
            ? { ...cartItem, quantity: cartItem.quantity + 1 }
            : cartItem
        );
      } else {
        return [...prevCartItems, { ...item, quantity: 1 }];
      }
    });
  };

   /**
   * Increases the quantity of an item in the cart.
   * @param id - The ID of the item.
   */
  const handleIncreaseQuantity = (id) => {
    setCartItems((prevCartItems) =>
      prevCartItems.map((item) =>
        item.id === id ? { ...item, quantity: item.quantity + 1 } : item
      )
    );
  };

     /**
   * Decreases the quantity of an item in the cart.
   * @param id - The ID of the item.
   */
  const handleDecreaseQuantity = (id) => {
    setCartItems((prevCartItems) =>
      prevCartItems
        .map((item) =>
          item.id === id ? { ...item, quantity: item.quantity - 1 } : item
        )
        .filter((item) => item.quantity > 0)
    );
  };

  /**
   * Updates the tip based on a selected percentage.
   * @param percent - The tip percentage.
   */
  const handleTipChange = (percent) => {
    setIsCustomTip(false);
    setTip(subtotal * (percent / 100));
  };

  /**
   * Updates the tip based on a custom percentage.
   */
  const handleCustomTip = () => {
    setIsCustomTip(true);
    setTip(customTip);
  };

   /**
   * Places an order with the selected items and tip.
   */
  const handlePlaceOrder = async () => {
    if (cartItems.length === 0) {
      alert('Your cart is empty. Please add items to your cart before placing an order.');
      return;
    }

    const totalPrice = subtotal + tax + tip;

    const orderData = {
      items: cartItems.reduce((acc, item) => {
        acc[item.name] = item.quantity;
        return acc;
      }, {}),
      totalPrice: parseFloat(totalPrice.toFixed(2)),
      tip: parseFloat(tip.toFixed(2)), 
    };

    try {
      const response = await OrderService.placeOrder(orderData);
      console.log("Order placed successfully:", response.data);
      alert('Order placed successfully!');
      setCartItems([]);
      setTip(0);
      setCustomTip(0);
    } catch (error) {
      console.error("Error placing order:", error);
      alert('Failed to place the order. Please try again.');
    }
  };

  return (
	<div className="menu-page">
	  <div className="menu-list">
	    <h2>Menu Items</h2>
	    <div className="items-grid">
	      {items.map((item) => (
	        <div
	          key={item.id}
	          className="item-card"
	          onClick={() => handleAddToCart(item)}
	        >
	          <h3>{item.name}</h3>
	          <div className="image-wrapper">
	            {images[item.name] && (
	              <img
	                src={images[item.name]}
	                alt={item.name}
	                className="item-image"
	              />
	            )}
	          </div>
	          <p className="item-description">{item.description}</p>
	          <p>${item.price.toFixed(2)}</p>
	        </div>
	      ))}
	    </div>
	  </div>

	  {/* Cart Section */}
	  {!isStaff && (
	    <div className="cart">
	      <h3>Shopping Cart</h3>
	      <div className="cart-items">
	        {cartItems.map((item) => (
	          <div key={item.id} className="cart-item">
	            <p className="cart-item-name">
	              {item.name} x{item.quantity}
	            </p>
	            <div className="quantity-controls">
	              <button onClick={() => handleDecreaseQuantity(item.id)}>
	                -
	              </button>
	              <span>{item.quantity}</span>
	              <button onClick={() => handleIncreaseQuantity(item.id)}>
	                +
	              </button>
	            </div>
	            <p className="cart-item-price">
	              ${(item.price * item.quantity).toFixed(2)}
	            </p>
	          </div>
	        ))}
	      </div>

	      <hr />

	      {/* Tip Selection */}
	      <div className="tip-selection">
	        <label>Select Tip:</label>
	        <div className="tip-options">
	          <button onClick={() => handleTipChange(15)}>15%</button>
	          <button onClick={() => handleTipChange(20)}>20%</button>
	          <button onClick={() => handleTipChange(25)}>25%</button>
	          <button onClick={handleCustomTip}>Custom</button>
	        </div>
	        {isCustomTip && (
	          <div className="custom-tip-container">
	            <input
	              type="text"
	              value={customTip}
	              onChange={(e) =>
	                setCustomTip(parseFloat(e.target.value) || 0)
	              }
	              onBlur={() => setTip(subtotal * (customTip / 100))}
	              placeholder="Enter %"
	            />
	            <span>%</span>
	          </div>
	        )}
	      </div>

	      {/* Total and Place Order */}
	      <div className="cart-total">
	        <p>Subtotal: ${subtotal.toFixed(2)}</p>
	        <p>Tax: ${tax.toFixed(2)}</p>
	        <p>Tip: ${tip.toFixed(2)}</p>
	        <p>Total: ${(subtotal + tax + tip).toFixed(2)}</p>
	      </div>
	      <button
	        className="place-order-button"
	        onClick={handlePlaceOrder}
	      >
	        Place Order
	      </button>
	    </div>
	  )}
	</div>

  );
  
};

export default ListItemsComponent;

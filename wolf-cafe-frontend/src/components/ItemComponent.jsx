import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";

/**
 * ItemComponent: A React component that displays details of an item, including its image.
 */
const ItemComponent = () => {
  const { id } = useParams(); // Get item ID from URL parameters
  const [imageUrl, setImageUrl] = useState(""); // State for storing the fetched image URL
  const apiKey = import.meta.env.VITE_UNSPLASH_API_KEY; // Access the Unsplash API key from environment variables

  // Static item data (for now, can be replaced with dynamic API data if needed)
  const item = {
    id: 1,
    name: "Cheeseburger",
    description:
      "A juicy cheeseburger with a perfectly seasoned beef patty, melted cheese, crisp lettuce, fresh tomato, and a soft toasted sesame bun.",
    price: 3.49,
  };

  /**
   * Fetches an image for the item from the Unsplash API.
   */
  useEffect(() => {
    const fetchImage = async () => {
      try {
        const response = await axios.get("https://api.unsplash.com/search/photos", {
          params: { query: item.name, per_page: 1 },
          headers: { Authorization: `Client-ID ${apiKey}` },
        });
        const imageUrl = response.data.results[0]?.urls?.small || "";
        setImageUrl(imageUrl);
      } catch (error) {
        console.error(`Error fetching image for ${item.name}:`, error.response?.data || error.message);
      }
    };
    fetchImage();
  }, [item.name, apiKey]);
  

  return (
    <div className="item-details-container">
      <div className="item-image-wrapper">
        {imageUrl && <img src={imageUrl} alt={item.name} className="item-image" />}
      </div>
      <div className="item-info">
        <h2>{item.name}</h2>
        <p>{item.description}</p>
        <p className="item-price">Price: ${item.price.toFixed(2)}</p>
        <button className="add-to-order-button">Add to Order</button>
      </div>
    </div>
  );
};

export default ItemComponent;

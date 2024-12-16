import React, { useEffect, useState } from 'react';
import { getItemById, saveItem, updateItem } from '../services/ItemService';
import { useNavigate, useParams } from 'react-router-dom';
import '../styles/ItemForm.css';
/**
 * ItemFormComponent: A React component for adding or updating an item.
 */ 
const ItemFormComponent = () => {
    const [name, setName] = useState(''); // Item name
    const [description, setDescription] = useState(''); // Item description
    const [price, setPrice] = useState(''); // Item price
    const [priceError, setPriceError] = useState(''); // Validation error for price
    const { id } = useParams(); // Get the item ID from the route parameters (used for updating)
    const navigate = useNavigate(); // Hook to move to other pages

     /**
     * useEffect to fetch item details if the `id` is provided (update mode).
     */
    useEffect(() => {
        if (id) {
            // Fetch item details if updating an item
            const fetchData = async () => {
                try {
                    const itemResponse = await getItemById(id);
                    setName(itemResponse.data.name);
                    setDescription(itemResponse.data.description);
                    setPrice(itemResponse.data.price);
                } catch (error) {
                    console.error("Error fetching item details:", error);
                }
            };
            fetchData();
        }
    }, [id]);

    /**
     * Validates the price input. Ensures the price is a positive number with up to 2 decimal places.
     *
     * @returns Whether the price is valid.
     */
    const validatePrice = () => {
        if (!/^\d+(\.\d{1,2})?$/.test(price) || Number(price) <= 0) {
            setPriceError("Please enter a positive number for the item price.");
            return false;
        }
        setPriceError("");
        return true;
    };

    /**
     * Handles form submission for saving or updating the item.
     *
     * @param e - Form submission event.
     */
    const saveOrUpdateItem = async (e) => {
        e.preventDefault();
        if (!validatePrice()) return;

        const item = { name, description, price: parseFloat(price) };

        try {
            if (id) {
                await updateItem(id, item);
            } else {
                await saveItem(item);
            }

            navigate('/inventory'); // Redirect to inventory after saving/updating

        } catch (error) {
            console.error("Error saving or updating item:", error);
        }
    };

    /**
     * Renders the page title based on the operation (add or update).
     */
    const pageTitle = () => {
        return <h2 className='form-title'>{id ? "Update Item" : "Add Item"}</h2>;
    };

    return (
        <div className='item-form-container'>
            <br /> <br />
            <div className='form-card'>
                {pageTitle()}
                <div className='form-body'>
                    <form onSubmit={saveOrUpdateItem}>
                        <div className='form-group'>
                            <label className='form-label'>Item Name:</label>
                            <input 
                                type='text'
                                className='form-input'
                                placeholder='Enter Item Name'
                                name='name'
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                required
                            />
                        </div>

                        <div className='form-group'>
                            <label className='form-label'>Item Description:</label>
                            <input 
                                type='text'
                                className='form-input'
                                placeholder='Enter Item Description'
                                name='description'
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                required
                            />
                        </div>

                        <div className='form-group'>
                            <label className='form-label'>Item Price:</label>
                            <input 
                                type='text'
                                className='form-input'
                                placeholder='Enter Item Price'
                                name='price'
                                value={price}
                                onChange={(e) => setPrice(e.target.value)}
                                required
                            />
                            {priceError && <small className="error-message">{priceError}</small>}
                        </div>

                        <button type='submit' className='form-button'>Submit</button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default ItemFormComponent;

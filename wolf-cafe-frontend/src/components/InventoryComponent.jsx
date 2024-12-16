import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { isAdminUser, isStaffUser } from '../services/AuthService';
import { getAllItems, deleteItemById } from '../services/ItemService';
import { getInventory, updateInventory } from '../services/InventoryService';
import '../styles/Inventory.css';
import '../styles/Header.css';

/**
 * InventoryComponent: A React component for managing inventory items.
 */
const InventoryComponent = () => {
    const [items, setItems] = useState([]);  // Store the list of items
    const [quantities, setQuantities] = useState({}); // Store quantities by item name
    const navigate = useNavigate();

    const isAdmin = isAdminUser(); // Check if the user is an Admin
    const isStaff = isStaffUser(); // Check if the user is a Staff member

    useEffect(() => {
        listItems();
        fetchInventoryQuantities();
    }, []);

    /**
     * Fetch all items from the backend and update the state.
     */
    function listItems() {
        getAllItems().then((response) => {
            setItems(response.data);
        }).catch(error => {
            console.error("Error fetching items:", error);
        });
    }

    /**
     * Fetch inventory quantities from the backend and update the state.
     */
    function fetchInventoryQuantities() {
        getInventory().then((response) => {
            // The response should contain items as an object with item names as keys and quantities as values
            setQuantities(response.data.items); 
        }).catch(error => {
            console.error("Error fetching inventory quantities:", error);
        });
    }

     /**
     * Handle changes to an item's quantity input.
     *
     * @param itemName - The name of the item being updated.
     * @param newQuantity - The new quantity value.
     */
    function handleQuantityChange(itemName, newQuantity) {
        setQuantities((prevQuantities) => ({
            ...prevQuantities,
            [itemName]: newQuantity
        }));
    }

    /**
     * Update the inventory quantities on the backend.
     */
    function handleUpdateInventory() {
        const inventoryUpdateData = {
            id: 1, // Assuming inventory ID is 1; adjust if dynamic.
            items: quantities
        };

        updateInventory(inventoryUpdateData).then(() => {
            alert("Inventory updated successfully");
        }).catch(error => {
            console.error("Error updating inventory:", error);
            alert("Failed to update inventory");
        });
    }

    /**
     * Navigate to the "Add Item" page.
     */
    function addNewItem() {
        navigate('/add-item');
    }

    /**
     * Navigate to the "Update Item" page for a specific item.
     *
     * @param id - The ID of the item to update.
     */
    function updateItem(id) {
        navigate(`/update-item/${id}`);
    }

     /**
     * Delete an item by its ID and refresh the item list.
     *
     * @param id - The ID of the item to delete.
     */
    function deleteItem(id) {
        deleteItemById(id).then(() => {
            listItems(); // Refresh the item list after deletion
        }).catch(error => {
            console.error("Error deleting item:", error);
        });
    }

    return (
        <div className='inventory-container'>
            <h2 className='inventory-title'>Items</h2>
            {isStaff && (
                <div className='inventory-button-container'>
                    <button className='inventory-button' onClick={addNewItem}>Add Item</button>
                </div>
            )}
            <table className='inventory-table'>
                <thead>
                    <tr className='inventory-header'>
                        <th className='inventory-item-cell'>Item Name</th>
                        <th className='inventory-item-cell'>Description</th>
                        <th className='inventory-amount-cell'>Price</th>
                        <th className='inventory-quantity-cell'>Quantity</th>
                        <th className='inventory-action-cell'>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {items.map((item) => (
                        <tr key={item.id} className='inventory-row'>
                            <td className='inventory-item-cell'>{item.name}</td>
                            <td className='inventory-item-cell'>{item.description}</td>
                            <td className='inventory-item-cell'>{item.price}</td>
                            <td className='inventory-item-cell'>
                                <input
                                    type="number"
                                    value={quantities[item.name] !== undefined ? quantities[item.name] : ""}
                                    onChange={(e) => handleQuantityChange(item.name, parseInt(e.target.value))}
                                    className='quantity-input'
									style={{ width: '60px' }}
                                />
                            </td>
                            <td className='inventory-item-cell'>
                                {isStaff && (
                                    <>
                                        <button className='inventory-button' onClick={() => updateItem(item.id)}>Edit</button>
                                        <button className='inventory-button' onClick={() => deleteItem(item.id)} style={{ marginLeft: "10px" }}>Delete</button>
                                    </>
                                )}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
            {isStaff && (
                <div className='update-inventory-container'>
				<button
				    className='inventory-button'
				    onClick={handleUpdateInventory}
				    style={{ marginTop: '20px' }} 
				>
				    Update Inventory
				</button>
                </div>
            )}
        </div>
    );
};

export default InventoryComponent;

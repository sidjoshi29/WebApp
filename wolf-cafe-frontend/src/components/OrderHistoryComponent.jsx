import React, { useState, useEffect } from 'react';
import OrderService from '../services/OrderService';
import '../styles/OrdersComponent.css';

/**
 * OrderHistoryComponent: Displays the order history of the user with filtering capabilities.
 */
const OrderHistoryComponent = () => {
    const [orders, setOrders] = useState([]);
    const [filter, setFilter] = useState(''); // For filtering by item name
    const [filteredOrders, setFilteredOrders] = useState([]); // Display filtered results

	    // Fetch order history on component mount
    useEffect(() => {
        fetchOrderHistory();
    }, []);

	// Apply the filter whenever the filter input or orders change
    useEffect(() => {
        applyFilter();
    }, [filter, orders]);

	/**
     * Fetches the user's order history from the backend.
     */
    const fetchOrderHistory = async () => {
        try {
            const response = await OrderService.viewOrderHistory();
            console.log('Fetched Order History:', response.data);
            setOrders(response.data);
        } catch (error) {
            console.error('Failed to fetch order history:', error);
        }
    };

	 /**
     * Filters the orders based on the current filter input.
     */
    const applyFilter = () => {
        if (filter.trim() === '') {
            setFilteredOrders(orders); // No filter, display all orders
        } else {
            const lowerCaseFilter = filter.toLowerCase();
            const filtered = orders.filter(order =>
                Object.keys(order.items).some(item => item.toLowerCase().includes(lowerCaseFilter))
            );
            setFilteredOrders(filtered);
        }
    };

	/**
     * Handles changes in the filter input field.
     * 
     * @param event - The input change event.
     */
    const handleFilterChange = (event) => {
        setFilter(event.target.value);
    };
	
	// Calculate the total cost of filtered orders
	const totalCost = filteredOrders.reduce((total, order) => total + order.totalPrice, 0);

    return (
		<div className="orders-list">
		            <h2>Order History</h2>

		            <div className="filter-section">
		                <label htmlFor="filter">Filter by Item Name:</label>
		                <input
		                    id="filter"
		                    type="text"
		                    value={filter}
		                    onChange={handleFilterChange}
		                    placeholder="Enter item name..."
		                />
		            </div>

		            <table>
		                <thead>
		                    <tr>
		                        <th>Order ID</th>
		                        <th>Items</th>
		                        <th>Cost</th>
		                        <th>Date</th>
		                    </tr>
		                </thead>
		                <tbody>
		                    {filteredOrders.map((order) => (
		                        <tr key={order.id}>
		                            <td>{order.id}</td>
		                            <td>
		                                {Object.entries(order.items).map(([item, quantity]) => (
		                                    <div key={item}>
		                                        {item}: {quantity}
		                                    </div>
		                                ))}
		                            </td>
		                            <td>${order.totalPrice.toFixed(2)}</td>
		                            <td>{new Date(order.createdAt).toLocaleString()}</td>
		                        </tr>
		                    ))}
		                </tbody>
		            </table>

		            {filteredOrders.length === 0 && <p>No orders found matching the filter.</p>}

		            {filteredOrders.length > 0 && (
		                <div className="total-section">
		                    <h3>Total: ${totalCost.toFixed(2)}</h3>
		                </div>
		            )}
		        </div>
    );
};

export default OrderHistoryComponent;

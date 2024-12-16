import React, { useEffect, useState } from 'react';
import OrderService from '../services/OrderService';
import '../styles/OrdersComponent.css';

/**
 * MyOrdersComponent: Displays the list of orders placed by the current user.
 */
const MyOrdersComponent = () => {
    const [orders, setOrders] = useState([]); // Store fetched orders
    const [error, setError] = useState(null); // Track errors
    const [loading, setLoading] = useState(false); // Track loading state

    // Fetch orders when the component mounts
    useEffect(() => {
        fetchMyOrders();
    }, []);

    /**
     * Fetches the current user's orders from the backend.
     */
    const fetchMyOrders = async () => {
        setLoading(true); // Start loading
        setError(null); // Clear previous errors
        try {
            const response = await OrderService.getOrdersForCurrentUser();
            setOrders(response.data); // Set fetched orders
        } catch (error) {
            console.error('Failed to fetch orders:', error);
            setError(
                error.response?.data?.message ||
                    'An error occurred while fetching your orders. Please try again.'
            );
        } finally {
            setLoading(false); // End loading
        }
    };

    return (
        <div className="orders-list">
            <h2>My Orders</h2>
            {loading && <p>Loading your orders...</p>} {/* Loading indicator */}
            {error && <div className="error-message">{error}</div>} {/* Error message */}
            {!loading && !error && orders.length === 0 && <p>You have no orders.</p>} {/* No orders */}
            {orders.length > 0 && (
                <table>
                    <thead>
                        <tr>
                            <th>Order ID</th>
                            <th>Items</th>
                            <th>Status</th>
                            <th>Total Price</th>
                            <th>Order Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        {orders.map((order) => (
                            <tr key={order.id}>
                                <td>{order.id}</td>
                                <td>
                                    {Object.entries(order.items).map(([item, quantity]) => (
                                        <div key={item}>
                                            {item}: {quantity}
                                        </div>
                                    ))}
                                </td>
                                <td className="status">{order.status.replace('_', ' ')}</td>
                                <td>${order.totalPrice.toFixed(2)}</td>
                                <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
};

export default MyOrdersComponent;

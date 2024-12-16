import React, { useState, useEffect } from 'react';
import OrderService from '../services/OrderService';
import '../styles/OrdersComponent.css';

/**
 * OrdersList Component: Displays a list of orders and provides actions for fulfilling, 
 * canceling, or marking orders as picked up. Tracks unsaved changes locally.
 */
const OrdersList = () => {
    const [orders, setOrders] = useState([]); // State to store orders
    const [localChanges, setLocalChanges] = useState({}); // Track unsaved changes

    useEffect(() => {
        fetchOrders();
    }, []);

	/**
     * Fetches orders from the backend and filters out 'PICKED_UP' and 'CANCELLED' orders.
     */
    const fetchOrders = async () => {
        try {
            const response = await OrderService.getAllOrders();
            console.log('Fetched Orders:', response.data); // Debugging log
			const filteredOrders = response.data.filter(order => 
			            order.status !== 'PICKED_UP' && order.status !== 'CANCELLED'
			        );
            setOrders(filteredOrders);
        } catch (error) {
            console.error('Failed to fetch orders:', error);
        }
    };

	/**
     * Handles local changes to an order's status.
     * 
     * @param orderId - The ID of the order to change.
     * @param newStatus - The new status to set.
     */
    const handleStatusChange = (orderId, newStatus) => {
        // Save changes locally without sending to backend
        setLocalChanges((prevChanges) => ({
            ...prevChanges,
            [orderId]: newStatus,
        }));
    };

	/**
     * Undoes a local status change for an order.
     * 
     * @param orderId - The ID of the order to undo the change for.
     */
    const handleUndo = (orderId) => {
        // Remove the change for this order
        setLocalChanges((prevChanges) => {
            const updatedChanges = { ...prevChanges };
            delete updatedChanges[orderId];
            return updatedChanges;
        });
    };

	/**
     * Saves all local changes to the backend.
     */
    const handleSave = async () => {
        try {
            const requests = Object.entries(localChanges).map(([orderId, newStatus]) => {
                if (newStatus === 'FULFILLED') {
                    return OrderService.fulfillOrder(orderId);
                }
                if (newStatus === 'PICKED_UP') {
                    return OrderService.pickupOrder(orderId);
                }
                return null;
            });

            await Promise.all(requests);

            // Update orders list to reflect changes
            setOrders((prevOrders) =>
                prevOrders.map((order) => ({
                    ...order,
                    status: localChanges[order.id] || order.status, // Apply changes
                }))
            );

            setLocalChanges({}); // Clear local changes after saving
        } catch (error) {
            console.error('Failed to save changes:', error);
        }
	};
	
	/**
     * Cancels an order by sending a request to the backend.
     * 
     * @param orderId - The ID of the order to cancel.
     */
	const handleCancelOrder = async (orderId) => {
	        try {
	            await OrderService.cancelOrder(orderId);
	            alert(`Order ${orderId} has been canceled.`);
	            fetchOrders(); 
	        } catch (error) {
	            console.error(`Failed to cancel order ${orderId}:`, error);
	            alert(`Failed to cancel order ${orderId}. Please try again.`);
	        }
	    };

	/**
     * Determines the display status for an order, prioritizing local changes.
     * 
     * @param orderId - The ID of the order to get the status for.
     * @returns - The current display status of the order.
     */
    const getStatusDisplay = (orderId) =>
        localChanges[orderId] || orders.find((order) => order.id === orderId)?.status;

	// Generated with Chat GPT
	return (
	        <div className="orders-list">
	            <h2>Orders</h2>
	            <table>
	                <thead>
	                    <tr>
	                        <th>Order ID</th>
	                        <th>Items</th>
	                        <th>Status</th>
	                        <th>Actions</th>
	                    </tr>
	                </thead>
					<tbody>
					    {orders.map((order) => {
					        const currentStatus = getStatusDisplay(order.id);

					        return (
					            <tr key={order.id}>
					                <td>{order.id}</td>
					                <td>
					                    {Object.entries(order.items).map(([item, quantity]) => (
					                        <div key={item}>
					                            {item}: {quantity}
					                        </div>
					                    ))}
					                </td>
					                <td className="status">{currentStatus.replace('_', ' ')}</td>
					                <td>
					                    {order.status === 'PLACED' && !localChanges[order.id] && (
					                        <>
					                            <button
					                                className="fulfill"
					                                onClick={() => handleStatusChange(order.id, 'FULFILLED')}
					                            >
					                                Fulfill
					                            </button>
					                            <button
					                                className="cancel-order"
					                                onClick={() => handleCancelOrder(order.id)}
					                            >
					                                Cancel
					                            </button>
					                        </>
					                    )}
					                    {order.status === 'PLACED' && localChanges[order.id] && (
					                        <button
					                            className="undo"
					                            onClick={() => handleUndo(order.id)}
					                        >
					                            Undo
					                        </button>
					                    )}
					                    {order.status === 'FULFILLED' &&
					                        currentStatus === 'FULFILLED' && (
					                            <button
					                                className="pickup"
					                                onClick={() =>
					                                    handleStatusChange(order.id, 'PICKED_UP')
					                                }
					                            >
					                                Mark Picked Up
					                            </button>
					                        )}
					                </td>
					            </tr>
					        );
					    })}
					</tbody>
	            </table>
	            {Object.keys(localChanges).length > 0 && (
	                <div className="save-actions">
	                    <button className="save" onClick={handleSave}>
	                        Save Changes
	                    </button>
	                </div>
	            )}
	            {orders.length === 0 && <p>No active orders to display.</p>}
	        </div>
	    );
	};

export default OrdersList;

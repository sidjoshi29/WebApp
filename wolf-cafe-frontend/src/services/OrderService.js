// src/services/OrderService.js
import axios from "axios";

// Base API ENDPOINT for order related requests
const API_BASE_URL = "http://localhost:8080/api";

class OrderService {
  /**
   * Places a new order.
   */
  placeOrder(orderData) {
    return axios.post(`${API_BASE_URL}/orders`, orderData, {
      headers: { "Content-Type": "application/json" },
    });
  }

  /**
   * Retrieves all orders (accessible by staff only).
   */
  getAllOrders() {
    return axios.get(`${API_BASE_URL}/orders`);
  }

  /**
   * Retrieves orders for the current customer.
   */
  getOrdersForCurrentUser() {
    return axios.get(`${API_BASE_URL}/orders/customer`);
  }

  /**
   * Retrieves a single order by ID (accessible by staff only).
   */
  getOrder(orderId) {
    return axios.get(`${API_BASE_URL}/orders/${orderId}`);
  }

  /**
   * Cancels an order (accessible by the customer who placed the order)
   */
  cancelOrder(orderId) {
    return axios.delete(`${API_BASE_URL}/orders/cancel/${orderId}`);
  }

  /**
   * Changes an order status to fulfilled (accessible by the staff)
   */
  fulfillOrder(orderId) {
    return axios.put(`${API_BASE_URL}/orders/fulfill/${orderId}`);
  }

  /**
   * Changes an order status to picked up (accessible by the staff)
   */
  pickupOrder(orderId) {
    return axios.put(`${API_BASE_URL}/orders/pickup/${orderId}`);
  }

  /**
   * Retrieves all available items from the menu.
   */
  getItems() {
    return axios.get(`${API_BASE_URL}/items`); // Adjust the endpoint if necessary
  }

  /**
   * Retrieves the order history with an optional filter by item name.
   */
  viewOrderHistory(itemName = "") {
    const params = itemName ? { itemName } : {};
    return axios.get(`${API_BASE_URL}/orders/history`, { params });
  }
}

export default new OrderService();

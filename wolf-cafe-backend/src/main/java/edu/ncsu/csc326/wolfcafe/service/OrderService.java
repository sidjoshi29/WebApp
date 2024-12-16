package edu.ncsu.csc326.wolfcafe.service;

import java.util.List;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;

/**
 * Service interface for handling order-related operations in the WolfCafe
 * system.
 */
public interface OrderService {
    /**
     * Places a new order in the system.
     *
     * @param orderDto
     *            The data transfer object containing order details.
     * @return true if the order was placed successfully.
     */
    OrderDto placeOrder ( OrderDto orderDto );

    // /**
    // * Marks an order as complete.
    // *
    // * @param id
    // * The ID of the order to mark as complete.
    // * @return true if the order was marked as complete successfully.
    // */
    // boolean completeOrder ( Long id );

    /**
     * Cancels an existing order in the system.
     *
     * @param id
     *            The ID of the order to cancel.
     * @return true if the order was canceled successfully.
     */
    boolean cancelOrder ( Long id );

    // /**
    // * Marks an order as picked up and removes it from the system.
    // *
    // * @param id
    // * The ID of the order to mark as picked up.
    // * @return true if the order was marked as picked up successfully.
    // */
    // boolean pickupOrder ( Long id );

    /**
     * Retrieves a list of all orders in the system.
     *
     * @return A list of all orders as OrderDto objects.
     */
    List<OrderDto> getAllOrders ();

    /**
     * Retrieves a specific order by its ID.
     *
     * @param id
     *            The ID of the order to retrieve.
     * @return The OrderDto object containing the order details.
     */
    OrderDto getOrder ( Long id );

    /**
     * Retrieves all orders for the currently authenticated user.
     *
     * @return List of OrderDto objects representing the user's orders
     */
    List<OrderDto> getOrdersForCurrentUser ();

    /**
     * Fulfills an order if it is in PLACED status. Updates the order status to
     * FULFILLED.
     *
     * @param id
     *            ID of the order to fulfill
     * @return true if the order was successfully fulfilled, false otherwise
     */
    boolean fulfillOrder ( Long id );

    /**
     * Pick up an order if it is in FULFILLED status. Updates the order status
     * to PICKED_UP.
     *
     * @param id
     *            ID of the order to pick up
     * @return true if the order was successfully picked up, false otherwise
     */
    boolean pickupOrder ( Long id );

    /**
     * Retrieves order history with optional filtering by item name.
     *
     * @param itemName
     *            (Optional) The name of the item to filter the orders by.
     * @return A list of OrderDto objects representing the order history.
     */
    List<OrderDto> viewOrderHistory ( String itemName );
}

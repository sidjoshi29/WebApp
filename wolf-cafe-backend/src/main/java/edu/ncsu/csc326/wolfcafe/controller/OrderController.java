package edu.ncsu.csc326.wolfcafe.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.service.OrderService;
import lombok.AllArgsConstructor;

/**
 * Controller for handling order-related API requests in WolfCafe.
 */
@RestController
@RequestMapping ( "/api/orders" )
@AllArgsConstructor
@CrossOrigin ( "*" )
public class OrderController {

    /** Service for order operations */
    private final OrderService orderService;

    /**
     * Places a new order in the system. Only customers are allowed to access
     * this endpoint. Customers can place an order
     *
     * @param orderDto
     *            the order to place
     * @return ResponseEntity containing the placed order or an error status
     */
    @PostMapping
    @PreAuthorize ( "hasRole('CUSTOMER')" )
    public ResponseEntity<OrderDto> placeOrder ( @RequestBody final OrderDto orderDto ) {
        final OrderDto savedOrderDto = orderService.placeOrder( orderDto );
        return ResponseEntity.status( HttpStatus.CREATED ).body( savedOrderDto ); // 201
                                                                                  // Created
    }

    /**
     * Retrieves a list of all orders in the system. Accessible through the
     * STAFF roles.
     *
     * @return ResponseEntity containing a list of orders
     */
    @GetMapping
    @PreAuthorize ( "hasRole('STAFF')" )
    public ResponseEntity<List<OrderDto>> getAllOrders () {
        final List<OrderDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok( orders );
    }

    /**
     * Retrieves a list of all orders for a particular user. Customers see their
     * own orders
     *
     * @return ResponseEntity containing the list of customer orders
     */
    @GetMapping ( "/customer" )
    @PreAuthorize ( "hasRole('CUSTOMER')" )
    public ResponseEntity<List<OrderDto>> getOrdersForCurrentUser () {
        final List<OrderDto> orders = orderService.getOrdersForCurrentUser();
        return ResponseEntity.ok( orders );
    }

    /**
     * Retrieves an order by its ID. Accessible by all authenticated users
     * (STAFF members).
     *
     * @param id
     *            the ID of the order to retrieve
     * @return ResponseEntity containing the order or an error status
     */
    @GetMapping ( "/{id}" )
    @PreAuthorize ( "hasRole('STAFF')" )
    public ResponseEntity<OrderDto> getOrder ( @PathVariable final Long id ) {
        final OrderDto order = orderService.getOrder( id );
        return ResponseEntity.ok( order );
    }

    /**
     * Cancels an order by its ID. Only accessible by users with the STAFF role.
     *
     * @param id
     *            the ID of the order to cancel
     * @return ResponseEntity with a success message or an error status
     */
    @DeleteMapping ( "/cancel/{id}" )
    @PreAuthorize ( "hasRole('STAFF')" )
    public ResponseEntity<String> cancelOrder ( @PathVariable final Long id ) {
        final boolean success = orderService.cancelOrder( id );
        if ( success ) {
            return ResponseEntity.ok( "Order cancelled successfully." );
        }
        return ResponseEntity.badRequest().body( "Failed to cancel the order." );
    }

    /**
     * Fulfills an order by its ID. Only accessible by staff.
     *
     * @param id
     *            the ID of the order to fulfill
     * @return ResponseEntity with a success message if fulfilled, or an error
     *         status if not
     */
    @PutMapping ( "/fulfill/{id}" )
    @PreAuthorize ( "hasRole('STAFF')" )
    public ResponseEntity<String> fulfillOrder ( @PathVariable final Long id ) {
        final boolean isFulfilled = orderService.fulfillOrder( id );
        if ( isFulfilled ) {
            return ResponseEntity.ok( "Order fulfilled successfully." );
        }
        return ResponseEntity.badRequest().body( "Order cannot be fulfilled." );
    }

    /**
     * Set the status of an order to PICKED UP. This change of status can only
     * be done by STAFF users
     *
     * @param id
     *            the ID of the order
     * @return ResponseEntity with a success message or an error status
     */
    @PutMapping ( "/pickup/{id}" )
    @PreAuthorize ( "hasRole('STAFF')" )
    public ResponseEntity<String> pickupOrder ( @PathVariable final Long id ) {
        final boolean isPickedUp = orderService.pickupOrder( id );
        if ( isPickedUp ) {
            return ResponseEntity.ok( "Order picked up successfully." );
        }
        return ResponseEntity.badRequest().body( "Order cannot be picked up." );
    }

    /**
     * Retrieves order history with an optional filter by item name. Only orders
     * with the PICKED_UP status are included. Accessible by staff only.
     *
     * The itemName is an optional query parameter used to filter results. Thats
     * why we are using RequestParam
     *
     * @param itemName
     *            The name of the item to filter orders by.
     * @return ResponseEntity containing the order history or an error status.
     */
    @GetMapping ( "/history" )
    @PreAuthorize ( "hasRole('STAFF')" )
    public ResponseEntity<List<OrderDto>> viewOrderHistory (
            @RequestParam ( required = false ) final String itemName ) {
        final List<OrderDto> orderHistory = orderService.viewOrderHistory( itemName );
        return ResponseEntity.ok( orderHistory );
    }

}

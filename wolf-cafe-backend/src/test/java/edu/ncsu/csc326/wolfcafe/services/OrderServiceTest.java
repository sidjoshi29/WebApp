package edu.ncsu.csc326.wolfcafe.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import edu.ncsu.csc326.wolfcafe.entity.TaxRate;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.repository.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;
import edu.ncsu.csc326.wolfcafe.repository.TaxRateRepository;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;
import edu.ncsu.csc326.wolfcafe.service.OrderService;

/*
 * Tests OrderService
 */
@SpringBootTest
public class OrderServiceTest {

    /**
     * Reference to OrderService
     */
    @Autowired
    private OrderService        orderService;

    /**
     * Reference to item repository
     */
    @Autowired
    private ItemRepository      itemRepository;

    /**
     * Reference to inventory repository
     */
    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * Reference to user repository
     */
    @Autowired
    private UserRepository      userRepository;

    /**
     * Reference to tax rate repository
     */
    @Autowired
    private TaxRateRepository   taxRateRepository;

    /**
     * Represents a customer user in the system
     */
    private User                customer1;

    /*
     * Clear all the information before the tests. And set necessary information
     * for future tests
     */
    @BeforeEach
    public void setUp () {
        // Ensure all related data is cleared between tests
        inventoryRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
        taxRateRepository.deleteAll();

        final TaxRate taxRate = new TaxRate();
        taxRate.setId( 1L );
        taxRate.setRate( 0.05 );
        taxRateRepository.saveAndFlush( taxRate );

        // Create and save customer1 if not present
        customer1 = userRepository.findByUsername( "customer1" ).orElseGet( () -> {
            final User customer1 = new User();
            customer1.setUsername( "customer1" );
            customer1.setEmail( "customer1@example.com" );
            customer1.setPassword( "password" );
            return userRepository.save( customer1 );
        } );

        // Create and save customer1 if not present
        final User customer2 = new User();
        customer2.setUsername( "customer2" );
        customer2.setEmail( "customer2@example.com" );
        customer2.setPassword( "password" );
        userRepository.save( customer2 );

        // Create and save test items and inventory
        final Item coffee = itemRepository.save( new Item( null, "Coffee", "Freshly ground coffee", 3.0 ) );
        final Item milk = itemRepository.save( new Item( null, "Milk", "Whole milk", 1.5 ) );

        final Inventory inventory = new Inventory();
        final Map<Item, Integer> items = new HashMap<>();
        items.put( coffee, 20 );
        items.put( milk, 15 );
        inventory.setItems( items );
        inventoryRepository.save( inventory );
    }

    /*
     * Tests placeOrder
     */
    @Test
    @Transactional
    @WithMockUser ( username = "customer1", roles = "CUSTOMER" )
    public void testPlaceOrder () {
        final Map<String, Integer> orderItems = new HashMap<>();
        orderItems.put( "Coffee", 2 );
        orderItems.put( "Milk", 3 );

        final OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId( 1L );
        orderDto.setItems( orderItems );

        final OrderDto savedOrderDto = orderService.placeOrder( orderDto );

        assertAll( "Order Creation", () -> assertNotNull( savedOrderDto.getId() ),
                () -> assertEquals( 2, savedOrderDto.getItems().get( "Coffee" ) ),
                () -> assertEquals( 3, savedOrderDto.getItems().get( "Milk" ) ),
                () -> assertEquals( OrderStatus.PLACED, savedOrderDto.getStatus() ) );

        final Inventory inventory = inventoryRepository.findAll().get( 0 );
        assertEquals( 18, inventory.getItems().get( itemRepository.findByName( "Coffee" ).get() ) );
        assertEquals( 12, inventory.getItems().get( itemRepository.findByName( "Milk" ).get() ) );
    }

    /*
     * Tests placing and order with an invalid item
     */
    @Test
    @Transactional
    @WithMockUser ( username = "customer", roles = "CUSTOMER" )
    public void testPlaceOrderWithInvalidItem () {
        final Map<String, Integer> orderItems = new HashMap<>();
        orderItems.put( "Espresso", 1 );

        final OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId( 1L );
        orderDto.setItems( orderItems );

        assertThrows( ResourceNotFoundException.class, () -> orderService.placeOrder( orderDto ) );
    }

    /*
     * Tests placing an order when there is insufficient inventory
     */
    @Test
    @Transactional
    @WithMockUser ( username = "customer1", roles = "CUSTOMER" )
    public void testPlaceOrderWithInsufficientInventory () {
        final Map<String, Integer> orderItems = new HashMap<>();
        orderItems.put( "Milk", 20 );

        final OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId( 1L );
        orderDto.setItems( orderItems );

        assertThrows( IllegalStateException.class, () -> orderService.placeOrder( orderDto ) );
    }

    /*
     * Tests getting all orders
     */
    @Test
    @Transactional
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testGetAllOrders () {
        final Map<String, Map<String, Integer>> userToItem = Map.of( "customer1", Map.of( "Coffee", 1 ), "customer2",
                Map.of( "Milk", 2 ) );

        placeOrderForMultipleUsers( userToItem );

        final List<OrderDto> orders = orderService.getAllOrders();

        assertEquals( 2, orders.size() );
        assertTrue( orders.stream().anyMatch( order -> order.getItems().containsKey( "Coffee" ) ) );
        assertTrue( orders.stream().anyMatch( order -> order.getItems().containsKey( "Milk" ) ) );
    }

    /*
     * Tests placing and order for multiple users
     */
    private void placeOrderForMultipleUsers ( final Map<String, Map<String, Integer>> userToItem ) {
        for ( final Map.Entry<String, Map<String, Integer>> entry : userToItem.entrySet() ) {
            final String username = entry.getKey();
            final Map<String, Integer> items = entry.getValue();

            SecurityContextHolder.getContext()
                    .setAuthentication( new UsernamePasswordAuthenticationToken( username, "password", List.of() ) );

            final Long customerId = userRepository.findByUsername( username )
                    .orElseThrow( () -> new ResourceNotFoundException( "User not found" ) ).getId();

            final OrderDto orderDto = new OrderDto();
            orderDto.setCustomerId( customerId );
            orderDto.setItems( items );

            orderService.placeOrder( orderDto );

            SecurityContextHolder.clearContext();
        }
    }

    /*
     * Test getting all orders for a specific customer
     */
    @Test
    @Transactional
    @WithMockUser ( username = "customer1", roles = "CUSTOMER" )
    public void testGetOrdersForCurrentUser () {
        placeOrderForUser( "customer1", Map.of( "Coffee", 2 ) );

        final List<OrderDto> orders = orderService.getOrdersForCurrentUser();

        assertEquals( 1, orders.size() );
        assertEquals( "Coffee", orders.get( 0 ).getItems().keySet().iterator().next() );
    }

    /*
     * Places an order for the user (helper)
     */
    private void placeOrderForUser ( final String username, final Map<String, Integer> items ) {
        final OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId( userRepository.findByUsername( username ).get().getId() );
        orderDto.setItems( items );
        orderService.placeOrder( orderDto );
    }

    /*
     * Tests cancelling an order
     */
    @Test
    @Transactional
    @WithMockUser ( username = "customer1", roles = "CUSTOMER" )
    public void testCancelOrder () {
        final Map<String, Integer> orderItems = new HashMap<>();
        orderItems.put( "Coffee", 1 );

        final OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId( 1L );
        orderDto.setItems( orderItems );

        final OrderDto savedOrderDto = orderService.placeOrder( orderDto );

        final boolean isCancelled = orderService.cancelOrder( savedOrderDto.getId() );
        assertTrue( isCancelled );

        final OrderDto cancelledOrderDto = orderService.getOrder( savedOrderDto.getId() );
        assertEquals( OrderStatus.CANCELLED, cancelledOrderDto.getStatus() );
    }

    /*
     * Tests canceling an order that does not have the placed status
     */
    @Test
    @Transactional
    @WithMockUser ( username = "customer1", roles = "CUSTOMER" )
    public void testCancelOrderWithNonPlacedStatus () {
        // Step 1: Place and fulfill the order to set its status to FULFILLED
        final Map<String, Integer> orderItems = new HashMap<>();
        orderItems.put( "Coffee", 2 );
        orderItems.put( "Milk", 1 );

        final OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId( 1L );
        orderDto.setItems( orderItems );

        final OrderDto placedOrderDto = orderService.placeOrder( orderDto );
        orderService.fulfillOrder( placedOrderDto.getId() ); // Fulfill the
                                                             // order

        // Step 2: Attempt to cancel the fulfilled order, expecting a
        // WolfCafeAPIException
        final Exception exception = assertThrows( WolfCafeAPIException.class, () -> {
            orderService.cancelOrder( placedOrderDto.getId() );
        } );

        // Step 3: Verify that the exception contains the expected error message
        assertEquals( "Order with ID " + placedOrderDto.getId() + " cannot be canceled as it is not in PLACED status.",
                exception.getMessage() );
    }

    /*
     * Tests placing a ordering and checking the changed amoutn in the inventory
     */
    @Test
    @Transactional
    @WithMockUser ( username = "customer1", roles = "CUSTOMER" )
    public void testPlaceOrderAndUpdateInventory () {
        final Map<String, Integer> orderItems = new HashMap<>();
        orderItems.put( "Coffee", 5 );

        final OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId( 1L );
        orderDto.setItems( orderItems );

        orderService.placeOrder( orderDto );

        final Inventory inventory = inventoryRepository.findAll().get( 0 );
        assertEquals( 15, inventory.getItems().get( itemRepository.findByName( "Coffee" ).get() ) );
    }

    /*
     * Tests fulfulling an order when the order has the placed status
     */
    @Test
    @Transactional
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testFulfillOrderWithPlacedStatus () {
        // Step 1: Place a new order as customer1
        SecurityContextHolder.getContext()
                .setAuthentication( new UsernamePasswordAuthenticationToken( "customer1", "password" ) );

        final Map<String, Integer> orderItems = new HashMap<>();
        orderItems.put( "Coffee", 2 );

        final OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId( customer1.getId() );
        orderDto.setItems( orderItems );

        final OrderDto placedOrderDto = orderService.placeOrder( orderDto );

        // Step 2: Fulfill the order as staff, expecting success
        SecurityContextHolder.getContext()
                .setAuthentication( new UsernamePasswordAuthenticationToken( "staff", "password" ) );

        final boolean isFulfilled = orderService.fulfillOrder( placedOrderDto.getId() );
        assertTrue( isFulfilled, "Order should be fulfilled successfully" );

        // Step 3: Verify that the order status is updated to FULFILLED
        final OrderDto fulfilledOrderDto = orderService.getOrder( placedOrderDto.getId() );
        assertEquals( OrderStatus.FULFILLED, fulfilledOrderDto.getStatus(), "Order status should be FULFILLED" );
    }

    /*
     * Tests fulfulling an order when the order does not have the placed status
     */
    @Test
    @Transactional
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testFulfillOrderWithNonPlacedStatus () {
        // Step 1: Place and fulfill the order as customer1
        SecurityContextHolder.getContext()
                .setAuthentication( new UsernamePasswordAuthenticationToken( "customer1", "password" ) );

        final Map<String, Integer> orderItems = new HashMap<>();
        orderItems.put( "Coffee", 2 );

        final OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId( customer1.getId() );
        orderDto.setItems( orderItems );

        final OrderDto placedOrderDto = orderService.placeOrder( orderDto );

        // Fulfill the order initially as staff
        SecurityContextHolder.getContext()
                .setAuthentication( new UsernamePasswordAuthenticationToken( "staff", "password" ) );

        final boolean isFulfilledInitially = orderService.fulfillOrder( placedOrderDto.getId() );
        assertTrue( isFulfilledInitially, "Order should be fulfilled successfully when in PLACED status" );

        // Step 2: Attempt to fulfill the already fulfilled order, expecting an
        // exception
        final Exception exception = assertThrows( IllegalStateException.class, () -> {
            orderService.fulfillOrder( placedOrderDto.getId() );
        } );

        assertEquals( "Order with ID " + placedOrderDto.getId() + " is not in PLACED status and cannot be fulfilled.",
                exception.getMessage() );
    }

    /*
     * Tests picking up an order
     */
    @Test
    @Transactional
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testPickUpOrderSuccess () {

        SecurityContextHolder.getContext()
                .setAuthentication( new UsernamePasswordAuthenticationToken( "customer1", "password" ) );

        final Map<String, Integer> orderItems = new HashMap<>();
        orderItems.put( "Coffee", 2 );

        final OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId( customer1.getId() );
        orderDto.setItems( orderItems );

        final OrderDto placedOrderDto = orderService.placeOrder( orderDto );

        SecurityContextHolder.getContext()
                .setAuthentication( new UsernamePasswordAuthenticationToken( "staff", "password" ) );

        final boolean isFulfilled = orderService.fulfillOrder( placedOrderDto.getId() );
        assertTrue( isFulfilled, "Order should be fulfilled successfully" );

        final OrderDto fulfilledOrderDto = orderService.getOrder( placedOrderDto.getId() );
        assertEquals( OrderStatus.FULFILLED, fulfilledOrderDto.getStatus(), "Order status should be FULFILLED" );

        final boolean isPickedUp = orderService.pickupOrder( fulfilledOrderDto.getId() );
        assertTrue( isPickedUp, "Order should be picked up successfully" );

        final OrderDto pickedupOrderDto = orderService.getOrder( fulfilledOrderDto.getId() );
        assertEquals( OrderStatus.PICKED_UP, pickedupOrderDto.getStatus(), "Order status should be PICKED_UP" );
    }

    /*
     * Tests picking up an order on failure since the order is not fulfilled
     */
    @Test
    @Transactional
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testPickUpOrderFailure () {

        SecurityContextHolder.getContext()
                .setAuthentication( new UsernamePasswordAuthenticationToken( "customer1", "password" ) );

        final Map<String, Integer> orderItems = new HashMap<>();
        orderItems.put( "Coffee", 2 );

        final OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId( customer1.getId() );
        orderDto.setItems( orderItems );

        final OrderDto placedOrderDto = orderService.placeOrder( orderDto );
        assertEquals( OrderStatus.PLACED, placedOrderDto.getStatus(), "Order status should be PLACED" );

        SecurityContextHolder.getContext()
                .setAuthentication( new UsernamePasswordAuthenticationToken( "staff", "password" ) );

        // Expecting an exception since the order is in PLACED status, not
        // FULFILLED
        final Exception exception = assertThrows( IllegalStateException.class, () -> {
            orderService.pickupOrder( placedOrderDto.getId() );
        } );

        assertEquals(
                "Order with ID " + placedOrderDto.getId() + " is not in FULFILLED status and cannot be picked up.",
                exception.getMessage() );

        final OrderDto pickedupOrderDto = orderService.getOrder( placedOrderDto.getId() );
        assertEquals( OrderStatus.PLACED, pickedupOrderDto.getStatus(), "Order status should remain PLACED" );
    }

    /*
     * Tests viewOrderHistory
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    @Transactional
    public void testViewOrderHistory () {
        // Set up test users
        final User customer1 = userRepository.findByUsername( "customer1" ).orElseGet( () -> {
            final User user = new User();
            user.setUsername( "customer1" );
            user.setEmail( "customer1@example.com" );
            user.setPassword( "password" );
            return userRepository.save( user );
        } );

        final User customer2 = userRepository.findByUsername( "customer2" ).orElseGet( () -> {
            final User user = new User();
            user.setUsername( "customer2" );
            user.setEmail( "customer2@example.com" );
            user.setPassword( "password" );
            return userRepository.save( user );
        } );

        // Set up inventory
        final Item coffee = itemRepository.findByName( "Coffee" )
                .orElseGet( () -> itemRepository.save( new Item( null, "Coffee", "Freshly ground coffee", 3.0 ) ) );

        final Item milk = itemRepository.findByName( "Milk" )
                .orElseGet( () -> itemRepository.save( new Item( null, "Milk", "Whole milk", 1.5 ) ) );

        final Inventory inventory = new Inventory();
        final Map<Item, Integer> inventoryItems = new HashMap<>();
        inventoryItems.put( coffee, 20 );
        inventoryItems.put( milk, 15 );
        inventory.setItems( inventoryItems );
        inventoryRepository.save( inventory );

        // Place orders
        final Map<String, Integer> orderItems1 = Map.of( "Coffee", 2 );
        final Map<String, Integer> orderItems2 = Map.of( "Milk", 3 );

        SecurityContextHolder.getContext()
                .setAuthentication( new UsernamePasswordAuthenticationToken( "customer1", "password" ) );

        final OrderDto orderDto1 = new OrderDto();
        orderDto1.setCustomerId( customer1.getId() );
        orderDto1.setItems( orderItems1 );
        final OrderDto orderDto1_placed = orderService.placeOrder( orderDto1 );

        SecurityContextHolder.getContext()
                .setAuthentication( new UsernamePasswordAuthenticationToken( "customer2", "password" ) );

        final OrderDto orderDto2 = new OrderDto();
        orderDto2.setCustomerId( customer2.getId() );
        orderDto2.setItems( orderItems2 );
        final OrderDto orderDto2_placed = orderService.placeOrder( orderDto2 );

        orderService.fulfillOrder( orderDto1_placed.getId() );
        orderService.fulfillOrder( orderDto2_placed.getId() );
        orderService.pickupOrder( orderDto1_placed.getId() );
        orderService.pickupOrder( orderDto2_placed.getId() );

        // Retrieve order history without filtering
        final List<OrderDto> orderHistory = orderService.viewOrderHistory( null );

        assertNotNull( orderHistory, "Order history should not be null" );
        assertEquals( 2, orderHistory.size(), "Order history should contain 2 orders" );

        // Validate createdAt and totalPrice
        for ( final OrderDto order : orderHistory ) {
            assertNotNull( order.getCreatedAt(), "createdAt should not be null" );
            assertNotNull( order.getTotalPrice(), "totalPrice should not be null" );
            assertTrue( order.getTotalPrice() > 0, "totalPrice should be greater than zero" );
        }

        // Retrieve order history filtered by "Milk"
        final List<OrderDto> filteredHistory = orderService.viewOrderHistory( "Milk" );

        assertNotNull( filteredHistory, "Filtered order history should not be null" );
        assertEquals( 1, filteredHistory.size(), "Filtered order history should contain 1 order" );
        assertTrue( filteredHistory.get( 0 ).getItems().containsKey( "Milk" ), "Filtered order should contain 'Milk'" );
    }

}

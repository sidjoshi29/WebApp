package edu.ncsu.csc326.wolfcafe.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.service.OrderService;

/*
 * Tests the OrderController Class
 */
@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    /*
     * Reference to MVC for HTTP requests
     */
    @Autowired
    private MockMvc mvc;

    /*
     * Reference to order service
     */
    @MockBean
    private OrderService orderService;

    /*
     * Reference to mapper to help with dto conversions
     */
    private static final ObjectMapper mapper = new ObjectMapper();
    /*
     * API endpoint
     */
    private static final String API_PATH = "/api/orders";

    /*
     * Tests placing an order
     */
    @Test
    @WithMockUser ( username = "customer", roles = "CUSTOMER" )
    public void testPlaceOrder () throws Exception {
        final OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId( 1L );

        final Map<String, Integer> items = new HashMap<>();
        items.put( "Deluxe Hamburger", 1 );
        orderDto.setItems( items );
        orderDto.setStatus( OrderStatus.PLACED ); // Set initial status

        Mockito.when( orderService.placeOrder( ArgumentMatchers.any() ) ).thenReturn( orderDto );

        final String json = mapper.writeValueAsString( orderDto );

        mvc.perform( post( API_PATH ).contentType( MediaType.APPLICATION_JSON ).content( json )
                .accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isCreated() )
                .andExpect( jsonPath( "$.customerId" ).value( 1 ) )
                .andExpect( jsonPath( "$.items['Deluxe Hamburger']" ).value( 1 ) )
                .andExpect( jsonPath( "$.status" ).value( OrderStatus.PLACED.toString() ) ); // Check
                                                                                             // status
    }

    /*
     * Tests placing an order when an item is not found
     */
    @Test
    @WithMockUser ( username = "customer", roles = "CUSTOMER" )
    public void testPlaceOrderNotFound () throws Exception {
        final OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId( 1L );

        final Map<String, Integer> items = new HashMap<>();
        items.put( "Unknown Item", 1 );
        orderDto.setItems( items );
        orderDto.setStatus( OrderStatus.PLACED );

        Mockito.when( orderService.placeOrder( ArgumentMatchers.any() ) )
                .thenThrow( new ResourceNotFoundException( "Item not found" ) );

        final String json = mapper.writeValueAsString( orderDto );

        mvc.perform( post( API_PATH ).contentType( MediaType.APPLICATION_JSON ).content( json )
                .accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isNotFound() )
                .andExpect( jsonPath( "$.message" ).value( "Item not found" ) )
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/orders" ) )
                .andExpect( jsonPath( "$.timeStamp" ).exists() );
    }

    /*
     * Tests getting all the orders as a staff member
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testGetAllOrdersAsStaff () throws Exception {
        final List<OrderDto> orders = createSampleOrders();

        Mockito.when( orderService.getAllOrders() ).thenReturn( orders );

        mvc.perform( get( API_PATH ).contentType( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.size()" ).value( 2 ) ).andExpect( jsonPath( "$[0].customerId" ).value( 1 ) )
                .andExpect( jsonPath( "$[0].items['Deluxe Hamburger']" ).value( 1 ) )
                .andExpect( jsonPath( "$[0].status" ).value( OrderStatus.PLACED.toString() ) ) // Check
                                                                                               // status
                .andExpect( jsonPath( "$[1].customerId" ).value( 1 ) )
                .andExpect( jsonPath( "$[1].items['Fries']" ).value( 2 ) )
                .andExpect( jsonPath( "$[1].status" ).value( OrderStatus.PLACED.toString() ) ); // Check
                                                                                                // status
    }

    /*
     * Tests getting all the orders for the current customer
     */
    @Test
    @WithMockUser ( username = "customer", roles = "CUSTOMER" )
    public void testGetOrdersForCurrentUser () throws Exception {
        final List<OrderDto> orders = createSampleOrders();

        Mockito.when( orderService.getOrdersForCurrentUser() ).thenReturn( orders );

        mvc.perform( get( API_PATH + "/customer" ).contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.size()" ).value( 2 ) )
                .andExpect( jsonPath( "$[0].customerId" ).value( 1 ) )
                .andExpect( jsonPath( "$[0].items['Deluxe Hamburger']" ).value( 1 ) )
                .andExpect( jsonPath( "$[0].status" ).value( OrderStatus.PLACED.toString() ) ) // Check
                                                                                               // status
                .andExpect( jsonPath( "$[1].customerId" ).value( 1 ) )
                .andExpect( jsonPath( "$[1].items['Fries']" ).value( 2 ) )
                .andExpect( jsonPath( "$[1].status" ).value( OrderStatus.PLACED.toString() ) ); // Check
                                                                                                // status
    }

    /*
     * Tests getting an order by the given id
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testGetOrderById () throws Exception {
        final OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId( 1L );

        final Map<String, Integer> items = new HashMap<>();
        items.put( "Deluxe Hamburger", 1 );
        orderDto.setItems( items );
        orderDto.setStatus( OrderStatus.PLACED ); // Set status

        Mockito.when( orderService.getOrder( ArgumentMatchers.eq( 1L ) ) ).thenReturn( orderDto );

        mvc.perform( get( API_PATH + "/1" ).contentType( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.customerId" ).value( 1 ) )
                .andExpect( jsonPath( "$.items['Deluxe Hamburger']" ).value( 1 ) )
                .andExpect( jsonPath( "$.status" ).value( OrderStatus.PLACED.toString() ) ); // Check
                                                                                             // status
    }

    /*
     * Tests canceling an order as a staff
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testCancelOrder () throws Exception {
        Mockito.when( orderService.cancelOrder( ArgumentMatchers.eq( 1L ) ) ).thenReturn( true );

        mvc.perform( delete( API_PATH + "/cancel/1" ).contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$" ).value( "Order cancelled successfully." ) );
    }

    /*
     * Helper to create sample orders to be used in other tests
     */
    private List<OrderDto> createSampleOrders () {
        final OrderDto order1 = new OrderDto();
        order1.setCustomerId( 1L );
        final Map<String, Integer> items1 = new HashMap<>();
        items1.put( "Deluxe Hamburger", 1 );
        order1.setItems( items1 );
        order1.setStatus( OrderStatus.PLACED ); // Set status

        final OrderDto order2 = new OrderDto();
        order2.setCustomerId( 1L );
        final Map<String, Integer> items2 = new HashMap<>();
        items2.put( "Fries", 2 );
        order2.setItems( items2 );
        order2.setStatus( OrderStatus.PLACED ); // Set status

        final List<OrderDto> orders = new ArrayList<>();
        orders.add( order1 );
        orders.add( order2 );

        return orders;
    }

    /*
     * Helper to create sample orders with a time and price to be used in other
     * tests
     */
    private List<OrderDto> createSampleOrdersWithTimeAndPrice () {
        final OrderDto order1 = new OrderDto();
        order1.setCustomerId( 1L );
        final Map<String, Integer> items1 = new HashMap<>();
        items1.put( "Deluxe Hamburger", 1 );
        order1.setItems( items1 );
        order1.setStatus( OrderStatus.PLACED );
        order1.setCreatedAt( LocalDateTime.of( 2024, 11, 16, 14, 30 ) ); // Example
                                                                         // timestamp
        order1.setTotalPrice( 15.99 );

        final OrderDto order2 = new OrderDto();
        order2.setCustomerId( 1L );
        final Map<String, Integer> items2 = new HashMap<>();
        items2.put( "Fries", 2 );
        order2.setItems( items2 );
        order2.setStatus( OrderStatus.PLACED );
        order2.setCreatedAt( LocalDateTime.of( 2024, 11, 16, 15, 0 ) ); // Example
                                                                        // timestamp
        order2.setTotalPrice( 5.99 );

        final List<OrderDto> orders = new ArrayList<>();
        orders.add( order1 );
        orders.add( order2 );

        return orders;
    }

    /*
     * Tests fulfilling an order as a staff
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testFulfillOrder () throws Exception {
        // Mock a successful fulfillment scenario
        Mockito.when( orderService.fulfillOrder( ArgumentMatchers.eq( 1L ) ) ).thenReturn( true );

        mvc.perform( put( API_PATH + "/fulfill/1" ).contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$" ).value( "Order fulfilled successfully." ) );
    }

    /*
     * Tests fulfilling an order when the order is not found
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testFulfillOrderNotFound () throws Exception {
        Mockito.when( orderService.fulfillOrder( ArgumentMatchers.eq( 99L ) ) )
                .thenThrow( new ResourceNotFoundException( "Order not found" ) );

        mvc.perform( put( API_PATH + "/fulfill/99" ).contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNotFound() ).andExpect( jsonPath( "$.message" ).value( "Order not found" ) )
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/orders/fulfill/99" ) )
                .andExpect( jsonPath( "$.timeStamp" ).exists() );
    }

    /*
     * Tests fulfilling an order when the order is in the wrong status
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testFulfillOrderBadRequest () throws Exception {
        // Mock a scenario where the order cannot be fulfilled (e.g., incorrect
        // status)
        Mockito.when( orderService.fulfillOrder( ArgumentMatchers.eq( 1L ) ) ).thenReturn( false );

        mvc.perform( put( API_PATH + "/fulfill/1" ).contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath( "$" ).value( "Order cannot be fulfilled." ) );
    }

    /*
     * Tests picking up an order on success
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testPickupOrderSuccess () throws Exception {
        // Mock a successful pickup scenario
        Mockito.when( orderService.pickupOrder( ArgumentMatchers.eq( 1L ) ) ).thenReturn( true );

        mvc.perform( put( API_PATH + "/pickup/1" ).contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$" ).value( "Order picked up successfully." ) );
    }

    /*
     * Tests picking up an order when the order is not found
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testPickupOrderNotFound () throws Exception {
        // Mock a scenario where the order is not found
        Mockito.when( orderService.pickupOrder( ArgumentMatchers.eq( 99L ) ) )
                .thenThrow( new ResourceNotFoundException( "Order not found" ) );

        mvc.perform( put( API_PATH + "/pickup/99" ).contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNotFound() ).andExpect( jsonPath( "$.message" ).value( "Order not found" ) ) // Validate
                                                                                                                    // error
                                                                                                                    // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/orders/pickup/99" ) ) // Validate
                                                                                           // URI
                                                                                           // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

    /*
     * Tests picking up an order when the order is not in the correct status
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testPickupOrderFailure () throws Exception {
        // Mock a scenario where the order cannot be picked up (e.g., incorrect
        // status)
        Mockito.when( orderService.pickupOrder( ArgumentMatchers.eq( 1L ) ) ).thenReturn( false );

        mvc.perform( put( API_PATH + "/pickup/1" ).contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath( "$" ).value( "Order cannot be picked up." ) );
    }

    /*
     * Test viewOrderHistory with the results and the total price
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testViewOrderHistoryWithResultsAndTotalPrice () throws Exception {
        // Mock order history data
        final List<OrderDto> orderHistory = createSampleOrdersWithTimeAndPrice();

        // Mock service call to return the order history
        Mockito.when( orderService.viewOrderHistory( ArgumentMatchers.anyString() ) ).thenReturn( orderHistory );

        // Perform GET request to the history endpoint with a query parameter
        mvc.perform( get( API_PATH + "/history" ).param( "itemName", "Deluxe Hamburger" )
                .contentType( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.size()" ).value( 2 ) ).andExpect( jsonPath( "$[0].customerId" ).value( 1 ) )
                .andExpect( jsonPath( "$[0].items['Deluxe Hamburger']" ).value( 1 ) )
                .andExpect( jsonPath( "$[0].status" ).value( OrderStatus.PLACED.toString() ) )
                .andExpect( jsonPath( "$[0].createdAt" ).isNotEmpty() )
                .andExpect( jsonPath( "$[0].totalPrice" ).value( 15.99 ) )
                .andExpect( jsonPath( "$[1].customerId" ).value( 1 ) )
                .andExpect( jsonPath( "$[1].items['Fries']" ).value( 2 ) )
                .andExpect( jsonPath( "$[1].status" ).value( OrderStatus.PLACED.toString() ) )
                .andExpect( jsonPath( "$[1].createdAt" ).isNotEmpty() )
                .andExpect( jsonPath( "$[1].totalPrice" ).value( 5.99 ) );
    }

    /*
     * Tests viewOrderHistory when an unexpected error occurs
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testViewOrderHistoryFailure () throws Exception {
        // Mock service call to throw a generic exception
        Mockito.when( orderService.viewOrderHistory( ArgumentMatchers.anyString() ) )
                .thenThrow( new RuntimeException( "Unexpected error" ) );

        // Perform GET request to the history endpoint
        mvc.perform( get( API_PATH + "/history" ).param( "itemName", "Deluxe Hamburger" )
                .contentType( MediaType.APPLICATION_JSON ) ).andExpect( status().isInternalServerError() )
                .andExpect( jsonPath( "$.message" ).value( "An unexpected error occurred" ) ) // Validate
                                                                                              // generic
                                                                                              // error
                                                                                              // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/orders/history" ) ) // Validate
                                                                                         // URI
                                                                                         // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

    /*
     * Tests placing an order when there is insufficient inventory
     */
    @Test
    @WithMockUser ( username = "customer", roles = "CUSTOMER" )
    public void testPlaceOrderIllegalStateException () throws Exception {
        final OrderDto orderDto = new OrderDto();
        orderDto.setCustomerId( 1L );

        final Map<String, Integer> items = new HashMap<>();
        items.put( "Deluxe Hamburger", 1 );
        orderDto.setItems( items );

        Mockito.when( orderService.placeOrder( ArgumentMatchers.any() ) )
                .thenThrow( new IllegalStateException( "Insufficient inventory for item: Deluxe Hamburger" ) );

        final String json = mapper.writeValueAsString( orderDto );

        mvc.perform( post( API_PATH )
                .contentType( MediaType.APPLICATION_JSON ).content( json ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isConflict() )
                .andExpect( jsonPath( "$.message" )
                        .value( "An illegal state occurred: Insufficient inventory for item: Deluxe Hamburger" ) ) // Validate
                                                                                                                   // error
                                                                                                                   // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/orders" ) ) // Validate
                                                                                 // URI
                                                                                 // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

    /*
     * Tests canceling and order when the user is not authorized to cancel i.e
     * not a staff
     */
    @Test
    @WithMockUser ( username = "customer", roles = "CUSTOMER" )
    public void testCancelOrderAccessDeniedException () throws Exception {
        Mockito.when( orderService.cancelOrder( ArgumentMatchers.eq( 1L ) ) )
                .thenThrow( new AccessDeniedException( "You are not authorized to cancel this order." ) );

        mvc.perform( delete( API_PATH + "/cancel/1" ).contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isForbidden() ).andExpect( jsonPath( "$.message" ).value( "Access is denied" ) ) // Validate
                                                                                                                      // generic
                                                                                                                      // access
                                                                                                                      // denied
                                                                                                                      // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/orders/cancel/1" ) ) // Validate
                                                                                          // URI
                                                                                          // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

    /*
     * Tests fulfilling an order when the order is not in the correct state
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testFulfillOrderIllegalStateException () throws Exception {
        Mockito.when( orderService.fulfillOrder( ArgumentMatchers.eq( 1L ) ) ).thenThrow(
                new IllegalStateException( "Order with ID 1 is not in PLACED status and cannot be fulfilled." ) );

        mvc.perform( put( API_PATH + "/fulfill/1" ).contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isConflict() )
                .andExpect( jsonPath( "$.message" )
                        .value( "An illegal state occurred: Order with ID 1 is not in PLACED status and cannot be fulfilled." ) ) // Validate
                                                                                                                                  // error
                                                                                                                                  // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/orders/fulfill/1" ) ) // Validate
                                                                                           // URI
                                                                                           // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

    /*
     * Tests picking up an order when the order is not in the correct status
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testPickupOrderIllegalStateException () throws Exception {
        Mockito.when( orderService.pickupOrder( ArgumentMatchers.eq( 1L ) ) ).thenThrow(
                new IllegalStateException( "Order with ID 1 is not in FULFILLED status and cannot be picked up." ) );

        mvc.perform( put( API_PATH + "/pickup/1" ).contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isConflict() )
                .andExpect( jsonPath( "$.message" )
                        .value( "An illegal state occurred: Order with ID 1 is not in FULFILLED status and cannot be picked up." ) ) // Validate
                                                                                                                                     // error
                                                                                                                                     // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/orders/pickup/1" ) ) // Validate
                                                                                          // URI
                                                                                          // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

    /*
     * Tests getting all the orders for the current user but the user doesn't
     * exist
     */
    @Test
    @WithMockUser ( username = "customer", roles = "CUSTOMER" )
    public void testGetOrdersForCurrentUserResourceNotFound () throws Exception {
        // Simulate the user not being found
        Mockito.when( orderService.getOrdersForCurrentUser() )
                .thenThrow( new ResourceNotFoundException( "User not found" ) );

        mvc.perform( get( API_PATH + "/customer" ).contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNotFound() ).andExpect( jsonPath( "$.message" ).value( "User not found" ) ) // Validate
                                                                                                                   // error
                                                                                                                   // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/orders/customer" ) ) // Validate
                                                                                          // URI
                                                                                          // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

    /*
     * Tests getting an order by id but the order doesnt exist
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testGetOrderByIdResourceNotFound () throws Exception {
        // Simulate order not found
        Mockito.when( orderService.getOrder( ArgumentMatchers.eq( 99L ) ) )
                .thenThrow( new ResourceNotFoundException( "Order not found" ) );

        mvc.perform( get( API_PATH + "/99" ).contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNotFound() ).andExpect( jsonPath( "$.message" ).value( "Order not found" ) ) // Validate
                                                                                                                    // error
                                                                                                                    // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/orders/99" ) ) // Validate
                                                                                    // URI
                                                                                    // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

    /*
     * Tests canceling an order but the order doesnt exist
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testCancelOrderResourceNotFound () throws Exception {
        // Simulate order not found during cancellation
        Mockito.when( orderService.cancelOrder( ArgumentMatchers.eq( 99L ) ) )
                .thenThrow( new ResourceNotFoundException( "Order not found" ) );

        mvc.perform( delete( API_PATH + "/cancel/99" ).contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNotFound() ).andExpect( jsonPath( "$.message" ).value( "Order not found" ) ) // Validate
                                                                                                                    // error
                                                                                                                    // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/orders/cancel/99" ) ) // Validate
                                                                                           // URI
                                                                                           // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

    /*
     * Tests fulfilling an order but the order doesnt exist
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testFulfillOrderResourceNotFound () throws Exception {
        // Simulate order not found during fulfillment
        Mockito.when( orderService.fulfillOrder( ArgumentMatchers.eq( 99L ) ) )
                .thenThrow( new ResourceNotFoundException( "Order not found" ) );

        mvc.perform( put( API_PATH + "/fulfill/99" ).contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNotFound() ).andExpect( jsonPath( "$.message" ).value( "Order not found" ) ) // Validate
                                                                                                                    // error
                                                                                                                    // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/orders/fulfill/99" ) ) // Validate
                                                                                            // URI
                                                                                            // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

    /*
     * Tests picking up an order but the order doesnt exist
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testPickupOrderResourceNotFound () throws Exception {
        // Simulate order not found during pickup
        Mockito.when( orderService.pickupOrder( ArgumentMatchers.eq( 99L ) ) )
                .thenThrow( new ResourceNotFoundException( "Order not found" ) );

        mvc.perform( put( API_PATH + "/pickup/99" ).contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNotFound() ).andExpect( jsonPath( "$.message" ).value( "Order not found" ) ) // Validate
                                                                                                                    // error
                                                                                                                    // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/orders/pickup/99" ) ) // Validate
                                                                                           // URI
                                                                                           // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

    /*
     * Tests viewing the order history with filtering but the item is not in the
     * order history
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testViewOrderHistoryResourceNotFound () throws Exception {
        // Simulate resource not found when filtering order history
        Mockito.when( orderService.viewOrderHistory( ArgumentMatchers.anyString() ) )
                .thenThrow( new ResourceNotFoundException( "Item not found in order history" ) );

        mvc.perform( get( API_PATH + "/history" ).param( "itemName", "Nonexistent Item" )
                .contentType( MediaType.APPLICATION_JSON ) ).andExpect( status().isNotFound() )
                .andExpect( jsonPath( "$.message" ).value( "Item not found in order history" ) ) // Validate
                                                                                                 // error
                                                                                                 // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/orders/history" ) ) // Validate
                                                                                         // URI
                                                                                         // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

}

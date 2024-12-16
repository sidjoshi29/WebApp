package edu.ncsu.csc326.wolfcafe.repositories;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.test.context.support.WithMockUser;

import edu.ncsu.csc326.wolfcafe.entity.Order;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.repository.OrderRepository;
import jakarta.persistence.EntityManager;

/*
 * Tests the OrderRepository
 */
@DataJpaTest
@AutoConfigureTestDatabase ( replace = Replace.NONE )
public class OrderRepositoryTest {
    /** Reference to order repository */

    @Autowired
    private OrderRepository   orderRepository;
    /** Reference to EntityManager */
    @Autowired
    private TestEntityManager testEntityManager;
    /** Represents an order */
    private Order             order;
    /** Represents a cutomer user */
    private User              customer;

    /**
     * Sets up the test case. Clears up existing information for future tests
     */
    @BeforeEach
    public void setUp () {
        final EntityManager entityManager = testEntityManager.getEntityManager();

        // Clean up existing records in the correct order
        entityManager.createNativeQuery( "DELETE FROM order_items" ).executeUpdate();
        entityManager.createNativeQuery( "DELETE FROM customer_order" ).executeUpdate();
        entityManager.createNativeQuery( "DELETE FROM users_roles" ).executeUpdate();
        entityManager.createNativeQuery( "DELETE FROM users" ).executeUpdate();
        entityManager.createNativeQuery( "ALTER TABLE customer_order AUTO_INCREMENT = 1" ).executeUpdate();

        // Create and persist a User (customer)
        customer = new User();
        customer.setUsername( "customer1" );
        customer.setEmail( "customer1@example.com" );
        customer.setPassword( "password" );
        customer = testEntityManager.persistAndFlush( customer );

        // Set up an order with items and associate it with the customer
        final Map<String, Integer> items = new HashMap<>();
        items.put( "Deluxe Hamburger", 1 );
        items.put( "Fries", 2 );
        items.put( "Water Bottle", 1 );

        double totalPrice = 0.0;
        totalPrice += 1 * 5.0; // Assuming "Deluxe Hamburger" costs 5.0
        totalPrice += 2 * 2.0; // Assuming "Fries" costs 2.0 each
        totalPrice += 1 * 1.0; // Assuming "Water Bottle" costs 1.0

        order = new Order();
        order.setCustomer( customer ); // Associate the order with customer1
        order.setItems( items );
        order.setStatus( OrderStatus.PLACED ); // Set initial status to PLACED
        order.setCreatedAt( LocalDateTime.now() ); // Set createdAt to the
                                                   // current timestamp
        order.setTotalPrice( totalPrice ); // Set totalPrice
        orderRepository.save( order );
    }

    /*
     * Tests saving and getting an order
     */
    @Test
    public void testSaveAndGetOrder () {
        final Optional<Order> fetchedOrderOptional = orderRepository.findById( order.getId() );
        assertTrue( fetchedOrderOptional.isPresent(), "Order should be present" );

        final Order fetchedOrder = fetchedOrderOptional.get();
        final Map<String, Integer> items = fetchedOrder.getItems();

        assertAll( "Order contents", () -> assertEquals( order.getId(), fetchedOrder.getId() ),
                () -> assertEquals( order.getCustomer().getId(), fetchedOrder.getCustomer().getId(),
                        "Customer ID should match" ),
                () -> assertEquals( "customer1", fetchedOrder.getCustomer().getUsername(),
                        "Customer username should match" ),
                () -> assertEquals( 1, items.get( "Deluxe Hamburger" ) ), () -> assertEquals( 2, items.get( "Fries" ) ),
                () -> assertEquals( 1, items.get( "Water Bottle" ) ),
                () -> assertEquals( OrderStatus.PLACED, fetchedOrder.getStatus(), "Order status should be PLACED" ),
                () -> assertNotNull( fetchedOrder.getCreatedAt(), "createdAt should not be null" ) );
        assertEquals( 10.0, fetchedOrder.getTotalPrice(), "Total price should match the calculated value" );

    }

    /*
     * Tests updateOrder
     */
    @Test
    public void testUpdateOrder () {
        // Fetch the order and update item quantities and status
        final Optional<Order> fetchedOrderOptional = orderRepository.findById( order.getId() );
        assertTrue( fetchedOrderOptional.isPresent(), "Order should be present" );

        final Order fetchedOrder = fetchedOrderOptional.get();
        final Map<String, Integer> items = fetchedOrder.getItems();
        items.put( "Deluxe Hamburger", 2 ); // Update to 2 deluxe hamburgers
        items.put( "Fries", 3 ); // Update to 3 fries

        // Save updated order with status change
        fetchedOrder.setItems( items );
        fetchedOrder.setStatus( OrderStatus.FULFILLED ); // Change status to
                                                         // READY
        orderRepository.save( fetchedOrder );

        // Fetch the updated order and check values
        final Order updatedOrder = orderRepository.findById( order.getId() ).get();
        final Map<String, Integer> updatedItems = updatedOrder.getItems();

        assertAll( "Updated Order contents", () -> assertEquals( 2, updatedItems.get( "Deluxe Hamburger" ) ),
                () -> assertEquals( 3, updatedItems.get( "Fries" ) ),
                () -> assertEquals( 1, updatedItems.get( "Water Bottle" ) ),
                () -> assertEquals( OrderStatus.FULFILLED, updatedOrder.getStatus(), "Order status should be READY" ) );

    }

    /*
     * Tests deleteOrder
     */
    @Test
    public void testDeleteOrder () {
        // Delete the order and confirm it no longer exists
        orderRepository.delete( order );
        assertTrue( orderRepository.findById( order.getId() ).isEmpty(), "Order should be deleted" );

    }

    /**
     * Test finding orders by customer ID.
     */
    @WithMockUser ( username = "customer1", roles = "CUSTOMER" )
    @Test
    public void testFindByCustomerId () {
        // Assuming `findByCustomerId` is updated to query the customer entity
        final List<Order> orders = orderRepository.findByCustomerId( customer.getId() );
        assertEquals( 1, orders.size(), "Should find one order for the customer" );

        final Order fetchedOrder = orders.get( 0 );
        final Map<String, Integer> items = fetchedOrder.getItems();

        assertAll( "Order contents for customer",
                () -> assertEquals( customer.getId(), fetchedOrder.getCustomer().getId(), "Customer ID should match" ),
                () -> assertEquals( "customer1", fetchedOrder.getCustomer().getUsername(),
                        "Customer username should match" ),
                () -> assertEquals( 1, items.get( "Deluxe Hamburger" ) ), () -> assertEquals( 2, items.get( "Fries" ) ),
                () -> assertEquals( 1, items.get( "Water Bottle" ) ),
                () -> assertEquals( OrderStatus.PLACED, fetchedOrder.getStatus(), "Order status should be PLACED" ) );

    }
}

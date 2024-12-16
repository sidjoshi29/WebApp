package edu.ncsu.csc326.wolfcafe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.ncsu.csc326.wolfcafe.entity.Order;

/**
 * Repository interface for Order entities. Provides methods for CRUD operations
 * and data access related to orders in the database.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Finds the list of orders by the customer id.
     *
     * @param customerId
     *            id of the customer orders we want
     * @return List of orders for that customer
     */
    List<Order> findByCustomerId ( Long customerId );
}

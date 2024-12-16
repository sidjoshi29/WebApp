package edu.ncsu.csc326.wolfcafe.entity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing a customer order in the WolfCafe system.
 */
@Entity
@Table ( name = "customer_order" )
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /** Unique identifier for the order. */
    @Id
    @GeneratedValue ( strategy = GenerationType.IDENTITY )
    private Long                 id;

    /** The customer who placed the order. */
    @ManyToOne ( fetch = FetchType.EAGER )
    @JoinColumn ( name = "customer_id", nullable = false )
    private User                 customer;

    /** The tip the customer gives on the order. */
    @Column ( name = "tip", nullable = false )
    private Double               tip    = 0.0;

    /**
     * Map storing the items and their quantities in the order. The key is the
     * item's name, and the value is the quantity.
     */
    @ElementCollection
    @CollectionTable ( name = "order_items", joinColumns = @JoinColumn ( name = "order_id" ) )
    @MapKeyColumn ( name = "item_name" )
    @Column ( name = "quantity" )
    @NotEmpty ( message = "The order must contain at least one item." )
    private Map<String, Integer> items  = new HashMap<>();

    /** Status of the order (PLACED, READY, COMPLETED, or CANCELLED). */
    @Enumerated ( EnumType.STRING )
    private OrderStatus          status = OrderStatus.PLACED; // Default to
                                                              // PLACED

    /** keep track of the date of purchase for order history */
    @Column ( name = "created_at", nullable = false, updatable = false )
    private LocalDateTime        createdAt;

    /** Store total price for order history */
    @Column ( name = "total_price", nullable = false )
    private Double               totalPrice;

}

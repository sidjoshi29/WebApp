package edu.ncsu.csc326.wolfcafe.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Order DTO class for data transfer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude ( JsonInclude.Include.NON_NULL ) // Only include non-null fields in
                                              // JSON
public class OrderDto {

    /** Unique identifier for the order. */
    private Long                 id;

    /** ID of the customer who placed the order. */
    private Long                 customerId;

    /** Map of items in the order with their quantities. */
    private Map<String, Integer> items;

    /** Status of the order, only available in responses */
    @JsonProperty ( access = Access.READ_ONLY ) // Makes status read-only in
                                                // JSON
    private OrderStatus          status;

    /**
     * Name of the customer who placed the order (for convenience in responses).
     */
    @JsonProperty ( access = Access.READ_ONLY ) // Makes customerName read-only
                                                // in JSON
    private String               customerName;

    /** Date and time of the order creation. */
    @JsonProperty ( access = Access.READ_ONLY ) // Makes createdAt read-only in
                                                // JSON
    private LocalDateTime        createdAt;

    /** Total price of the order, only available in responses. */
    @JsonProperty ( access = Access.READ_ONLY ) // Makes totalPrice read-only in
                                                // JSON
    private Double               totalPrice;
    
    /** Tip added to the order */
    private Double tip;

}

/**
 * Entity class representing the Inventory in the Coffee Maker application. The
 * inventory contains a collection of ingredients and their quantities, and is
 * mapped to a database table using JPA annotations.
 */
package edu.ncsu.csc326.wolfcafe.entity;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyJoinColumn;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inventory class represents the WolfCafe's inventory, which consists of items
 * and their quantities. This class is an entity that maps to the database using
 * JPA.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    /** Unique identifier for the inventory entry */
    @Id
    @GeneratedValue ( strategy = GenerationType.IDENTITY )
    private Long               id;

    /** Map of items and their respective quantities in the inventory */
    @ElementCollection
    @CollectionTable ( name = "inventory_items", joinColumns = @JoinColumn ( name = "inventory_id" ) )
    @MapKeyJoinColumn ( name = "item_id" )
    @Column ( name = "quantity" )
    @PositiveOrZero // Ensure quantity is zero or positive
    private Map<Item, Integer> items = new HashMap<>();

}

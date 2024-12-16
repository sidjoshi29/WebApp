package edu.ncsu.csc326.wolfcafe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Item class represents an item for sale in WolfCafe, which consists of an id,
 * name, description. and price. This class is an entity that maps to the
 * database using JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table ( name = "items" )
public class Item {

    /** Unique identifier for the item entry */
    @Id
    @GeneratedValue ( strategy = GenerationType.IDENTITY )
    private Long   id;

    /** Name of the item entry */
    @Column ( nullable = false, unique = true )
    private String name;
    /** Description of the item entry */
    private String description;
    /** Price of the item entry (must be greater than 0) */
    @Column ( nullable = false )
    @Positive ( message = "Price must be greater than 0" )
    private double price;

}

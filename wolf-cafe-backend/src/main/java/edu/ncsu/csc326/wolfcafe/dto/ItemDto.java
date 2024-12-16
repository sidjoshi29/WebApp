package edu.ncsu.csc326.wolfcafe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for representing an Item in the WolfCafe system.
 * The ItemDto holds the ID, name, description and price for an item in the
 * WolfCafe system. This class is used to transfer item data between different
 * layers of the application.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    /** The unique identifier for an item */
    private Long   id;
    /** The name of the item */
    private String name;
    /** The description of the item */
    private String description;
    /** The price of the item */
    private double price;
}

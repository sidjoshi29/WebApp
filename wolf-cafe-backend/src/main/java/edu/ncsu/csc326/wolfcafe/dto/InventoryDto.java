package edu.ncsu.csc326.wolfcafe.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for representing the Inventory in the Coffee Maker
 * system. The InventoryDto holds the inventory ID and a map of item names with
 * their respective quantities. This class is used to transfer inventory data
 * between different layers of the application.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDto {

	/** The unique identifier for the inventory */
	private Long id;

	/**
	 * A map that holds the item names and their quantities in the inventory
	 */
	private Map<String, Integer> items = new HashMap<>();
}

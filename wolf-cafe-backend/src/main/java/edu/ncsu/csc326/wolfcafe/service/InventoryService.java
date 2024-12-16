package edu.ncsu.csc326.wolfcafe.service;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.entity.Item;

/**
 * Interface defining the inventory behaviors.
 */
public interface InventoryService {

	/**
	 * Creates inventory.
	 *
	 * @param inventoryDto inventory to create
	 * @return updated inventory after creation
	 */
	InventoryDto createInventory(InventoryDto inventoryDto);

	/**
	 * Returns inventory.
	 *
	 * @return the returned inventory
	 */
	InventoryDto getInventory();

	/**
	 * Updates the contents of an inventory.
	 *
	 * @param inventoryDto values to update
	 * @return updated inventory
	 */
	InventoryDto updateInventory(InventoryDto inventoryDto);

	/**
	 * Adds an item to the inventory with a specified quantity.
	 *
	 * @param item     the item to add
	 * @param quantity the initial quantity of the item in the inventory
	 */
	boolean addItemToInventory(Item item);

	/**
	 * Updates an existing item in the inventory if present.
	 *
	 * @param updatedItem the item with updated details
	 */
	void updateItemInInventory(Item updatedItem);

}

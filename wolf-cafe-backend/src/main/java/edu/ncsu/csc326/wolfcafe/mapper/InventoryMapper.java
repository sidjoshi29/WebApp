package edu.ncsu.csc326.wolfcafe.mapper;

import java.util.HashMap;
import java.util.Map;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;

/**
 * Converts between InventoryDto and Inventory entity.
 */
public class InventoryMapper {

	/**
	 * Converts an Inventory entity to InventoryDto.
	 *
	 * @param inventory Inventory to convert
	 * @return InventoryDto object
	 */
	public static InventoryDto mapToInventoryDto(final Inventory inventory) {
		final InventoryDto inventoryDto = new InventoryDto();
		inventoryDto.setId(inventory.getId());

		// Convert the Map<Item, Integer> to Map<String, Integer>
		final Map<String, Integer> itemMap = new HashMap<>();
		for (final Map.Entry<Item, Integer> entry : inventory.getItems().entrySet()) {
			final Item item = entry.getKey();
			final Integer amount = entry.getValue();

			// Use the name of the item as the key for the DTO map
			itemMap.put(item.getName(), amount);
		}

		// Set the converted item map to the DTO
		inventoryDto.setItems(itemMap);

		return inventoryDto;
	}

	/**
	 * Converts an InventoryDto to an Inventory entity.
	 *
	 * @param inventoryDto   InventoryDto to convert
	 * @param itemRepository is an instance of the current item repository to get
	 *                       items from
	 * @return Inventory entity
	 */
	public static Inventory mapToInventory(final InventoryDto inventoryDto, final ItemRepository itemRepository) {
		final Inventory inventory = new Inventory();
		inventory.setId(inventoryDto.getId());

		// Convert the Map<String, Integer> to Map<Item, Integer>
		final Map<Item, Integer> itemMap = new HashMap<>();
		for (final Map.Entry<String, Integer> entry : inventoryDto.getItems().entrySet()) {
			final String itemName = entry.getKey();
			final Integer amount = entry.getValue();

			// Fetch the item from the repository by name to ensure persistence
			final Item item = itemRepository.findByName(itemName)
					.orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemName));

			itemMap.put(item, amount);
		}

		inventory.setItems(itemMap);
		return inventory;
	}
}

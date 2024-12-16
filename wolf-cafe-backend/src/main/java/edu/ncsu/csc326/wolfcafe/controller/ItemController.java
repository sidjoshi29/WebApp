package edu.ncsu.csc326.wolfcafe.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.service.ItemService;
import lombok.AllArgsConstructor;

/**
 * Controller for API endpoints for an Item
 */
@RestController
@RequestMapping("api/items")
@AllArgsConstructor
@CrossOrigin("*")
public class ItemController {

	/** Link to ItemService */
	private ItemService itemService;

	/**
	 * Adds an item to the list of items. Requires the STAFF role.
	 *
	 * @param itemDto item to add
	 * @return added item
	 */
	@PreAuthorize("hasRole('STAFF')")
	@PostMapping
	public ResponseEntity<ItemDto> addItem(@RequestBody ItemDto itemDto) {
		ItemDto savedItem = itemService.addItem(itemDto);
		return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
	}

	/**
	 * Gets an item by id. Requires the STAFF or CUSTOMER role.
	 *
	 * @param id item id
	 * @return item with the id
	 */
	@PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')")
	@GetMapping("{id}")
	public ResponseEntity<ItemDto> getItem(@PathVariable("id") Long id) {
		ItemDto item = itemService.getItem(id);
		return ResponseEntity.ok(item);
	}

	/**
	 * Returns all items. Requires the STAFF or CUSTOMER role.
	 *
	 * @return a list of all items
	 */
	@PreAuthorize("hasAnyRole('STAFF', 'CUSTOMER')")
	@GetMapping
	public ResponseEntity<List<ItemDto>> getAllItems() {
		List<ItemDto> items = itemService.getAllItems();
		return ResponseEntity.ok(items);
	}

	/**
	 * Updates the item with the given id. Requires STAFF role.
	 *
	 * @param id      item to update
	 * @param itemDto information about the item to update
	 * @return updated item
	 */
	@PreAuthorize("hasRole('STAFF')")
	@PutMapping("{id}")
	public ResponseEntity<ItemDto> updateItem(@PathVariable("id") Long id, @RequestBody ItemDto itemDto) {
		ItemDto updatedItem = itemService.updateItem(id, itemDto);
		return ResponseEntity.ok(updatedItem);
	}

	/**
	 * Deletes the item with the given id. Requires the STAFF role.
	 *
	 * @param id item to delete
	 * @return response indicating success or failure
	 */
	@PreAuthorize("hasRole('STAFF')")
	@DeleteMapping("{id}")
	public ResponseEntity<String> deleteItem(@PathVariable("id") Long id) {
		itemService.deleteItem(id);
		return ResponseEntity.ok("Item deleted successfully.");
	}
}

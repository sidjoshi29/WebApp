package edu.ncsu.csc326.wolfcafe.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.repository.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;
import edu.ncsu.csc326.wolfcafe.service.InventoryService;
import edu.ncsu.csc326.wolfcafe.service.ItemService;

/*
 * Tests ItemService
 */
@SpringBootTest
public class ItemServiceTest {

    /**
     * Reference to Item Service
     */
    @Autowired
    private ItemService         itemService;

    /**
     * Reference to item repository
     */
    @Autowired
    private ItemRepository      itemRepository;

    /**
     * Reference to iventory repository
     */
    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * Reference to inventory service
     */
    @Autowired
    private InventoryService    inventoryService;

    /*
     * Deletes all the items and inventory before tests
     */
    @BeforeEach
    public void setUp () {
        itemRepository.deleteAll();
        inventoryRepository.deleteAll();
    }

    /**
     * Tests creating an item and ensuring it’s added to the inventory.
     */
    @Test
    @Transactional
    public void testCreateItem () {
        // Prepare ItemDto for creation
        final ItemDto itemDto = new ItemDto( null, "Espresso", "Strong and black coffee", 2.5 );

        // Create and save the item
        final ItemDto savedItemDto = itemService.addItem( itemDto );

        // Verify item was saved and added to inventory
        assertAll( "Item Creation", () -> assertNotNull( savedItemDto.getId() ),
                () -> assertEquals( "Espresso", savedItemDto.getName() ),
                () -> assertEquals( "Strong and black coffee", savedItemDto.getDescription() ),
                () -> assertEquals( 2.5, savedItemDto.getPrice() ) );

        // Check that item exists in the inventory with quantity 0
        assertEquals( 0, inventoryService.getInventory().getItems().get( "Espresso" ) );
    }

    /**
     * Tests creating a duplicate item and ensuring it’s not added.
     */
    @Test
    @Transactional
    public void testDuplicateCreateItem () {
        // Prepare ItemDto for creation
        final ItemDto itemDto = new ItemDto( null, "Espresso", "Strong and black coffee", 2.5 );

        // Create and save the item
        final ItemDto savedItemDto = itemService.addItem( itemDto );

        // Verify item was saved and added to inventory
        assertAll( "Item Creation", () -> assertNotNull( savedItemDto.getId() ),
                () -> assertEquals( "Espresso", savedItemDto.getName() ),
                () -> assertEquals( "Strong and black coffee", savedItemDto.getDescription() ),
                () -> assertEquals( 2.5, savedItemDto.getPrice() ) );

        // Check that item exists in the inventory with quantity 0
        assertEquals( 0, inventoryService.getInventory().getItems().get( "Espresso" ) );

        final Exception exception = assertThrows( WolfCafeAPIException.class, () -> {
            itemService.addItem( itemDto );

        } );

        final String expectedMessage = "Invalid fields for an Item. Be sure that item fields are non-null and not a duplicate.";
        final String actualMessage = exception.getMessage();

        assertEquals( expectedMessage, actualMessage );
    }

    /**
     * Tests retrieving an item by ID.
     */
    @Test
    @Transactional
    public void testGetItemById () {
        // Use itemService to create the item to ensure inventory consistency
        final ItemDto itemDto = new ItemDto( null, "Latte", "Coffee with milk", 3.0 );
        final ItemDto savedItemDto = itemService.addItem( itemDto );

        // Retrieve the item by ID using the service
        final ItemDto retrievedItemDto = itemService.getItem( savedItemDto.getId() );

        // Verify that the retrieved item matches the saved item
        assertAll( "Retrieve Item by ID", () -> assertEquals( savedItemDto.getId(), retrievedItemDto.getId() ),
                () -> assertEquals( "Latte", retrievedItemDto.getName() ),
                () -> assertEquals( "Coffee with milk", retrievedItemDto.getDescription() ),
                () -> assertEquals( 3.0, retrievedItemDto.getPrice() ) );
    }

    /**
     * Tests updating an existing item.
     */
    @Test
    @Transactional
    public void testUpdateItem () {
        // Use itemService to create the item
        final ItemDto itemDto = new ItemDto( null, "Latte", "Coffee with milk", 3.0 );
        final ItemDto savedItemDto = itemService.addItem( itemDto );

        // Prepare updated data for the item
        final ItemDto updatedItemDto = new ItemDto( savedItemDto.getId(), "Mocha",
                "Chocolate coffee with whipped cream", 4.0 );

        // Perform the update
        final ItemDto updatedDto = itemService.updateItem( savedItemDto.getId(), updatedItemDto );

        // Verify the updated item
        assertAll( "Updated Item", () -> assertEquals( "Mocha", updatedDto.getName() ),
                () -> assertEquals( "Chocolate coffee with whipped cream", updatedDto.getDescription() ),
                () -> assertEquals( 4.0, updatedDto.getPrice() ) );
    }

    /*
     * Tests deleting an item from the inventory when the item is not in the
     * inventory
     */
    @Test
    @Transactional
    public void testDeleteItemInventoryRemovalFailure () {
        // Create an item
        final ItemDto itemDto = new ItemDto( null, "Latte", "Coffee with milk", 3.0 );
        final ItemDto savedItemDto = itemService.addItem( itemDto );

        // Remove the item from inventory manually
        final InventoryDto inventory = inventoryService.getInventory();
        inventory.getItems().remove( savedItemDto.getName() );
        inventoryService.updateInventory( inventory );

        // Attempt to delete the item
        final Exception exception = assertThrows( WolfCafeAPIException.class,
                () -> itemService.deleteItem( savedItemDto.getId() ) );

        // Verify the error message
        final String expectedMessage = "Item with name 'Latte' does not exist in the inventory.";
        assertEquals( expectedMessage, exception.getMessage() );
    }

    /*
     * Tests updating an item in the inventory when there is a synchronization
     * issue
     */
    @Test
    @Transactional
    public void testUpdateItemInventorySyncFailure () {
        // Create an item
        final ItemDto itemDto = new ItemDto( null, "Latte", "Coffee with milk", 3.0 );
        final ItemDto savedItemDto = itemService.addItem( itemDto );
        assertNotNull( itemRepository.findById( savedItemDto.getId() ).orElse( null ),
                "Item should exist in the repository" );

        final InventoryDto inventory = inventoryService.getInventory();
        inventory.getItems().remove( savedItemDto.getName() );
        inventoryService.updateInventory( inventory );

        // Attempt to update the item
        final ItemDto updatedItemDto = new ItemDto( null, "Mocha", "Coffee with chocolate", 4.0 );

        final Exception exception = assertThrows( WolfCafeAPIException.class,
                () -> itemService.updateItem( savedItemDto.getId(), updatedItemDto ) );

        // Verify the error message
        final String expectedMessage = "Failed to update item: Inventory synchronization failed for item with ID "
                + savedItemDto.getId() + ".";
        assertEquals( expectedMessage, exception.getMessage() );
    }

    /**
     * Tests retrieving all items.
     */
    @Test
    @Transactional
    public void testGetAllItems () {
        // Create multiple items using itemService to ensure inventory
        // consistency
        itemService.addItem( new ItemDto( null, "Americano", "Black coffee with water", 2.0 ) );
        itemService.addItem( new ItemDto( null, "Cappuccino", "Coffee with steamed milk foam", 3.5 ) );

        // Retrieve all items through the service
        final List<ItemDto> items = itemService.getAllItems();

        // Verify the count and contents
        assertEquals( 2, items.size() );
        assertTrue( items.stream().anyMatch( i -> i.getName().equals( "Americano" ) ) );
        assertTrue( items.stream().anyMatch( i -> i.getName().equals( "Cappuccino" ) ) );
    }

    /**
     * Tests deleting an item by ID.
     */
    @Test
    @Transactional
    public void testDeleteItem () {
        // Create and save an item using itemService to ensure inventory
        // consistency
        final ItemDto itemDto = new ItemDto( null, "Macchiato", "Espresso with foam", 2.8 );
        final ItemDto savedItemDto = itemService.addItem( itemDto );

        // Delete the item and verify deletion
        itemService.deleteItem( savedItemDto.getId() );
        assertThrows( ResourceNotFoundException.class, () -> itemService.getItem( savedItemDto.getId() ) );
        assertNull( inventoryService.getInventory().getItems().get( savedItemDto.getName() ) );

    }

    /**
     * Tests updating a non-existent item.
     */
    @Test
    @Transactional
    public void testUpdateNonExistentItem () {
        // Attempt to update a non-existent item and expect a
        // ResourceNotFoundException
        final ItemDto nonExistentItemDto = new ItemDto( 999L, "NonExistent", "Non-existent item", 1.0 );
        assertThrows( ResourceNotFoundException.class, () -> itemService.updateItem( 999L, nonExistentItemDto ) );
    }
}

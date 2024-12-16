package edu.ncsu.csc326.wolfcafe.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;
import edu.ncsu.csc326.wolfcafe.service.InventoryService;
import jakarta.persistence.EntityManager;

/*
 * Tests InventoryService
 */
@SpringBootTest
public class InventoryServiceTest {

    /*
     * Connection to inventory service
     */
    @Autowired
    private InventoryService inventoryService;

    /*
     * Connection to item repository
     */
    @Autowired
    private ItemRepository itemRepository;

    /*
     * Connection to entity manager
     */
    @Autowired
    private EntityManager entityManager;

    /*
     * Represents a coffee item
     */
    private Item coffee;
    /*
     * Represents a milk item
     */
    private Item milk;
    /*
     * Represents a sugar item
     */
    private Item sugar;
    /*
     * Represents a chocolate item
     */
    private Item chocolate;

    /*
     * Cleans and adds the items to the tables to setup next tests
     */
    @BeforeEach
    public void setUp () {
        // Clean up existing records
        entityManager.createNativeQuery( "DELETE FROM inventory_items" ).executeUpdate();
        entityManager.createNativeQuery( "DELETE FROM items" ).executeUpdate();
        entityManager.createNativeQuery( "DELETE FROM inventory" ).executeUpdate();
        entityManager.createNativeQuery( "ALTER TABLE inventory AUTO_INCREMENT = 1" ).executeUpdate();

        // Create and save items
        coffee = itemRepository.save( new Item( null, "Coffee", "Freshly ground coffee", 3.0 ) );
        milk = itemRepository.save( new Item( null, "Milk", "Whole milk", 1.5 ) );
        sugar = itemRepository.save( new Item( null, "Sugar", "Refined white sugar", 0.5 ) );
        chocolate = itemRepository.save( new Item( null, "Chocolate", "Rich dark chocolate", 2.0 ) );
    }

    /*
     * Tests createInventory
     */
    @Test
    @Transactional
    public void testCreateInventory () {
        final Map<String, Integer> items = new HashMap<>();
        items.put( coffee.getName(), 5 );
        items.put( milk.getName(), 10 );
        items.put( sugar.getName(), 15 );
        items.put( chocolate.getName(), 20 );

        final InventoryDto inventoryDto = new InventoryDto();
        inventoryDto.setItems( items );

        final InventoryDto createdInventoryDto = inventoryService.createInventory( inventoryDto );

        assertAll( "InventoryDto contents",
                () -> assertEquals( 5, createdInventoryDto.getItems().get( coffee.getName() ) ),
                () -> assertEquals( 10, createdInventoryDto.getItems().get( milk.getName() ) ),
                () -> assertEquals( 15, createdInventoryDto.getItems().get( sugar.getName() ) ),
                () -> assertEquals( 20, createdInventoryDto.getItems().get( chocolate.getName() ) ) );
    }

    /*
     * Tests getInventory
     */
    @Test
    @Transactional
    public void testGetInventory () {
        final InventoryDto inventoryDto = inventoryService.getInventory();
        assertAll( "Inventory ID and initial contents", () -> assertEquals( 1L, inventoryDto.getId() ),
                () -> assertTrue( inventoryDto.getItems().isEmpty() ) );
    }

    /*
     * Tests updateInventory
     */
    @Test
    @Transactional
    public void testUpdateInventory () {
        // Create an initial inventory
        final Map<String, Integer> items = new HashMap<>();
        items.put( coffee.getName(), 10 );
        items.put( milk.getName(), 5 );
        final InventoryDto inventoryDto = new InventoryDto();
        inventoryDto.setItems( items );
        final InventoryDto createdInventoryDto = inventoryService.createInventory( inventoryDto );

        // Update the inventory
        final Map<String, Integer> updatedItems = createdInventoryDto.getItems();
        updatedItems.put( coffee.getName(), 20 );
        updatedItems.put( milk.getName(), 8 );
        updatedItems.put( sugar.getName(), 7 );
        createdInventoryDto.setItems( updatedItems );

        final InventoryDto updatedInventoryDto = inventoryService.updateInventory( createdInventoryDto );

        assertAll( "Updated InventoryDto contents",
                () -> assertEquals( 20, updatedInventoryDto.getItems().get( coffee.getName() ) ),
                () -> assertEquals( 8, updatedInventoryDto.getItems().get( milk.getName() ) ),
                () -> assertEquals( 7, updatedInventoryDto.getItems().get( sugar.getName() ) ) );
    }

    /*
     * Tests addItemToInventory
     */
    @Test
    @Transactional
    public void testAddItemToInventory () {
        // Add item to inventory
        final boolean itemAdded = inventoryService.addItemToInventory( coffee );
        final InventoryDto updatedInventory = inventoryService.getInventory();

        assertAll( "Add Item to Inventory", () -> assertTrue( itemAdded ),
                () -> assertEquals( 0, updatedInventory.getItems().get( coffee.getName() ) ) );
    }

    /*
     * Tests addExistingItemToInventory
     */
    @Test
    @Transactional
    public void testAddExistingItemToInventory () {
        inventoryService.addItemToInventory( coffee );
        assertThrows( IllegalStateException.class, () -> inventoryService.addItemToInventory( coffee ) );

    }

    /*
     * Tests updateItemInInventory
     */
    @Test
    @Transactional
    public void testUpdateItemInInventory () {
        // First add the item to ensure it exists in the inventory
        inventoryService.addItemToInventory( coffee );

        // Update the item details
        coffee.setDescription( "Premium Arabica coffee" );
        coffee.setPrice( 3.5 );
        inventoryService.updateItemInInventory( coffee );

        // Verify that the inventory reflects the updated item details
        final InventoryDto updatedInventory = inventoryService.getInventory();
        assertEquals( 0, updatedInventory.getItems().get( coffee.getName() ) );
    }

    /*
     * Error testing for updateItemInInventory. Tries to update a non existing
     * item
     */
    @Test
    @Transactional
    public void testUpdateNonExistentItemInInventory () {
        final Item nonExistentItem = new Item( null, "NonExistentItem", "This item is not in the inventory", 2.0 );
        assertThrows( ResourceNotFoundException.class,
                () -> inventoryService.updateItemInInventory( nonExistentItem ) );
    }
}

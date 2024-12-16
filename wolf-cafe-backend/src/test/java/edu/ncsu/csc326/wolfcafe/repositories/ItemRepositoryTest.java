package edu.ncsu.csc326.wolfcafe.repositories;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;
import jakarta.persistence.EntityManager;

/*
 * Tests the ItemRepository
 */
@DataJpaTest
@AutoConfigureTestDatabase ( replace = Replace.NONE )
public class ItemRepositoryTest {

    /** Connection to the item repository */
    @Autowired
    private ItemRepository    itemRepository;

    /** Entity manager to help with SQL table commands */
    @Autowired
    private TestEntityManager testEntityManager;

    /** Coffee item */
    private Item              coffee;
    /** Milk item */
    private Item              milk;
    /** Sugar item */
    private Item              sugar;

    /*
     * Clears the existing data in the tables for future tests
     */
    @BeforeEach
    public void setUp () {
        final EntityManager entityManager = testEntityManager.getEntityManager();

        // Clean up existing records
        entityManager.createNativeQuery( "DELETE FROM inventory_items" ).executeUpdate();
        entityManager.createNativeQuery( "DELETE FROM items" ).executeUpdate();
        entityManager.createNativeQuery( "ALTER TABLE items AUTO_INCREMENT = 1" ).executeUpdate();

        // Define and save items without specifying the id
        coffee = itemRepository.save( new Item( null, "Coffee", "Freshly ground coffee", 3.0 ) );
        milk = itemRepository.save( new Item( null, "Milk", "Whole milk", 1.5 ) );
        sugar = itemRepository.save( new Item( null, "Sugar", "Refined white sugar", 0.5 ) );
    }

    /**
     * Test saving and retrieving items from the repository.
     */
    @Test
    public void testSaveAndRetrieveItems () {
        final List<Item> items = itemRepository.findAll();

        assertAll( "Item Repository contents", () -> assertEquals( 3, items.size() ),
                () -> assertTrue( items.contains( coffee ) ), () -> assertTrue( items.contains( milk ) ),
                () -> assertTrue( items.contains( sugar ) ) );
    }

    /**
     * Test finding an item by name.
     */
    @Test
    public void testFindByName () {
        final Optional<Item> foundItem = itemRepository.findByName( "Coffee" );

        assertTrue( foundItem.isPresent(), "Coffee should be present in the repository" );
        assertEquals( coffee.getDescription(), foundItem.get().getDescription() );
    }

    /**
     * Test updating an item in the repository.
     */
    @Test
    public void testUpdateItem () {
        // Fetch item and update price
        coffee.setPrice( 3.5 );
        itemRepository.save( coffee );

        // Verify update
        final Optional<Item> updatedItem = itemRepository.findById( coffee.getId() );
        assertTrue( updatedItem.isPresent() );
        assertEquals( 3.5, updatedItem.get().getPrice(), "Price should be updated to 3.5" );
    }

    /**
     * Test deleting an item from the repository.
     */
    @Test
    public void testDeleteItem () {
        itemRepository.delete( sugar );
        final List<Item> itemsAfterDeletion = itemRepository.findAll();

        assertAll( "Item Deletion", () -> assertEquals( 2, itemsAfterDeletion.size() ),
                () -> assertTrue( itemsAfterDeletion.stream().noneMatch( i -> i.getName().equals( "Sugar" ) ) ) );
    }
}

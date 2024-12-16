package edu.ncsu.csc326.wolfcafe.repositories;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.repository.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;
import jakarta.persistence.EntityManager;

/**
 * Tests InventoryRepository. Uses the real database - not an embedded one.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class InventoryRepositoryTest {

	/** Reference to inventory repository */
	@Autowired
	private InventoryRepository inventoryRepository;

	@Autowired
	private ItemRepository itemRepository;

	/** Reference to EntityManager */
	@Autowired
	private TestEntityManager testEntityManager;

	/** Reference to inventory */
	private Inventory inventory;

	/** Coffee, Milk, Sugar, and Chocolate items */
	private Item coffee;
	private Item milk;
	private Item sugar;
	private Item chocolate;

	/**
	 * Sets up the test case. We assume only one inventory row.
	 */
	@BeforeEach
	public void setUp() {
		final EntityManager entityManager = testEntityManager.getEntityManager();

		// Clean up existing records
		entityManager.createNativeQuery("DELETE FROM inventory_items").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM items").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM inventory").executeUpdate();
		entityManager.createNativeQuery("ALTER TABLE inventory AUTO_INCREMENT = 1").executeUpdate();

		// Define and save items without specifying the id
		coffee = itemRepository.save(new Item(null, "Coffee", "Freshly ground coffee", 3.0));
		milk = itemRepository.save(new Item(null, "Milk", "Whole milk", 1.5));
		sugar = itemRepository.save(new Item(null, "Sugar", "Refined white sugar", 0.5));
		chocolate = itemRepository.save(new Item(null, "Chocolate", "Rich dark chocolate", 2.0));

		// Create a map to store items with quantities
		final Map<Item, Integer> items = new HashMap<>();
		items.put(coffee, 20);
		items.put(milk, 14);
		items.put(sugar, 32);
		items.put(chocolate, 10);

		// Create and save the inventory
		inventory = new Inventory();
		inventory.setItems(items);
		inventoryRepository.save(inventory);
	}

	/**
	 * Test saving the inventory and retrieving from the repository.
	 */
	@Test
	public void testSaveAndGetInventory() {
		final Inventory fetchedInventory = inventoryRepository.findById(inventory.getId()).get();
		final Map<Item, Integer> items = fetchedInventory.getItems();

		assertAll("Inventory contents", () -> assertEquals(inventory.getId(), fetchedInventory.getId()),
				() -> assertEquals(20, items.get(coffee)), () -> assertEquals(14, items.get(milk)),
				() -> assertEquals(32, items.get(sugar)), () -> assertEquals(10, items.get(chocolate)));
	}

	/**
	 * Tests updating the inventory
	 */
	@Test
	public void testUpdateInventory() {
		// Fetch the inventory using the repository
		final Inventory fetchedInventory = inventoryRepository.findById(inventory.getId()).get();
		final Map<Item, Integer> items = fetchedInventory.getItems();

		// Update the quantities for the existing items
		items.put(coffee, 13); // Update coffee to 13 units
		items.put(milk, 14); // Milk remains 14 units
		items.put(sugar, 27); // Update sugar to 27 units
		items.put(chocolate, 23); // Update chocolate to 23 units

		// Make sure items are not null before saving
		assertNotNull(fetchedInventory.getItems());

		// Save the updated inventory
		fetchedInventory.setItems(items);
		inventoryRepository.save(fetchedInventory);

		// Fetch the updated inventory and check values
		final Inventory updatedInventory = inventoryRepository.findById(inventory.getId()).get();
		assertNotNull(updatedInventory.getItems());
		final Map<Item, Integer> updatedItems = updatedInventory.getItems();

		// Assert values
		assertAll("Updated Inventory contents", () -> assertEquals(13, updatedItems.get(coffee)),
				() -> assertEquals(14, updatedItems.get(milk)), () -> assertEquals(27, updatedItems.get(sugar)),
				() -> assertEquals(23, updatedItems.get(chocolate)));
	}
}

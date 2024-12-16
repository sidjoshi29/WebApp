package edu.ncsu.csc326.wolfcafe.service.impl;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.mapper.InventoryMapper;
import edu.ncsu.csc326.wolfcafe.repository.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;
import edu.ncsu.csc326.wolfcafe.service.InventoryService;

/**
 * Implementation of the InventoryService interface.
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    /** Connection to the repository to work with the DAO + database */
    @Autowired
    private InventoryRepository inventoryRepository;

    /** Connection to the item repository */
    @Autowired
    private ItemRepository      itemRepository;

    /**
     * Creates the single inventory if it doesn't already exist.
     *
     * @param inventoryDto
     *            inventory to create
     * @return updated inventory after creation
     */
    @Override
    public InventoryDto createInventory ( final InventoryDto inventoryDto ) {
        final List<Inventory> inventories = inventoryRepository.findAll();
        if ( !inventories.isEmpty() ) {
            throw new IllegalStateException( "Cannot create inventory: An inventory already exists." );
        }

        final Inventory inventory = InventoryMapper.mapToInventory( inventoryDto, itemRepository );
        final Inventory savedInventory = inventoryRepository.save( inventory );
        return InventoryMapper.mapToInventoryDto( savedInventory );
    }

    /**
     * Returns the single inventory. If none exists, creates a new empty
     * inventory.
     *
     * @return the single inventory
     */
    @Override
    public InventoryDto getInventory () {
        final List<Inventory> inventories = inventoryRepository.findAll();
        if ( inventories.isEmpty() ) {
            final InventoryDto newInventoryDto = new InventoryDto( 1L, new HashMap<>() );
            return createInventory( newInventoryDto );
        }

        return InventoryMapper.mapToInventoryDto( inventories.get( 0 ) );
    }

    /**
     * Updates the contents of the single inventory. If the inventory does not
     * exist, throws an error.
     *
     * @param inventoryDto
     *            values to update
     * @return updated inventory
     */
    @Override
    public InventoryDto updateInventory ( final InventoryDto inventoryDto ) {
        final Inventory inventory = inventoryRepository.findById( inventoryDto.getId() ).orElseThrow(
                () -> new ResourceNotFoundException( "Inventory not found with id: " + inventoryDto.getId() ) );

        final Inventory updatedInventory = InventoryMapper.mapToInventory( inventoryDto, itemRepository );
        updatedInventory.setId( inventory.getId() );

        final Inventory savedInventory = inventoryRepository.save( updatedInventory );
        return InventoryMapper.mapToInventoryDto( savedInventory );
    }

    /**
     * Add the given item to the inventory of the WolfCafe system
     *
     * @param item
     *            the item to be added to the inventory
     * 
     * @return whether or not the addition of the item was successful
     */
    @Override
    public boolean addItemToInventory ( final Item item ) {
        // Retrieve or create the inventory if it doesn’t exist
        final InventoryDto inventoryDto = getInventory();
        final Inventory inventory = inventoryRepository.findById( inventoryDto.getId() )
                .orElseThrow( () -> new IllegalStateException( "Cannot add item: No inventory found." ) );

        // Check if the item already exists in the inventory
        if ( inventory.getItems().containsKey( item ) ) {
            throw new IllegalStateException( "Item with ID " + item.getId() + " already exists in the inventory." );
        }

        // Add item with quantity 0 and save the inventory
        inventory.getItems().put( item, 0 );
        inventoryRepository.save( inventory );

        return true; // Item successfully added to the inventory
    }

    /**
     * Updates an existing item in the inventory if present.
     *
     * @param updatedItem
     *            the item with updated details
     */
    @Override
    public void updateItemInInventory ( final Item updatedItem ) {
        final InventoryDto inventoryDto = getInventory();
        final Inventory inventory = inventoryRepository.findById( inventoryDto.getId() )
                .orElseThrow( () -> new ResourceNotFoundException( "Cannot update item: No inventory found." ) );

        if ( !inventory.getItems().containsKey( updatedItem ) ) {
            throw new ResourceNotFoundException(
                    "Cannot update item: Item with ID " + updatedItem.getId() + " not found in the inventory." );
        }

        final Integer currentQuantity = inventory.getItems().get( updatedItem );
        inventory.getItems().remove( updatedItem );
        inventory.getItems().put( updatedItem, currentQuantity );
        inventoryRepository.save( inventory );
    }

}

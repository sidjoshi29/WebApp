package edu.ncsu.csc326.wolfcafe.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;
import edu.ncsu.csc326.wolfcafe.service.InventoryService;
import edu.ncsu.csc326.wolfcafe.service.ItemService;

/**
 * Implementation of the ItemService interface.
 */
@Service
public class ItemServiceImpl implements ItemService {

    /** Connection to the item repository */
    @Autowired
    private ItemRepository   itemRepository;

    /** The item service to carry out the requests */
    @Autowired
    private InventoryService inventoryService;

    /** This is used to help map to item dtos */
    @Autowired
    private ModelMapper      modelMapper;

    /**
     * Adds given item and initializes it in the inventory with quantity 0.
     *
     * @param itemDto
     *            item to add
     * @return added item
     */
    @Override
    public ItemDto addItem ( final ItemDto itemDto ) {
        // Map the DTO to the entity
        final Item item = modelMapper.map( itemDto, Item.class );

        if ( item.getPrice() <= 0 ) {
            throw new WolfCafeAPIException( HttpStatus.BAD_REQUEST, "Price must be non-zero and positive." );
        }

        try {
            // Save the item to the repository
            final Item savedItem = itemRepository.save( item );

            // Add the item to the inventory
            inventoryService.addItemToInventory( savedItem );

            // Return the saved item as a DTO
            return modelMapper.map( savedItem, ItemDto.class );

        }
        catch ( final DataIntegrityViolationException e ) {
            // Handle unique constraint violations (e.g., duplicate name)
            throw new WolfCafeAPIException( HttpStatus.CONFLICT,
                    "Invalid fields for an Item. Be sure that item fields are non-null and not a duplicate." );
        }
        catch ( final Exception e ) {
            // Handle other unexpected exceptions
            throw new WolfCafeAPIException( HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred while adding the item." );
        }
    }

    /**
     * Gets item by id
     *
     * @param id
     *            id of item to get
     * @return returned item
     */
    @Override
    public ItemDto getItem ( final Long id ) {
        final Item item = itemRepository.findById( id )
                .orElseThrow( () -> new ResourceNotFoundException( "Item with ID " + id + "not found." ) );
        return modelMapper.map( item, ItemDto.class );
    }

    /**
     * Returns all items
     *
     * @return all items
     */
    @Override
    public List<ItemDto> getAllItems () {
        final List<Item> items = itemRepository.findAll();
        return items.stream().map( ( item ) -> modelMapper.map( item, ItemDto.class ) ).collect( Collectors.toList() );
    }

    /**
     * Updates the item with the given id
     *
     * @param id
     *            id of item to update
     * @param itemDto
     *            information of item to update
     * @return updated item
     */
    @Override
    public ItemDto updateItem ( final Long id, final ItemDto itemDto ) {
        // Retrieve and update the item
        final Item item = itemRepository.findById( id )
                .orElseThrow( () -> new ResourceNotFoundException( "Item with ID " + id + " not found." ) );
        item.setName( itemDto.getName() );
        item.setDescription( itemDto.getDescription() );
        item.setPrice( itemDto.getPrice() );

        try {
            final Item updatedItem = itemRepository.save( item );

            // Sync changes with inventory
            inventoryService.updateItemInInventory( updatedItem );

            return modelMapper.map( updatedItem, ItemDto.class );
        }
        catch ( final ResourceNotFoundException e ) {
            throw new WolfCafeAPIException( HttpStatus.CONFLICT,
                    "Failed to update item: Inventory synchronization failed for item with ID " + id + "." );
        }
        catch ( final Exception e ) {
            throw new WolfCafeAPIException( HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred while updating item with ID " + id + "." );
        }
    }

    /**
     * Deletes the item with the given id
     *
     * @param id
     *            id of item to delete
     */
    @Override
    public void deleteItem ( final Long id ) {
        // First, find the item. This should throw a ResourceNotFoundException
        // if the
        // item is not found.
        final Item item = itemRepository.findById( id )
                .orElseThrow( () -> new ResourceNotFoundException( "Item with ID " + id + " not found." ) );

        // Now, attempt to update the inventory and delete the item
        try {
            final InventoryDto updated = inventoryService.getInventory();

            if ( updated.getItems().remove( item.getName() ) == null ) {
                // If the item does not exist in the inventory, throw a conflict
                // exception
                throw new WolfCafeAPIException( HttpStatus.CONFLICT,
                        "Item with name '" + item.getName() + "' does not exist in the inventory." );
            }

            // Update inventory after removing the item
            inventoryService.updateInventory( updated );

            // Delete the item from the repository
            itemRepository.delete( item );

        }
        catch ( final WolfCafeAPIException e ) {
            // Re-throw WolfCafeAPIException with the same message to maintain
            // clarity
            throw e;
        }
        catch ( final Exception e ) {
            // Handle unexpected exceptions
            throw new WolfCafeAPIException( HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred while deleting item with ID " + id + ": " + e.getMessage() );
        }
    }

}

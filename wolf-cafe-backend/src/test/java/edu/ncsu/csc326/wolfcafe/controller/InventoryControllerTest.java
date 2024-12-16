package edu.ncsu.csc326.wolfcafe.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.TestUtils;
import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;
import edu.ncsu.csc326.wolfcafe.service.InventoryService;
import jakarta.persistence.EntityManager;

/**
 * Tests the Inventory Controller Class
 */
@SpringBootTest
@AutoConfigureMockMvc
public class InventoryControllerTest {

    /** MockMvc is used to simulate HTTP requests */
    @Autowired
    private MockMvc             mvc;

    /** The entityManager to deal with SQL tables */
    @Autowired
    private EntityManager       entityManager;

    /** The connection to the item repository */
    @Autowired
    private ItemRepository      itemRepository;

    /** The connection to the inventory repository */
    @MockBean
    private InventoryService    inventoryService;

    /** The api endpoint */
    private static final String API_PATH = "/api/inventory";
    /** The encoding type */
    private static final String ENCODING = "utf-8";

    /**
     * Clears the SQL tables and resets them for the next test
     */
    @BeforeEach
    public void setUp () throws Exception {
        // Clear associations and reset inventory tables
        entityManager.createNativeQuery( "DELETE FROM inventory_items" ).executeUpdate();
        entityManager.createNativeQuery( "DELETE FROM items" ).executeUpdate();
        entityManager.createNativeQuery( "DELETE FROM inventory" ).executeUpdate();

        // Reset auto-increment for inventory table
        entityManager.createNativeQuery( "ALTER TABLE inventory AUTO_INCREMENT = 1" ).executeUpdate();

        // Save items to the database
        saveItems();
    }

    /*
     * Saves Items to be used in the tests
     */
    private void saveItems () {
        itemRepository.save( new Item( null, "coffee", "Freshly ground coffee", 3.0 ) );
        itemRepository.save( new Item( null, "milk", "Whole milk", 1.5 ) );
        itemRepository.save( new Item( null, "sugar", "Refined white sugar", 0.5 ) );
        itemRepository.save( new Item( null, "chocolate", "Rich dark chocolate", 2.0 ) );
    }

    /*
     * Tests getInventory
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    @Transactional
    public void testGetInventory () throws Exception {
        // Prepare a mock inventory response
        final Map<String, Integer> items = new HashMap<>();
        items.put( "coffee", 10 );
        items.put( "milk", 5 );
        items.put( "sugar", 15 );
        final InventoryDto mockInventory = new InventoryDto( 1L, items );

        // Stub the getInventory method in the mock service
        Mockito.when( inventoryService.getInventory() ).thenReturn( mockInventory );

        // Perform GET request and verify the response
        mvc.perform( get( API_PATH ).contentType( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.id" ).value( 1 ) ).andExpect( jsonPath( "$.items.coffee" ).value( 10 ) )
                .andExpect( jsonPath( "$.items.milk" ).value( 5 ) )
                .andExpect( jsonPath( "$.items.sugar" ).value( 15 ) );
    }

    /*
     * Tests updateInventory
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    @Transactional
    public void testUpdateInventory () throws Exception {
        // Prepare updated items
        final Map<String, Integer> updatedItems = new HashMap<>();
        updatedItems.put( "coffee", 5 );
        updatedItems.put( "milk", 10 );
        updatedItems.put( "sugar", 15 );
        updatedItems.put( "chocolate", 20 );

        final InventoryDto updatedInventory = new InventoryDto( 1L, updatedItems );

        // Stub the updateInventory method in the mock service
        Mockito.when( inventoryService.updateInventory( Mockito.any( InventoryDto.class ) ) )
                .thenReturn( updatedInventory );

        // Perform PUT request to update the inventory
        mvc.perform( put( API_PATH ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( TestUtils.asJsonString( updatedInventory ) ).accept( MediaType.APPLICATION_JSON ) )
                .andDo( print() ).andExpect( status().isOk() ).andExpect( jsonPath( "$.id" ).value( 1 ) )
                .andExpect( jsonPath( "$.items.coffee" ).value( 5 ) )
                .andExpect( jsonPath( "$.items.milk" ).value( 10 ) )
                .andExpect( jsonPath( "$.items.sugar" ).value( 15 ) )
                .andExpect( jsonPath( "$.items.chocolate" ).value( 20 ) );
    }

    /*
     * Error testing with getInventory. Tries to get the inventory but the user
     * is unauthorized
     */
    @Test
    @Transactional
    public void testGetInventoryUnauthorized () throws Exception {
        // Try to get inventory without authentication
        mvc.perform( get( API_PATH ) ).andDo( print() ).andExpect( status().isUnauthorized() );
    }

    /*
     * Error testing with updateInventory. Tries to update the inventory but the
     * user is unauthorized
     */
    @Test
    @Transactional
    public void testUpdateInventoryUnauthorized () throws Exception {
        // Prepare updated items for the PUT request
        final Map<String, Integer> updatedItems = new HashMap<>();
        updatedItems.put( "coffee", 5 );
        updatedItems.put( "milk", 10 );
        updatedItems.put( "sugar", 15 );
        updatedItems.put( "chocolate", 20 );

        final InventoryDto updatedInventory = new InventoryDto( 1L, updatedItems );

        // Try to update inventory without authentication
        mvc.perform( put( API_PATH ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( TestUtils.asJsonString( updatedInventory ) ).accept( MediaType.APPLICATION_JSON ) )
                .andDo( print() ).andExpect( status().isUnauthorized() );
    }

    /*
     * Error testing with updateInventory. Tries to update the inventory but the
     * inventory is not present
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    @Transactional
    public void testUpdateInventoryNotFound () throws Exception {
        // Prepare an inventory update request
        final Map<String, Integer> itemsToUpdate = new HashMap<>();
        itemsToUpdate.put( "coffee", 5 );
        final InventoryDto inventoryUpdateDto = new InventoryDto( 1L, itemsToUpdate );

        // Configure the mock to throw ResourceNotFoundException
        Mockito.when( inventoryService.updateInventory( Mockito.any( InventoryDto.class ) ) )
                .thenThrow( new ResourceNotFoundException( "Inventory not found" ) );

        // Perform PUT request and expect 404 Not Found
        mvc.perform( put( API_PATH ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( TestUtils.asJsonString( inventoryUpdateDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andDo( print() ).andExpect( status().isNotFound() )
                .andExpect( jsonPath( "$.message" ).value( "Inventory not found" ) ) // Validate
                                                                                     // the
                                                                                     // error
                                                                                     // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/inventory" ) ) // Validate
                                                                                    // the
                                                                                    // request
                                                                                    // URI
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

    /**
     *
     * Test case for handling a generic exception during the retrieval of
     * inventory. This ensures the application returns a proper 500 Internal
     * Server Error response.
     *
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    @Transactional
    public void testGetInventoryGenericException () throws Exception {
        // Simulate a generic exception during getInventory
        Mockito.doThrow( new RuntimeException( "Unexpected error" ) ).when( inventoryService ).getInventory();

        // Perform GET request and expect 500 Internal Server Error
        mvc.perform( get( API_PATH ).contentType( MediaType.APPLICATION_JSON ) ).andDo( print() )
                .andExpect( status().isInternalServerError() )
                .andExpect( jsonPath( "$.message" ).value( "An unexpected error occurred" ) ) // Validate
                                                                                              // generic
                                                                                              // error
                                                                                              // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/inventory" ) ) // Validate
                                                                                    // the
                                                                                    // request
                                                                                    // URI
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

    /**
     * Test case for handling a generic exception during the update of
     * inventory. This ensures the application returns a proper 500 Internal
     * Server Error response.
     *
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    @Transactional
    public void testUpdateInventoryGenericException () throws Exception {
        // Prepare updated items for the PUT request
        final Map<String, Integer> updatedItems = new HashMap<>();
        updatedItems.put( "coffee", 5 );
        updatedItems.put( "milk", 10 );
        updatedItems.put( "sugar", 15 );
        updatedItems.put( "chocolate", 20 );

        final InventoryDto updatedInventory = new InventoryDto( 1L, updatedItems );

        // Simulate a generic exception during updateInventory
        Mockito.doThrow( new RuntimeException( "Unexpected error" ) ).when( inventoryService )
                .updateInventory( Mockito.any( InventoryDto.class ) );

        // Perform PUT request and expect 500 Internal Server Error
        mvc.perform( put( API_PATH ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( TestUtils.asJsonString( updatedInventory ) ).accept( MediaType.APPLICATION_JSON ) )
                .andDo( print() ).andExpect( status().isInternalServerError() )
                .andExpect( jsonPath( "$.message" ).value( "An unexpected error occurred" ) ) // Validate
                                                                                              // generic
                                                                                              // error
                                                                                              // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/inventory" ) ) // Validate
                                                                                    // the
                                                                                    // request
                                                                                    // URI
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

}

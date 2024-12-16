package edu.ncsu.csc326.wolfcafe.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.service.ItemService;

/*
 * Tests the Item Controller Class
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ItemControllerTest {

    /** MockMvc is used to simulate HTTP requests */
    @Autowired
    private MockMvc                   mvc;

    /** Connection to the item service */
    @MockBean
    private ItemService               itemService;

    /** Mapper to help in testing */
    private static final ObjectMapper mapper           = new ObjectMapper();

    /** API endpoint */
    private static final String       API_PATH         = "/api/items";
    /** Encoding type */
    private static final String       ENCODING         = "utf-8";
    /** Item name for coffee item */
    private static final String       ITEM_NAME        = "Coffee";
    /** Description for coffee item */
    private static final String       ITEM_DESCRIPTION = "Coffee is life";
    /** Price for coffee price */
    private static final double       ITEM_PRICE       = 3.25;

    /*
     * Tests createItem
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testCreateItem () throws Exception {
        // Create ItemDto with all contents but the id
        final ItemDto itemDto = new ItemDto();
        itemDto.setName( ITEM_NAME );
        itemDto.setDescription( ITEM_DESCRIPTION );
        itemDto.setPrice( ITEM_PRICE );

        Mockito.when( itemService.addItem( ArgumentMatchers.any() ) ).thenReturn( itemDto );

        final String json = mapper.writeValueAsString( itemDto );

        // Set id for the response
        itemDto.setId( 57L );

        mvc.perform( post( API_PATH ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isCreated() )
                .andExpect( jsonPath( "$.id", Matchers.equalTo( 57 ) ) )
                .andExpect( jsonPath( "$.name", Matchers.equalTo( ITEM_NAME ) ) )
                .andExpect( jsonPath( "$.description", Matchers.equalTo( ITEM_DESCRIPTION ) ) )
                .andExpect( jsonPath( "$.price", Matchers.equalTo( ITEM_PRICE ) ) );
    }

    /**
     * Error testing with createItem when you are not the admin
     *
     */
    @Test
    public void testCreateItemNotAdmin () throws Exception {
        // Create ItemDto with all contents but the id
        final ItemDto itemDto = new ItemDto();
        itemDto.setName( ITEM_NAME );
        itemDto.setDescription( ITEM_DESCRIPTION );
        itemDto.setPrice( ITEM_PRICE );

        Mockito.when( itemService.addItem( ArgumentMatchers.any() ) ).thenReturn( itemDto );

        final String json = mapper.writeValueAsString( itemDto );

        // Set id for the response
        itemDto.setId( 57L );

        mvc.perform( post( API_PATH ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isUnauthorized() );
    }

    /*
     * Tests the getItemById method
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testGetItemById () throws Exception {
        final ItemDto itemDto = new ItemDto();
        itemDto.setId( 27L );
        itemDto.setName( ITEM_NAME );
        itemDto.setDescription( ITEM_DESCRIPTION );
        itemDto.setPrice( ITEM_PRICE );

        Mockito.when( itemService.getItem( ArgumentMatchers.any() ) ).thenReturn( itemDto );
        final String json = "";

        mvc.perform( get( API_PATH + "/27" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.id", Matchers.equalTo( 27 ) ) )
                .andExpect( jsonPath( "$.name", Matchers.equalTo( ITEM_NAME ) ) )
                .andExpect( jsonPath( "$.description", Matchers.equalTo( ITEM_DESCRIPTION ) ) )
                .andExpect( jsonPath( "$.price", Matchers.equalTo( ITEM_PRICE ) ) );
    }

    /*
     * Tests the getAllItemsAsCustomer method
     */
    @Test
    @WithMockUser ( username = "staff", roles = { "STAFF", "CUSTOMER" } )
    public void testGetAllItemsAsCustomer () throws Exception {
        // Create sample item list
        final ItemDto item1 = new ItemDto( 1L, "Espresso", "Strong coffee", 2.5 );
        final ItemDto item2 = new ItemDto( 2L, "Latte", "Coffee with milk", 3.0 );
        final List<ItemDto> items = Arrays.asList( item1, item2 );

        Mockito.when( itemService.getAllItems() ).thenReturn( items );

        // Perform GET request as a customer
        mvc.perform( get( API_PATH ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.size()", Matchers.equalTo( 2 ) ) )
                .andExpect( jsonPath( "$[0].name", Matchers.equalTo( "Espresso" ) ) )
                .andExpect( jsonPath( "$[1].name", Matchers.equalTo( "Latte" ) ) );
    }

    /*
     * Tests the updateItem method
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testUpdateItem () throws Exception {
        // Create ItemDto for updating
        final ItemDto itemDto = new ItemDto();
        itemDto.setId( 57L );
        itemDto.setName( "Mocha" );
        itemDto.setDescription( "Chocolate coffee" );
        itemDto.setPrice( 3.5 );

        // Mock the service call to return the updated item
        Mockito.when( itemService.updateItem( ArgumentMatchers.eq( 57L ), ArgumentMatchers.any( ItemDto.class ) ) )
                .thenReturn( itemDto );

        final String json = mapper.writeValueAsString( itemDto );

        // Perform PUT request to update the item
        mvc.perform( put( API_PATH + "/57" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.id", Matchers.equalTo( 57 ) ) )
                .andExpect( jsonPath( "$.name", Matchers.equalTo( "Mocha" ) ) )
                .andExpect( jsonPath( "$.description", Matchers.equalTo( "Chocolate coffee" ) ) )
                .andExpect( jsonPath( "$.price", Matchers.equalTo( 3.5 ) ) );
    }

    /*
     * Tests the deleteItem method
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testDeleteItem () throws Exception {
        // Perform DELETE request to delete the item
        mvc.perform( delete( API_PATH + "/1" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$", Matchers.equalTo( "Item deleted successfully." ) ) );
    }

    /*
     * Error testing for deleting an item as a customer
     */
    @Test
    @WithMockUser ( username = "customer", roles = "CUSTOMER" )
    public void testDeleteItemForbiddenForCustomer () throws Exception {
        // Try to delete an item as a CUSTOMER, expecting FORBIDDEN
        mvc.perform( delete( API_PATH + "/1" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING ) )
                .andExpect( status().isForbidden() );
    }

    /*
     * Tests adding an item with invalid data
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testAddItemWolfCafeAPIException () throws Exception {
        // Simulate WolfCafeAPIException
        Mockito.when( itemService.addItem( ArgumentMatchers.any() ) )
                .thenThrow( new WolfCafeAPIException( HttpStatus.BAD_REQUEST, "Invalid item data" ) );

        final ItemDto itemDto = new ItemDto();
        itemDto.setName( ITEM_NAME );
        itemDto.setDescription( ITEM_DESCRIPTION );
        itemDto.setPrice( ITEM_PRICE );

        final String json = mapper.writeValueAsString( itemDto );

        mvc.perform( post( API_PATH ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ) ) // Include
                                   // JSON
                                   // content
                                   // in
                                   // the
                                   // POST
                                   // request
                .andExpect( status().isBadRequest() ) // Validate the HTTP
                                                      // status is 400 Bad
                                                      // Request
                .andExpect( jsonPath( "$.message" ).value( "Invalid item data" ) ) // Validate
                                                                                   // the
                                                                                   // error
                                                                                   // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/items" ) ) // Validate
                                                                                // the
                                                                                // URI
                                                                                // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure the
                                                                  // timestamp
                                                                  // field
                                                                  // exists
    }

    /*
     * Test case for handling a generic exception when adding an item.
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testAddItemGenericException () throws Exception {
        // Simulate a generic exception
        Mockito.when( itemService.addItem( ArgumentMatchers.any() ) )
                .thenThrow( new RuntimeException( "Unexpected error" ) );

        final ItemDto itemDto = new ItemDto();
        itemDto.setName( ITEM_NAME );
        itemDto.setDescription( ITEM_DESCRIPTION );
        itemDto.setPrice( ITEM_PRICE );

        final String json = mapper.writeValueAsString( itemDto );

        mvc.perform( post( API_PATH ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ) ).andExpect( status().isInternalServerError() )
                .andExpect( jsonPath( "$.message" ).value( "An unexpected error occurred" ) ) // Match
                                                                                              // the
                                                                                              // error
                                                                                              // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/items" ) ) // Match
                                                                                // the
                                                                                // URI
                                                                                // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure the
                                                                  // timestamp
                                                                  // exists
    }

    /*
     * Test case for handling a ResourceNotFoundException when retrieving an
     * item.
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testGetItemResourceNotFound () throws Exception {
        // Simulate ResourceNotFoundException
        Mockito.when( itemService.getItem( ArgumentMatchers.eq( 99L ) ) )
                .thenThrow( new ResourceNotFoundException( "Item not found" ) );

        mvc.perform( get( API_PATH + "/99" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING ) )
                .andExpect( status().isNotFound() ).andExpect( jsonPath( "$.message" ).value( "Item not found" ) )
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/items/99" ) )
                .andExpect( jsonPath( "$.timeStamp" ).exists() );
    }

    /*
     * Test case for handling a ResourceNotFoundException when updating an item.
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testUpdateItemResourceNotFound () throws Exception {
        // Simulate ResourceNotFoundException
        Mockito.when( itemService.updateItem( ArgumentMatchers.eq( 99L ), ArgumentMatchers.any( ItemDto.class ) ) )
                .thenThrow( new ResourceNotFoundException( "Item not found" ) );

        final ItemDto itemDto = new ItemDto();
        itemDto.setName( ITEM_NAME );
        itemDto.setDescription( ITEM_DESCRIPTION );
        itemDto.setPrice( ITEM_PRICE );

        final String json = mapper.writeValueAsString( itemDto );

        mvc.perform( put( API_PATH + "/99" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ) ).andExpect( status().isNotFound() )
                .andExpect( jsonPath( "$.message" ).value( "Item not found" ) ) // Match
                                                                                // the
                                                                                // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/items/99" ) ) // Match
                                                                                   // the
                                                                                   // URI
                                                                                   // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure a
                                                                  // timestamp
                                                                  // exists
    }

    /*
     * Test case for handling a WolfCafeAPIException when updating an item.
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testUpdateItemWolfCafeAPIException () throws Exception {
        // Simulate WolfCafeAPIException
        Mockito.when( itemService.updateItem( ArgumentMatchers.eq( 57L ), ArgumentMatchers.any( ItemDto.class ) ) )
                .thenThrow( new WolfCafeAPIException( HttpStatus.CONFLICT, "Item update conflict" ) );

        final ItemDto itemDto = new ItemDto();
        itemDto.setName( "Updated Name" );
        itemDto.setDescription( "Updated Description" );
        itemDto.setPrice( 4.5 );

        final String json = mapper.writeValueAsString( itemDto );

        mvc.perform( put( API_PATH + "/57" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ) ) // Add
                                   // content
                                   // to
                                   // the
                                   // request
                .andExpect( status().isConflict() ) // Validate the HTTP status
                                                    // is 409 Conflict
                .andExpect( jsonPath( "$.message" ).value( "Item update conflict" ) ) // Validate
                                                                                      // the
                                                                                      // error
                                                                                      // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/items/57" ) ) // Validate
                                                                                   // the
                                                                                   // URI
                                                                                   // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure the
                                                                  // timestamp
                                                                  // field
                                                                  // exists
    }

    /*
     * Test case for handling a WolfCafeAPIException when updating an item.
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testUpdateItemGenericException () throws Exception {
        // Simulate a generic exception
        Mockito.when( itemService.updateItem( ArgumentMatchers.eq( 57L ), ArgumentMatchers.any( ItemDto.class ) ) )
                .thenThrow( new RuntimeException( "Unexpected error" ) );

        final ItemDto itemDto = new ItemDto();
        itemDto.setName( "Updated Name" );
        itemDto.setDescription( "Updated Description" );
        itemDto.setPrice( 4.5 );

        final String json = mapper.writeValueAsString( itemDto );

        mvc.perform( put( API_PATH + "/57" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ) ).andExpect( status().isInternalServerError() )
                .andExpect( jsonPath( "$.message" ).value( "An unexpected error occurred" ) ) // Validate
                                                                                              // the
                                                                                              // error
                                                                                              // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/items/57" ) ) // Validate
                                                                                   // the
                                                                                   // URI
                                                                                   // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure the
                                                                  // timestamp
                                                                  // field
                                                                  // exists
    }

    /*
     * Test case for handling a ResourceNotFoundException when deleting an item.
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testDeleteItemResourceNotFound () throws Exception {
        // Simulate ResourceNotFoundException for a void method
        Mockito.doThrow( new ResourceNotFoundException( "Item not found" ) ).when( itemService )
                .deleteItem( ArgumentMatchers.eq( 99L ) );

        mvc.perform(
                delete( API_PATH + "/99" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING ) )
                .andExpect( status().isNotFound() ).andExpect( jsonPath( "$.message" ).value( "Item not found" ) ) // Validate
                                                                                                                   // the
                                                                                                                   // error
                                                                                                                   // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/items/99" ) ) // Validate
                                                                                   // the
                                                                                   // URI
                                                                                   // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure the
                                                                  // timestamp
                                                                  // field
                                                                  // exists
    }

    /*
     * Test case for handling a generic exception when deleting an item.
     */
    @Test
    @WithMockUser ( username = "staff", roles = "STAFF" )
    public void testDeleteItemGenericException () throws Exception {
        // Simulate a generic exception for a void method
        Mockito.doThrow( new RuntimeException( "Unexpected error" ) ).when( itemService )
                .deleteItem( ArgumentMatchers.eq( 99L ) );

        mvc.perform(
                delete( API_PATH + "/99" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING ) )
                .andExpect( status().isInternalServerError() )
                .andExpect( jsonPath( "$.message" ).value( "An unexpected error occurred" ) ) // Validate
                                                                                              // the
                                                                                              // error
                                                                                              // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/items/99" ) ) // Validate
                                                                                   // the
                                                                                   // URI
                                                                                   // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure the
                                                                  // timestamp
                                                                  // field
                                                                  // exists
    }

}

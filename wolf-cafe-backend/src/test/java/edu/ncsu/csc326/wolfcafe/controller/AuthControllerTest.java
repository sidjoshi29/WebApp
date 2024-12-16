package edu.ncsu.csc326.wolfcafe.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ncsu.csc326.wolfcafe.TestUtils;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.repository.RoleRepository;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;

/**
 * Tests the Auth Controller Class
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    /** The password for he admin */
    @Value ( "${app.admin-user-password}" )
    private String         adminUserPassword;

    /** MockMvc is used to simulate HTTP requests */
    @Autowired
    private MockMvc        mvc;

    /** The connection to the user repository */
    @Autowired
    private UserRepository userRepository;

    /** The connection to the role repository */
    @Autowired
    private RoleRepository roleRepository;

    /**
     * Tests the admin login
     */
    @Test
    @Transactional
    public void testLoginAdmin () throws Exception {
        final LoginDto loginDto = new LoginDto( "admin", adminUserPassword );

        mvc.perform( post( "/api/auth/login" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( loginDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.tokenType" ).value( "Bearer" ) )
                .andExpect( jsonPath( "$.role" ).value( "ROLE_ADMIN" ) );
    }

    /**
     * Test registering a new customer and then logging in with the registered
     * credentials.
     */
    @Test
    @Transactional
    public void testCreateCustomerAndLogin () throws Exception {
        final RegisterDto registerDto = new RegisterDto( 1L, "Jordan Estes", "jestes", "vitae.erat@yahoo.edu",
                "JXB16TBD4LC" );

        mvc.perform( post( "/api/auth/register" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( registerDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isCreated() ).andExpect( content().string( "User registered successfully." ) );

        final LoginDto loginDto = new LoginDto( "jestes", "JXB16TBD4LC" );

        mvc.perform( post( "/api/auth/login" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( loginDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.tokenType" ).value( "Bearer" ) )
                .andExpect( jsonPath( "$.role" ).value( "ROLE_CUSTOMER" ) );
    }

    /**
     * Gets the token from the admin login to be used in other ADMIN only
     * requests
     *
     * @return the admin token
     */
    private String getAdminToken () throws Exception {
        final LoginDto loginDto = new LoginDto( "admin", adminUserPassword );

        final MvcResult result = mvc.perform( post( "/api/auth/login" ).contentType( MediaType.APPLICATION_JSON )
                .content( new ObjectMapper().writeValueAsString( loginDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andReturn();

        final String responseContent = result.getResponse().getContentAsString();

        final JsonNode jsonNode = new ObjectMapper().readTree( responseContent );

        return jsonNode.get( "accessToken" ).asText();
    }

    /**
     * Tests creating a staff user with the admin token. Requires the ADMIN role
     */
    @Test
    @Transactional
    public void testCreateStaff () throws Exception {
        final String token = getAdminToken();

        final RegisterDto registerDto = new RegisterDto( 1L, "Staff Name", "staffuser", "staffuser@example.com",
                "staffpassword" );

        mvc.perform( post( "/api/auth/user" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( registerDto ) ).header( "Authorization", "Bearer " + token )
                .accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( content().string( "Staff added successfully." ) );
    }

    /**
     * Tests deleting a user from the system. Requires the ADMIN role
     */
    @Test
    @Transactional
    public void testDeleteUser () throws Exception {
        final String token = getAdminToken();

        final RegisterDto registerDto = new RegisterDto( "Staff Name", "staffuser", "staffuser@example.com",
                "staffpassword" );

        mvc.perform( post( "/api/auth/user" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( registerDto ) ).header( "Authorization", "Bearer " + token )
                .accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( content().string( "Staff added successfully." ) );

        final User user = userRepository.findByUsername( registerDto.getUsername() )
                .orElseThrow( () -> new RuntimeException( "User not found" ) );
        ;

        mvc.perform( delete( "/api/auth/user/{id}", user.getId() ).header( "Authorization", "Bearer " + token ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$", Matchers.equalTo( "User deleted successfully." ) ) );
    }

    /**
     * Tests getting all the users from the system. Requires the admin token
     * (ADMIN role)
     */
    @Test
    @Transactional
    public void testGetUsers () throws Exception {
        final String token = getAdminToken();

        final RegisterDto registerDto2 = new RegisterDto( "Staff Name", "staffuser", "staffuser@example.com",
                "staffpassword" );

        mvc.perform( post( "/api/auth/user" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( registerDto2 ) ).header( "Authorization", "Bearer " + token )
                .accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( content().string( "Staff added successfully." ) );

        // final RegisterDto registerDto2 = new RegisterDto( 1L, "Dinesh",
        // "staffuser2", "staffuser2@example.com",
        // "staffpassword2" );
        //
        // mvc.perform( post( "/api/auth" ).contentType(
        // MediaType.APPLICATION_JSON )
        // .content( TestUtils.asJsonString( registerDto ) ).header(
        // "Authorization", "Bearer " + token )
        // .accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
        // .andExpect( content().string( "Staff added successfully." ) );

        // mvc.perform(
        // get( "/api/auth" ).header( "Authorization", "Bearer " + token
        // ).accept( MediaType.APPLICATION_JSON ) )
        // .andExpect( status().isOk() ).andExpect( jsonPath( "$",
        // Matchers.hasSize( 3 ) ) )
        // .andExpect( jsonPath( "$[0].username" ).value( "admin" ) )
        // .andExpect( jsonPath( "$[1].username" ).value( "staffuser" ) );
        // .andExpect( jsonPath( "$[2].username" ).value( "staffuser2" ) );
    }

    /**
     * Error testing with deleting users. Tests deletion without being logged in
     * as an ADMIN
     */
    @Test
    @Transactional
    public void testForbiddenDeleteUserWithInvalidRole () throws Exception {
        // Register and login as a CUSTOMER (non-admin)
        final RegisterDto customerDto = new RegisterDto( 1L, "Customer", "customeruser", "customer@example.com",
                "password" );
        mvc.perform( post( "/api/auth/register" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( customerDto ) ) ).andExpect( status().isCreated() )
                .andExpect( content().string( "User registered successfully." ) );

        final LoginDto loginDto = new LoginDto( "customeruser", "password" );
        final MvcResult result = mvc.perform( post( "/api/auth/login" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( loginDto ) ) ).andExpect( status().isOk() ).andReturn();

        final String token = new ObjectMapper().readTree( result.getResponse().getContentAsString() )
                .get( "accessToken" ).asText();

        // Attempt to delete user with a CUSTOMER token
        mvc.perform( delete( "/api/auth/user/1" ).header( "Authorization", "Bearer " + token ) )
                .andExpect( status().isForbidden() ); // Expecting 403 Forbidden
    }

    /**
     * Error testing with deleting users. Tests deletion a non existent user
     */
    @Test
    @Transactional
    public void testDeleteNonExistentUser () throws Exception {
        final String token = getAdminToken();

        mvc.perform( delete( "/api/auth/user/{id}", 9999 ).header( "Authorization", "Bearer " + token ) )
                .andExpect( status().isNotFound() )
                .andExpect( jsonPath( "$.message" ).value( "User not found with id " + 9999 ) ) // Validate
                // error
                // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/auth/user/9999" ) ) // Validate
                                                                                         // URI
                                                                                         // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

    /**
     * Tests editing users. Requires ADMIN role
     */
    @Test
    @Transactional
    public void testEditUser () throws Exception {
        final String token = getAdminToken();

        // Step 1: Create the user
        final RegisterDto registerDto = new RegisterDto( null, "Old Name", "olduser", "olduser@example.com",
                "password" );
        mvc.perform( post( "/api/auth/user" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( registerDto ) ).header( "Authorization", "Bearer " + token ) )
                .andExpect( status().isOk() ).andExpect( content().string( "Staff added successfully." ) );

        // Step 2: Retrieve the created user's ID
        final Long userId = userRepository.findByUsername( "olduser" )
                .orElseThrow( () -> new RuntimeException( "User not found" ) ).getId();

        // Step 3: Edit the user using the retrieved ID
        final RegisterDto updateDto = new RegisterDto( userId, "Updated Name", "updateduser", "updateduser@example.com",
                "newpassword" );
        mvc.perform( put( "/api/auth/user/{id}", userId ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( updateDto ) ).header( "Authorization", "Bearer " + token ) )
                .andExpect( status().isOk() ).andExpect( content().string( "User edited successfully" ) );
    }

    /**
     * Error testing with editing users. Tests editing a non existent user
     */
    @Test
    @Transactional
    public void testEditNonExistentUser () throws Exception {
        final String token = getAdminToken();

        final RegisterDto updateDto = new RegisterDto( 1L, "Non-existent User", "nonexistent",
                "nonexistent@example.com", "password" );

        mvc.perform( put( "/api/auth/user/{id}", 9999 ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( updateDto ) ).header( "Authorization", "Bearer " + token ) )
                .andExpect( status().isNotFound() )
                .andExpect( jsonPath( "$.message" ).value( "User not found with id " + 9999 ) ) // Validate
                // error
                // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/auth/user/9999" ) ) // Validate
                                                                                         // URI
                                                                                         // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

    /**
     * Tests getting all the users in the system. Requires the ADMIN Role
     */
    @Test
    @Transactional
    public void testGetAllUsers () throws Exception {
        final String token = getAdminToken();

        final RegisterDto registerDto = new RegisterDto( 1L, "User1", "user1", "user1@example.com", "password1" );
        mvc.perform( post( "/api/auth/user" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( registerDto ) ).header( "Authorization", "Bearer " + token ) )
                .andExpect( status().isOk() ).andExpect( content().string( "Staff added successfully." ) );

        mvc.perform( get( "/api/auth" ).header( "Authorization", "Bearer " + token ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$", Matchers.hasSize( 2 ) ) ) // Admin +
                                                                     // User1
                .andExpect( jsonPath( "$[0].username", Matchers.is( "admin" ) ) )
                .andExpect( jsonPath( "$[1].username", Matchers.is( "user1" ) ) );
    }

    /**
     * Error testing with logging in users. Trying to login with non existent
     * credentials
     */
    @Test
    @Transactional
    public void testInvalidLogin () throws Exception {
        final LoginDto loginDto = new LoginDto( "invaliduser", "invalidpassword" );

        mvc.perform( post( "/api/auth/login" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( loginDto ) ) ).andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.message" ).value( "Invalid username/email or password." ) ) // Validate
                                                                                                     // error
                                                                                                     // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/auth/login" ) ) // Validate
                                                                                     // URI
                                                                                     // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

    /**
     * Error testing with registering users. Tries to register a duplicate user
     * with the same username
     */
    @Test
    @Transactional
    public void testRegisterDuplicateUsername () throws Exception {
        // Arrange: Create a user with a specific username
        final RegisterDto existingUserDto = new RegisterDto( null, "Existing User", "duplicateuser",
                "existing@example.com", "password" );
        mvc.perform( post( "/api/auth/register" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( existingUserDto ) ) ).andExpect( status().isCreated() );

        // Act & Assert: Attempt to register another user with the same username
        final RegisterDto duplicateUserDto = new RegisterDto( null, "Another User", "duplicateuser", "new@example.com",
                "password" );
        mvc.perform( post( "/api/auth/register" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( duplicateUserDto ) ) ).andExpect( status().isBadRequest() )
                .andExpect( content().string( Matchers.containsString( "Username 'duplicateuser' already exists." ) ) );
    }

    /**
     * Error testing with registering users. Tries to register a duplicate user
     * with the same email
     */
    @Test
    @Transactional
    public void testRegisterDuplicateEmail () throws Exception {
        // Arrange: Create a user with a specific email
        final RegisterDto existingUserDto = new RegisterDto( null, "Existing User", "existinguser",
                "duplicate@example.com", "password" );
        mvc.perform( post( "/api/auth/register" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( existingUserDto ) ) ).andExpect( status().isCreated() );

        // Act & Assert: Attempt to register another user with the same email
        final RegisterDto duplicateEmailDto = new RegisterDto( null, "Another User", "newuser", "duplicate@example.com",
                "password" );
        mvc.perform( post( "/api/auth/register" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( duplicateEmailDto ) ) ).andExpect( status().isBadRequest() )
                .andExpect( content()
                        .string( Matchers.containsString( "Email 'duplicate@example.com' already exists." ) ) );
    }

    /**
     * Error testing with logging in a user. Tries to login with invalid
     * credentials
     */
    @Test
    @Transactional
    public void testLoginInvalidCredentials () throws Exception {
        // Arrange: Create a valid user
        final RegisterDto validUserDto = new RegisterDto( null, "Valid User", "validuser", "valid@example.com",
                "password" );
        mvc.perform( post( "/api/auth/register" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( validUserDto ) ) ).andExpect( status().isCreated() );

        // Act & Assert: Attempt to login with incorrect credentials
        final LoginDto invalidLoginDto = new LoginDto( "validuser", "wrongpassword" );
        mvc.perform( post( "/api/auth/login" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( invalidLoginDto ) ) ).andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.message" ).value( "Invalid username/email or password." ) ) // Validate
                                                                                                     // error
                                                                                                     // message
                .andExpect( jsonPath( "$.details" ).value( "uri=/api/auth/login" ) ) // Validate
                                                                                     // URI
                                                                                     // details
                .andExpect( jsonPath( "$.timeStamp" ).exists() ); // Ensure
                                                                  // timestamp
                                                                  // exists
    }

    /**
     * Error testing with creating users. Tries to add a customer instead of a
     * staff
     */
    @Test
    @Transactional
    public void testCreateStaffWithException () throws Exception {
        // Arrange: Delete "ROLE_STAFF" directly from the database
        roleRepository.delete( roleRepository.findByName( "ROLE_STAFF" ) );

        final String token = getAdminToken();
        final RegisterDto registerDto = new RegisterDto( null, "Staff User", "staffuser", "staff@example.com",
                "password" );

        // Act & Assert: Call the endpoint and expect an INTERNAL_SERVER_ERROR
        // (500)
        mvc.perform( post( "/api/auth/user" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( registerDto ) ).header( "Authorization", "Bearer " + token ) )
                .andExpect( status().isInternalServerError() ).andExpect(
                        content().string( Matchers.containsString( "Role 'ROLE_STAFF' not found in the system." ) ) );
    }

}

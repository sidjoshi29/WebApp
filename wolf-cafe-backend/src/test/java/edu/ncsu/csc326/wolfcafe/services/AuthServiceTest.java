package edu.ncsu.csc326.wolfcafe.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;
import edu.ncsu.csc326.wolfcafe.service.AuthService;

/*
 * Tests AuthService
 */
@SpringBootTest
class AuthServiceTest {

    /**
     * Reference to AuthService
     */
    @Autowired
    private AuthService    authService;

    /**
     * Reference to the user repository
     */
    @Autowired
    private UserRepository userRepository;

    /*
     * Clears all the users to set up the next test
     */
    @BeforeEach
    void setUp () {
        userRepository.deleteAll();
    }

    // -------------------------------------
    // SUCCESS TEST CASES
    // -------------------------------------

    /*
     * Tests createStaff on success
     */
    @Test
    @Transactional
    void testCreateStaffSuccess () {
        final RegisterDto registerDto = new RegisterDto( null, "Dinesh", "drkarnat", "drkarnat@example.com",
                "password" );

        final String result = authService.createStaff( registerDto );

        assertEquals( "Staff added successfully.", result );

        final User savedUser = userRepository.findByUsername( "drkarnat" ).orElse( null );

        assertNotNull( savedUser, "The saved user should not be null" );

        assertAll( "Saved user properties", () -> assertEquals( "Dinesh", savedUser.getName() ),
                () -> assertEquals( "drkarnat", savedUser.getUsername() ),
                () -> assertEquals( "drkarnat@example.com", savedUser.getEmail() ) );
    }

    /*
     * Tests deleteStaff on success
     */
    @Test
    void testDeleteUserSuccess () {
        final RegisterDto registerDto = new RegisterDto( null, "Dinesh", "drkarnat", "drkarnat@example.com",
                "password" );

        authService.createStaff( registerDto );

        final User user = userRepository.findByUsername( registerDto.getUsername() )
                .orElseThrow( () -> new RuntimeException( "User not found" ) );

        authService.deleteUserById( user.getId() );

        assertFalse( userRepository.findByUsername( registerDto.getUsername() ).isPresent(),
                "User should be deleted." );
    }

    /*
     * Tests getAllUsers on success
     */
    @Test
    void testGetAllUsersSuccess () {
        final RegisterDto registerDto1 = new RegisterDto( null, "Dinesh", "drkarnat", "drkarnat@example.com",
                "password" );
        final RegisterDto registerDto2 = new RegisterDto( null, "Ameer", "ahmed", "aahmed@example.com", "password" );

        authService.createStaff( registerDto1 );
        authService.createStaff( registerDto2 );

        final List<User> users = authService.getAllUsers();

        assertEquals( 2, users.size(), "There should be 2 users in the system." );
    }

    /*
     * Tests editUser on success
     */
    @Test
    void testEditUserSuccess () {
        final RegisterDto registerDto = new RegisterDto( null, "Dinesh", "drkarnat", "drkarnat@example.com",
                "password" );

        authService.createStaff( registerDto );

        final User savedUser = userRepository.findByUsername( "drkarnat" ).orElse( null );

        assertNotNull( savedUser, "Saved user should not be null" );

        final RegisterDto updatedDto = new RegisterDto( null, "UpdatedName", "drkarnat", "drkarnat@example.com",
                "newpassword" );

        final String result = authService.editUser( savedUser.getId(), updatedDto );

        assertEquals( "User edited successfully", result );

        final User updatedUser = userRepository.findById( savedUser.getId() ).orElse( null );

        assertNotNull( updatedUser, "Updated user should not be null" );
        assertEquals( "UpdatedName", updatedUser.getName(), "Name should be updated." );
    }

    // -------------------------------------
    // ERROR TEST CASES
    // -------------------------------------

    /*
     * Tests createStaff on an error. Trying to create a duplicate staff
     */
    @Test
    void testCreateStaffDuplicateUserError () {
        final RegisterDto registerDto = new RegisterDto( null, "Dinesh", "drkarnat", "drkarnat@example.com",
                "password" );

        authService.createStaff( registerDto );

        final WolfCafeAPIException exception = assertThrows( WolfCafeAPIException.class,
                () -> authService.createStaff( registerDto ) );

        assertEquals( "A user with the provided username or email already exists.", exception.getMessage() );
    }

    /*
     * Tests deleteStaff on error. Tries to delete a user that doesnt exist
     */
    @Test
    void testDeleteUserNotFoundError () {
        final ResourceNotFoundException exception = assertThrows( ResourceNotFoundException.class,
                () -> authService.deleteUserById( 999L ) );

        assertEquals( "User not found with id 999", exception.getMessage() );
    }

    /*
     * Tests editStaff on failure. Tries to edit a non existent user
     */
    @Test
    void testEditUserNotFoundError () {
        final RegisterDto updatedDto = new RegisterDto( null, "UpdatedName", "nonexistent", "nonexistent@example.com",
                "password" );

        final ResourceNotFoundException exception = assertThrows( ResourceNotFoundException.class,
                () -> authService.editUser( 999L, updatedDto ) );

        assertEquals( "User not found with id 999", exception.getMessage() );
    }

    /*
     * Tests editStaff on failure. Tries to edit the user with invalid fields
     */
    @Test
    void testEditUserInvalidFieldsError () {
        final RegisterDto registerDto = new RegisterDto( null, "Dinesh", "drkarnat", "drkarnat@example.com",
                "password" );

        authService.createStaff( registerDto );

        final User savedUser = userRepository.findByUsername( "drkarnat" ).orElse( null );

        assertNotNull( savedUser, "Saved user should not be null" );

        final RegisterDto invalidDto = new RegisterDto( null, null, null, null, null );

        final WolfCafeAPIException exception = assertThrows( WolfCafeAPIException.class,
                () -> authService.editUser( savedUser.getId(), invalidDto ) );

        assertEquals( "All fields (name, username, email, password) are required.", exception.getMessage() );
    }
}

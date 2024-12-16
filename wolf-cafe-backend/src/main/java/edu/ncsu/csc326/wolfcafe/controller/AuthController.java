package edu.ncsu.csc326.wolfcafe.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.dto.JwtAuthResponse;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.service.AuthService;
import lombok.AllArgsConstructor;

/**
 * Controller for authentication functionality in WolfCafe.
 */
@CrossOrigin ( "*" )
@RestController
@RequestMapping ( "/api/auth" )
@AllArgsConstructor
public class AuthController {

    /** Link to AuthService */
    private final AuthService authService;

    /**
     * Registers a new customer user with the system.
     *
     * @param registerDto
     *            object with registration info
     * @return response indicating success or failure
     */
    @PostMapping ( "/register" )
    public ResponseEntity<String> register ( @RequestBody final RegisterDto registerDto ) {
        final String response = authService.register( registerDto );
        return ResponseEntity.status( 201 ).body( response ); // 201 Created
    }

    /**
     * Logs in the given user
     *
     * @param loginDto
     *            user information for login
     * @return object representing the logged in user
     */
    @PostMapping ( "/login" )
    public ResponseEntity<JwtAuthResponse> login ( @RequestBody final LoginDto loginDto ) {
        final JwtAuthResponse jwtAuthResponse = authService.login( loginDto );
        return ResponseEntity.ok( jwtAuthResponse ); // 200 OK
    }

    /**
     * Deletes the given user. Requires the ADMIN role.
     *
     * @param id
     *            id of user to delete
     * @return response indicating success or failure
     */
    @PreAuthorize ( "hasRole('ADMIN')" )
    @DeleteMapping ( "/user/{id}" )
    public ResponseEntity<String> deleteUser ( @PathVariable ( "id" ) final Long id ) {
        authService.deleteUserById( id );
        return ResponseEntity.ok( "User deleted successfully." ); // 200 OK
    }

    /**
     * Edits the given user. Requires the ADMIN role.
     *
     * @param id
     *            ID of the user to edit
     * @param registerDto
     *            User details to update
     * @return Response indicating success or failure
     */
    @PreAuthorize ( "hasRole('ADMIN')" )
    @PutMapping ( "/user/{id}" )
    public ResponseEntity<String> editUser ( @PathVariable ( "id" ) final Long id,
            @RequestBody final RegisterDto registerDto ) {
        final String result = authService.editUser( id, registerDto );
        return ResponseEntity.ok( result ); // 200 OK
    }

    /**
     * Adds the given staff. Requires the ADMIN role.
     *
     * @param id
     *            id of the staff to add
     * @return response indicating success or failure
     */
    @PreAuthorize ( "hasRole('ADMIN')" )
    @PostMapping ( "/user" )
    public ResponseEntity<String> createStaff ( @RequestBody final RegisterDto registerDto ) {
        final String result = authService.createStaff( registerDto );
        return ResponseEntity.ok( result ); // 200 OK
    }

    /**
     * Returns all the users in the system. Requires the ADMIN role.
     *
     * @return a list of all users
     */
    @PreAuthorize ( "hasRole('ADMIN')" )
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers () {
        final List<User> users = authService.getAllUsers();
        return ResponseEntity.ok( users ); // 200 OK
    }
}

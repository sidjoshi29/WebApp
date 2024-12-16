
package edu.ncsu.csc326.wolfcafe.service;

import java.util.List;

import edu.ncsu.csc326.wolfcafe.dto.JwtAuthResponse;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.entity.User;

/**
 * Authorization service
 */
public interface AuthService {
    /**
     * Registers the given user
     *
     * @param registerDto
     *            new user information
     * @return message for success or failure
     */
    String register ( RegisterDto registerDto );

    /**
     * Logins in the given user
     *
     * @param loginDto
     *            username/email and password
     * @return response with authenticated user
     */
    JwtAuthResponse login ( LoginDto loginDto );

    /**
     * Deletes the given user by id
     *
     * @param id
     *            id of user to delete
     */
    void deleteUserById ( Long id );

    /**
     * edits user by id
     *
     * @param id
     *            id of user to edit
     * @param registerDto
     *            updated user fields
     */
    String editUser ( Long id, RegisterDto registerDto );

    /**
     * Creates a new user by given information
     *
     * @param registerDto
     *            the new user to be added
     */
    String createStaff ( RegisterDto registerDto );

    /**
     * Gets all the users in the system
     *
     * @return list of all the users in the system
     */
    List<User> getAllUsers ();
}

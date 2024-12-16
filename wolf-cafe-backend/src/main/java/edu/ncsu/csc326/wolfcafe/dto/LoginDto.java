package edu.ncsu.csc326.wolfcafe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for representing the information to login a user.
 * The LoginDto holds the username or the email and the inputted password to
 * login the WolfCafe system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {

    /** The username or email of the user trying to login */
    private String usernameOrEmail;
    /** The password of the user trying to login */
    private String password;

}

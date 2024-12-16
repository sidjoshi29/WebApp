package edu.ncsu.csc326.wolfcafe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for representing the information to register a new
 * customer. The RegisterDto holds the unique id, name, username, email, and
 * password of the new customer to be added to the WolfCafe system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDto {
    /** The unique identifier for new cutomer being added */
    private Long   id;
    /** The name of the new customer */
    private String name;
    /** The username of the new customer */
    private String username;
    /** The email of the new customer */
    private String email;
    /** The password of the new customer */
    private String password;

    /**
     * Constructor for a RegisterDto, to register a customer without mentioning
     * the id (used for testing)
     */
    public RegisterDto ( final String name, final String username, final String email, final String password ) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
    }
}

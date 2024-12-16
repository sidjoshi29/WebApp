package edu.ncsu.csc326.wolfcafe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A Data Transfer Object (DTO) for handling responses related to user
 * authentication and authorization. This class is used to encapsulate the
 * necessary details about the JWT that will be returned to a client upon
 * successful authentication.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponse {

    /**
     * The access token that can be used for other tasks that need
     * authentication
     */
    private String accessToken;
    /** The token type being returned */
    private String tokenType = "Bearer";
    /** The role of the user authenticated */
    private String role;
}

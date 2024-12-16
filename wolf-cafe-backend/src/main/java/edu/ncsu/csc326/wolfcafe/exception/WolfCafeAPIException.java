package edu.ncsu.csc326.wolfcafe.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Exception for WolfCafe API calls.
 */
@Getter
@AllArgsConstructor
public class WolfCafeAPIException extends RuntimeException {

    /** A unique identifier for Serializable classes */
    private static final long serialVersionUID = 1L;
    /** HTTP Status associated with the error */
    private final HttpStatus  status;
    /** The error message associated with the error */
    private final String      message;
}

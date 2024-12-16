package edu.ncsu.csc326.wolfcafe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception when a resource is not found.
 */
@ResponseStatus ( value = HttpStatus.NOT_FOUND )
public class ResourceNotFoundException extends RuntimeException {

    /** A unique identifier for Serializable classes */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor for the ResourceNotFoundException.
     *
     * @param message
     *            The error message describing the exception.
     */

    public ResourceNotFoundException ( final String message ) {
        super( message );
    }
}

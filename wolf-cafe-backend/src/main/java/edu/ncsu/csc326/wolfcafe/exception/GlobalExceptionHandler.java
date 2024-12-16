package edu.ncsu.csc326.wolfcafe.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Handles global errors for the application.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ResourceNotFoundException and returns a 404 Not Found response.
     *
     * @param ex
     *            The exception that was thrown.
     * @param request
     *            The current web request.
     * @return A ResponseEntity containing the error details and HTTP status.
     */
    @ExceptionHandler ( ResourceNotFoundException.class )
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException ( final ResourceNotFoundException ex,
            final WebRequest request ) {
        final ErrorDetails errorDetails = new ErrorDetails( LocalDateTime.now(), ex.getMessage(),
                request.getDescription( false ) );
        return new ResponseEntity<>( errorDetails, HttpStatus.NOT_FOUND );
    }

    /**
     * Handles WolfCafeAPIException and returns the custom HTTP status from the
     * exception.
     *
     * @param ex
     *            The exception that was thrown.
     * @param request
     *            The current web request
     * @return A ResponseEntity containing the error details and custom HTTP
     *         status.
     */
    @ExceptionHandler ( WolfCafeAPIException.class )
    public ResponseEntity<ErrorDetails> handleWolfCafeAPIException ( final WolfCafeAPIException ex,
            final WebRequest request ) {
        final ErrorDetails errorDetails = new ErrorDetails( LocalDateTime.now(), ex.getMessage(),
                request.getDescription( false ) );
        return new ResponseEntity<>( errorDetails, ex.getStatus() );
    }

    /**
     * Handles all uncaught exceptions and returns an Internal Server Error
     * response.
     *
     * @param ex
     *            The exception that was thrown.
     * @param request
     *            The current web request
     * @return A ResponseEntity containing generic error details and HTTP
     *         status.
     */
    @ExceptionHandler ( Exception.class )
    public ResponseEntity<ErrorDetails> handleGlobalException ( final Exception ex, final WebRequest request ) {
        final ErrorDetails errorDetails = new ErrorDetails( LocalDateTime.now(), "An unexpected error occurred",
                request.getDescription( false ) );
        return new ResponseEntity<>( errorDetails, HttpStatus.INTERNAL_SERVER_ERROR );
    }

    /**
     * Handles AccessDeniedException and returns a Forbidden response.
     *
     * @param ex
     *            The exception that was thrown.
     * @param request
     *            The current web request
     * @return A ResponseEntity containing the error details and HTTP status.
     */
    @ExceptionHandler ( AccessDeniedException.class )
    public ResponseEntity<ErrorDetails> handleAccessDeniedException ( final AccessDeniedException ex,
            final WebRequest request ) {
        final ErrorDetails errorDetails = new ErrorDetails( LocalDateTime.now(), "Access is denied",
                request.getDescription( false ) );
        return new ResponseEntity<>( errorDetails, HttpStatus.FORBIDDEN );
    }

    /**
     * Handles AuthenticationException and returns a Unauthorized response.
     *
     * @param ex
     *            The exception that was thrown.
     * @param request
     *            The current web request
     * @return A ResponseEntity containing the error details and HTTP status.
     */
    @ExceptionHandler ( AuthenticationException.class )
    public ResponseEntity<ErrorDetails> handleAuthenticationException ( final AuthenticationException ex,
            final WebRequest request ) {
        final ErrorDetails errorDetails = new ErrorDetails( LocalDateTime.now(), "Authentication is required",
                request.getDescription( false ) );
        return new ResponseEntity<>( errorDetails, HttpStatus.UNAUTHORIZED );
    }

    /**
     * Handles IllegalStateException and returns a Conflict response.
     *
     * @param ex
     *            The exception that was thrown.
     * @param request
     *            The current web request details.
     * @return A ResponseEntity containing the error details and HTTP status.
     */
    @ExceptionHandler ( IllegalStateException.class )
    public ResponseEntity<ErrorDetails> handleIllegalStateException ( final IllegalStateException ex,
            final WebRequest request ) {
        final ErrorDetails errorDetails = new ErrorDetails( LocalDateTime.now(),
                "An illegal state occurred: " + ex.getMessage(), request.getDescription( false ) );
        return new ResponseEntity<>( errorDetails, HttpStatus.CONFLICT ); // 409
                                                                          // Conflict
    }
}

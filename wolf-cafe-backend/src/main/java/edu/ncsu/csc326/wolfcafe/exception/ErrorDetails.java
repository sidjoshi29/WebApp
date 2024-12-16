package edu.ncsu.csc326.wolfcafe.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Provides details on errors.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails {

    /** The timestamp the error occurred */
    private LocalDateTime timeStamp;
    /** The error message */
    private String        message;
    /** More details of the user */
    private String        details;
}

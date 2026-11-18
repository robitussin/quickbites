package com.quickbite.exception;

/** Checked exception: Input is missing or invalid. The message is shown to the user as is. */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}

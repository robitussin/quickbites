package com.quickbite.exception;

/** Checked exception: A record does not exist. The message is shown to the user as is. */
public class NotFoundException extends Exception {
    public NotFoundException(String message) {
        super(message);
    }
}

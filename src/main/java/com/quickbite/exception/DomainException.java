package com.quickbite.exception;

/** Checked exception: A business rule was broken. The message is shown to the user as is. */
public class DomainException extends Exception {
    public DomainException(String message) {
        super(message);
    }
}

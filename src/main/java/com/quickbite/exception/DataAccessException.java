package com.quickbite.exception;

/** Unchecked exception: wraps a SQLException so upper layers do not depend on JDBC. */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

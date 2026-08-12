package com.transit.exception;

public class InvalidCredentialsException extends Exception {
    public InvalidCredentialsException(String username) {
        super("Invalid username or password for '" + username + "'.");
    }
}

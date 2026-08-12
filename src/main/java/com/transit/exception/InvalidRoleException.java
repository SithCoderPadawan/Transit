package com.transit.exception;

public class InvalidRoleException extends Exception {
    public InvalidRoleException(String role) {
        super("Unrecognised role: '" + role + "'.");
    }
}

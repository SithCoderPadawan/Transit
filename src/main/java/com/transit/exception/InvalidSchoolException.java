package com.transit.exception;

public class InvalidSchoolException extends Exception {
    public InvalidSchoolException(String schoolId) {
        super("No school found with id '" + schoolId + "'.");
    }
}

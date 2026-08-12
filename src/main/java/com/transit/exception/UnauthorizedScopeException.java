package com.transit.exception;

public class UnauthorizedScopeException extends Exception {
    public UnauthorizedScopeException(String userId, String targetSchoolId) {
        super("User '" + userId + "' is not authorised to edit records at school '" + targetSchoolId + "'.");
    }
}

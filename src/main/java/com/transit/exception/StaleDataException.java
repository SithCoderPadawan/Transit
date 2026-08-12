package com.transit.exception;

/**
 * Thrown when an update targets a record whose version number no
 * longer matches the current stored version -- i.e. someone else
 * modified the record after it was last read. Implements the
 * optimistic locking strategy for requirement 3e.
 */
public class StaleDataException extends Exception {
    public StaleDataException(String recordId) {
        super("Record '" + recordId + "' was modified by another user. Please reload and try again.");
    }
}

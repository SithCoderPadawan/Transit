package com.transit.exception;

/**
 * Thrown when a performance rating supplied for a bus contractor
 * falls outside the valid range (1-5). Distinct from
 * InvalidContractorException, which covers an unknown contractor
 * rather than a bad value for an otherwise-valid one.
 */
public class InvalidRatingException extends Exception {
    public InvalidRatingException(int rating) {
        super("Invalid performance rating '" + rating + "'. Must be between 1 and 5.");
    }
}

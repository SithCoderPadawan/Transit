package com.transit.exception;

public class InvalidContractorException extends Exception {
    public InvalidContractorException(String companyName) {
        super("No bus contractor found with name '" + companyName + "'.");
    }
}

package com.transit.exception;

/**
 * Thrown when a row in an imported CSV file is malformed -- e.g. the
 * wrong number of columns, or a required field is blank. New in
 * Phase B to support requirement 1a (CSV data entry).
 */
public class CsvFormatException extends Exception {
    public CsvFormatException(int lineNumber, String reason) {
        super("Malformed CSV at line " + lineNumber + ": " + reason);
    }
}

package com.transit.exception;

public class PupilNotFoundException extends Exception {
    public PupilNotFoundException(String pupilId) {
        super("No pupil found with id '" + pupilId + "'.");
    }
}

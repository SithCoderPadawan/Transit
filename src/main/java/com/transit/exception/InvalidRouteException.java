package com.transit.exception;

public class InvalidRouteException extends Exception {
    public InvalidRouteException(String routeId) {
        super("No route found with id '" + routeId + "'.");
    }
}

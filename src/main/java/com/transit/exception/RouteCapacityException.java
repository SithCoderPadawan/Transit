package com.transit.exception;

public class RouteCapacityException extends Exception {
    private final String routeId;

    public RouteCapacityException(String routeId, int capacity) {
        super("Route '" + routeId + "' is at full capacity (" + capacity + "/" + capacity + ").");
        this.routeId = routeId;
    }

    public String getRouteId() { return routeId; }
}

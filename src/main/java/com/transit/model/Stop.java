package com.transit.model;

public class Stop {
    private final String location;
    private final String pickupTime;

    public Stop(String location, String pickupTime) {
        this.location = location;
        this.pickupTime = pickupTime;
    }

    public String getLocation() { return location; }
    public String getPickupTime() { return pickupTime; }

    @Override
    public String toString() { return location + " (" + pickupTime + ")"; }
}

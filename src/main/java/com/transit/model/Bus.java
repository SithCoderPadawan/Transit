package com.transit.model;

public class Bus {
    private final String regPlate;
    private final int capacity;
    private Driver driver;

    public Bus(String regPlate, int capacity, Driver driver) {
        this.regPlate = regPlate;
        this.capacity = capacity;
        this.driver = driver;
    }

    public String getRegPlate() { return regPlate; }
    public int getCapacity() { return capacity; }
    public Driver getDriver() { return driver; }
}

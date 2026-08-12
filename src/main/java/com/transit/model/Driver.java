package com.transit.model;

public class Driver {
    private final String name;
    private final String licenceNo;

    public Driver(String name, String licenceNo) {
        this.name = name;
        this.licenceNo = licenceNo;
    }

    public String getName() { return name; }
    public String getLicenceNo() { return licenceNo; }
}

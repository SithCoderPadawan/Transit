package com.transit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BusContractor {
    private final String companyName;
    private int performanceRating;
    private final List<Bus> buses = new ArrayList<>();

    public BusContractor(String companyName, int performanceRating) {
        this.companyName = companyName;
        this.performanceRating = performanceRating;
    }

    public String getCompanyName() { return companyName; }
    public int getPerformanceRating() { return performanceRating; }

    public void addBus(Bus bus) { buses.add(bus); }
    public List<Bus> getBuses() { return Collections.unmodifiableList(buses); }

    public void reviewPerformance(int newRating) { this.performanceRating = newRating; }
}

package com.transit.model;

import com.transit.exception.RouteCapacityException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * STRUCTURAL DESIGN PATTERN: Composite.
 * A Route is composed of Stop objects (1..* per the class diagram);
 * Stops have no meaning or lifecycle outside their owning Route.
 */
public class Route {
    private final String routeId;
    private final int capacity;
    private final List<Stop> stops = new ArrayList<>();
    private final List<Pupil> pupils = new ArrayList<>();
    private Bus bus;

    public Route(String routeId, int capacity) {
        this.routeId = routeId;
        this.capacity = capacity;
    }

    public String getRouteId() { return routeId; }
    public int getCapacity() { return capacity; }
    public Bus getBus() { return bus; }
    public void setBus(Bus bus) { this.bus = bus; }

    public void addStop(Stop stop) { stops.add(stop); }
    public List<Stop> getStops() { return Collections.unmodifiableList(stops); }

    public void addPupil(Pupil pupil) throws RouteCapacityException {
        if (pupils.size() >= capacity) {
            throw new RouteCapacityException(routeId, capacity);
        }
        pupils.add(pupil);
    }

    public void removePupil(Pupil pupil) {
        pupils.removeIf(p -> p.getPupilId().equals(pupil.getPupilId()));
    }

    public List<Pupil> getPupils() { return Collections.unmodifiableList(pupils); }
}

package com.transit.data;

import com.transit.exception.RouteCapacityException;
import com.transit.model.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Hard-coded sample data shared by the service layer -- kept in one
 * place (SRP) so a fully file-driven version only needs to change
 * this one class. seedPupils() below provides the demo dataset used
 * by the console walkthroughs; CsvImportService (Phase B, iteration 1)
 * can additionally import further pupils on top of this at runtime,
 * via the same addPupilToRoute() entry point.
 */
public class TransportDataStore {

    private final Map<String, School> schools = new HashMap<>();
    private final Map<String, Pupil> pupils = new HashMap<>();
    private final Map<String, Route> routes = new HashMap<>();
    private final Map<String, BusContractor> contractors = new HashMap<>();

    private Bus bus07;
    private Bus bus12;

    public TransportDataStore() {
        seedSchools();
        seedContractorsAndBuses();
        seedRoutes();
        seedPupils();
    }

    private void seedSchools() {
        schools.put("SCH-01", new School("SCH-01", "Northgate High", SchoolType.HIGH_SCHOOL));
        schools.put("SCH-02", new School("SCH-02", "Elmwood Junior", SchoolType.FEEDER_SCHOOL));
    }

    private void seedContractorsAndBuses() {
        BusContractor valley = new BusContractor("Valley Coaches", 4);
        bus07 = new Bus("BUS-07", 4, new Driver("M. Ahmed", "LIC-2201"));
        bus12 = new Bus("BUS-12", 2, new Driver("R. Owusu", "LIC-2202"));
        valley.addBus(bus07);
        valley.addBus(bus12);
        contractors.put(valley.getCompanyName(), valley);
    }

    private void seedRoutes() {
        Route r101 = new Route("R-101", 2);
        r101.addStop(new Stop("Elm Street", "08:05"));
        r101.addStop(new Stop("Oak Avenue", "08:12"));
        r101.setBus(bus12);
        routes.put(r101.getRouteId(), r101);

        Route r102 = new Route("R-102", 4);
        r102.addStop(new Stop("Birch Road", "07:55"));
        r102.addStop(new Stop("Maple Close", "08:02"));
        r102.addStop(new Stop("Cedar Grove", "08:10"));
        r102.setBus(bus07);
        routes.put(r102.getRouteId(), r102);
    }

    private void seedPupils() {
        try {
            addPupilToRoute(new Pupil("P-001", "Amelia Clarke", "07700-111111", "SCH-01", null), "R-101");
            addPupilToRoute(new Pupil("P-002", "Noah Bennett", "07700-222222", "SCH-01", null), "R-101");
            addPupilToRoute(new Pupil("P-003", "Isla Thompson", "07700-333333", "SCH-02", null), "R-102");
            addPupilToRoute(new Pupil("P-004", "Leo Farrell", "07700-444444", "SCH-02", null), "R-102");
        } catch (RouteCapacityException e) {
            throw new IllegalStateException("Seed data exceeded route capacity", e);
        }
    }

    /**
     * Adds a pupil to the store and assigns them to a route in one
     * step. Public so both internal seeding and CsvImportService can
     * use the same entry point, keeping "how a pupil gets added" in
     * exactly one place.
     */
    public void addPupilToRoute(Pupil pupil, String routeId) throws RouteCapacityException {
        Route route = routes.get(routeId);
        if (route == null) {
            // Caller (CsvImportService) is expected to validate the
            // route exists before calling this; defensive check kept
            // here too since this method is a public entry point.
            throw new IllegalArgumentException("Unknown route: " + routeId);
        }
        pupil.setRouteId(routeId);
        pupils.put(pupil.getPupilId(), pupil);
        route.addPupil(pupil);
    }

    public School findSchool(String schoolId) { return schools.get(schoolId); }
    public Pupil findPupil(String pupilId) { return pupils.get(pupilId); }
    public Route findRoute(String routeId) { return routes.get(routeId); }
    public BusContractor findContractor(String companyName) { return contractors.get(companyName); }

    public Map<String, Route> getAllRoutes() { return routes; }
    public Map<String, Pupil> getAllPupils() { return pupils; }
    public Map<String, School> getAllSchools() { return schools; }
    public Map<String, BusContractor> getAllContractors() { return contractors; }
}

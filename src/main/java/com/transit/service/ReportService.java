package com.transit.service;

import com.transit.data.TransportDataStore;
import com.transit.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements requirement 3f: "Provide reporting facilities for each
 * level of access." Corresponds to the "Generate report" use case
 * generalization on the use case diagram -- one shared entry point,
 * dispatching to a role-scoped report variant.
 *
 * Each report method reuses the same scope-checking philosophy
 * already established in PupilService: Admin and LEA see broad or
 * unrestricted data, School Staff and Parent see only what they are
 * permitted to see elsewhere in the system. Reporting is treated as
 * the read-only counterpart of the same access boundaries, not a
 * separate permission model.
 */
public class ReportService {

    private final TransportDataStore dataStore;

    public ReportService(TransportDataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Single entry point matching the abstract "Generate report" use
     * case. Dispatches to the correct role-scoped variant based on
     * the concrete User subclass -- mirrors how checkScope() in
     * PupilService already distinguishes roles where the underlying
     * action is shared but the data in view differs.
     */
    public List<String> generateReport(User user) {
        if (user instanceof Admin) {
            return generateFullSystemReport();
        }
        if (user instanceof LEAOfficer) {
            return generateRouteContractReport();
        }
        if (user instanceof SchoolStaff schoolStaff) {
            return generateSchoolPupilReport(schoolStaff);
        }
        if (user instanceof Parent parent) {
            return generateChildRouteReport(parent);
        }
        return List.of("No report available for this role.");
    }

    // ------------------------------------------------------------------
    // Admin: Full system report -- every pupil, route, and contractor
    // ------------------------------------------------------------------
    private List<String> generateFullSystemReport() {
        List<String> lines = new ArrayList<>();
        lines.add("=== Full System Report ===");
        lines.add("");
        lines.add("-- Schools --");
        for (School school : dataStore.getAllSchools().values()) {
            lines.add(String.format("  %s (%s) - %s", school.getName(), school.getSchoolId(), school.getType()));
        }
        lines.add("");
        lines.add("-- Routes --");
        for (Route route : dataStore.getAllRoutes().values()) {
            lines.add(String.format("  %s - %d/%d pupils, bus %s", route.getRouteId(),
                    route.getPupils().size(), route.getCapacity(),
                    route.getBus() != null ? route.getBus().getRegPlate() : "unassigned"));
        }
        lines.add("");
        lines.add("-- Pupils --");
        for (Pupil pupil : dataStore.getAllPupils().values()) {
            lines.add(String.format("  %s (%s) - school %s, route %s",
                    pupil.getName(), pupil.getPupilId(), pupil.getSchoolId(), pupil.getRouteId()));
        }
        lines.add("");
        lines.add("-- Bus Contractors --");
        for (BusContractor contractor : dataStore.getAllContractors().values()) {
            lines.add(String.format("  %s - rating %d/5, %d bus(es)",
                    contractor.getCompanyName(), contractor.getPerformanceRating(), contractor.getBuses().size()));
        }
        return lines;
    }

    // ------------------------------------------------------------------
    // LEA: Route & contract report -- all routes and contractor data
    // ------------------------------------------------------------------
    private List<String> generateRouteContractReport() {
        List<String> lines = new ArrayList<>();
        lines.add("=== Route & Contract Report ===");
        lines.add("");
        lines.add("-- Routes --");
        for (Route route : dataStore.getAllRoutes().values()) {
            lines.add(String.format("  %s - %d/%d pupils", route.getRouteId(),
                    route.getPupils().size(), route.getCapacity()));
            for (Stop stop : route.getStops()) {
                lines.add("      stop: " + stop);
            }
        }
        lines.add("");
        lines.add("-- Bus Contractors --");
        for (BusContractor contractor : dataStore.getAllContractors().values()) {
            lines.add(String.format("  %s - performance rating %d/5", contractor.getCompanyName(),
                    contractor.getPerformanceRating()));
            for (Bus bus : contractor.getBuses()) {
                lines.add(String.format("      bus %s (capacity %d), driver %s",
                        bus.getRegPlate(), bus.getCapacity(), bus.getDriver().getName()));
            }
        }
        return lines;
    }

    // ------------------------------------------------------------------
    // School Staff: School Pupil report -- scoped to their school
    // ------------------------------------------------------------------
    private List<String> generateSchoolPupilReport(SchoolStaff staff) {
        List<String> lines = new ArrayList<>();
        School school = dataStore.findSchool(staff.getSchoolId());
        lines.add("=== School Pupil Report ("
                + (school != null ? school.getName() : staff.getSchoolId()) + ") ===");
        lines.add("");

        boolean any = false;
        for (Pupil pupil : dataStore.getAllPupils().values()) {
            if (!pupil.getSchoolId().equals(staff.getSchoolId())) {
                continue; // same scope boundary enforced in PupilService.checkScope()
            }
            any = true;
            Route route = dataStore.findRoute(pupil.getRouteId());
            lines.add(String.format("  %s - route %s, emergency contact %s",
                    pupil.getName(),
                    route != null ? route.getRouteId() : "unassigned",
                    pupil.getEmergencyContact()));
        }
        if (!any) {
            lines.add("  No pupils currently recorded for this school.");
        }
        return lines;
    }

    // ------------------------------------------------------------------
    // Parent: Child's route report -- only their own child/children
    // ------------------------------------------------------------------
    private List<String> generateChildRouteReport(Parent parent) {
        List<String> lines = new ArrayList<>();
        lines.add("=== Child's Route Report ===");
        lines.add("");

        if (parent.getChildIds().isEmpty()) {
            lines.add("  No children linked to this account.");
            return lines;
        }

        for (String childId : parent.getChildIds()) {
            Pupil pupil = dataStore.findPupil(childId);
            if (pupil == null) {
                lines.add("  (Linked child id '" + childId + "' not found in system.)");
                continue;
            }
            Route route = dataStore.findRoute(pupil.getRouteId());
            lines.add("  " + pupil.getName() + ":");
            if (route == null) {
                lines.add("      No route currently assigned.");
                continue;
            }
            lines.add(String.format("      Route %s (bus %s)", route.getRouteId(),
                    route.getBus() != null ? route.getBus().getRegPlate() : "unassigned"));
            for (Stop stop : route.getStops()) {
                lines.add("      stop: " + stop);
            }
        }
        return lines;
    }
}

package com.transit.service;

import com.transit.data.TransportDataStore;
import com.transit.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportServiceTest {

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(new TransportDataStore());
    }

    @Test
    void adminReportListsAllPupilsAcrossAllSchools() {
        Admin admin = new Admin("U-102", "Test Admin", "admin@test.local");
        List<String> report = reportService.generateReport(admin);

        String joined = String.join("\n", report);
        assertTrue(joined.contains("Full System Report"));
        // Pupils from BOTH schools should appear -- Admin is unrestricted
        assertTrue(joined.contains("Amelia Clarke"));  // SCH-01
        assertTrue(joined.contains("Isla Thompson"));   // SCH-02
    }

    @Test
    void leaReportShowsRoutesAndContractorsNotIndividualPupils() {
        LEAOfficer lea = new LEAOfficer("U-101", "Test LEA", "lea@test.local");
        List<String> report = reportService.generateReport(lea);

        String joined = String.join("\n", report);
        assertTrue(joined.contains("Route & Contract Report"));
        assertTrue(joined.contains("Valley Coaches"));
        assertTrue(joined.contains("R-101"));
    }

    @Test
    void schoolStaffReportOnlyShowsOwnSchoolPupils() {
        SchoolStaff staff = new SchoolStaff("U-100", "Test Head", "head@sch01.local", "SCH-01");
        List<String> report = reportService.generateReport(staff);

        String joined = String.join("\n", report);
        assertTrue(joined.contains("Amelia Clarke"));    // SCH-01 -- should appear
        assertFalse(joined.contains("Isla Thompson"));   // SCH-02 -- should NOT appear
    }

    @Test
    void parentReportOnlyShowsLinkedChild() {
        Parent parent = new Parent("U-104", "Test Parent", "parent@test.local", List.of("P-001"));
        List<String> report = reportService.generateReport(parent);

        String joined = String.join("\n", report);
        assertTrue(joined.contains("Amelia Clarke"));    // P-001 -- linked child
        assertFalse(joined.contains("Noah Bennett"));    // P-002 -- not linked, should NOT appear
    }

    @Test
    void parentWithNoLinkedChildrenGetsExplicitMessage() {
        Parent parent = new Parent("U-105", "Childless Parent", "parent2@test.local", List.of());
        List<String> report = reportService.generateReport(parent);

        assertTrue(report.stream().anyMatch(line -> line.contains("No children linked")));
    }
}

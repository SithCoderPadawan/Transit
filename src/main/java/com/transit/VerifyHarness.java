package com.transit;

import com.transit.auth.AuthService;
import com.transit.data.TransportDataStore;
import com.transit.exception.*;
import com.transit.model.*;
import com.transit.service.CsvImportService;
import com.transit.service.ContractService;
import com.transit.service.PupilService;
import com.transit.service.ReportService;
import com.transit.service.RouteManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * NOT part of the deliverable. Dependency-free stand-in for the real
 * JUnit 5 tests, used only to verify logic in this sandbox, which has
 * no route to Maven Central for the JUnit jar. Real tests live in
 * src/test/java/ and run normally via `mvn test` in NetBeans.
 */
public class VerifyHarness {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        // AuthService
        validLoginReturnsCorrectlyTypedUser();
        wrongPasswordThrowsInvalidCredentialsException();
        unknownUsernameThrowsInvalidCredentialsException();
        thirdConsecutiveFailureLocksAccount();
        remainingAttemptsCountsDownCorrectly();

        // RouteManager
        movingPupilIntoFullRouteThrowsRouteCapacityException();
        movingPupilToRouteWithSpaceSucceeds();
        movingUnknownPupilThrowsPupilNotFoundException();
        movingToUnknownRouteThrowsInvalidRouteException();

        // PupilService
        schoolStaffCanEditOwnSchoolPupil();
        schoolStaffCannotEditOtherSchoolPupil();
        leaOfficerCanEditAnySchoolPupil();
        adminCanEditAnySchoolPupil();
        staleVersionThrowsStaleDataException();
        secondEditorSucceedsAfterReReadingCurrentVersion();

        // CsvImportService (Phase B, iteration 1)
        validCsvImportsAllRows();
        blankRequiredFieldThrowsCsvFormatException();
        wrongColumnCountThrowsCsvFormatException();
        unknownRouteThrowsCsvFormatException();
        missingFileThrowsIOException();

        // ReportService (Phase B, iteration 2)
        adminReportListsAllPupilsAcrossAllSchools();
        leaReportShowsRoutesAndContractors();
        schoolStaffReportOnlyShowsOwnSchoolPupils();
        parentReportOnlyShowsLinkedChild();
        parentWithNoLinkedChildrenGetsExplicitMessage();

        // ContractService (Phase B, iteration 4)
        validReviewUpdatesPerformanceRating();
        unknownContractorThrowsInvalidContractorException();
        ratingBelowMinimumThrowsInvalidRatingException();
        ratingAboveMaximumThrowsInvalidRatingException();

        System.out.println();
        System.out.println("===== RESULTS: " + passed + " passed, " + failed + " failed =====");
    }

    static void check(String testName, boolean condition) {
        if (condition) { System.out.println("[PASS] " + testName); passed++; }
        else { System.out.println("[FAIL] " + testName); failed++; }
    }

    // ---- AuthService ----

    static void validLoginReturnsCorrectlyTypedUser() throws Exception {
        AuthService authService = new AuthService();
        User user = authService.login("j.matthews", "lea123");
        check("validLogin: correct subtype", user instanceof LEAOfficer);
        check("validLogin: correct role label", "LEA Officer".equals(user.getRoleLabel()));
        check("validLogin: has MANAGE_ROUTES", user.hasPermission(Permission.MANAGE_ROUTES));
        check("validLogin: has EDIT_OWN_PUPIL_RECORDS (superset fix, req 2c)",
                user.hasPermission(Permission.EDIT_OWN_PUPIL_RECORDS));
        check("validLogin: lacks CORRECT_ANY_DATA", !user.hasPermission(Permission.CORRECT_ANY_DATA));
    }

    static void wrongPasswordThrowsInvalidCredentialsException() {
        AuthService authService = new AuthService();
        boolean threw = false;
        try { authService.login("admin1", "wrong-password"); }
        catch (InvalidCredentialsException e) { threw = true; }
        catch (Exception e) { }
        check("wrongPasswordThrowsInvalidCredentialsException", threw);
    }

    static void unknownUsernameThrowsInvalidCredentialsException() {
        AuthService authService = new AuthService();
        boolean threw = false;
        try { authService.login("nobody", "whatever"); }
        catch (InvalidCredentialsException e) { threw = true; }
        catch (Exception e) { }
        check("unknownUsernameThrowsInvalidCredentialsException", threw);
    }

    static void thirdConsecutiveFailureLocksAccount() {
        AuthService authService = new AuthService();
        int invalidCredCount = 0;
        for (int i = 0; i < 3; i++) {
            try { authService.login("parent1", "wrong-password"); }
            catch (InvalidCredentialsException e) { invalidCredCount++; }
            catch (Exception e) { }
        }
        check("thirdConsecutiveFailure: 3 InvalidCredentialsException thrown", invalidCredCount == 3);

        boolean lockedDespiteCorrect = false;
        try { authService.login("parent1", "parent123"); }
        catch (AccountLockedException e) { lockedDespiteCorrect = true; }
        catch (Exception e) { }
        check("thirdConsecutiveFailure: locked despite correct password", lockedDespiteCorrect);
    }

    static void remainingAttemptsCountsDownCorrectly() {
        AuthService authService = new AuthService();
        check("remainingAttempts: starts at 3", authService.getRemainingAttempts("school1") == 3);
        try { authService.login("school1", "wrong-password"); } catch (Exception e) { }
        check("remainingAttempts: drops to 2 after one failure", authService.getRemainingAttempts("school1") == 2);
    }

    // ---- RouteManager ----

    static void movingPupilIntoFullRouteThrowsRouteCapacityException() {
        RouteManager routeManager = new RouteManager(new TransportDataStore());
        boolean threw = false;
        try { routeManager.movePupilToRoute("P-003", "R-101"); }
        catch (RouteCapacityException e) { threw = true; }
        catch (Exception e) { }
        check("movingPupilIntoFullRouteThrowsRouteCapacityException", threw);
    }

    static void movingPupilToRouteWithSpaceSucceeds() throws Exception {
        RouteManager routeManager = new RouteManager(new TransportDataStore());
        routeManager.movePupilToRoute("P-001", "R-102");
        Route newRoute = routeManager.getRouteForPupil("P-001");
        check("movingPupilToRouteWithSpace: now on R-102", "R-102".equals(newRoute.getRouteId()));
        Route oldRoute = routeManager.getRouteForPupil("P-002");
        boolean stillOnOld = oldRoute.getPupils().stream().anyMatch(p -> p.getPupilId().equals("P-001"));
        check("movingPupilToRouteWithSpace: removed from R-101", !stillOnOld);
    }

    static void movingUnknownPupilThrowsPupilNotFoundException() {
        RouteManager routeManager = new RouteManager(new TransportDataStore());
        boolean threw = false;
        try { routeManager.movePupilToRoute("NO-SUCH-PUPIL", "R-102"); }
        catch (PupilNotFoundException e) { threw = true; }
        catch (Exception e) { }
        check("movingUnknownPupilThrowsPupilNotFoundException", threw);
    }

    static void movingToUnknownRouteThrowsInvalidRouteException() {
        RouteManager routeManager = new RouteManager(new TransportDataStore());
        boolean threw = false;
        try { routeManager.movePupilToRoute("P-003", "NO-SUCH-ROUTE"); }
        catch (InvalidRouteException e) { threw = true; }
        catch (Exception e) { }
        check("movingToUnknownRouteThrowsInvalidRouteException", threw);
    }

    // ---- PupilService ----

    static void schoolStaffCanEditOwnSchoolPupil() throws Exception {
        TransportDataStore ds = new TransportDataStore();
        PupilService svc = new PupilService(ds);
        SchoolStaff staff = new SchoolStaff("U-100", "Test Head", "head@sch01.local", "SCH-01");
        svc.updatePupilRecord(staff, "P-001", new PupilDetails("Amelia Updated", null), 0);
        check("schoolStaffCanEditOwnSchoolPupil", "Amelia Updated".equals(ds.findPupil("P-001").getName()));
    }

    static void schoolStaffCannotEditOtherSchoolPupil() {
        TransportDataStore ds = new TransportDataStore();
        PupilService svc = new PupilService(ds);
        SchoolStaff staff = new SchoolStaff("U-100", "Test Head", "head@sch01.local", "SCH-01");
        boolean threw = false;
        try { svc.updatePupilRecord(staff, "P-003", new PupilDetails("Hacked", null), 0); }
        catch (UnauthorizedScopeException e) { threw = true; }
        catch (Exception e) { }
        check("schoolStaffCannotEditOtherSchoolPupil", threw);
    }

    static void leaOfficerCanEditAnySchoolPupil() throws Exception {
        TransportDataStore ds = new TransportDataStore();
        PupilService svc = new PupilService(ds);
        LEAOfficer lea = new LEAOfficer("U-101", "Test LEA", "lea@test.local");
        svc.updatePupilRecord(lea, "P-003", new PupilDetails("LEA Updated", null), 0);
        check("leaOfficerCanEditAnySchoolPupil (proves req 2c superset fix)",
                "LEA Updated".equals(ds.findPupil("P-003").getName()));
    }

    static void adminCanEditAnySchoolPupil() throws Exception {
        TransportDataStore ds = new TransportDataStore();
        PupilService svc = new PupilService(ds);
        Admin admin = new Admin("U-102", "Test Admin", "admin@test.local");
        svc.updatePupilRecord(admin, "P-004", new PupilDetails("Admin Updated", null), 0);
        check("adminCanEditAnySchoolPupil", "Admin Updated".equals(ds.findPupil("P-004").getName()));
    }

    // ---- Optimistic locking (Phase B, iteration 3, requirement 3e) ----

    static void staleVersionThrowsStaleDataException() throws Exception {
        // Simulates two users both reading P-001 (version 0), then
        // User A saving first (record moves to version 1). User B's
        // write, still holding the stale version 0 they originally
        // read, must be rejected rather than silently overwriting A's
        // change.
        TransportDataStore ds = new TransportDataStore();
        PupilService svc = new PupilService(ds);
        Admin userA = new Admin("U-200", "User A", "a@test.local");
        Admin userB = new Admin("U-201", "User B", "b@test.local");

        int versionReadByBoth = svc.getPupil("P-001").getVersion(); // both read version 0

        svc.updatePupilRecord(userA, "P-001", new PupilDetails("Set By A", null), versionReadByBoth);

        boolean threw = false;
        try {
            svc.updatePupilRecord(userB, "P-001", new PupilDetails("Set By B", null), versionReadByBoth);
        } catch (StaleDataException e) {
            threw = true;
        } catch (Exception e) { }
        check("staleVersionThrowsStaleDataException", threw);
        check("staleVersionThrowsStaleDataException: A's change was not overwritten",
                "Set By A".equals(ds.findPupil("P-001").getName()));
    }

    static void secondEditorSucceedsAfterReReadingCurrentVersion() throws Exception {
        TransportDataStore ds = new TransportDataStore();
        PupilService svc = new PupilService(ds);
        Admin userA = new Admin("U-200", "User A", "a@test.local");
        Admin userB = new Admin("U-201", "User B", "b@test.local");

        int v0 = svc.getPupil("P-002").getVersion();
        svc.updatePupilRecord(userA, "P-002", new PupilDetails("Set By A", null), v0);

        // User B re-reads the record (correct recovery from a stale
        // write) and retries with the now-current version.
        int v1 = svc.getPupil("P-002").getVersion();
        svc.updatePupilRecord(userB, "P-002", new PupilDetails("Set By B", null), v1);

        check("secondEditorSucceedsAfterReReadingCurrentVersion",
                "Set By B".equals(ds.findPupil("P-002").getName()));
    }

    // ---- CsvImportService ----

    static void validCsvImportsAllRows() throws Exception {
        TransportDataStore ds = new TransportDataStore();
        CsvImportService svc = new CsvImportService(ds);
        Path tmp = Files.createTempFile("valid", ".csv");
        Files.writeString(tmp, "P-100,Test Pupil,07700-000000,SCH-01,R-102\n");
        int count = svc.importPupilsFromCsv(tmp.toString());
        check("validCsvImportsAllRows: count == 1", count == 1);
        check("validCsvImportsAllRows: pupil actually added", ds.findPupil("P-100") != null);
        Files.deleteIfExists(tmp);
    }

    static void blankRequiredFieldThrowsCsvFormatException() throws IOException {
        TransportDataStore ds = new TransportDataStore();
        CsvImportService svc = new CsvImportService(ds);
        Path tmp = Files.createTempFile("blank", ".csv");
        Files.writeString(tmp, "P-101,,07700-000000,SCH-01,R-102\n");
        boolean threw = false;
        try { svc.importPupilsFromCsv(tmp.toString()); }
        catch (CsvFormatException e) { threw = true; }
        catch (Exception e) { }
        check("blankRequiredFieldThrowsCsvFormatException", threw);
        Files.deleteIfExists(tmp);
    }

    static void wrongColumnCountThrowsCsvFormatException() throws IOException {
        TransportDataStore ds = new TransportDataStore();
        CsvImportService svc = new CsvImportService(ds);
        Path tmp = Files.createTempFile("badcols", ".csv");
        Files.writeString(tmp, "P-102,Test Pupil,07700-000000\n");
        boolean threw = false;
        try { svc.importPupilsFromCsv(tmp.toString()); }
        catch (CsvFormatException e) { threw = true; }
        catch (Exception e) { }
        check("wrongColumnCountThrowsCsvFormatException", threw);
        Files.deleteIfExists(tmp);
    }

    static void unknownRouteThrowsCsvFormatException() throws IOException {
        TransportDataStore ds = new TransportDataStore();
        CsvImportService svc = new CsvImportService(ds);
        Path tmp = Files.createTempFile("badroute", ".csv");
        Files.writeString(tmp, "P-103,Test Pupil,07700-000000,SCH-01,R-999\n");
        boolean threw = false;
        try { svc.importPupilsFromCsv(tmp.toString()); }
        catch (CsvFormatException e) { threw = true; }
        catch (Exception e) { }
        check("unknownRouteThrowsCsvFormatException", threw);
        Files.deleteIfExists(tmp);
    }

    static void missingFileThrowsIOException() {
        TransportDataStore ds = new TransportDataStore();
        CsvImportService svc = new CsvImportService(ds);
        boolean threw = false;
        try { svc.importPupilsFromCsv("/tmp/definitely-does-not-exist-12345.csv"); }
        catch (IOException e) { threw = true; }
        catch (Exception e) { }
        check("missingFileThrowsIOException", threw);
    }

    // ---- ReportService ----

    static void adminReportListsAllPupilsAcrossAllSchools() {
        ReportService svc = new ReportService(new TransportDataStore());
        Admin admin = new Admin("U-102", "Test Admin", "admin@test.local");
        String joined = String.join("\n", svc.generateReport(admin));
        check("adminReport: contains Full System Report header", joined.contains("Full System Report"));
        check("adminReport: contains SCH-01 pupil", joined.contains("Amelia Clarke"));
        check("adminReport: contains SCH-02 pupil (unrestricted)", joined.contains("Isla Thompson"));
    }

    static void leaReportShowsRoutesAndContractors() {
        ReportService svc = new ReportService(new TransportDataStore());
        LEAOfficer lea = new LEAOfficer("U-101", "Test LEA", "lea@test.local");
        String joined = String.join("\n", svc.generateReport(lea));
        check("leaReport: contains Route & Contract Report header", joined.contains("Route & Contract Report"));
        check("leaReport: contains contractor name", joined.contains("Valley Coaches"));
    }

    static void schoolStaffReportOnlyShowsOwnSchoolPupils() {
        ReportService svc = new ReportService(new TransportDataStore());
        SchoolStaff staff = new SchoolStaff("U-100", "Test Head", "head@sch01.local", "SCH-01");
        String joined = String.join("\n", svc.generateReport(staff));
        check("schoolStaffReport: contains own-school pupil", joined.contains("Amelia Clarke"));
        check("schoolStaffReport: excludes other-school pupil", !joined.contains("Isla Thompson"));
    }

    static void parentReportOnlyShowsLinkedChild() {
        ReportService svc = new ReportService(new TransportDataStore());
        Parent parent = new Parent("U-104", "Test Parent", "parent@test.local", java.util.List.of("P-001"));
        String joined = String.join("\n", svc.generateReport(parent));
        check("parentReport: contains linked child", joined.contains("Amelia Clarke"));
        check("parentReport: excludes non-linked pupil", !joined.contains("Noah Bennett"));
    }

    static void parentWithNoLinkedChildrenGetsExplicitMessage() {
        ReportService svc = new ReportService(new TransportDataStore());
        Parent parent = new Parent("U-105", "Childless Parent", "parent2@test.local", java.util.List.of());
        boolean hasMessage = svc.generateReport(parent).stream().anyMatch(line -> line.contains("No children linked"));
        check("parentWithNoLinkedChildrenGetsExplicitMessage", hasMessage);
    }

    // ---- ContractService ----

    static void validReviewUpdatesPerformanceRating() throws Exception {
        TransportDataStore ds = new TransportDataStore();
        ContractService svc = new ContractService(ds);
        svc.reviewContractorPerformance("Valley Coaches", 2);
        check("validReviewUpdatesPerformanceRating", ds.findContractor("Valley Coaches").getPerformanceRating() == 2);
    }

    static void unknownContractorThrowsInvalidContractorException() {
        ContractService svc = new ContractService(new TransportDataStore());
        boolean threw = false;
        try { svc.reviewContractorPerformance("Ghost Coaches", 3); }
        catch (InvalidContractorException e) { threw = true; }
        catch (Exception e) { }
        check("unknownContractorThrowsInvalidContractorException", threw);
    }

    static void ratingBelowMinimumThrowsInvalidRatingException() {
        ContractService svc = new ContractService(new TransportDataStore());
        boolean threw = false;
        try { svc.reviewContractorPerformance("Valley Coaches", 0); }
        catch (InvalidRatingException e) { threw = true; }
        catch (Exception e) { }
        check("ratingBelowMinimumThrowsInvalidRatingException", threw);
    }

    static void ratingAboveMaximumThrowsInvalidRatingException() {
        ContractService svc = new ContractService(new TransportDataStore());
        boolean threw = false;
        try { svc.reviewContractorPerformance("Valley Coaches", 6); }
        catch (InvalidRatingException e) { threw = true; }
        catch (Exception e) { }
        check("ratingAboveMaximumThrowsInvalidRatingException", threw);
    }
}

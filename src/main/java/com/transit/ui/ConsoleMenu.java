package com.transit.ui;

import com.transit.auth.AuthService;
import com.transit.data.TransportDataStore;
import com.transit.exception.*;
import com.transit.model.*;
import com.transit.service.CsvImportService;
import com.transit.service.ContractService;
import com.transit.service.PupilService;
import com.transit.service.ReportService;
import com.transit.service.RouteManager;
import com.transit.service.SchoolService;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ConsoleMenu {

    private final AuthService authService = new AuthService();
    private final TransportDataStore dataStore = new TransportDataStore();
    private final RouteManager routeManager = new RouteManager(dataStore);
    private final PupilService pupilService = new PupilService(dataStore);
    private final SchoolService schoolService = new SchoolService(dataStore);
    private final CsvImportService csvImportService = new CsvImportService(dataStore);
    private final ReportService reportService = new ReportService(dataStore);
    private final ContractService contractService = new ContractService(dataStore);
    private final Scanner scanner = new Scanner(System.in);

    private boolean inputExhausted = false;

    public void run() {
        System.out.println("==============================================");
        System.out.println(" Transit -- School Transport System");
        System.out.println(" School transport, simplified.");
        System.out.println("==============================================");

        boolean running = true;
        while (running && !inputExhausted) {
            System.out.println();
            System.out.println("1) Log in");
            System.out.println("2) Exit");
            System.out.print("Select an option: ");

            String choice = readLine();
            if (inputExhausted) break;
            switch (choice) {
                case "1":
                    attemptLogin();
                    break;
                case "2":
                    running = false;
                    System.out.println("Goodbye.");
                    break;
                default:
                    System.out.println("Unrecognised option. Please enter 1 or 2.");
            }
        }
        if (inputExhausted) {
            System.out.println();
            System.out.println("Input stream closed. Goodbye.");
        }
    }

    // ------------------------------------------------------------------
    // Login
    // ------------------------------------------------------------------

    private void attemptLogin() {
        System.out.print("Username: ");
        String username = readLine();
        if (inputExhausted) return;
        System.out.print("Password: ");
        String password = readLine();
        if (inputExhausted) return;

        try {
            User user = authService.login(username, password);
            System.out.println();
            System.out.println("Login successful. Welcome, " + user.getName() + " (" + user.getRoleLabel() + ")");
            roleMenu(user);

        } catch (InvalidCredentialsException e) {
            System.out.println();
            System.out.println("Login failed: " + e.getMessage());
            System.out.println("Attempts remaining before lockout: " + authService.getRemainingAttempts(username));

        } catch (AccountLockedException e) {
            System.out.println();
            System.out.println("Login failed: " + e.getMessage());
            System.out.println("Please contact your Admin to unlock this account.");

        } catch (InvalidRoleException e) {
            System.out.println();
            System.out.println("System error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Post-login, role-scoped menu
    // ------------------------------------------------------------------

    private void roleMenu(User user) {
        boolean loggedIn = true;
        while (loggedIn && !inputExhausted) {
            System.out.println();
            System.out.println("---- Menu (" + user.getRoleLabel() + ") ----");
            int optionNumber = 1;

            boolean canMoveRoute = user.hasPermission(Permission.MANAGE_ROUTES);
            boolean canEditPupil = user.hasPermission(Permission.EDIT_ANY_PUPIL_RECORDS)
                    || user.hasPermission(Permission.EDIT_OWN_PUPIL_RECORDS);
            boolean canViewRoute = user.hasPermission(Permission.VIEW_ROUTES);
            boolean canEnterStaffContact = user.hasPermission(Permission.CORRECT_ANY_DATA);
            boolean canImportCsv = user.hasPermission(Permission.CORRECT_ANY_DATA);
            boolean canGenerateReport = user.hasPermission(Permission.VIEW_REPORTS);
            boolean canManageContracts = user.hasPermission(Permission.MANAGE_CONTRACTS);

            if (canMoveRoute)         System.out.println((optionNumber++) + ") Move pupil to new route");
            if (canEditPupil)         System.out.println((optionNumber++) + ") Update pupil record");
            if (canViewRoute)         System.out.println((optionNumber++) + ") View route & pickup point");
            if (canEnterStaffContact) System.out.println((optionNumber++) + ") Enter school staff contact");
            if (canImportCsv)         System.out.println((optionNumber++) + ") Import pupil data (CSV)");
            if (canGenerateReport)    System.out.println((optionNumber++) + ") Generate report");
            if (canManageContracts)   System.out.println((optionNumber++) + ") Manage bus contracts");
            System.out.println((optionNumber) + ") Log out");
            int logoutOption = optionNumber;

            System.out.print("Select an option: ");
            String choice = readLine();
            if (inputExhausted) return;

            try {
                int selected = Integer.parseInt(choice);
                int cursor = 1;

                if (canMoveRoute && selected == cursor++) { movePupil(); continue; }
                if (canEditPupil && selected == cursor++) { updatePupil(user); continue; }
                if (canViewRoute && selected == cursor++) { viewRoute(); continue; }
                if (canEnterStaffContact && selected == cursor++) { enterStaffContact(); continue; }
                if (canImportCsv && selected == cursor++) { importCsv(); continue; }
                if (canGenerateReport && selected == cursor++) { generateReport(user); continue; }
                if (canManageContracts && selected == cursor++) { manageContracts(); continue; }
                if (selected == logoutOption) {
                    System.out.println("Logged out.");
                    loggedIn = false;
                    continue;
                }
                System.out.println("Unrecognised option.");

            } catch (NumberFormatException e) {
                System.out.println("Please enter a number from the menu.");
            }
        }
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    private void movePupil() {
        System.out.print("Pupil ID: ");
        String pupilId = requireNonBlank(readLine(), "Pupil ID");
        if (inputExhausted || pupilId == null) return;

        System.out.print("New Route ID: ");
        String newRouteId = requireNonBlank(readLine(), "Route ID");
        if (inputExhausted || newRouteId == null) return;

        try {
            routeManager.movePupilToRoute(pupilId, newRouteId);
            System.out.println("Pupil reassigned to route " + newRouteId + ".");
        } catch (RouteCapacityException e) {
            System.out.println("Move failed: " + e.getMessage());
            System.out.println("Choose a different route or contact LEA.");
        } catch (InvalidRouteException | PupilNotFoundException e) {
            System.out.println("Move failed: " + e.getMessage());
        }
    }

    private void updatePupil(User currentUser) {
        System.out.print("Pupil ID: ");
        String pupilId = requireNonBlank(readLine(), "Pupil ID");
        if (inputExhausted || pupilId == null) return;

        // Read the current record first, capturing its version --
        // this is the "read" half of optimistic locking. The version
        // captured here is what gets checked against the stored
        // version at write time.
        Pupil currentRecord;
        try {
            currentRecord = pupilService.getPupil(pupilId);
        } catch (PupilNotFoundException e) {
            System.out.println("Update failed: " + e.getMessage());
            return;
        }

        System.out.println("Current record: " + currentRecord.getName()
                + " / " + currentRecord.getEmergencyContact()
                + " (version " + currentRecord.getVersion() + ")");

        System.out.print("New name (leave blank to keep unchanged): ");
        String name = blankToNull(readLine());
        if (inputExhausted) return;
        System.out.print("New emergency contact (leave blank to keep unchanged): ");
        String contact = blankToNull(readLine());
        if (inputExhausted) return;

        try {
            pupilService.updatePupilRecord(currentUser, pupilId, new PupilDetails(name, contact),
                    currentRecord.getVersion());
            System.out.println("Pupil record updated.");
        } catch (UnauthorizedScopeException | PupilNotFoundException e) {
            System.out.println("Update failed: " + e.getMessage());
        } catch (StaleDataException e) {
            System.out.println("Update failed: " + e.getMessage());
            System.out.println("Please view the record again and retry your change.");
        }
    }

    private void viewRoute() {
        System.out.print("Pupil ID (whose route you want to view): ");
        String pupilId = requireNonBlank(readLine(), "Pupil ID");
        if (inputExhausted || pupilId == null) return;

        try {
            Route route = routeManager.getRouteForPupil(pupilId);
            System.out.println("Route " + route.getRouteId() + " -- capacity "
                    + route.getPupils().size() + "/" + route.getCapacity());
            for (Stop stop : route.getStops()) {
                System.out.println("  - " + stop);
            }
        } catch (PupilNotFoundException | InvalidRouteException e) {
            System.out.println("Could not display route: " + e.getMessage());
        }
    }

    private void enterStaffContact() {
        System.out.print("School ID: ");
        String schoolId = requireNonBlank(readLine(), "School ID");
        if (inputExhausted || schoolId == null) return;

        System.out.print("Contact name: ");
        String name = requireNonBlank(readLine(), "Contact name");
        if (inputExhausted || name == null) return;

        System.out.print("Role (e.g. Headmaster): ");
        String role = readLine();
        if (inputExhausted) return;
        System.out.print("Email: ");
        String email = readLine();
        if (inputExhausted) return;
        System.out.print("Phone: ");
        String phone = readLine();
        if (inputExhausted) return;

        try {
            schoolService.addSchoolContact(schoolId, new SchoolContact(name, role, email, phone));
            System.out.println("Contact saved for school " + schoolId + ".");
        } catch (InvalidSchoolException e) {
            System.out.println("Could not save contact: " + e.getMessage());
        }
    }

    private void manageContracts() {
        System.out.println();
        for (String line : contractService.listContractors()) {
            System.out.println(line);
        }
        System.out.println();
        System.out.print("Company name to review (leave blank to cancel): ");
        String companyName = blankToNull(readLine());
        if (inputExhausted || companyName == null) return;

        System.out.print("New performance rating (1-5): ");
        String ratingInput = readLine();
        if (inputExhausted) return;

        try {
            int rating = Integer.parseInt(ratingInput);
            contractService.reviewContractorPerformance(companyName, rating);
            System.out.println("Performance rating updated for " + companyName + ".");
        } catch (NumberFormatException e) {
            System.out.println("Rating must be a number.");
        } catch (InvalidContractorException | InvalidRatingException e) {
            System.out.println("Review failed: " + e.getMessage());
        }
    }

    private void generateReport(User user) {
        System.out.println();
        for (String line : reportService.generateReport(user)) {
            System.out.println(line);
        }
    }

    private void importCsv() {
        System.out.print("CSV file path: ");
        String filePath = requireNonBlank(readLine(), "File path");
        if (inputExhausted || filePath == null) return;

        try {
            int count = csvImportService.importPupilsFromCsv(filePath);
            System.out.println("Imported " + count + " pupil record(s) from " + filePath + ".");

        } catch (IOException e) {
            // Genuine Java API exception (file not found / unreadable),
            // handled here rather than left to crash the program.
            System.out.println("Could not read file: " + e.getMessage());

        } catch (CsvFormatException e) {
            System.out.println("Import stopped: " + e.getMessage());

        } catch (RouteCapacityException e) {
            System.out.println("Import stopped: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Input helpers
    // ------------------------------------------------------------------

    private String readLine() {
        try {
            return scanner.nextLine().trim();
        } catch (NoSuchElementException | IllegalStateException e) {
            inputExhausted = true;
            return "";
        }
    }

    private String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            System.out.println(fieldName + " cannot be blank.");
            return null;
        }
        return value;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}

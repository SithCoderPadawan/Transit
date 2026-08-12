package com.transit.service;

import com.transit.data.TransportDataStore;
import com.transit.exception.PupilNotFoundException;
import com.transit.exception.StaleDataException;
import com.transit.exception.UnauthorizedScopeException;
import com.transit.model.Admin;
import com.transit.model.LEAOfficer;
import com.transit.model.PupilDetails;
import com.transit.model.SchoolStaff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PupilServiceTest {

    private PupilService pupilService;
    private TransportDataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = new TransportDataStore();
        pupilService = new PupilService(dataStore);
    }

    @Test
    void schoolStaffCanEditOwnSchoolPupil() throws Exception {
        SchoolStaff staff = new SchoolStaff("U-100", "Test Head", "head@sch01.local", "SCH-01");
        pupilService.updatePupilRecord(staff, "P-001", new PupilDetails("Amelia Updated", null), 0);
        assertEquals("Amelia Updated", dataStore.findPupil("P-001").getName());
    }

    @Test
    void schoolStaffCannotEditOtherSchoolPupil() {
        SchoolStaff staff = new SchoolStaff("U-100", "Test Head", "head@sch01.local", "SCH-01");
        assertThrows(UnauthorizedScopeException.class,
                () -> pupilService.updatePupilRecord(staff, "P-003", new PupilDetails("Hacked", null), 0));
    }

    @Test
    void leaOfficerCanEditAnySchoolPupil() throws Exception {
        LEAOfficer lea = new LEAOfficer("U-101", "Test LEA", "lea@test.local");
        pupilService.updatePupilRecord(lea, "P-003", new PupilDetails("LEA Updated", null), 0);
        assertEquals("LEA Updated", dataStore.findPupil("P-003").getName());
    }

    @Test
    void adminCanEditAnySchoolPupil() throws Exception {
        Admin admin = new Admin("U-102", "Test Admin", "admin@test.local");
        pupilService.updatePupilRecord(admin, "P-004", new PupilDetails("Admin Updated", null), 0);
        assertEquals("Admin Updated", dataStore.findPupil("P-004").getName());
    }

    @Test
    void updatingUnknownPupilThrowsPupilNotFoundException() {
        Admin admin = new Admin("U-102", "Test Admin", "admin@test.local");
        assertThrows(PupilNotFoundException.class,
                () -> pupilService.updatePupilRecord(admin, "NO-SUCH-PUPIL", new PupilDetails("X", null), 0));
    }

    // ---- Optimistic locking (requirement 3e) ----

    @Test
    void staleVersionIsRejectedWithoutOverwritingTheFirstEditorsChange() throws Exception {
        Admin userA = new Admin("U-200", "User A", "a@test.local");
        Admin userB = new Admin("U-201", "User B", "b@test.local");

        int versionReadByBoth = pupilService.getPupil("P-001").getVersion();

        pupilService.updatePupilRecord(userA, "P-001", new PupilDetails("Set By A", null), versionReadByBoth);

        assertThrows(StaleDataException.class, () ->
                pupilService.updatePupilRecord(userB, "P-001", new PupilDetails("Set By B", null), versionReadByBoth));

        // Confirm A's change survived -- B's stale write must not have applied.
        assertEquals("Set By A", dataStore.findPupil("P-001").getName());
    }

    @Test
    void secondEditorSucceedsAfterReReadingCurrentVersion() throws Exception {
        Admin userA = new Admin("U-200", "User A", "a@test.local");
        Admin userB = new Admin("U-201", "User B", "b@test.local");

        int v0 = pupilService.getPupil("P-002").getVersion();
        pupilService.updatePupilRecord(userA, "P-002", new PupilDetails("Set By A", null), v0);

        int v1 = pupilService.getPupil("P-002").getVersion();
        pupilService.updatePupilRecord(userB, "P-002", new PupilDetails("Set By B", null), v1);

        assertEquals("Set By B", dataStore.findPupil("P-002").getName());
    }

    @Test
    void versionIncrementsOnEachSuccessfulUpdate() throws Exception {
        Admin admin = new Admin("U-102", "Test Admin", "admin@test.local");
        int v0 = pupilService.getPupil("P-001").getVersion();

        pupilService.updatePupilRecord(admin, "P-001", new PupilDetails("First Edit", null), v0);
        int v1 = pupilService.getPupil("P-001").getVersion();

        assertEquals(v0 + 1, v1);
    }
}

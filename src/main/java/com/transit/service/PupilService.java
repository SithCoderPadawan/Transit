package com.transit.service;

import com.transit.data.TransportDataStore;
import com.transit.exception.PupilNotFoundException;
import com.transit.exception.StaleDataException;
import com.transit.exception.UnauthorizedScopeException;
import com.transit.model.*;

/**
 * Implements the shared "Manage pupil & parent info" use case.
 * LEA Officer and Admin may edit any pupil; School Staff may only
 * edit pupils at their own school -- the {scope: own school only}
 * constraint from the use case diagram, enforced here rather than in
 * the UI layer.
 *
 * REQUIREMENT 3e: also enforces optimistic locking to preserve data
 * integrity under multi-user access. Every caller must supply the
 * version number of the pupil record they read before editing; if
 * another user has modified the record since then (the stored
 * version has moved on), the update is rejected with
 * StaleDataException rather than silently overwriting the other
 * user's change. See the written report for the justification of
 * optimistic over pessimistic locking for this system.
 */
public class PupilService {
    private final TransportDataStore dataStore;

    public PupilService(TransportDataStore dataStore) { this.dataStore = dataStore; }

    /**
     * Read-only lookup, used by callers to fetch the current record
     * (including its version number) before presenting an edit form.
     */
    public Pupil getPupil(String pupilId) throws PupilNotFoundException {
        Pupil pupil = dataStore.findPupil(pupilId);
        if (pupil == null) throw new PupilNotFoundException(pupilId);
        return pupil;
    }

    public void updatePupilRecord(User currentUser, String pupilId, PupilDetails details, int expectedVersion)
            throws PupilNotFoundException, UnauthorizedScopeException, StaleDataException {

        Pupil pupil = dataStore.findPupil(pupilId);
        if (pupil == null) throw new PupilNotFoundException(pupilId);

        checkScope(currentUser, pupil.getSchoolId());

        if (pupil.getVersion() != expectedVersion) {
            // Someone else has modified this record since it was read.
            // Reject the write rather than silently overwriting their
            // change -- the caller must re-read the current record and
            // decide whether to retry.
            throw new StaleDataException(pupilId);
        }

        pupil.applyDetails(details);
    }

    private void checkScope(User user, String pupilSchoolId) throws UnauthorizedScopeException {
        if (user.hasPermission(Permission.EDIT_ANY_PUPIL_RECORDS)) return;

        if (user.hasPermission(Permission.EDIT_OWN_PUPIL_RECORDS)
                && user instanceof SchoolStaff schoolStaff
                && schoolStaff.getSchoolId().equals(pupilSchoolId)) {
            return;
        }
        throw new UnauthorizedScopeException(user.getUserId(), pupilSchoolId);
    }
}

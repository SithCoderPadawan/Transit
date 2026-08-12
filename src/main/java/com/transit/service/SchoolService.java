package com.transit.service;

import com.transit.data.TransportDataStore;
import com.transit.exception.InvalidSchoolException;
import com.transit.model.School;
import com.transit.model.SchoolContact;

public class SchoolService {
    private final TransportDataStore dataStore;

    public SchoolService(TransportDataStore dataStore) { this.dataStore = dataStore; }

    public void addSchoolContact(String schoolId, SchoolContact contact) throws InvalidSchoolException {
        School school = dataStore.findSchool(schoolId);
        if (school == null) throw new InvalidSchoolException(schoolId);
        school.addContact(contact);
    }
}

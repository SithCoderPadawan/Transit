package com.transit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class School {
    private final String schoolId;
    private final String name;
    private final SchoolType type;
    private final List<SchoolContact> contacts = new ArrayList<>();

    public School(String schoolId, String name, SchoolType type) {
        this.schoolId = schoolId;
        this.name = name;
        this.type = type;
    }

    public String getSchoolId() { return schoolId; }
    public String getName() { return name; }
    public SchoolType getType() { return type; }

    public void addContact(SchoolContact contact) { contacts.add(contact); }
    public List<SchoolContact> getContacts() { return Collections.unmodifiableList(contacts); }
}

package com.transit.model;

public class SchoolStaff extends User {
    private final String schoolId;

    public SchoolStaff(String userId, String name, String email, String schoolId) {
        super(userId, name, email, new SchoolPermissionStrategy());
        this.schoolId = schoolId;
    }

    public String getSchoolId() { return schoolId; }

    @Override
    public String getRoleLabel() { return "School Staff"; }
}

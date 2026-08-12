package com.transit.model;

public class LEAOfficer extends User {
    public LEAOfficer(String userId, String name, String email) {
        super(userId, name, email, new LEAPermissionStrategy());
    }

    @Override
    public String getRoleLabel() { return "LEA Officer"; }
}

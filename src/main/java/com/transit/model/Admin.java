package com.transit.model;

public class Admin extends User {
    public Admin(String userId, String name, String email) {
        super(userId, name, email, new AdminPermissionStrategy());
    }

    @Override
    public String getRoleLabel() { return "Admin"; }
}

package com.transit.model;

public class SchoolContact {
    private final String name;
    private final String role;
    private final String email;
    private final String phone;

    public SchoolContact(String name, String role, String email, String phone) {
        this.name = name;
        this.role = role;
        this.email = email;
        this.phone = phone;
    }

    public String getName() { return name; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    @Override
    public String toString() {
        return name + " (" + role + ") -- " + email + ", " + phone;
    }
}

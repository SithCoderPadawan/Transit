package com.transit.model;

import java.util.List;

public class Parent extends User {
    private final List<String> childIds;

    public Parent(String userId, String name, String email, List<String> childIds) {
        super(userId, name, email, new ParentPermissionStrategy());
        this.childIds = childIds;
    }

    public List<String> getChildIds() { return childIds; }

    @Override
    public String getRoleLabel() { return "Parent"; }
}

package com.transit.model;

import java.util.EnumSet;
import java.util.Set;

final class AdminPermissionStrategy implements PermissionStrategy {
    @Override
    public Set<Permission> resolvePermissions() {
        return EnumSet.allOf(Permission.class);
    }
}

final class LEAPermissionStrategy implements PermissionStrategy {
    @Override
    public Set<Permission> resolvePermissions() {
        // Requirement 2c: LEA can do everything School can, plus more.
        // Built explicitly on top of School's set (superset), rather
        // than duplicated, so the two can't drift apart.
        Set<Permission> perms = EnumSet.noneOf(Permission.class);
        perms.addAll(new SchoolPermissionStrategy().resolvePermissions());
        perms.add(Permission.EDIT_ANY_PUPIL_RECORDS);
        perms.add(Permission.MANAGE_ROUTES);
        perms.add(Permission.MANAGE_CONTRACTS);
        perms.add(Permission.VIEW_REPORTS);
        return perms;
    }
}

final class SchoolPermissionStrategy implements PermissionStrategy {
    @Override
    public Set<Permission> resolvePermissions() {
        return EnumSet.of(
                Permission.VIEW_ROUTES,
                Permission.EDIT_OWN_PUPIL_RECORDS,
                Permission.VIEW_REPORTS
        );
    }
}

final class ParentPermissionStrategy implements PermissionStrategy {
    @Override
    public Set<Permission> resolvePermissions() {
        return EnumSet.of(Permission.VIEW_ROUTES, Permission.VIEW_REPORTS);
    }
}

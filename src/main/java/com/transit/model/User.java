package com.transit.model;

import java.util.Collections;
import java.util.Set;

/**
 * Abstract parent of the role hierarchy. Subtype polymorphism: every
 * subclass fulfils this contract without weakening it (LSP), so
 * calling code never needs to check which concrete subclass it holds.
 */
public abstract class User {

    protected final String userId;
    protected final String name;
    protected final String email;
    private final Set<Permission> permissions;

    protected User(String userId, String name, String email, PermissionStrategy strategy) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.permissions = Collections.unmodifiableSet(strategy.resolvePermissions());
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public Set<Permission> getPermissions() { return permissions; }
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    public abstract String getRoleLabel();
}

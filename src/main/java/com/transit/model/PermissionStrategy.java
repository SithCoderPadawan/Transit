package com.transit.model;

import java.util.Set;

/**
 * BEHAVIOURAL DESIGN PATTERN: Strategy.
 * Each role's permission-resolution algorithm is an interchangeable
 * object supplied to the User constructor by UserFactory, rather than
 * hard-coded conditionals inside User itself.
 */
public interface PermissionStrategy {
    Set<Permission> resolvePermissions();
}

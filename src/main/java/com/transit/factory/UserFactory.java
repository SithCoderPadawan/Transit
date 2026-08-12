package com.transit.factory;

import com.transit.exception.InvalidRoleException;
import com.transit.model.*;

import java.util.List;

/**
 * CREATIONAL DESIGN PATTERN: Factory Method.
 * Centralises which concrete User subclass gets built for a given
 * Role, so callers never use "new Admin(...)" directly.
 */
public final class UserFactory {

    private UserFactory() { }

    public static User createUser(Role role, String userId, String name, String email,
                                   String schoolId, List<String> childIds) throws InvalidRoleException {
        switch (role) {
            case ADMIN:  return new Admin(userId, name, email);
            case LEA:    return new LEAOfficer(userId, name, email);
            case SCHOOL: return new SchoolStaff(userId, name, email, schoolId);
            case PARENT: return new Parent(userId, name, email, childIds);
            default:     throw new InvalidRoleException(String.valueOf(role));
        }
    }
}

package com.transit.auth;

import com.transit.model.Role;

import java.util.List;

final class Credential {
    final String username;
    final String password;
    final Role role;
    final String userId;
    final String name;
    final String email;
    final String schoolId;
    final List<String> childIds;

    Credential(String username, String password, Role role, String userId,
               String name, String email, String schoolId, List<String> childIds) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.schoolId = schoolId;
        this.childIds = childIds;
    }
}

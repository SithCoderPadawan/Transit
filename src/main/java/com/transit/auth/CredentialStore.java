package com.transit.auth;

import com.transit.model.Role;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CredentialStore {
    private final Map<String, Credential> credentials = new HashMap<>();

    CredentialStore() {
        credentials.put("admin1", new Credential(
                "admin1", "admin123", Role.ADMIN, "U-001", "Priya Nair", "priya.nair@transit.local", null, null));
        credentials.put("j.matthews", new Credential(
                "j.matthews", "lea123", Role.LEA, "U-002", "Janet Matthews", "j.matthews@lea.gov.uk", null, null));
        credentials.put("school1", new Credential(
                "school1", "school123", Role.SCHOOL, "U-003", "Mr. Owusu", "owusu@northgatehigh.sch.uk", "SCH-01", null));
        credentials.put("parent1", new Credential(
                "parent1", "parent123", Role.PARENT, "U-004", "Aisha Khan", "aisha.khan@example.com", null, List.of("P-001")));
    }

    Credential find(String username) { return credentials.get(username); }
}

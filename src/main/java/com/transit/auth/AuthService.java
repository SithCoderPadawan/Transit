package com.transit.auth;

import com.transit.exception.AccountLockedException;
import com.transit.exception.InvalidCredentialsException;
import com.transit.exception.InvalidRoleException;
import com.transit.factory.UserFactory;
import com.transit.model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * STRUCTURAL DESIGN PATTERN: Facade.
 * Hides credential lookup, lockout tracking, and user creation behind
 * one login() call, so the UI layer never touches CredentialStore or
 * UserFactory directly.
 */
public class AuthService {
    private static final int MAX_FAILED_ATTEMPTS = 3;

    private final CredentialStore credentialStore = new CredentialStore();
    private final Map<String, Integer> failedAttempts = new HashMap<>();

    public User login(String username, String password)
            throws InvalidCredentialsException, AccountLockedException, InvalidRoleException {

        if (failedAttempts.getOrDefault(username, 0) >= MAX_FAILED_ATTEMPTS) {
            throw new AccountLockedException(username);
        }

        Credential credential = credentialStore.find(username);

        if (credential == null || !credential.password.equals(password)) {
            failedAttempts.merge(username, 1, Integer::sum);
            throw new InvalidCredentialsException(username);
        }

        failedAttempts.remove(username);

        return UserFactory.createUser(credential.role, credential.userId, credential.name,
                credential.email, credential.schoolId, credential.childIds);
    }

    public int getRemainingAttempts(String username) {
        return Math.max(0, MAX_FAILED_ATTEMPTS - failedAttempts.getOrDefault(username, 0));
    }
}

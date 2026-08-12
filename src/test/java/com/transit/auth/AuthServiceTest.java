package com.transit.auth;

import com.transit.exception.AccountLockedException;
import com.transit.exception.InvalidCredentialsException;
import com.transit.model.LEAOfficer;
import com.transit.model.Permission;
import com.transit.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() { authService = new AuthService(); }

    @Test
    void validLoginReturnsCorrectlyTypedUser() throws Exception {
        User user = authService.login("j.matthews", "lea123");
        assertInstanceOf(LEAOfficer.class, user);
        assertEquals("LEA Officer", user.getRoleLabel());
        assertTrue(user.hasPermission(Permission.MANAGE_ROUTES));
        assertTrue(user.hasPermission(Permission.EDIT_OWN_PUPIL_RECORDS)); // superset fix, req 2c
        assertFalse(user.hasPermission(Permission.CORRECT_ANY_DATA));
    }

    @Test
    void wrongPasswordThrowsInvalidCredentialsException() {
        assertThrows(InvalidCredentialsException.class,
                () -> authService.login("admin1", "wrong-password"));
    }

    @Test
    void unknownUsernameThrowsInvalidCredentialsException() {
        assertThrows(InvalidCredentialsException.class,
                () -> authService.login("nobody", "whatever"));
    }

    @Test
    void thirdConsecutiveFailureLocksAccount() {
        for (int i = 0; i < 3; i++) {
            assertThrows(InvalidCredentialsException.class,
                    () -> authService.login("parent1", "wrong-password"));
        }
        assertThrows(AccountLockedException.class,
                () -> authService.login("parent1", "parent123"));
    }

    @Test
    void remainingAttemptsCountsDownCorrectly() {
        assertEquals(3, authService.getRemainingAttempts("school1"));
        assertThrows(InvalidCredentialsException.class,
                () -> authService.login("school1", "wrong-password"));
        assertEquals(2, authService.getRemainingAttempts("school1"));
    }
}

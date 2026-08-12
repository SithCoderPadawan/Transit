package com.transit.exception;

public class AccountLockedException extends Exception {
    public AccountLockedException(String username) {
        super("Account '" + username + "' is locked after too many failed attempts.");
    }
}

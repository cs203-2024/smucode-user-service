package com.cs203.smucode.models;

public enum UserRole {
    PLAYER("ROLE_PLAYER"),
    ADMIN("ROLE_ADMIN");

    private final String authority;

    UserRole(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }

    @Override
    public String toString() {
        return this.authority;
    }
}

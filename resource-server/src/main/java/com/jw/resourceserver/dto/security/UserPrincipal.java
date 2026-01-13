package com.jw.resourceserver.dto.security;


public abstract class UserPrincipal {
    public abstract boolean isAuthenticated();

    public abstract String getIdentifier();

    public abstract UserType getUserType();
}

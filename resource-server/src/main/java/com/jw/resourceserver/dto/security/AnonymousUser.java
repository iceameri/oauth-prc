package com.jw.resourceserver.dto.security;

public class AnonymousUser extends UserPrincipal {
    private static final AnonymousUser INSTANCE = new AnonymousUser();

    private AnonymousUser() {
    }

    public static AnonymousUser getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }

    @Override
    public String getIdentifier() {
        return "anonymous";
    }

    @Override
    public UserType getUserType() {
        return UserType.ANONYMOUS;
    }
}


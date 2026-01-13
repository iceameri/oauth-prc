package com.jw.resourceserver.dto.security;

import lombok.Getter;

public class AuthenticatedUser extends UserPrincipal {
    @Getter
    private final String socialUserId;
    @Getter
    private final String email;
    @Getter
    private final String username;
    private final UserType userType;

    public AuthenticatedUser(final String socialUserId, final String email, final String username, final UserType userType) {
        this.socialUserId = socialUserId;
        this.email = email;
        this.username = username;
        this.userType = userType;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return socialUserId;
    }

    @Override
    public UserType getUserType() {
        return userType;
    }

    // 비즈니스 메서드
    public boolean canAccessResource(String resourceId) {
        return isAuthenticated() && resourceId != null;
    }

    public boolean hasRole(UserRole role) {
        return userType.hasRole(role);
    }
}


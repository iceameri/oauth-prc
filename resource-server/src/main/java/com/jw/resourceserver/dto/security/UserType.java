package com.jw.resourceserver.dto.security;

import lombok.Getter;

import java.util.Collections;
import java.util.Set;

@Getter
public enum UserType {
    SOCIAL(Set.of(UserRole.USER)),
    ADMIN(Set.of(UserRole.USER, UserRole.ADMIN)),
    ANONYMOUS(Set.of());

    private final Set<UserRole> roles;

    UserType(Set<UserRole> roles) {
        this.roles = Collections.unmodifiableSet(roles);
    }

    public boolean hasRole(UserRole role) {
        return roles.contains(role);
    }

}
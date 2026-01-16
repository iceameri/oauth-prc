package com.jw.authorizationserver.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Builder
public record StoredAuthorization(
        String id,
        String registeredClientId,
        String principalName,
        String authorizationGrantType,
        Set<String> authorizedScopes,
        Map<String, Object> attributes,
        String state,
        Token authorizationCode,
        Token accessToken,
        Token refreshToken,
        Token oidcIdToken,
        Token userCode,
        Token deviceCode
) {
    @Builder
    public record Token(
            String tokenValue,
            Instant issuedAt,
            Instant expiresAt,
            Map<String, Object> metadata,
            Set<String> scopes,
            String tokenType
    ) {
    }
}

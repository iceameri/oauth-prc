package com.jw.resourceserver.dto;

public record OAuth2TokenResponse(
        String access_token,
        String refresh_token,
        String scope,
        String id_token,
        String token_type,
        int expires_in
) {
}

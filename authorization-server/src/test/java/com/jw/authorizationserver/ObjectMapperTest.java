package com.jw.authorizationserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jw.authorizationserver.dto.StoredAuthorization;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectMapperTest {

    @Disabled
    @Test
    public void testStoredAuthorizationSerialization() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        StoredAuthorization dto = StoredAuthorization.builder()
                .id("test-id")
                .registeredClientId("client-1")
                .principalName("user1")
                .authorizationGrantType("authorization_code")
                .authorizedScopes(Set.of("read", "write"))
                .attributes(Map.of("attr1", "val1"))
                .accessToken(StoredAuthorization.Token.builder()
                        .tokenValue("access-token-123")
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(3600))
                        .tokenType("Bearer")
                        .scopes(Set.of("read"))
                        .build())
                .build();

        String json = objectMapper.writeValueAsString(dto);
        System.out.println("[DEBUG_LOG] StoredAuthorization JSON: " + json);

        StoredAuthorization deserialized = objectMapper.readValue(json, StoredAuthorization.class);

        assertThat(deserialized.getId()).isEqualTo(dto.getId());
        assertThat(deserialized.getAccessToken().getTokenValue()).isEqualTo(dto.getAccessToken().getTokenValue());
        assertThat(deserialized.getAuthorizedScopes()).containsAll(dto.getAuthorizedScopes());
    }
}

package com.jw.authorizationserver.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2DeviceCode;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2UserCode;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
{
  "@class" : "org.springframework.security.oauth2.server.authorization.OAuth2Authorization",
  "id" : "4e73d762-2c81-4f4f-9b53-9d31710b9b65",
  "registeredClientId" : "1",
  "principalName" : "qwer",
  "authorizationGrantType" : {
    "value" : "authorization_code"
  },
  "authorizedScopes" : [ "java.util.Collections$UnmodifiableSet", [ "openid", "profile", "email" ] ],
  "attributes" : {
    "@class" : "java.util.Collections$UnmodifiableMap",
    "java.security.Principal" : {
      "@class" : "org.springframework.security.authentication.UsernamePasswordAuthenticationToken",
      "authorities" : [ "java.util.Collections$UnmodifiableRandomAccessList", [ {
        "@class" : "org.springframework.security.core.authority.SimpleGrantedAuthority",
        "authority" : "ROLE_USER"
      } ] ],
      "details" : {
        "@class" : "org.springframework.security.web.authentication.WebAuthenticationDetails",
        "remoteAddress" : "0:0:0:0:0:0:0:1",
        "sessionId" : "A6836256D6281EDF15B84B6F63B93AB5"
      },
      "authenticated" : true,
      "principal" : {
        "@class" : "org.springframework.security.core.userdetails.User",
        "password" : null,
        "username" : "qwer",
        "authorities" : [ "java.util.Collections$UnmodifiableSet", [ {
          "@class" : "org.springframework.security.core.authority.SimpleGrantedAuthority",
          "authority" : "ROLE_USER"
        } ] ],
        "accountNonExpired" : true,
        "accountNonLocked" : true,
        "credentialsNonExpired" : true,
        "enabled" : true
      },
      "credentials" : null
    },
    "org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest" : {
      "@class" : "org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest",
      "authorizationUri" : "http://localhost:9090/oauth2/authorize",
      "authorizationGrantType" : {
        "value" : "authorization_code"
      },
      "responseType" : {
        "value" : "code"
      },
      "clientId" : "test-client",
      "redirectUri" : "http://localhost:9090/oauth2/token",
      "scopes" : [ "java.util.Collections$UnmodifiableSet", [ "openid", "profile", "email" ] ],
      "state" : null,
      "additionalParameters" : {
        "@class" : "java.util.Collections$UnmodifiableMap",
        "continue" : ""
      },
      "authorizationRequestUri" : "http://localhost:9090/oauth2/authorize?response_type=code&client_id=test-client&scope=openid%20profile%20email&redirect_uri=http://localhost:9090/oauth2/token&continue=",
      "attributes" : {
        "@class" : "java.util.Collections$UnmodifiableMap"
      }
    }
  },
  "refreshToken" : null,
  "accessToken" : null
}
*/
public class OAuth2AuthorizationDeserializer
        extends JsonDeserializer<OAuth2Authorization> {

    private final RegisteredClientRepository clientRepository;

    public OAuth2AuthorizationDeserializer(
            RegisteredClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public OAuth2Authorization deserialize(
            JsonParser p,
            DeserializationContext ctxt
    ) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        ObjectNode node = mapper.readTree(p);

        String id = node.get("id").asText();
        String registeredClientId =
                node.get("registeredClientId").asText();
        String principalName =
                node.get("principalName").asText();

        RegisteredClient client =
                clientRepository.findById(registeredClientId);

        OAuth2Authorization.Builder builder =
                OAuth2Authorization.withRegisteredClient(client)
                        .id(id)
                        .principalName(principalName);

        // grant type
        if (node.has("authorizationGrantType")) {
            JsonNode grantTypeNode = node.get("authorizationGrantType");
            String grantTypeValue = grantTypeNode.isObject() ? grantTypeNode.get("value").asText() : grantTypeNode.asText();
            builder.authorizationGrantType(new AuthorizationGrantType(grantTypeValue));
        }

        // scopes
        if (node.has("authorizedScopes")) {
            Set<String> scopes = new HashSet<>();
            JsonNode scopesNode = node.get("authorizedScopes");

            // Jackson default typing 구조 대응
            if (scopesNode.isArray() && scopesNode.size() == 2 && scopesNode.get(1).isArray()) {
                // [ "java.util.Collections$UnmodifiableSet", [ "openid", ... ] ]
                scopesNode.get(1).forEach(scopeNode ->
                        scopes.add(scopeNode.asText())
                );
            }
            // 혹시라도 평범한 배열로 오는 경우
            else if (scopesNode.isArray()) {
                scopesNode.forEach(scopeNode ->
                        scopes.add(scopeNode.asText())
                );
            }

            builder.authorizedScopes(scopes);
        }

        // attributes
        if (node.has("attributes")) {
            Map<String, Object> attrs = mapper.convertValue(node.get("attributes"), new TypeReference<Map<String, Object>>() {});
            builder.attributes(a -> a.putAll(attrs));
        }

        // Tokens
        if (node.has("tokens")) {
            JsonNode tokensNode = node.get("tokens");
            
            // Authorization Code
            if (tokensNode.has(OAuth2AuthorizationCode.class.getName())) {
                JsonNode tokenNode = tokensNode.get(OAuth2AuthorizationCode.class.getName());
                OAuth2AuthorizationCode token = deserializeToken(tokenNode, mapper, OAuth2AuthorizationCode.class);
                Map<String, Object> metadata = deserializeMetadata(tokenNode.get("metadata"), mapper);
                builder.token(token, (m) -> m.putAll(metadata));
            } else if (node.has("authorizationCode")) {
                JsonNode tokenNode = node.get("authorizationCode");
                if (tokenNode != null && !tokenNode.isNull()) {
                    OAuth2AuthorizationCode token = deserializeToken(tokenNode, mapper, OAuth2AuthorizationCode.class);
                    Map<String, Object> metadata = deserializeMetadata(tokenNode.get("metadata"), mapper);
                    builder.token(token, (m) -> m.putAll(metadata));
                }
            }

            // Access Token
            if (tokensNode.has(OAuth2AccessToken.class.getName())) {
                JsonNode tokenNode = tokensNode.get(OAuth2AccessToken.class.getName());
                OAuth2AccessToken token = deserializeAccessToken(tokenNode, mapper);
                Map<String, Object> metadata = deserializeMetadata(tokenNode.get("metadata"), mapper);
                builder.token(token, (m) -> m.putAll(metadata));
            } else if (node.has("accessToken")) {
                JsonNode tokenNode = node.get("accessToken");
                if (tokenNode != null && !tokenNode.isNull()) {
                    OAuth2AccessToken token = deserializeAccessToken(tokenNode, mapper);
                    Map<String, Object> metadata = deserializeMetadata(tokenNode.get("metadata"), mapper);
                    builder.token(token, (m) -> m.putAll(metadata));
                }
            }

            // Refresh Token
            if (tokensNode.has(OAuth2RefreshToken.class.getName())) {
                JsonNode tokenNode = tokensNode.get(OAuth2RefreshToken.class.getName());
                OAuth2RefreshToken token = deserializeRefreshToken(tokenNode, mapper);
                Map<String, Object> metadata = deserializeMetadata(tokenNode.get("metadata"), mapper);
                builder.token(token, (m) -> m.putAll(metadata));
            } else if (node.has("refreshToken")) {
                JsonNode tokenNode = node.get("refreshToken");
                if (tokenNode != null && !tokenNode.isNull()) {
                    OAuth2RefreshToken token = deserializeRefreshToken(tokenNode, mapper);
                    Map<String, Object> metadata = deserializeMetadata(tokenNode.get("metadata"), mapper);
                    builder.token(token, (m) -> m.putAll(metadata));
                }
            }

            // OIDC ID Token
            if (tokensNode.has(OidcIdToken.class.getName())) {
                JsonNode tokenNode = tokensNode.get(OidcIdToken.class.getName());
                OidcIdToken token = deserializeOidcIdToken(tokenNode, mapper);
                Map<String, Object> metadata = deserializeMetadata(tokenNode.get("metadata"), mapper);
                builder.token(token, (m) -> m.putAll(metadata));
            } else if (node.has("oidcIdToken")) {
                JsonNode tokenNode = node.get("oidcIdToken");
                if (tokenNode != null && !tokenNode.isNull()) {
                    OidcIdToken token = deserializeOidcIdToken(tokenNode, mapper);
                    Map<String, Object> metadata = deserializeMetadata(tokenNode.get("metadata"), mapper);
                    builder.token(token, (m) -> m.putAll(metadata));
                }
            }

            // User Code
            if (tokensNode.has(OAuth2UserCode.class.getName())) {
                JsonNode tokenNode = tokensNode.get(OAuth2UserCode.class.getName());
                OAuth2UserCode token = deserializeToken(tokenNode, mapper, OAuth2UserCode.class);
                Map<String, Object> metadata = deserializeMetadata(tokenNode.get("metadata"), mapper);
                builder.token(token, (m) -> m.putAll(metadata));
            } else if (node.has("userCode")) {
                JsonNode tokenNode = node.get("userCode");
                if (tokenNode != null && !tokenNode.isNull()) {
                    OAuth2UserCode token = deserializeToken(tokenNode, mapper, OAuth2UserCode.class);
                    Map<String, Object> metadata = deserializeMetadata(tokenNode.get("metadata"), mapper);
                    builder.token(token, (m) -> m.putAll(metadata));
                }
            }

            // Device Code
            if (tokensNode.has(OAuth2DeviceCode.class.getName())) {
                JsonNode tokenNode = tokensNode.get(OAuth2DeviceCode.class.getName());
                OAuth2DeviceCode token = deserializeToken(tokenNode, mapper, OAuth2DeviceCode.class);
                Map<String, Object> metadata = deserializeMetadata(tokenNode.get("metadata"), mapper);
                builder.token(token, (m) -> m.putAll(metadata));
            } else if (node.has("deviceCode")) {
                JsonNode tokenNode = node.get("deviceCode");
                if (tokenNode != null && !tokenNode.isNull()) {
                    OAuth2DeviceCode token = deserializeToken(tokenNode, mapper, OAuth2DeviceCode.class);
                    Map<String, Object> metadata = deserializeMetadata(tokenNode.get("metadata"), mapper);
                    builder.token(token, (m) -> m.putAll(metadata));
                }
            }
        } else {
            // 루트 레벨에 토큰이 있는 경우 처리
            if (node.has("authorizationCode")) {
                JsonNode tokenNode = node.get("authorizationCode");
                if (tokenNode != null && !tokenNode.isNull()) {
                    OAuth2AuthorizationCode token = deserializeToken(tokenNode, mapper, OAuth2AuthorizationCode.class);
                    Map<String, Object> metadata = deserializeMetadata(tokenNode.get("metadata"), mapper);
                    builder.token(token, (m) -> m.putAll(metadata));
                }
            }
            if (node.has("accessToken")) {
                JsonNode tokenNode = node.get("accessToken");
                if (tokenNode != null && !tokenNode.isNull()) {
                    OAuth2AccessToken token = deserializeAccessToken(tokenNode, mapper);
                    Map<String, Object> metadata = deserializeMetadata(tokenNode.get("metadata"), mapper);
                    builder.token(token, (m) -> m.putAll(metadata));
                }
            }
            if (node.has("refreshToken")) {
                JsonNode tokenNode = node.get("refreshToken");
                if (tokenNode != null && !tokenNode.isNull()) {
                    OAuth2RefreshToken token = deserializeRefreshToken(tokenNode, mapper);
                    Map<String, Object> metadata = deserializeMetadata(tokenNode.get("metadata"), mapper);
                    builder.token(token, (m) -> m.putAll(metadata));
                }
            }
            if (node.has("oidcIdToken")) {
                JsonNode tokenNode = node.get("oidcIdToken");
                if (tokenNode != null && !tokenNode.isNull()) {
                    OidcIdToken token = deserializeOidcIdToken(tokenNode, mapper);
                    Map<String, Object> metadata = deserializeMetadata(tokenNode.get("metadata"), mapper);
                    builder.token(token, (m) -> m.putAll(metadata));
                }
            }
            if (node.has("userCode")) {
                JsonNode tokenNode = node.get("userCode");
                if (tokenNode != null && !tokenNode.isNull()) {
                    OAuth2UserCode token = deserializeToken(tokenNode, mapper, OAuth2UserCode.class);
                    Map<String, Object> metadata = deserializeMetadata(tokenNode.get("metadata"), mapper);
                    builder.token(token, (m) -> m.putAll(metadata));
                }
            }
            if (node.has("deviceCode")) {
                JsonNode tokenNode = node.get("deviceCode");
                if (tokenNode != null && !tokenNode.isNull()) {
                    OAuth2DeviceCode token = deserializeToken(tokenNode, mapper, OAuth2DeviceCode.class);
                    Map<String, Object> metadata = deserializeMetadata(tokenNode.get("metadata"), mapper);
                    builder.token(token, (m) -> m.putAll(metadata));
                }
            }
        }

        return builder.build();
    }

    private <T> T deserializeToken(JsonNode tokenNode, ObjectMapper mapper, Class<T> tokenClass) {
        JsonNode innerToken = tokenNode.get("token");
        return mapper.convertValue(innerToken, tokenClass);
    }

    private OAuth2AccessToken deserializeAccessToken(JsonNode tokenNode, ObjectMapper mapper) {
        JsonNode innerToken = tokenNode.get("token");
        String tokenValue = innerToken.get("tokenValue").asText();
        Instant issuedAt = Instant.parse(innerToken.get("issuedAt").asText());
        Instant expiresAt = Instant.parse(innerToken.get("expiresAt").asText());
        Set<String> scopes = new HashSet<>();
        innerToken.get("scopes").forEach(n -> scopes.add(n.asText()));
        
        String tokenType = innerToken.get("tokenType").isObject() 
                ? innerToken.get("tokenType").get("value").asText() 
                : innerToken.get("tokenType").asText();
        OAuth2AccessToken.TokenType type = OAuth2AccessToken.TokenType.BEARER;
        if (OAuth2AccessToken.TokenType.BEARER.getValue().equalsIgnoreCase(tokenType)) {
            type = OAuth2AccessToken.TokenType.BEARER;
        }

        return new OAuth2AccessToken(type, tokenValue, issuedAt, expiresAt, scopes);
    }

    private OAuth2RefreshToken deserializeRefreshToken(JsonNode tokenNode, ObjectMapper mapper) {
        JsonNode innerToken = tokenNode.get("token");
        String tokenValue = innerToken.get("tokenValue").asText();
        Instant issuedAt = Instant.parse(innerToken.get("issuedAt").asText());
        Instant expiresAt = innerToken.has("expiresAt") && !innerToken.get("expiresAt").isNull() 
                ? Instant.parse(innerToken.get("expiresAt").asText()) : null;
        
        return new OAuth2RefreshToken(tokenValue, issuedAt, expiresAt);
    }

    private OidcIdToken deserializeOidcIdToken(JsonNode tokenNode, ObjectMapper mapper) {
        JsonNode innerToken = tokenNode.get("token");
        String tokenValue = innerToken.get("tokenValue").asText();
        Instant issuedAt = Instant.parse(innerToken.get("issuedAt").asText());
        Instant expiresAt = Instant.parse(innerToken.get("expiresAt").asText());
        Map<String, Object> claims = mapper.convertValue(innerToken.get("claims"), new TypeReference<Map<String, Object>>() {});
        
        return new OidcIdToken(tokenValue, issuedAt, expiresAt, claims);
    }

    private Map<String, Object> deserializeMetadata(JsonNode metadataNode, ObjectMapper mapper) {
        return mapper.convertValue(metadataNode, new TypeReference<Map<String, Object>>() {});
    }
}
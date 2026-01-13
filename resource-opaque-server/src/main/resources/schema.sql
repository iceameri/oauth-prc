USE oauth;
INSERT INTO oauth2_registered_client (id,
                                      client_id,
                                      client_id_issued_at,
                                      client_secret,
                                      client_name,
                                      client_authentication_methods,
                                      authorization_grant_types,
                                      redirect_uris,
                                      scopes,
                                      client_settings,
                                      token_settings)
VALUES
(
 '2',
 'opaque-client',
 GETDATE(),
 '{bcrypt}$2a$10$vNyUhWG2.Gd70U4zW9runOVnKaNmY9/DxcAdLweXqHch48eEYdO7i', -- 비번: P@$$w0rd1!
 'opaque-client-service',
 'client_secret_basic,client_secret_post',
 'authorization_code,refresh_token,client_credentials',
 'http://localhost:9090/oauth2/authorize,http://localhost:9090/oauth2/token,http://localhost:9090/auth/authorized',
 'openid,profile,email',
 '{"@class":"java.util.Collections$UnmodifiableMap",
   "settings.client.require-proof-key":false,
   "settings.client.require-authorization-consent":false
  }',
    -- ▼ token_settings: access token 형식 = opaque
 '{
     "@class": "java.util.Collections$UnmodifiableMap",
     "settings.token.authorization-code-time-to-live": ["java.time.Duration", 300.000000000],
     "settings.token.access-token-time-to-live": ["java.time.Duration", 1800.000000000],
     "settings.token.refresh-token-time-to-live": ["java.time.Duration", 86400.000000000],
     "settings.token.reuse-refresh-tokens": ["java.lang.Boolean", false],
     "settings.token.device-code-time-to-live": ["java.time.Duration", 300.000000000],
     "settings.token.access-token-format": ["org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat", "reference"]
  }'
);
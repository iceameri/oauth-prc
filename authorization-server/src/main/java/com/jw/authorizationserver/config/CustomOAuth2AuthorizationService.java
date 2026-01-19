package com.jw.authorizationserver.config;

import com.jw.authorizationserver.constants.BeanNameConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2DeviceCode;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.OAuth2UserCode;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class CustomOAuth2AuthorizationService extends JdbcOAuth2AuthorizationService {

    private static final String PREFIX = "oauth2:auth:";
    private static final String STATE = "state:";
    private static final String CODE = "code:";
    private static final String ACCESS = "access:";
    private static final String REFRESH = "refresh:";
    private static final String USER_CODE = "user:";
    private static final String DEVICE_CODE = "device:";

    private final RedisTemplate<String, String> redisTemplate;

    public CustomOAuth2AuthorizationService(
            @Qualifier(BeanNameConstants.OAUTH_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository,
            RedisTemplate<String, String> redisTemplate
    ) {
        super(jdbcTemplate, registeredClientRepository);
        this.redisTemplate = redisTemplate;
    }

    // =========================
    // Save
    // =========================
    @Override
    public void save(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");

        // 1️⃣ DB 저장 (부모 로직)
        super.save(authorization);

        // 2️⃣ 캐시 가능 상태만 Redis 인덱싱
        if (!this.isCacheable(authorization)) {
            return;
        }

        long ttl = this.resolveTtlSeconds(authorization);
        String id = authorization.getId();

        this.cacheIndex(STATE,
                authorization.getAttribute(OAuth2ParameterNames.STATE),
                id, ttl);

        this.cacheToken(CODE,
                authorization.getToken(OAuth2AuthorizationCode.class),
                id, ttl);

        this.cacheAccessToken(authorization, id, ttl);
        this.cacheRefreshToken(authorization, id, ttl);

        this.cacheToken(USER_CODE,
                authorization.getToken(OAuth2UserCode.class),
                id, ttl);

        this.cacheToken(DEVICE_CODE,
                authorization.getToken(OAuth2DeviceCode.class),
                id, ttl);
    }

    // =========================
    // Remove
    // =========================
    @Override
    public void remove(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");

        this.deleteIndex(STATE,
                authorization.getAttribute(OAuth2ParameterNames.STATE));

        this.deleteToken(CODE,
                authorization.getToken(OAuth2AuthorizationCode.class));

        this.deleteAccessToken(authorization);
        this.deleteRefreshToken(authorization);

        this.deleteToken(USER_CODE,
                authorization.getToken(OAuth2UserCode.class));

        this.deleteToken(DEVICE_CODE,
                authorization.getToken(OAuth2DeviceCode.class));

        super.remove(authorization);
    }

    // =========================
    // Find
    // =========================
    @Override
    @Nullable
    public OAuth2Authorization findByToken(String token, @Nullable OAuth2TokenType tokenType) {
        Assert.hasText(token, "token cannot be empty");

        String key = resolveKey(token, tokenType);
        if (key != null) {
            String id = redisTemplate.opsForValue().get(key);
            if (id != null) {
                OAuth2Authorization authorization = super.findById(id);
                if (authorization != null) {
                    return authorization;
                }
            }
        }
        return super.findByToken(token, tokenType);
    }

    // =========================
    // Cache rules
    // =========================
    private boolean isCacheable(OAuth2Authorization authorization) {
        AuthorizationGrantType grantType = authorization.getAuthorizationGrantType();

        if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(grantType)) {
            return authorization.getToken(OAuth2AuthorizationCode.class) != null
                    || authorization.getAccessToken() != null;
        }
        return authorization.getAccessToken() != null;
    }

    private long resolveTtlSeconds(OAuth2Authorization authorization) {
        Instant now = Instant.now();
        Instant expires = now.plus(Duration.ofHours(1));

        if (authorization.getAccessToken() != null &&
                authorization.getAccessToken().getToken().getExpiresAt() != null) {
            expires = authorization.getAccessToken().getToken().getExpiresAt();
        }
        if (authorization.getRefreshToken() != null &&
                authorization.getRefreshToken().getToken().getExpiresAt() != null &&
                authorization.getRefreshToken().getToken().getExpiresAt().isAfter(expires)) {
            expires = authorization.getRefreshToken().getToken().getExpiresAt();
        }

        return Math.max(Duration.between(now, expires).getSeconds(), 3600);
    }

    // =========================
    // Redis helpers
    // =========================
    private void cacheIndex(String prefix, String value, String id, long ttl) {
        if (value != null) {
            redisTemplate.opsForValue().set(
                    PREFIX + prefix + value,
                    id,
                    ttl,
                    TimeUnit.SECONDS
            );
        }
    }

    private <T extends OAuth2Token> void cacheToken(
            String prefix,
            OAuth2Authorization.Token<T> token,
            String id,
            long ttl
    ) {
        if (token != null && token.getToken() != null) {
            redisTemplate.opsForValue().set(
                    PREFIX + prefix + token.getToken().getTokenValue(),
                    id,
                    ttl,
                    TimeUnit.SECONDS
            );
        }
    }

    private void cacheAccessToken(OAuth2Authorization authorization, String id, long ttl) {
        if (authorization.getAccessToken() != null) {
            redisTemplate.opsForValue().set(
                    PREFIX + ACCESS + authorization.getAccessToken().getToken().getTokenValue(),
                    id,
                    ttl,
                    TimeUnit.SECONDS
            );
        }
    }

    private void cacheRefreshToken(OAuth2Authorization authorization, String id, long ttl) {
        if (authorization.getRefreshToken() != null) {
            redisTemplate.opsForValue().set(
                    PREFIX + REFRESH + authorization.getRefreshToken().getToken().getTokenValue(),
                    id,
                    ttl,
                    TimeUnit.SECONDS
            );
        }
    }

    private void deleteIndex(String prefix, String value) {
        if (value != null) {
            redisTemplate.delete(PREFIX + prefix + value);
        }
    }

    private <T extends OAuth2Token> void deleteToken(
            String prefix,
            OAuth2Authorization.Token<T> token
    ) {
        if (token != null && token.getToken() != null) {
            redisTemplate.delete(
                    PREFIX + prefix + token.getToken().getTokenValue()
            );
        }
    }

    private void deleteAccessToken(OAuth2Authorization authorization) {
        if (authorization.getAccessToken() != null) {
            redisTemplate.delete(
                    PREFIX + ACCESS +
                            authorization.getAccessToken().getToken().getTokenValue()
            );
        }
    }

    private void deleteRefreshToken(OAuth2Authorization authorization) {
        if (authorization.getRefreshToken() != null) {
            redisTemplate.delete(
                    PREFIX + REFRESH +
                            authorization.getRefreshToken().getToken().getTokenValue()
            );
        }
    }

    private String resolveKey(String token, OAuth2TokenType tokenType) {
        if (tokenType == null) return null;

        if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
            return PREFIX + STATE + token;
        }
        if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
            return PREFIX + CODE + token;
        }
        if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            return PREFIX + ACCESS + token;
        }
        if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            return PREFIX + REFRESH + token;
        }
        if (OAuth2ParameterNames.USER_CODE.equals(tokenType.getValue())) {
            return PREFIX + USER_CODE + token;
        }
        if (OAuth2ParameterNames.DEVICE_CODE.equals(tokenType.getValue())) {
            return PREFIX + DEVICE_CODE + token;
        }
        return null;
    }
}

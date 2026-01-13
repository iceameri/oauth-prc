package com.jw.authorizationserver.config;

import com.jw.authorizationserver.constants.BeanNameConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class CustomOAuth2AuthorizationService extends JdbcOAuth2AuthorizationService {//JdbcOAuth2AuthorizationService

    private final JdbcTemplate oauthJdbcTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RegisteredClientRepository registeredClientRepository;

    private static final String AUTHORIZATION_KEY = "oauth2_authorization:";

    public CustomOAuth2AuthorizationService(
            @Qualifier(BeanNameConstants.OAUTH_JDBC_TEMPLATE) JdbcTemplate oauthJdbcTemplate,
            RedisTemplate<String, Object> redisTemplate,
            @Qualifier(BeanNameConstants.OAUTH2_OBJECT_MAPPER) ObjectMapper objectMapper,
            RegisteredClientRepository registeredClientRepository
    ) {
        super(oauthJdbcTemplate, registeredClientRepository);
        this.oauthJdbcTemplate = oauthJdbcTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.registeredClientRepository = registeredClientRepository;

        // JdbcOAuth2AuthorizationService Mapper 설정
        JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper rowMapper =
                new JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper(registeredClientRepository);
        rowMapper.setObjectMapper(objectMapper);

        JdbcOAuth2AuthorizationService.OAuth2AuthorizationParametersMapper parametersMapper =
                new JdbcOAuth2AuthorizationService.OAuth2AuthorizationParametersMapper();
        parametersMapper.setObjectMapper(objectMapper);

        this.setAuthorizationRowMapper(rowMapper);
        this.setAuthorizationParametersMapper(parametersMapper);
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        String json = this.serialize(authorization);
        String key = AUTHORIZATION_KEY + authorization.getId();

        // 엑세스 토큰 또는 리프레시 토큰의 만료 시간 중 가장 긴 시간을 TTL로 설정
        long timeout = this.getTimeout(authorization);
        redisTemplate.opsForValue().set(key, json, timeout, TimeUnit.SECONDS);

        // 토큰별 인덱스 생성
        if (authorization.getAttribute(OAuth2ParameterNames.STATE) != null) {
            String state = authorization.getAttribute(OAuth2ParameterNames.STATE);
            redisTemplate.opsForValue().set(AUTHORIZATION_KEY + "state:" + state, authorization.getId(), timeout, TimeUnit.SECONDS);
        }
        OAuth2Authorization.Token<org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode> authorizationCode =
                authorization.getToken(org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode.class);
        if (authorizationCode != null) {
            redisTemplate.opsForValue().set(AUTHORIZATION_KEY + "code:" + authorizationCode.getToken().getTokenValue(), authorization.getId(), timeout, TimeUnit.SECONDS);
        }
        if (authorization.getAccessToken() != null) {
            String accessToken = authorization.getAccessToken().getToken().getTokenValue();
            redisTemplate.opsForValue().set(AUTHORIZATION_KEY + "access_token:" + accessToken, authorization.getId(), timeout, TimeUnit.SECONDS);
        }
        if (authorization.getRefreshToken() != null) {
            String refreshToken = authorization.getRefreshToken().getToken().getTokenValue();
            redisTemplate.opsForValue().set(AUTHORIZATION_KEY + "refresh_token:" + refreshToken, authorization.getId(), timeout, TimeUnit.SECONDS);
        }
        OAuth2Authorization.Token<org.springframework.security.oauth2.core.OAuth2UserCode> userCode =
                authorization.getToken(org.springframework.security.oauth2.core.OAuth2UserCode.class);
        if (userCode != null) {
            redisTemplate.opsForValue().set(AUTHORIZATION_KEY + "user_code:" + userCode.getToken().getTokenValue(), authorization.getId(), timeout, TimeUnit.SECONDS);
        }
        OAuth2Authorization.Token<org.springframework.security.oauth2.core.OAuth2DeviceCode> deviceCode =
                authorization.getToken(org.springframework.security.oauth2.core.OAuth2DeviceCode.class);
        if (deviceCode != null) {
            redisTemplate.opsForValue().set(AUTHORIZATION_KEY + "device_code:" + deviceCode.getToken().getTokenValue(), authorization.getId(), timeout, TimeUnit.SECONDS);
        }

        super.save(authorization);
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        super.remove(authorization);

        Assert.notNull(authorization, "authorization cannot be null");
        String key = AUTHORIZATION_KEY + authorization.getId();
        redisTemplate.delete(key);

        if (authorization.getAttribute(OAuth2ParameterNames.STATE) != null) {
            redisTemplate.delete(AUTHORIZATION_KEY + "state:" + (String) authorization.getAttribute(OAuth2ParameterNames.STATE));
        }
        OAuth2Authorization.Token<org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode> authorizationCode =
                authorization.getToken(org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode.class);
        if (authorizationCode != null) {
            redisTemplate.delete(AUTHORIZATION_KEY + "code:" + authorizationCode.getToken().getTokenValue());
        }
        if (authorization.getAccessToken() != null) {
            redisTemplate.delete(AUTHORIZATION_KEY + "access_token:" + authorization.getAccessToken().getToken().getTokenValue());
        }
        if (authorization.getRefreshToken() != null) {
            redisTemplate.delete(AUTHORIZATION_KEY + "refresh_token:" + authorization.getRefreshToken().getToken().getTokenValue());
        }
        OAuth2Authorization.Token<org.springframework.security.oauth2.core.OAuth2UserCode> userCode =
                authorization.getToken(org.springframework.security.oauth2.core.OAuth2UserCode.class);
        if (userCode != null) {
            redisTemplate.delete(AUTHORIZATION_KEY + "user_code:" + userCode.getToken().getTokenValue());
        }
        OAuth2Authorization.Token<org.springframework.security.oauth2.core.OAuth2DeviceCode> deviceCode =
                authorization.getToken(org.springframework.security.oauth2.core.OAuth2DeviceCode.class);
        if (deviceCode != null) {
            redisTemplate.delete(AUTHORIZATION_KEY + "device_code:" + deviceCode.getToken().getTokenValue());
        }
    }

    @Nullable
    @Override
    public OAuth2Authorization findById(String id) {
        Assert.hasText(id, "id cannot be empty");
        String json = (String) redisTemplate.opsForValue().get(AUTHORIZATION_KEY + id);
        if (json == null) {
            super.findById(id);
        }
        return this.deserialize(json);
    }

    @Nullable
    @Override
    public OAuth2Authorization findByToken(String token, @Nullable OAuth2TokenType tokenType) {
        Assert.hasText(token, "token cannot be empty");
        String indexKey;
        if (tokenType == null) {
            indexKey = this.findTokenIndex(token);
        } else if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
            indexKey = AUTHORIZATION_KEY + "state:" + token;
        } else if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
            indexKey = AUTHORIZATION_KEY + "code:" + token;
        } else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            indexKey = AUTHORIZATION_KEY + "access_token:" + token;
        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            indexKey = AUTHORIZATION_KEY + "refresh_token:" + token;
        } else if (OAuth2ParameterNames.USER_CODE.equals(tokenType.getValue())) {
            indexKey = AUTHORIZATION_KEY + "user_code:" + token;
        } else if (OAuth2ParameterNames.DEVICE_CODE.equals(tokenType.getValue())) {
            indexKey = AUTHORIZATION_KEY + "device_code:" + token;
        } else {
            return null;
        }

        if (indexKey == null) {
            return null;
        }

        String id = (String) redisTemplate.opsForValue().get(indexKey);
        return id != null ? this.findById(id) : null;
    }

    private String findTokenIndex(String token) {
        String[] types = {"code:", "access_token:", "refresh_token:", "state:", "user_code:", "device_code:"};
        for (String type : types) {
            String id = (String) redisTemplate.opsForValue().get(AUTHORIZATION_KEY + type + token);
            if (id != null) {
                return AUTHORIZATION_KEY + type + token;
            }
        }
        return null;
    }

    private String serialize(OAuth2Authorization authorization) {
        try {
            return objectMapper.writeValueAsString(authorization);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private OAuth2Authorization deserialize(String json) {
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, OAuth2Authorization.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private long getTimeout(OAuth2Authorization authorization) {
        Instant expiresAt = Instant.now().plus(Duration.ofDays(1)); // 기본 1일

        if (authorization.getAccessToken() != null && authorization.getAccessToken().getToken().getExpiresAt() != null) {
            expiresAt = authorization.getAccessToken().getToken().getExpiresAt();
        }
        if (authorization.getRefreshToken() != null && authorization.getRefreshToken().getToken().getExpiresAt() != null) {
            Instant refreshExpiresAt = authorization.getRefreshToken().getToken().getExpiresAt();
            if (refreshExpiresAt.isAfter(expiresAt)) {
                expiresAt = refreshExpiresAt;
            }
        }

        return Math.max(Duration.between(Instant.now(), expiresAt).getSeconds(), 3600); // 최소 1시간 유지
    }
}

### `tokenGenerator.generate(tokenContext)`가 `null`을 반환하는 이유

`OAuth2TokenGenerator.generate(tokenContext)` 메서드가 호출되었으나 결과값이 `null`이 되는 주요 원인은 다음과 같습니다.

#### 1. `DelegatingOAuth2TokenGenerator` 내 적절한 생성기 부재
프로젝트에서 사용하는 `tokenGenerator`는 `DelegatingOAuth2TokenGenerator`입니다. 이 클래스는 내부에 등록된 여러 `OAuth2TokenGenerator`들을 순회하며 토큰 생성을 시도합니다. 만약 현재 `tokenContext`가 요청하는 토큰 타입(Access Token, Refresh Token, ID Token 등)을 처리할 수 있는 생성기가 목록에 없거나, 모든 생성기가 `null`을 반환하면 최종적으로 `null`이 반환됩니다.

```java
// AuthorizationServerConfig.java
@Bean
public OAuth2TokenGenerator<?> tokenGenerator(JwtEncoder jwtEncoder) {
    JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
    OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
    OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
    return new DelegatingOAuth2TokenGenerator(
            jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
}
```

#### 2. 토큰 형식(Token Format)과 생성기 불일치
Spring Security Authorization Server는 클라이언트 설정(`TokenSettings`)에 정의된 `accessTokenFormat`에 따라 다른 생성기를 사용합니다.
 
*   **Self-contained (JWT)**: `JwtGenerator`가 담당합니다. 만약 클라이언트 설정이 JWT 방식인데 `tokenGenerator` 목록에 `JwtGenerator`가 없으면 `null`을 반환합니다.
*   **Reference (Opaque)**: `OAuth2AccessTokenGenerator`가 담당합니다. 불투명 토큰을 생성해야 할 때 이 생성기가 없으면 `null`을 반환합니다.

#### 3. `tokenContext` 설정 미비 (예: ID Token 생성 시)
`OIDC`가 활성화된 경우 `ID Token` 생성을 시도합니다. `ID Token`은 항상 JWT 형식이어야 하므로 `JwtGenerator`가 이를 처리합니다. 하지만 다음과 같은 상황에서 `null`이 발생할 수 있습니다:
*   `tokenContext`에 `RegisteredClient` 정보가 누락된 경우.
*   요청된 `Scope`에 `openid`가 포함되지 않아 `ID Token` 생성이 필요 없는 경우.
*   `JwtEncoder`가 정상적으로 동작하지 않거나 설정되지 않은 경우.

#### 4. 커스텀 생성기의 조건부 반환
만약 특정 조건(예: 특정 권한 부여 유형 `grant_type`)에서만 토큰을 생성하도록 커스텀 생성기를 구현했다면, 해당 조건에 맞지 않을 때 명시적으로 `null`을 반환하도록 설계되었을 가능성이 큽니다.

---

### 요약
`generate()`가 `null`을 반환한다면, **"현재 요청된 컨텍스트(토큰 타입 + 클라이언트 설정)를 처리할 수 있는 생성기가 등록되지 않았음"**을 의미합니다. 위 코드처럼 `JwtGenerator`, `OAuth2AccessTokenGenerator`, `OAuth2RefreshTokenGenerator`가 모두 올바르게 등록되어 있는지 확인해야 합니다.

---

### Redis Session/Token 저장 및 다단계 캐시(Local -> Global -> DB) 작업 계획

토큰 조회 성능 최적화 및 DB 부하 감소를 위해 **Local Cache -> Global Cache (Redis) -> DB** 순으로 데이터를 조회하는 계층형 캐싱 전략을 적용합니다.

#### 1. 의존성 추가 (`build.gradle`)
*   `spring-boot-starter-data-redis`: Redis 연동을 위한 스타터 추가.
*   `spring-boot-starter-cache`: Spring Cache 추상화 사용.

#### 2. Redis 및 캐시 설정 (`RedisConfig.java`, `application.yml`)
*   `RedisConnectionFactory` 및 `RedisTemplate` 설정.
*   `CacheManager` 설정 (Redis 기반 및 Local 기반 Caffeine/Ehcache 등 고려).
*   `application.yml`에 Redis 호스트, 포트 등 접속 정보 설정.

#### 3. 다단계 캐싱 조회를 위한 `OAuth2AuthorizationService` 데코레이터 구현
`JdbcOAuth2AuthorizationService`를 감싸는(Delegation) 커스텀 서비스를 구현하거나 
Spring Cache(@Cacheable)를 활용합니다.

*   **Local Cache**: 짧은 TTL을 가진 로컬 캐시 (동일 인스턴스 내 반복 요청 처리).
*   **Global Cache (Redis)**: 세션 및 토큰 정보를 클러스터 간 공유.
*   **DB (MS SQL)**: 최종적인 영속성 저장소.

#### 4. `AuthorizationServerConfig` 수정
기존 `authorizationService` 빈 등록 로직을 수정하여, 새로 구현한 캐싱 레이어가 적용된 서비스가 반환되도록 교체합니다.

#### 5. 데이터 정합성 보장 (Eviction)
*   토큰 발급 시 캐시 저장.
*   토큰 무효화(`revoke`) 또는 만료 시 캐시에서 즉시 삭제(Evict)하는 로직 포함.

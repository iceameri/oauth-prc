# Authorization Server (Spring Authorization Server)

Spring Authorization Server 기반의 OAuth 2.1 / OIDC 토큰 발급 서버다. \
Authorization Code, Refresh Token, Client Credentials 그랜트 타입을 지원하며, 
토큰 무효화(revocation), 토큰 유효성 검사(introspection), JWK 공개 키 제공(jwks) 엔드포인트를 포함한다.

## 주요 기능
- OAuth 2.1 Authorization Code 그랜트로 액세스 토큰/리프레시 토큰 발급
- Client Credentials 그랜트 지원
- Refresh Token 재발급 지원
- JWKS 공개 키 제공(`/oauth2/jwks`)
- 토큰 무효화(`/oauth2/revoke`)
- 토큰 유효성 검사(`/oauth2/introspect`)

## 기술 스택
- Java 17+ (권장)
- Spring Boot 3.x
- Spring Authorization Server
- MS SQL (`docker-compose.yml` 참고) (스키마는 `src/main/resources/schema.sql` 참고)

기본 포트 및 콜백
- 서버 포트: `http://localhost:9090`
- 콜백 URL(예시): `http://localhost:9090/auth/callback`

애플리케이션 설정은 `src\main\resources\application.yml`에서 변경할 수 있습니다.

## 엔드포인트 요약 및 예제

아래 예제는 기본 포트(9090) 기준입니다. 필요 시 `client_id`, `redirect_uri` 등을 환경에 맞게 수정하세요.

### 1) 로그인 화면 / 인가 코드 요청 (Authorization Code)
```
GET http://localhost:9090/oauth2/authorize?response_type=code&client_id=opaque-client&redirect_uri=http://localhost:9090/oauth2/token&scope=email openid
```
또는 cURL:
```
curl -X GET "http://localhost:9090/oauth2/authorize?response_type=code&client_id=test-client&redirect_uri=http://localhost:9090/oauth2/token&scope=openid%20profile%20email"
```

브라우저로 접근하면 로그인 페이지로 리다이렉트되고 인증 성공 시 `redirect_uri`로 `code`가 전달된다.

### 2) 토큰 발급 (Authorization Code → Access/Refresh Token)
```
curl -X POST http://localhost:9090/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "code={AUTH_CODE}" \
  -d "redirect_uri=http://localhost:9090/oauth2/token" \
  -d "user_id={user_id}" \
  -d "password={password}"
```

응답은 `access_token`, `refresh_token`, `expires_in`, `scope`, `token_type` 등을 포함한다.

### 3) Refresh Token 재발급
```
curl -X POST http://localhost:9090/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=refresh_token" \
  -d "refresh_token={refresh_token}"
```

### 4) Client Credentials 토큰 발급
```
curl -X POST http://localhost:9090/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials"
```

### 5) JWK 공개 키 목록
```
curl -X GET http://localhost:9090/oauth2/jwks
```

### 6) 토큰 무효화 (Revocation)
```
curl -X POST http://localhost:9090/oauth2/revoke \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d token={access_token OR refresh_token}
```

### 7) 토큰 유효성 검사 (Introspection)
```
curl -X POST http://localhost:9090/oauth2/introspect \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d token={access_token OR refresh_token}
```

## Archive 이전 정리
* 로그인화면 URL
  `curl -X GET "http://localhost:9090/oauth2/authorize
  ?response_type=code
  &client_id=test-client
  &redirect_uri=http://localhost:9090/auth/callback&scope=openid%20profile%20email"`
  `(GET http://local/host:9090/oauth2/authorize?response_type=code&client_id=test-client&redirect_uri=http://localhost:9090/auth/callback&scope=openid profile email)`

* Token 발급
  `curl -X POST http://localhost:9090/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "code={AUTH_CODE}" \
  -d "redirect_uri=http://localhost:9090/auth/callback" \
  -d "user_id={user_id}" \
  -d "password={password}"`

* refresh_token 재발급 (옵션필요)
  `curl -X POST http://localhost:9090/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=refresh_token" \
  -d "refresh_token={refresh_token}"`

* client_credentials Token 발급
  `curl -X POST http://localhost:9090/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials"`

* JWT 정보
  `curl -X GET http://localhost:9090/oauth2/jwks`

* 토큰 무효화
  `curl -X POST http://localhost:9090/oauth2/revoke \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d token={access_token OR refresh_token}`

* 토큰 유효성 검사
  `curl -X POST http://localhost:9090/oauth2/introspect \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d token={access_token OR refresh_token}`

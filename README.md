## OAuth 2.1 서버 구현
OAuth 2.1 기반 인증 서버와 리소스 서버를 구현 예제입니다.
Spring Boot 3.5 + Gradle + JAVA 17 로 진행하였습니다.

### 모듈 구성
```text
.
├── authorization-server (OAuth2 인증 서버)
│   └── src.main.java.com.jw.authorization server
│       ├── config (설정 관련)
│       │   ├── jdbc (JDBC 관련 설정)
│       │   └── user details (UserDetailsService 구현체 및 관련 클래스)
│       ├── constants (상수 클래스)
│       ├── controller (인증 및 토큰 관련 API)
│       └── dto (데이터 전송 객체)
├── resource-server (JWT 기반 리소스 서버)
│   └── src.main.java.com.jw.resourceserver
│       ├── config (Security, Swagger, JPA 설정)
│       ├── controller (API 컨트롤러)
│       │   ├── opened (비인증 API)
│       │   └── secured (인증 필요 API)
│       ├── dto (데이터 전송 객체)
│       │   ├── request (요청 DTO)
│       │   ├── response (응답 DTO)
│       │   └── security (보안 관련 DTO)
│       ├── entity (JPA 엔티티)
│       │   └── resource (리소스 관련 엔티티)
│       ├── repository (Data JPA 레포지토리)
│       └── service (비즈니스 로직)
└── resource-opaque-server (Opaque Token 기반 리소스 서버)
    └── src.main.java.com.jw.resourceopaqueserver
        ├── config (Security 설정)
        └── controller (API 컨트롤러)
```

### 실행방법
1. 최상위 디렉토리 위치에서 터미널로 docker-compose up -d 실행 (DB, Redis)
2. DB에 접속하여 authorization-server/src/main/resources/schema.sql 실행
3. 각 프로젝트 실행 
   1) authorization-server/src/main/java/com/jw/authorizationserver/AuthorizationServerApplication.java
   2) resource-server/src/main/java/com/jw/resourceserver/ResourceServerApplication.java
   3) resource-opaque-server/src/main/java/com/jw/resourceopaqueserver/ResourceOpaqueServerApplication.java



### 사전 지식
#### OAuth 2.0의 Opaque Token
-  OAuth 2.0 에서 Opaque Token이 기본이라고 명시된 적은 없다. 하지만 기본 전제에 가깝다.
- RFC 6749 (OAuth 2.0 Core) 에서 **Access Token은 “단순 문자열(string)”**로만 정의한다.\
  형식에 대한 규정이 없으나 위의 내용에서 가장 자연스러운 구현이 Opaque Token 이다.(스펙적 관점)
- 역사적으로 OAuth2.0(2012~2014)보다 JWT(2015)가 늦게 나왔다.

#### OAuth 2.0 -> 2.1 버전업이 된 핵심계기
1. OAuth 2.0의 복잡성 → OAuth 2.1 단순화\
   10개 넘는 Grant Type과 옵션이 있었지만 불필요 기능들이 폐기되었다.\
   그 중 `Implicit Grant`와 `Password Grant` 제거되면서
   프론트에서 Access Token을 클라이언트로 직접 받을 수 없게되고 서버가 JWT를 만들어주는 패턴이 일반화 되었다.

2. Stateful -> Stateless\
   Opaque Token 랜덤 문자열을 검증하려면
```
    Client → Resource Server → Authorization Server(introspection)
```
핸덤 문자열이기때문에 사용자정보,권한, 만료시간, 발급 정보 등 아무것도 알수 없다.\
이로인해 서버에 전달되어 검증이 필요하며 서비스가 많으면 병목이 발생한다.\
JWT는 자체 서명이 포함되어있어서 리소스 서버가 토큰 단독 검증이 가능하다. 그렇기 때문에 성능상에 이점이 있다.

> 하지만 보안이 중요한 곳에서는 여전히 Opaque Token이 사용된다.\
> 중요한 정보들은 JWT 내에 저장하지 않으므로 보안을 위한 권한체크는 해당 정보를 조회는 서버와의 통신을 할 수 밖에 없기 때문에
해당 로직을 추가하는 것이 곧 Stateful 서버가 되는 것이므로 설계 초기부터 의사결정이 필요하다.\

> 쿠팡사태와 같이 토큰을 탈취하게 되면 서버쪽에서는 손쓸방법이 없기 때문에 인증을 가볍게 사용할 수 있는 서버는 JWT를 사용하고
> 사업측면에서 매출이 영향에 큰 서버에는 Opaque Token으로 사용해야한다.\
> 최적화 위해서는 Local Cache -> Global Cache -> DB 순으로 데이터를 조회한다.

#### OIDC 와 Access Token 차이
OIDC: 로그인 성공 후 프론트앤드, 클라이언트가 사용자 정보 확인, API 호출에 사용하면 안됨
Access Token: Resource Server 검증 대상

#### 참고사항
jwt 토큰의 위험성은 토큰이 탈취당하고 만료시간까지 소멸시키는 방법이 불가하다.\
테스트의 용이성을 위해 어드민권한을 담은 만료시간이 긴 토큰은 절대 만들면 안된다.\
휴먼에러로 인한 취약성을 항상 조심해야 한다.

### 주요 기능
- OAuth 2.1 Authorization Code 그랜트로 액세스 토큰/리프레시 토큰 발급
- Client Credentials 그랜트 지원
- Refresh Token 재발급 지원
- JWKS 공개 키 제공(`/oauth2/jwks`)
- 토큰 무효화(`/oauth2/revoke`)
- 토큰 유효성 검사(`/oauth2/introspect`)



### 개선사항
- Redis 캐시 적용(진행중)
  - Local Cache -> Global Cache -> DB 순으로 토큰 조회
- 멀티모듈 아키텍쳐
  - 현재는 각 서버를 실행할 수 있기 위한 최상위 폴더로만 사용했습니다.
- Role 적용
- Scope 적용
- 다중 서버 실행을 고려한 서버포트 수정 예시 (실행옵션만으로 가능함)
  - authorization-server: 9090(변경사항 없음)
  - resource-server: 9100~9109
  - resource-opaque-server: 9110~9119
- JPA-QueryDSL 적용

### 참고사항
- 개인 프로젝트라서 feat 브랜치 작업 없이 master 브랜치로만 작업했습니다. 
- 또한 같은 이유로 commit이 잘게 나누어 있습니다.
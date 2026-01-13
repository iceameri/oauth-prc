# Resource Server (Spring Boot)

OAuth 2.1 Authorization Server의 JWT를 활용한 Resource Server다.

게시판/댓글 CRUD를 간단하게 구현하고 이후 확장 가능성에 대해 기술했다.


## Features
- JWT 를 활용한 Resource Server
- 게시판: CRUD, pin/unpin 기능
- 게시판 댓글: 댓글/대댓글의 CRUD
- 게시판의 경우 법적 검토를 받을 가능성으로 soft delete를 기본
- 불변 + 간결 + 의도 명확 + 버그 감소를 지향하여 DTO는 record를 사용
- MS Sql DB 사용
- 테이블 생성 후에 엔티티를 생성을 기본으로 하지만 JPA만으로 테이블 생성을 실험삼아 Boards, Comments, BoardViewLogs 에 적용

### BoardViewLogs를 사용하는 경우
- 비즈니스 로직에 영향이 가는 경우
  - 서버 신뢰성, 조작 불가, 감사 로그 기능
- 관리자 / 운영 지표가 필요한 경우
  - OLAP 데이터로 활용
- 언제 읽었는지 중요한 경우
  - SLA / 법적 감사 대응
- 추천 / 개인화 로직에 사용되는 경우
  - 이미 본 글 제외, 사용자 관심도 계산, 피드 개인화

#### 월별 파티셔닝 필요
- 월별 날짜 기반 Range 파티셔닝
- 연단위로 데이터 관리 전략 필요

> 읽은 게시글의 회색 텍스트 처리는 UX 편의 기능으로 프론트 로컬 캐시로 처리한다. (UX 목적은 클라이언트 책임)


## Configuration
Key settings live in `src/main/resources/application.yml`.

- Server port: `9091`
- OAuth2 Resource Server: `spring.security.oauth2.resourceserver.jwt.issuer-uri`
- Datasource (SQL Server):
  - `jdbc-url: jdbc:sqlserver://localhost:1433;databaseName=resource;trustServerCertificate=true;encrypt=false`
  - Username: `sa`
  - Password: `P@$$w0rd1!`
- JPA: SQL Server dialect, `ddl-auto: update`, formatted SQL, batch fetch size
- Swagger UI: `http://localhost:9091/swagger-ui` (prod 프로파일이 아닐 때 자동 허용)
- OpenAPI 문서: `http://localhost:9091/v3/api-docs`
- Secured 경로는 모두 `Authorization: Bearer <JWT>` 헤더가 필요합니다.
- Profiles: `spring.profiles.active` (e.g., `local`, `prod`).

## Build & Run
Active Profiles: local로 설정하여 ResourceServerApplication 실행 


## 부록
### 추천 테이블 분리 설계
읽음 / 조회 / 노출 개념 구분

**노출 (Impression)**
- 게시판 목록에 표시됨
- 추천 피드에 노출
- 자동 발생
- 사용자 행동 X
- 마케팅 지표
- 초대량이기 때문에 Redis / Kafka

**조회 (View)**
- 게시글 상 세 페이지 진입
- 조회수 증가 기준
- 클릭필요
- 조회수 통계
- 중복 방지 필요
- 중복 방지를 위해 Redis + RDB

**읽음 (Read)**
- 내용을 소비했다고 판단
- 스크롤 80%이상 | 10초 이상 체류 | `읽음` 버튼 클릭
- UX 이벤트
- 비즈니스 로직에 영향
- 가장 신뢰도가 높음
- 신뢰성을 위해 RDB

### BoardViewLog + Redis 하이브리드 설계
```
[ Client ]
↓
[ API Server ]
↓
[ Redis ]  ← 실시간 조회 체크
↓ (비동기)
[ RDB(BoardViewLog) ]  ← 영구 로그
↓
[ Board.view_count ]  ← 집계 컬럼
```
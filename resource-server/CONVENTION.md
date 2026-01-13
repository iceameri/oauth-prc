# Keynote
- CONVENTION.md는 프로젝트별로 정의한다.
- Naming Convention 뿐만아니라 프로젝트 전반의 모든 Convention을 기술한다.
- 특수 케이스가 있다면 추가한다.
- 기술부채 가능성이 있는 곳에 TODO 남긴다.
- 게시판 관련된 테이블 제외하고 테이블 정의는 데이터베이스에서 직접 정의한다.
- 다양한 실험적인 코드를 위해 패키지로 나누어 관리한다.


## Class
### DTO
- DTO 클래스는 record로 구현한다.
- 특수한 경우 해당 클래스에 주석표시한다.

### Entity
- CamelCase 사용한다.

### DataBase
- SnakeCase 사용한다.


## API
- Batch CRUD는 JdbcTemplate 사용한다.
- 기본 CRUD는 JPA 사용한다.
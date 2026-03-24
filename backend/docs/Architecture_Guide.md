# GoGo Backend Architecture Guide

> 이 문서는 GoGo 백엔드의 **확정된 아키텍처 규칙**입니다. 모든 코드 변경 시 반드시 참고하세요.

---

## 아키텍처 목표

- 단순하고 직관적인 레이어드 아키텍처
- Domain 중심 비즈니스 로직 구조
- 빠른 개발과 이해 용이성
- Port/Adapter, Mapper, RepositoryImpl 래퍼 **사용하지 않음**

---

## 레이어 흐름

```
         ┌──────┐
         │  api │  Controller + DTO
         └──┬───┘
            │ (domain만 참조)
            ▼
         ┌──────┐
         │domain│  Service + 도메인 모델 + Info 객체
         └┬───┬─┘
          │   │
     ┌────▼┐ ┌▼──────┐
     │ db  │ │client  │
     └─────┘ └────────┘

    config → domain.port 인터페이스 구현
```

---

## 의존성 규칙 (절대 준수)

| 모듈 | 참조 가능 | 참조 불가 |
|------|----------|----------|
| **api** | `domain.*` | `db.*`, `client.*`, `config.*` |
| **domain** | `db.*`, `client.*`, `domain.*` | `api.*`, `config.*` |
| **db** | `db.*` (자기만) | `domain.*`, `api.*`, `client.*`, `config.*` |
| **client** | `client.*` (자기만) | `domain.*`, `api.*`, `db.*`, `config.*` |
| **config** | `domain.port.*`, `domain.model.*` | `api.*`, `client.*`, `db.*` |

### 위반 검증 명령어

```bash
# api -> db/client/config 위반 검출
grep -rn "import com.gogo.db\|import com.gogo.client\|import com.gogo.config" \
  backend/src/main/java/com/gogo/api/

# domain -> api/config 위반 검출
grep -rn "import com.gogo.api\|import com.gogo.config" \
  backend/src/main/java/com/gogo/domain/

# db -> 외부 참조 위반 검출
grep -rn "import com.gogo\." backend/src/main/java/com/gogo/db/ \
  | grep -v "import com.gogo.db"

# client -> 외부 참조 위반 검출
grep -rn "import com.gogo\." backend/src/main/java/com/gogo/client/ \
  | grep -v "import com.gogo.client"
```

---

## 패키지 구조

```
com.gogo/
│
├── api/                              ← HTTP 경계 (domain만 참조)
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── PlacesController.java
│   │   ├── GroupsController.java
│   │   ├── MeetingsController.java
│   │   ├── ReviewsController.java
│   │   ├── HealthController.java
│   │   └── GlobalExceptionHandler.java
│   └── dto/
│       ├── request/                  ← 요청 DTO
│       └── response/                 ← 응답 DTO (domain.info → response 변환)
│
├── domain/                           ← 비즈니스 로직 중심 (db, client 직접 참조)
│   ├── service/                      ← 비즈니스 서비스 (@Service, @Transactional)
│   ├── info/                         ← domain → api 전달용 중간 객체 (record)
│   ├── model/                        ← 도메인 값 객체, enum (AuthenticatedUser 등)
│   └── port/                         ← 인프라 추상화 인터페이스 (최소한만)
│
├── db/                               ← 데이터 접근 (자기만 참조, 완전 독립)
│   ├── entity/                       ← JPA 엔티티 (@Entity 직접)
│   └── repository/                   ← Spring Data JPA 인터페이스
│
├── client/                           ← 외부 API 클라이언트 (자기만 참조, 완전 독립)
│   ├── kakao/                        ← Kakao OAuth
│   ├── google/                       ← Google OAuth
│   └── naver/                        ← Naver Local Search API
│
└── config/                           ← Spring 설정 + 보안 (domain.port 구현)
    ├── SecurityConfig.java
    ├── CorsConfig.java
    ├── DataInitializer.java
    └── security/                     ← JWT, Filter, SecurityContextHelper
```

---

## 각 레이어 역할과 규칙

### 1. API Layer (`api/`)

**역할**: HTTP 요청/응답 처리만.

- Controller: Request DTO를 받아 domain 서비스 호출, domain info를 Response DTO로 변환
- Request DTO → domain 서비스 메서드 파라미터로 전달
- Domain info → Response DTO 변환은 `Response.from(info)` 패턴

**금지 사항**:
- 비즈니스 로직 작성
- `db.*`, `client.*` 직접 참조
- Repository 직접 호출
- 외부 API 클라이언트 직접 호출

### 2. Domain Layer (`domain/`)

**역할**: 비즈니스 로직의 중심. 이 레이어가 애플리케이션의 핵심.

- `service/`: `@Service` 클래스. db repository와 client를 직접 주입받아 사용
- `info/`: db entity → api 전달용 불변 record. 서비스 내 `toInfo()` 메서드로 변환
- `model/`: 도메인 값 객체, enum (`AuthenticatedUser`, `PlaceStatus` 등)
- `port/`: 인프라 추상화 인터페이스 (테스트 mock 필요한 것만: `AuthContext`, `TokenProvider`)

**허용**: `db.entity.*`, `db.repository.*`, `client.*` 직접 import

**금지 사항**:
- `api.*` 참조 (역방향 의존)
- `config.*` 참조 (port 인터페이스로 추상화)

### 3. DB Layer (`db/`)

**역할**: 데이터 접근만.

- `entity/`: JPA 엔티티에 `@Entity` 직접 부착. 도메인 행위 메서드 포함 가능 (e.g., `markVisited()`)
- `repository/`: Spring Data `JpaRepository` 인터페이스. `@Query`로 커스텀 쿼리 작성

**금지 사항**:
- 비즈니스 로직 작성
- 외부 모듈 참조 (`com.gogo.db.*` 이외 import 금지)
- 트랜잭션 처리 (Service에서 관리)

### 4. Client Layer (`client/`)

**역할**: 외부 API 호출만.

- 각 외부 서비스별 하위 패키지 (`kakao/`, `google/`, `naver/`)
- 자체 DTO로 응답 반환 (domain DTO 사용 금지)
- JWT 생성 같은 비즈니스 로직 포함 금지

**금지 사항**:
- 외부 모듈 참조 (`com.gogo.client.*` 이외 import 금지)
- domain DTO/entity 반환
- JWT 토큰 생성 (domain 서비스에서 처리)

### 5. Config Layer (`config/`)

**역할**: Spring 설정 + `domain/port/` 인터페이스 구현.

- `SecurityConfig`, `CorsConfig`: Spring Security/CORS 설정
- `security/JwtServiceImpl`: `TokenProvider` 구현
- `security/SecurityContextHelper`: `AuthContext` 구현
- `DataInitializer`: 초기 데이터 세팅

---

## 데이터 흐름 패턴

### Request → Response 전체 흐름

```
HTTP Request
  ↓
api/controller/      Request DTO 파싱
  ↓
domain/service/      비즈니스 로직 수행
  ├── db/repository  데이터 조회/저장 (db entity 직접 사용)
  ├── client/        외부 API 호출 (client DTO 수신)
  └── domain/info/   db entity → info record 변환
  ↓
api/controller/      info → Response DTO 변환
  ↓
HTTP Response (JSON)
```

### 변환 체인

```
db.entity.Place → domain.info.PlaceInfo → api.dto.response.PlaceResponse → JSON
                  (서비스 내 toInfo())     (PlaceResponse.from(info))
```

### 외부 API 호출 흐름

```
client.naver.NaverSearchItem → domain.info.PlaceSearchInfo → JSON
                                (서비스 내 변환)
```

---

## Port 인터페이스 (최소한만 사용)

| 인터페이스 | 위치 | 구현체 | 이유 |
|-----------|------|--------|------|
| `AuthContext` | `domain/port/` | `config/security/SecurityContextHelper` | 테스트에서 SecurityContext mock 불편 |
| `TokenProvider` | `domain/port/` | `config/security/JwtServiceImpl` | domain → config 직접 참조 방지 |

**원칙**: 테스트 mock이 필요한 인프라만 port로. 나머지는 직접 의존.

---

## 새 기능 추가 시 체크리스트

1. **엔티티 추가**: `db/entity/` + `db/repository/`
2. **서비스 추가**: `domain/service/` (db repo, client 주입)
3. **Info 객체 추가**: `domain/info/` (서비스 반환용 record)
4. **DTO 추가**: `api/dto/request/`, `api/dto/response/`
5. **컨트롤러 추가**: `api/controller/` (domain 서비스만 호출)
6. **의존성 규칙 검증**: 위의 grep 명령어 실행

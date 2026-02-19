# Implementation Plan: GoGo Site - 친구들과 가고 싶은 장소 기록 & 약속 정하기

**Status**: 🔄 Ready to Start
**Started**: 2026-02-19
**Last Updated**: 2026-02-19
**Estimated Completion**: 2026-03-07

---

**⚠️ CRITICAL INSTRUCTIONS**: After completing each phase:
1. ✅ Check off completed task checkboxes
2. 🧪 Run all quality gate validation commands
3. ⚠️ Verify ALL quality gate items pass
4. 📅 Update "Last Updated" date above
5. 📝 Document learnings in Notes section
6. ➡️ Only then proceed to next phase

⛔ **DO NOT skip quality gates or proceed with failing checks**

---

## 📋 Overview

### Feature Description
**GoGo** - 친구들과 "우리 여기 가자!"라고 기록해두고, 약속을 정할 때 해당 기록을 보면서 장소를 선택하거나 방문 후기를 남길 수 있는 소셜 장소 기록 서비스.

**MVP 범위 (인증 없음):**
1. 가고 싶은 장소 등록/조회/삭제
2. 그룹 생성 + 초대 링크로 참여 (인증 없이 닉네임 기반)
3. 그룹 내 장소 공유 및 약속 투표
4. 방문 후기 작성

### Success Criteria
- [ ] 장소를 등록하고 목록으로 조회할 수 있다
- [ ] 그룹을 생성하고 초대 링크로 참여할 수 있다
- [ ] 그룹 내 약속 후보 장소 투표 및 확정이 가능하다
- [ ] 방문 후 후기를 작성할 수 있다
- [ ] Spring Boot 백엔드에 Clean Architecture 원칙이 적용된다
- [ ] 모든 비즈니스 로직에 단위 테스트 ≥80% 커버리지

### User Impact
별도 로그인 없이 바로 장소를 기록하고 친구들과 공유하여 약속 장소를 쉽게 결정할 수 있다.

---

## 🏗️ Architecture Decisions

| Decision | Rationale | Trade-offs |
|----------|-----------|------------|
| **Spring Boot 3.x + Java 21** | 팀에서 검증 가능, 주니어 개발자 성장에 적합 | 초기 설정 복잡도 있음 |
| **Clean Architecture (Hexagonal)** | 도메인 로직 독립, 테스트 용이, 인프라 교체 가능 | 초기 보일러플레이트 코드 증가 |
| **Spring Data JPA + PostgreSQL** | ORM으로 빠른 개발, 관계형 데이터 관리 용이 | N+1 문제 주의 필요 |
| **Gradle** | 빠른 빌드, Kotlin DSL 지원, 현대적 표준 | Maven 대비 낮은 친숙도 가능 |
| **Next.js 14 (프론트엔드)** | 바이브 코딩으로 빠른 UI 구성, Spring API 호출 | - |
| **인증 없음 (MVP)** | 빠른 기능 검증, 복잡도 감소 | 보안 취약점 (이후 추가 필요) |
| **JUnit 5 + Mockito** | Spring Boot 표준 테스트 스택 | - |

### 전체 프로젝트 구조
```
gogo/                          # 모노레포 루트
├── backend/                   # Spring Boot (Java)
│   └── src/main/java/com/gogo/
│       ├── domain/            # 🔵 핵심 비즈니스 규칙
│       │   ├── entity/        # Place, Group, Review, Meeting
│       │   ├── repository/    # Repository 인터페이스 (Port)
│       │   └── service/       # 도메인 서비스
│       ├── application/       # 🟡 유스케이스
│       │   ├── usecase/       # AddPlaceUseCase 등
│       │   └── dto/           # Request/Response DTO
│       ├── infrastructure/    # 🟠 JPA 구현체, 외부 연동
│       │   ├── persistence/   # JPA Entity, Repository 구현
│       │   └── config/        # Spring 설정
│       └── presentation/      # 🔴 REST Controller
│           └── api/           # @RestController
├── frontend/                  # Next.js (바이브 코딩)
│   └── src/
│       ├── app/               # App Router 페이지
│       ├── components/        # UI 컴포넌트
│       └── lib/api/           # Spring Boot API 클라이언트
└── docs/
    └── plans/
        └── PLAN_gogo-site.md  # 이 파일
```

### 주요 도메인 모델 (ERD 요약)
```
places          groups          meetings
--------        --------        --------
id (PK)         id (PK)         id (PK)
name            name            group_id (FK)
address         invite_code     title
category        created_by      status (VOTING/CONFIRMED)
url             created_at      confirmed_place_id
note
status          group_members   meeting_candidates
created_by      --------        --------
created_at      id (PK)         id (PK)
                group_id (FK)   meeting_id (FK)
reviews         nickname        place_id (FK)
--------        joined_at
id (PK)                         meeting_votes
place_id (FK)   group_places    --------
author_name     --------        id (PK)
rating (1-5)    id (PK)         meeting_id (FK)
content         group_id (FK)   place_id (FK)
visited_at      place_id (FK)   voter_name
created_at      shared_by
                shared_at
```

---

## 📦 Dependencies

### Backend (Spring Boot)
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    runtimeOnly 'org.postgresql:postgresql'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    // Mockito 포함됨
}
```

### Frontend (Next.js)
```
next: ^14.x
react: ^18.x
typescript: ^5.x
tailwindcss: ^3.x
axios or fetch (API 클라이언트)
```

### Required Before Starting
- [ ] Java 21 설치 확인 (`java --version`)
- [ ] Gradle 설치 확인 (`gradle --version`)
- [ ] PostgreSQL 설치 또는 Docker 실행 확인
- [ ] Node.js 18+ 설치 확인 (`node --version`)

---

## 🧪 Test Strategy

### Testing Approach
**TDD Principle**: 백엔드는 TDD 적용, 프론트엔드는 바이브 코딩으로 빠른 개발

### Backend Test Pyramid
| Test Type | Coverage Target | Purpose |
|-----------|-----------------|---------|
| **Unit Tests** | ≥80% | Domain entities, Use cases, Service 로직 |
| **Integration Tests** | Critical paths | Repository + UseCase 연동, Controller |
| **Slice Tests** | API Layer | `@WebMvcTest` 로 Controller 단독 테스트 |

### Test File Organization
```
backend/src/test/java/com/gogo/
├── domain/
│   └── entity/         # 엔티티 단위 테스트
├── application/
│   └── usecase/        # 유스케이스 단위 테스트 (Mockito)
└── infrastructure/
    └── presentation/   # Controller 슬라이스 테스트
```

---

## 🚀 Implementation Phases

---

### Phase 1: 프로젝트 기반 설정 (Scaffold)
**Goal**: Spring Boot + Next.js 프로젝트 초기화, DB 연결, 클린 아키텍처 폴더 구조 완성
**Estimated Time**: 2-3 hours
**Status**: ⏳ Pending

#### Tasks

**🟢 GREEN: 프로젝트 초기화**
- [ ] **Task 1.1**: Spring Boot 프로젝트 생성
  - `spring.io/start` 또는 IntelliJ에서 생성
  - Group: `com.gogo`, Artifact: `backend`
  - Dependencies: `Spring Web`, `Spring Data JPA`, `PostgreSQL Driver`, `Validation`
  - Java 21, Gradle - Groovy
  - File(s): `backend/build.gradle`, `backend/src/main/java/com/gogo/`

- [ ] **Task 1.2**: 클린 아키텍처 패키지 구조 생성
  - `com.gogo.domain.entity`
  - `com.gogo.domain.repository`
  - `com.gogo.application.usecase`
  - `com.gogo.application.dto`
  - `com.gogo.infrastructure.persistence`
  - `com.gogo.infrastructure.config`
  - `com.gogo.presentation.api`

- [ ] **Task 1.3**: PostgreSQL 연결 설정
  - File(s): `backend/src/main/resources/application.yml`
  ```yaml
  spring:
    datasource:
      url: jdbc:postgresql://localhost:5432/gogo
      username: gogo_user
      password: gogo_pass
    jpa:
      hibernate:
        ddl-auto: create-drop  # 개발 환경
      show-sql: true
  ```

- [ ] **Task 1.4**: PostgreSQL DB 생성
  - DB명: `gogo`, User: `gogo_user`

- [ ] **Task 1.5**: CORS 설정 (Next.js → Spring 통신 허용)
  - File(s): `com.gogo.infrastructure.config.CorsConfig.java`
  - `http://localhost:3000` 허용

- [ ] **Task 1.6**: Next.js 프론트엔드 프로젝트 생성
  - `pnpm create next-app@latest frontend --typescript --tailwind --app --src-dir`
  - File(s): `frontend/package.json`

- [ ] **Task 1.7**: 환경변수 설정
  - File(s): `frontend/.env.local`
  - `NEXT_PUBLIC_API_URL=http://localhost:8080`

**🔵 REFACTOR: 설정 검증**
- [ ] **Task 1.8**: 헬스체크 API 엔드포인트 추가
  - `GET /api/health` → `{"status": "ok"}` 반환
  - File(s): `com.gogo.presentation.api.HealthController.java`

#### Quality Gate ✋

**⚠️ STOP: Do NOT proceed to Phase 2 until ALL checks pass**

**Build & Tests**:
- [ ] **Build**: `./gradlew build` 오류 없음
- [ ] **Server Start**: `./gradlew bootRun` 정상 실행 (port 8080)
- [ ] **Health Check**: `curl http://localhost:8080/api/health` → 200 OK
- [ ] **Frontend Build**: `pnpm build` 오류 없음
- [ ] **DB 연결**: JPA Hibernate 로그에서 연결 성공 확인

**Validation Commands**:
```bash
# Backend
cd backend && ./gradlew build
./gradlew bootRun
curl http://localhost:8080/api/health

# Frontend
cd frontend && pnpm build
pnpm dev
```

**Manual Test Checklist**:
- [ ] `http://localhost:8080/api/health` 응답 확인
- [ ] `http://localhost:3000` Next.js 기본 페이지 확인
- [ ] 콘솔에 DB 연결 에러 없음

---

### Phase 2: Domain Layer - 핵심 엔티티 & Repository 인터페이스
**Goal**: 비즈니스 핵심 모델 정의 (Java 클래스), Repository 인터페이스, JPA 테이블 생성 확인
**Estimated Time**: 2-3 hours
**Status**: ⏳ Pending

#### Tasks

**🔴 RED: Domain 엔티티 테스트 작성**
- [ ] **Test 2.1**: Place 도메인 엔티티 단위 테스트
  - File(s): `src/test/java/com/gogo/domain/entity/PlaceTest.java`
  - Expected: FAIL (Place 클래스 미존재)
  - Test cases:
    ```java
    @Test void 장소_생성_성공() { ... }
    @Test void 이름이_빈_문자열이면_예외() { ... }
    @Test void 방문완료_상태_변경() { ... }
    ```

- [ ] **Test 2.2**: Group 도메인 엔티티 단위 테스트
  - File(s): `src/test/java/com/gogo/domain/entity/GroupTest.java`
  - Test cases:
    ```java
    @Test void 그룹_생성_시_초대코드_자동생성() { ... }
    @Test void 멤버_추가_성공() { ... }
    ```

**🟢 GREEN: Domain 엔티티 구현**
- [ ] **Task 2.3**: Place 엔티티 (Domain)
  - File(s): `com.gogo.domain.entity.Place.java`
  - 필드: `id`, `name`, `address`, `category`, `url`, `note`, `status`, `createdBy`, `createdAt`
  - 비즈니스 메서드: `markAsVisited()`, `validate()`
  - **주의**: 이 클래스는 순수 Java (JPA 어노테이션 없음)

- [ ] **Task 2.4**: Group 엔티티 (Domain)
  - File(s): `com.gogo.domain.entity.Group.java`
  - 필드: `id`, `name`, `inviteCode`, `createdBy`, `members`, `createdAt`

- [ ] **Task 2.5**: Review 엔티티 (Domain)
  - File(s): `com.gogo.domain.entity.Review.java`
  - 필드: `id`, `placeId`, `authorName`, `rating` (1-5), `content`, `visitedAt`

- [ ] **Task 2.6**: Meeting 엔티티 (Domain)
  - File(s): `com.gogo.domain.entity.Meeting.java`
  - 필드: `id`, `groupId`, `title`, `candidatePlaceIds`, `status`, `confirmedPlaceId`

- [ ] **Task 2.7**: PlaceStatus Enum
  - File(s): `com.gogo.domain.entity.PlaceStatus.java`
  - 값: `WANT_TO_GO`, `VISITED`

- [ ] **Task 2.8**: Repository 인터페이스 정의 (Port)
  - File(s): `com.gogo.domain.repository.PlaceRepository.java`
  - File(s): `com.gogo.domain.repository.GroupRepository.java`
  - File(s): `com.gogo.domain.repository.ReviewRepository.java`
  - File(s): `com.gogo.domain.repository.MeetingRepository.java`
  - 메서드: `save()`, `findById()`, `findAll()`, `deleteById()`

- [ ] **Task 2.9**: JPA Entity 클래스 (Infrastructure - 별도)
  - File(s): `com.gogo.infrastructure.persistence.entity.PlaceJpaEntity.java`
  - JPA 어노테이션은 여기에만 사용: `@Entity`, `@Table`, `@Column` 등
  - Domain Entity ↔ JPA Entity 변환 Mapper 작성

**🔵 REFACTOR**
- [ ] **Task 2.10**: Domain ↔ JPA Entity Mapper 클래스
  - File(s): `com.gogo.infrastructure.persistence.mapper.PlaceMapper.java`
- [ ] **Task 2.11**: `./gradlew bootRun` 후 DDL 자동 생성 확인 (테이블 생성 로그)

#### Quality Gate ✋

**⚠️ STOP: Do NOT proceed to Phase 3 until ALL checks pass**

**TDD Compliance**:
- [ ] Place, Group 엔티티 테스트 먼저 작성 후 구현
- [ ] Domain 단위 테스트 ≥90% 커버리지

**Build & Tests**:
- [ ] `./gradlew test` 모든 테스트 통과
- [ ] `./gradlew bootRun` 실행 시 PostgreSQL 테이블 자동 생성 확인

**Validation Commands**:
```bash
cd backend && ./gradlew test
./gradlew jacocoTestReport
./gradlew bootRun
# psql로 테이블 확인
psql -U gogo_user -d gogo -c "\dt"
```

**Manual Test Checklist**:
- [ ] Place, Group, Review, Meeting 테이블이 DB에 생성됨
- [ ] 도메인 엔티티에 JPA 어노테이션이 없음 (순수 Java)
- [ ] Repository 인터페이스가 도메인 패키지에 위치

---

### Phase 3: Application + Infrastructure - 장소 관리 API
**Goal**: 장소 CRUD 유스케이스, JPA Repository 구현, REST API 완성. 장소 등록/조회 작동
**Estimated Time**: 3-4 hours
**Status**: ⏳ Pending

#### Tasks

**🔴 RED: 장소 유스케이스 테스트**
- [ ] **Test 3.1**: AddPlaceUseCase 단위 테스트
  - File(s): `src/test/java/com/gogo/application/usecase/AddPlaceUseCaseTest.java`
  - Mock: `PlaceRepository` (Mockito)
  - Expected: FAIL
  - Test cases:
    ```java
    @Test void 유효한_데이터로_장소_추가_성공() { ... }
    @Test void 이름없는_장소_추가시_예외() { ... }
    ```

- [ ] **Test 3.2**: GetPlacesUseCase 단위 테스트
  - Test cases:
    ```java
    @Test void 전체_장소_목록_조회() { ... }
    @Test void 카테고리별_필터링() { ... }
    ```

- [ ] **Test 3.3**: PlacesController 슬라이스 테스트
  - File(s): `src/test/java/com/gogo/presentation/api/PlacesControllerTest.java`
  - `@WebMvcTest(PlacesController.class)` 사용
  - Test cases:
    ```java
    @Test void POST_api_places_성공() { ... }
    @Test void GET_api_places_목록_반환() { ... }
    ```

**🟢 GREEN: 구현**
- [ ] **Task 3.4**: PlaceJpaRepository (Spring Data JPA)
  - File(s): `com.gogo.infrastructure.persistence.PlaceJpaRepository.java`
  - `extends JpaRepository<PlaceJpaEntity, Long>`

- [ ] **Task 3.5**: PlaceRepositoryImpl (Domain Repository 구현)
  - File(s): `com.gogo.infrastructure.persistence.PlaceRepositoryImpl.java`
  - `implements PlaceRepository`
  - PlaceJpaRepository 위임 + Mapper 사용

- [ ] **Task 3.6**: AddPlaceUseCase 구현
  - File(s): `com.gogo.application.usecase.AddPlaceUseCase.java`
  - DTO: `com.gogo.application.dto.AddPlaceRequest.java`
  - DTO: `com.gogo.application.dto.PlaceResponse.java`

- [ ] **Task 3.7**: GetPlacesUseCase 구현
  - File(s): `com.gogo.application.usecase.GetPlacesUseCase.java`

- [ ] **Task 3.8**: DeletePlaceUseCase 구현
  - File(s): `com.gogo.application.usecase.DeletePlaceUseCase.java`

- [ ] **Task 3.9**: MarkPlaceVisitedUseCase 구현
  - File(s): `com.gogo.application.usecase.MarkPlaceVisitedUseCase.java`

- [ ] **Task 3.10**: PlacesController (REST API)
  - File(s): `com.gogo.presentation.api.PlacesController.java`
  - `POST /api/places` - 장소 추가
  - `GET /api/places` - 장소 목록 (?category= 필터)
  - `GET /api/places/{id}` - 장소 상세
  - `DELETE /api/places/{id}` - 장소 삭제
  - `PATCH /api/places/{id}/visit` - 방문 완료 처리

- [ ] **Task 3.11**: 프론트엔드 - 장소 목록 페이지 (바이브 코딩)
  - File(s): `frontend/src/app/places/page.tsx`
  - Spring Boot `/api/places` 호출
  - 장소 카드 목록 UI

- [ ] **Task 3.12**: 프론트엔드 - 장소 추가 폼 (바이브 코딩)
  - File(s): `frontend/src/components/AddPlaceForm.tsx`

**🔵 REFACTOR**
- [ ] **Task 3.13**: `@RestControllerAdvice` 전역 예외 처리
  - File(s): `com.gogo.presentation.api.GlobalExceptionHandler.java`
- [ ] **Task 3.14**: Bean Validation (`@Valid`, `@NotBlank` 등) 적용

#### Quality Gate ✋

**⚠️ STOP: Do NOT proceed to Phase 4 until ALL checks pass**

**TDD Compliance**:
- [ ] UseCase 테스트 먼저 작성 (Red → Green 순서)
- [ ] 테스트 커버리지 ≥80% (application layer)

**API Tests**:
```bash
# 장소 추가
curl -X POST http://localhost:8080/api/places \
  -H "Content-Type: application/json" \
  -d '{"name":"성수동 카페","address":"서울 성동구","category":"CAFE","note":"분위기 좋음"}'

# 장소 목록
curl http://localhost:8080/api/places

# 방문 완료
curl -X PATCH http://localhost:8080/api/places/1/visit
```

**Validation Commands**:
```bash
cd backend && ./gradlew test
./gradlew bootRun
# API 수동 테스트 (위의 curl 명령어)
```

**Manual Test Checklist**:
- [ ] 장소 추가 API 200 응답
- [ ] 장소 목록 조회 (추가한 장소 포함)
- [ ] 방문 완료 후 status 변경 확인
- [ ] 프론트엔드에서 장소 목록 표시

---

### Phase 4: 그룹/친구 기능 & 장소 공유
**Goal**: 그룹 생성, 초대 링크로 참여, 그룹 내 장소 공유 API 완성
**Estimated Time**: 2-3 hours
**Status**: ⏳ Pending

#### Tasks

**🔴 RED: 그룹 유스케이스 테스트**
- [ ] **Test 4.1**: CreateGroupUseCase 테스트
  - Test cases:
    ```java
    @Test void 그룹_생성_시_초대코드_자동생성() { ... }
    @Test void 그룹_이름_빈문자열_예외() { ... }
    ```

- [ ] **Test 4.2**: JoinGroupUseCase 테스트
  - Test cases:
    ```java
    @Test void 유효한_초대코드로_그룹_참여() { ... }
    @Test void 잘못된_초대코드_예외() { ... }
    ```

- [ ] **Test 4.3**: SharePlaceToGroupUseCase 테스트

**🟢 GREEN: 구현**
- [ ] **Task 4.4**: GroupJpaEntity, GroupMemberJpaEntity, GroupPlaceJpaEntity
- [ ] **Task 4.5**: GroupRepositoryImpl
- [ ] **Task 4.6**: CreateGroupUseCase (초대 코드: UUID 앞 8자리)
- [ ] **Task 4.7**: JoinGroupUseCase (닉네임으로 참여)
- [ ] **Task 4.8**: SharePlaceToGroupUseCase
- [ ] **Task 4.9**: GetGroupPlacesUseCase
- [ ] **Task 4.10**: GroupsController
  - `POST /api/groups` - 그룹 생성
  - `POST /api/groups/join` - 초대 코드로 참여 (`{"inviteCode": "abc123", "nickname": "홍길동"}`)
  - `GET /api/groups/{id}` - 그룹 정보 + 멤버 목록
  - `GET /api/groups/{id}/places` - 그룹 공유 장소 목록
  - `POST /api/groups/{id}/places` - 장소 공유

- [ ] **Task 4.11**: 프론트엔드 - 그룹 페이지 (바이브 코딩)
  - 그룹 생성 폼, 초대 링크 표시, 그룹 내 장소 목록

**🔵 REFACTOR**
- [ ] **Task 4.12**: 초대 링크 생성 도우미 클래스 (`InviteCodeGenerator`)

#### Quality Gate ✋

**⚠️ STOP: Do NOT proceed to Phase 5 until ALL checks pass**

**API Tests**:
```bash
# 그룹 생성
curl -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -d '{"name":"성수동 탐방대","createdBy":"홍길동"}'

# 초대 코드로 참여
curl -X POST http://localhost:8080/api/groups/join \
  -H "Content-Type: application/json" \
  -d '{"inviteCode":"abc12345","nickname":"김철수"}'

# 장소 공유
curl -X POST http://localhost:8080/api/groups/1/places \
  -H "Content-Type: application/json" \
  -d '{"placeId":1,"sharedBy":"홍길동"}'
```

**Manual Test Checklist**:
- [ ] 그룹 생성 후 초대 코드 반환
- [ ] 초대 코드로 다른 닉네임으로 참여 가능
- [ ] 그룹에 장소 공유 및 그룹 장소 목록 조회

---

### Phase 5: 약속 정하기 (투표 & 확정)
**Goal**: 그룹 내 약속 투표 생성, 장소 투표, 약속 확정 기능 완성
**Estimated Time**: 2-3 hours
**Status**: ⏳ Pending

#### Tasks

**🔴 RED: 약속 유스케이스 테스트**
- [ ] **Test 5.1**: CreateMeetingUseCase 테스트
  - Test cases:
    ```java
    @Test void 약속_생성_성공() { ... }
    @Test void 후보_장소_없이_생성시_예외() { ... }
    ```

- [ ] **Test 5.2**: VotePlaceUseCase 테스트
  - Test cases:
    ```java
    @Test void 투표_성공() { ... }
    @Test void 중복_투표_시_이전_투표_변경() { ... }
    @Test void 확정된_약속에_투표_불가() { ... }
    ```

- [ ] **Test 5.3**: FinalizeMeetingUseCase 테스트
  - Test cases:
    ```java
    @Test void 약속_확정_성공() { ... }
    @Test void 이미_확정된_약속_재확정_예외() { ... }
    ```

**🟢 GREEN: 구현**
- [ ] **Task 5.4**: MeetingJpaEntity, MeetingCandidateJpaEntity, MeetingVoteJpaEntity
- [ ] **Task 5.5**: MeetingRepositoryImpl
- [ ] **Task 5.6**: CreateMeetingUseCase
- [ ] **Task 5.7**: VotePlaceUseCase (같은 voter 재투표 시 이전 투표 변경)
- [ ] **Task 5.8**: FinalizeMeetingUseCase
- [ ] **Task 5.9**: GetMeetingResultUseCase (투표 현황, 각 장소별 득표수)
- [ ] **Task 5.10**: MeetingsController
  - `POST /api/groups/{groupId}/meetings` - 약속 생성
  - `GET /api/groups/{groupId}/meetings/{id}` - 약속 상세 + 투표 현황
  - `POST /api/meetings/{id}/vote` - 투표 (`{"placeId":1,"voterName":"홍길동"}`)
  - `POST /api/meetings/{id}/finalize` - 확정 (`{"confirmedPlaceId":1}`)

- [ ] **Task 5.11**: 프론트엔드 - 약속 정하기 페이지 (바이브 코딩)
  - 후보 장소 카드, 투표 버튼, 투표 현황 바 UI

**🔵 REFACTOR**
- [ ] **Task 5.12**: 투표 결과 집계 로직 도메인 서비스로 추출

#### Quality Gate ✋

**⚠️ STOP: Do NOT proceed to Phase 6 until ALL checks pass**

**API Tests**:
```bash
# 약속 생성
curl -X POST http://localhost:8080/api/groups/1/meetings \
  -H "Content-Type: application/json" \
  -d '{"title":"이번 주말 약속","candidatePlaceIds":[1,2,3]}'

# 투표
curl -X POST http://localhost:8080/api/meetings/1/vote \
  -H "Content-Type: application/json" \
  -d '{"placeId":2,"voterName":"홍길동"}'

# 투표 현황 조회
curl http://localhost:8080/api/groups/1/meetings/1

# 약속 확정
curl -X POST http://localhost:8080/api/meetings/1/finalize \
  -H "Content-Type: application/json" \
  -d '{"confirmedPlaceId":2}'
```

**Manual Test Checklist**:
- [ ] 약속 생성 및 후보 장소 설정
- [ ] 여러 명 투표 후 득표수 정확히 집계
- [ ] 약속 확정 후 status CONFIRMED 변경
- [ ] 확정 후 재투표 불가

---

### Phase 6: 후기 시스템 & 전체 UI 완성
**Goal**: 방문 후기 API, 전체 프론트엔드 UI 완성, 통합 테스트
**Estimated Time**: 3-4 hours
**Status**: ⏳ Pending

#### Tasks

**🔴 RED: 후기 테스트**
- [ ] **Test 6.1**: AddReviewUseCase 테스트
  - Test cases:
    ```java
    @Test void 후기_작성_성공() { ... }
    @Test void 별점_범위_초과_예외() { ... }  // 1-5 범위
    ```

**🟢 GREEN: 구현**
- [ ] **Task 6.2**: ReviewJpaEntity, ReviewRepositoryImpl
- [ ] **Task 6.3**: AddReviewUseCase, GetReviewsUseCase
- [ ] **Task 6.4**: ReviewsController
  - `POST /api/places/{placeId}/reviews` - 후기 작성
  - `GET /api/places/{placeId}/reviews` - 후기 목록

- [ ] **Task 6.5**: 프론트엔드 - 후기 작성 UI (바이브 코딩)
- [ ] **Task 6.6**: 프론트엔드 - 전체 네비게이션 (홈, 장소, 그룹, 약속)
- [ ] **Task 6.7**: 프론트엔드 - 메인 페이지 (대시보드)
- [ ] **Task 6.8**: 통합 테스트 시나리오 수동 실행

**🔵 REFACTOR: 완성도 향상**
- [ ] **Task 6.9**: API 응답 형식 통일 (`ApiResponse<T>` 래퍼)
- [ ] **Task 6.10**: 에러 코드 체계화 (`ErrorCode` Enum)
- [ ] **Task 6.11**: Swagger/OpenAPI 문서 추가 (`springdoc-openapi`)
- [ ] **Task 6.12**: `application-prod.yml` 배포용 설정 파일 분리

#### Quality Gate ✋

**⚠️ 최종 품질 게이트 - 모든 항목 통과 필요**

**TDD Compliance**:
- [ ] 전체 백엔드 테스트 커버리지 ≥80%

**Integration Test - 전체 흐름**:
```bash
# 1. 장소 등록
# 2. 그룹 생성 + 참여
# 3. 그룹에 장소 공유
# 4. 약속 생성 + 투표 + 확정
# 5. 후기 작성
```

**Validation Commands**:
```bash
cd backend && ./gradlew test
./gradlew jacocoTestReport
# 리포트 확인: build/reports/jacoco/test/html/index.html
./gradlew build
```

**Manual Test Checklist**:
- [ ] 전체 사용자 여정 브라우저에서 동작
- [ ] 모바일 뷰 기본 동작
- [ ] Swagger UI (`http://localhost:8080/swagger-ui.html`) 접속 확인

---

## ⚠️ Risk Assessment

| Risk | Probability | Impact | Mitigation Strategy |
|------|-------------|--------|---------------------|
| JPA N+1 쿼리 문제 | High | Medium | `@EntityGraph` 또는 `fetch join` 사용, Phase 3에서 주의 |
| Clean Architecture 레이어 위반 | Medium | High | 코드 리뷰 시 패키지 의존성 방향 확인 (domain → application → infrastructure) |
| CORS 설정 오류 | Low | High | Phase 1에서 먼저 해결 |
| PostgreSQL 연결 실패 | Low | High | Docker Compose로 DB 환경 통일 |

---

## 🔄 Rollback Strategy

### If Phase 1 Fails
- 프로젝트 폴더 삭제 후 재생성

### If Phase 2-3 Fails
- 해당 도메인 패키지 삭제
- DB 테이블 DROP 후 재생성

### If Phase 4-6 Fails
- 해당 Phase 파일만 삭제
- 이전 Phase의 동작하는 상태 유지

---

## 📊 Progress Tracking

### Completion Status
- **Phase 1**: ⏳ 0% - 프로젝트 설정
- **Phase 2**: ⏳ 0% - Domain Layer
- **Phase 3**: ⏳ 0% - 장소 관리
- **Phase 4**: ⏳ 0% - 그룹/공유
- **Phase 5**: ⏳ 0% - 약속 정하기
- **Phase 6**: ⏳ 0% - 후기/완성

**Overall Progress**: 0% complete

### Time Tracking
| Phase | Estimated | Actual | Variance |
|-------|-----------|--------|----------|
| Phase 1 (Scaffold) | 2-3 hours | - | - |
| Phase 2 (Domain) | 2-3 hours | - | - |
| Phase 3 (장소 관리) | 3-4 hours | - | - |
| Phase 4 (그룹/공유) | 2-3 hours | - | - |
| Phase 5 (약속 정하기) | 2-3 hours | - | - |
| Phase 6 (후기/완성) | 3-4 hours | - | - |
| **Total** | 14-20 hours | - | - |

---

## 📝 Notes & Learnings

### Clean Architecture 주요 원칙 (항상 체크)
```
의존성 방향: Presentation → Application → Domain ← Infrastructure

✅ 올바른 의존성:
  Controller → UseCase → Domain Entity
  RepositoryImpl → DomainRepository (interface)

❌ 잘못된 의존성:
  Domain → JPA Entity (절대 금지)
  UseCase → Controller
```

### Implementation Notes
- (구현 중 발견한 인사이트를 여기에 기록)

### Blockers Encountered
- (없음)

---

## 📚 References

### Documentation
- [Spring Boot 공식 문서](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Clean Architecture - Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Next.js App Router](https://nextjs.org/docs/app)

---

## ✅ Final Checklist

**Before marking plan as COMPLETE**:
- [ ] 모든 Phase 완료 및 품질 게이트 통과
- [ ] 전체 통합 테스트 수행
- [ ] Swagger API 문서 완성
- [ ] Clean Architecture 의존성 방향 검증
- [ ] 전체 사용자 여정 브라우저 테스트

---

**Plan Status**: 🔄 Ready to Start
**Next Action**: Phase 1 시작 - Spring Boot 프로젝트 생성 및 폴더 구조 설정
**Blocked By**: None

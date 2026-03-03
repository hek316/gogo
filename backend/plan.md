# GoGo Backend — 아키텍처 분석 & 개선 계획

> 작성일: 2026-03-03
> 기준: Clean Architecture/DDD를 참고하되, 생산성·유지보수성을 우선하는 Pragmatic Engineering을 기준으로 한다.

---

## Part 1. 현재 아키텍처 분석

### 현재 구조

```
┌─────────────────────────────────────────────────────┐
│              Presentation Layer                      │
│   AuthController / PlacesController / GroupsController│
│   MeetingsController / ReviewsController            │
│              GlobalExceptionHandler                  │
└─────────────────┬───────────────────────────────────┘
                  │ DTO (Request/Response)
┌─────────────────▼───────────────────────────────────┐
│              Application Layer                       │
│   24+ UseCase (각 1개 비즈니스 오퍼레이션)            │
│   SecurityContextHelper (@Component)                 │
│   KakaoOAuthClient / GoogleOAuthClient              │
└─────────────────┬───────────────────────────────────┘
                  │ Domain Interface (DI 역전)
┌─────────────────▼───────────────────────────────────┐
│               Domain Layer                           │
│   Entity: User/Place/Group/Meeting/Review/...        │
│   Repository Interfaces (순수 Java)                  │
│   Enum: PlaceStatus/MeetingStatus/OAuthProvider     │
└─────────────────┬───────────────────────────────────┘
                  │ Adapter Implementation
┌─────────────────▼───────────────────────────────────┐
│            Infrastructure Layer                      │
│   JPA Entity + Spring Data Repository               │
│   Domain↔JPA Mapper (PlaceMapper/GroupMapper/...)   │
│   SecurityConfig / JwtAuthenticationFilter           │
│   NaverLocalApiClient / RequestLoggingFilter         │
└─────────────────────────────────────────────────────┘
```

### 종합 평가

| 항목 | 점수 | 비고 | 대응 |
|------|------|------|------|
| 레이어 설계 | ★★★★☆ | UseCase 분리 구조 우수 | - |
| 도메인 모델 정확도 | ★★★☆☆ | FK 누락 (Review, MeetingVote, GroupMember) | 추후 고려 |
| 보안 | ★★☆☆☆ | 토큰 URL 노출, SSRF 위험 | - |
| 운영 안전성 | ★★☆☆☆ | `ddl-auto:update` 프로덕션 사용 중 | 추후 고려 |
| 확장성 | ★★★☆☆ | 현재 규모엔 충분, 페이지네이션 부재 | 추후 고려 |
| 테스트 커버리지 | ★★★☆☆ | 단위 테스트 존재, 통합 테스트 부재 | - |

---

## Part 2. 아키텍처 결정 기록 (ADR)

> 리뷰 과정에서 도출된 팀의 명시적 설계 결정. 향후 논의의 기준점으로 활용한다.

### ADR-001: Domain/JPA 이중 구조 → 단일 클래스 통합

**상태:** 결정됨

**배경:**
현재 Place, Group 등 모든 엔티티가 Domain Entity + JPA Entity + Mapper 3개 파일로 구성되어 있다. 그러나 Domain Entity와 JPA Entity가 필드 구조에서 완전히 1:1 대응이고, Mapper도 단순 필드 복사에 불과하다.

**결정:**
`@Entity` 어노테이션을 Domain 클래스에 직접 붙이고, JPA Entity와 Mapper를 제거한다.
도메인 메서드(`markAsVisited()`, `addMember()` 등)는 JPA Entity 클래스 안에 함께 유지한다.

> ⚠️ **명시적 트레이드오프:** 이 결정은 Clean Architecture 순수성을 포기하는 선택이다.
> Clean Architecture는 Domain 레이어에 Infrastructure 어노테이션(`@Entity`, `@Column` 등)이 침투하는 것을 명시적으로 금지한다.
> 현재 GoGo 프로젝트에서는 Domain ↔ JPA 1:1 대응이라는 단순한 현실을 근거로, 구조적 순수성보다 파일 수 감소와 개발 속도를 우선한다.
> JPA와 도메인 모델이 달라지는 시점에 즉시 분리를 복원해야 한다.

**분리를 유지해야 하는 시점 (재결정 트리거):**
- JPA Entity에 상속 전략(`JOINED`, `SINGLE_TABLE`)이 필요할 때
- 하나의 테이블이 여러 도메인 객체에 매핑될 때
- DB 스키마가 도메인 모델과 구조적으로 달라질 때

**결과:**
- 파일 수: 엔티티당 3개 → 1개
- 변경 비용: 필드 1개 추가 시 3개 파일 → 1개 파일
- 복잡도: Mapper 레이어 제거로 코드 흐름 단순화

---

### ADR-002: UseCase는 복잡한 오퍼레이션에 한해 유지한다 (Complexity-based)

**상태:** 결정됨

**배경:**
"레이어드 아키텍처(Controller → Service → Repository)로 충분하지 않은가?"라는 질문에서 출발.

**패턴 출처:**
UseCase 패턴은 Uncle Bob(Robert C. Martin)의 *Clean Architecture*(2017)와 Ivar Jacobson의 *Use Case Driven Object Modeling*(1999)에서 유래한다. "각 Use Case는 하나의 사용자 시나리오를 나타내며, 시스템의 의도를 명시적으로 드러낸다(Screaming Architecture)"는 원칙에 기반한다. Spring Boot 생태계의 전통적 `@Service` 패턴은 이 분리를 구조적으로 강제하지 않는다.

**트레이드오프 분석:**

| 관점 | UseCase 패턴 | 전통 Service 패턴 |
|------|-------------|-----------------|
| 파일 수 | 오퍼레이션당 1개 클래스 (현재 26개) | 도메인당 1개 클래스 (약 5-8개) |
| 가독성 | 파일명 = 사용자 시나리오 (Screaming) | 파일 내부 메서드 탐색 필요 |
| 트랜잭션 경계 | UseCase 간 직접 의존 시 propagation 복잡 | 한 Service 내 메서드 간 트랜잭션 자연스러움 |
| 신규 기능 추가 | 새 파일 1개 추가 (기존 코드 무변경) | 기존 Service 클래스 수정 |
| 팀 진입 장벽 | Clean Architecture 이해 필요 | Spring Boot 전통 패턴으로 즉시 이해 |
| 테스트 용이성 | UseCase별 독립 단위 테스트 자연스러움 | 메서드별 테스트, 의존 관계 복잡해질 수 있음 |

**복잡/단순 분류 기준:**

**복잡** (UseCase 유지): 다단계 시나리오 / 정책 판단 포함 / 실패 케이스가 여러 개인 오퍼레이션.
**단순** (도메인별 Service로 통합): 단일 Repository CRUD/조회, 분기 없음, 실패 케이스 1개 이하.

단순 CRUD/조회는 도메인별 `XxxQueryService` / `XxxCommandService`로 통합한다.


**현재 GoGo에서 이 결정을 내린 실질적 근거:**

1. **복잡한 UseCase의 SRP 혜택이 명확**: `KakaoLoginUseCase`(외부 OAuth + JWT 발급), `RefreshTokenUseCase`(탈취 감지 + 로테이션), `FinalizeMeetingUseCase`(투표 집계 + 상태 전이)는 Service 메서드로 합쳤을 때 클래스가 지나치게 커진다.
2. **"한 파일 = 한 사용자 시나리오"가 온보딩에 유리**: 신규 진입자가 `AddPlaceUseCase.java`만 열면 해당 기능의 전체 흐름을 파악할 수 있다.
3. **Mock 기반 단위 테스트가 이미 정착**: Repository 인터페이스 + UseCase 조합으로 `@MockBean` 없이 순수 Mockito 테스트가 가능하며, 이 패턴이 전 팀에 일관되게 적용되어 있다.

**가드레일:**
- UseCase는 다른 UseCase를 직접 호출하지 않는다(트랜잭션/의존성 복잡도 방지).

**알려진 리스크:**

- **UseCase 간 트랜잭션 경계**: UseCase A에서 UseCase B를 inject해 호출하면 `@Transactional` propagation이 프록시를 거쳐야 한다. `REQUIRES_NEW` 등 기대와 다른 동작이 발생할 수 있다. **현재는 UseCase 간 직접 의존이 없으므로 잠재적 이슈로만 기록**. 발생 시 Domain Service로 공통 로직을 추출해야 한다.
- **`domain/service/` 패키지가 비어 있음**: 복잡한 도메인 로직(인기도 점수 계산: `COUNT(DISTINCT gp.id) + COUNT(DISTINCT pl.id) * 0.5`)이 Repository 쿼리에 매립되어 있다. 비즈니스 규칙이 SQL로 표현되는 것은 테스트·변경 모두 어렵다. 향후 Domain Service로 추출을 권장한다.

**UseCase → Service 전환 재검토 트리거:**
- UseCase 수가 50개를 초과하여 탐색이 어려워질 때
- UseCase 간 직접 의존이 3개 이상 발생하여 트랜잭션 경계가 복잡해질 때
- 팀 규모가 커져 Spring Boot 전통 패턴 친숙도가 Clean Architecture보다 높은 인원이 다수가 될 때

**최종 권장 레이어 구조:**
```
// 복잡한 오퍼레이션
Controller → UseCase → Repository(interface) → JPA Entity(도메인 메서드 포함)

// 단순 CRUD/조회
Controller → XxxQueryService / XxxCommandService → Repository(interface) → JPA Entity
```

---

### ADR-003: AuthContext 인터페이스 도입

**상태:** 결정됨

**배경:**
`SecurityContextHelper`가 `infrastructure.security` 패키지에 있음에도 불구하고,
`AddPlaceUseCase`, `CreateGroupUseCase` 등 Application 레이어에서 직접 임포트하고 있다.
이는 의존 방향 위반이다 (Application → Infrastructure).

**결정:**
`application/port/AuthContext.java` 인터페이스를 신규 추가하고, `SecurityContextHelper`가 이를 구현하도록 변경한다.

```java
// 신규: application/port/AuthContext.java
public interface AuthContext {
    Optional<Long> currentUserId();
    Optional<String> currentNickname();
}

// 수정: SecurityContextHelper.java
@Component
public class SecurityContextHelper implements AuthContext {
    // 기존 구현 그대로 유지, implements 선언만 추가
}

// 수정: UseCase들
// Before: private final SecurityContextHelper securityContextHelper;
// After:  private final AuthContext authContext;
```

**영향 파일:** `AddPlaceUseCase.java:7,16`, `CreateGroupUseCase.java:7,16`
및 SecurityContextHelper를 직접 사용하는 모든 UseCase

---

### ADR-004: AuthController 레이어 위반 수정

**상태:** 결정됨

**배경:**
`PlacesController`, `GroupsController` 등 대부분의 컨트롤러는 UseCase만 의존하지만,
`AuthController`만 `UserRepository`와 `AuthenticatedUser`(Infrastructure)를 직접 참조한다.

```java
// 현재 위반 코드 (AuthController.java:32,126)
private final UserRepository userRepository; // Controller가 Repository 직접 주입
userRepository.findById(principal.userId()); // Controller가 직접 DB 조회
```

**결정:**
`GetCurrentUserUseCase`를 신규 추가하고, `AuthController`에서 Repository 의존을 제거한다.

---

## Part 3. 우선순위 개선 로드맵

### P1 — 즉시 수정 (레이어 경계 위반) ✅ 완료

- [x] **`AuthController`에서 `UserRepository` 제거** (완료 — GetCurrentUserUseCase 위임)
  - `GetCurrentUserUseCase` 신규 추가
  - `AuthController`는 UseCase만 의존하도록 변경

---

### P2 — 이번 스프린트 (아키텍처 정합성) ✅ 완료

- [x] **`AuthContext` 인터페이스 추가** (완료 — 11개 UseCase 교체)
  - `application/port/AuthContext.java` 신규 생성
  - `SecurityContextHelper implements AuthContext` 추가
  - 모든 UseCase의 `SecurityContextHelper` → `AuthContext` 교체
  - 파일: `AddPlaceUseCase.java:7`, `CreateGroupUseCase.java:7`

- [x] **Domain Entity + JPA Entity 통합 Phase 1** (완료 — Place/User/Review/PlaceLike)
  - `@Entity`를 도메인 클래스에 직접 추가
  - `PlaceJpaEntity`, `UserJpaEntity`, `ReviewJpaEntity`, `PlaceLikeJpaEntity`, `PlaceMapper` 삭제 (5개)
  - `GroupMapper`, `MeetingMapper` — Phase 2 (보류)

---

### P3 — 다음 스프린트 (ADR-002 Service 추출)

- [ ] **단순 UseCase → `XxxQueryService` / `XxxCommandService` 통합**
  - 단순 기준: 단일 Repository CRUD/조회, 분기 없음, 실패 케이스 1개 이하
  - 단순 CRUD/조회는 도메인별 `XxxQueryService` / `XxxCommandService`로 통합한다
  - 대상 후보: `DeletePlaceUseCase`, `GetGroupUseCase`, `GetReviewsUseCase`, `GetGroupPlacesUseCase` 등
  - 완료 기준: UseCase 수 26개 → 15개 이하, `./gradlew test` 통과

---

### 보류 — 현재 상황에서 고려하지 않음

> 문제는 인지하고 있으나 지금 당장 대응하지 않기로 결정한 항목.
> 상황 변화 시 P1/P2로 승격한다.

| 위험도 | 항목 | 이유 |
|--------|------|------|
| 🔴 즉시 위험 | `ddl-auto: update` → `validate` + Flyway | **프로덕션에서 칼럼 자동 삭제·변경 가능. 데이터 손실 위험.** 스키마 변경 시 마이그레이션 없이 적용되므로, 팀원이 필드를 리네임하면 기존 컬럼 데이터가 사라진다. 즉시 `validate` + Flyway로 전환을 강력 권고. |
| 🟡 중기 위험 | 토큰 전달 → HttpOnly Secure Cookie | 현재 URL 파라미터로 토큰 노출. 브라우저 히스토리·서버 로그에 기록될 수 있음. 프론트 협업 필요. |
| 🟡 중기 위험 | URL 미리보기 SSRF 방어 | 내부 네트워크 접근 가능. 공격자가 EC2 메타데이터(169.254.x.x) 조회 가능. |
| 🟢 낮음 | `show-sql: false` (프로덕션) | 성능·보안 로그 노이즈. 현재 기능에 영향 없음. |
| 🟢 낮음 | 도메인 모델 FK 복원 (Review/MeetingVote/GroupMember) | 참조 무결성 부재. 현재 규모에선 문제 없음. |
| 🟢 낮음 | 전체 목록 API Pageable 적용 | 데이터 증가 시 성능 저하. 현재 데이터 규모에선 허용 가능. |

---

## Part 4. 성공 기준 (검증 가능)

- [x] `AuthController` import에 `infrastructure.*` 없음 (`AuthenticatedUser`만 잔존 — logout 필요)
- [x] UseCase import에 `infrastructure.security.*` 없음
- [x] Domain/JPA 통합 후 기존 API 동작이 동일함 (`./gradlew test` BUILD SUCCESSFUL)

---

## Part 5. 잘된 점 (유지할 것)

| 항목 | 근거 |
|------|------|
| UseCase 단일 책임 분리 (24개) | 비즈니스 로직이 Controller/Repository에 새어나가지 않음 |
| PlacesController, GroupsController 경계 준수 | UseCase만 의존, 올바른 레이어 경계의 예시 |
| SecurityContextHelper 리팩터링 | 11개 파일 중복 제거, 다음 단계는 AuthContext 인터페이스화 |
| Repository 인터페이스 패턴 | 테스트 용이성 확보, Mock 주입 가능 — Domain/JPA 통합 후에도 유지 |
| 도메인 엔티티 순수 Java 메서드 | `markAsVisited()`, `addMember()` 등 도메인 행위 표현 — 통합 후에도 유지 |

---

*설계 기준: Clean Architecture/DDD 참고, 생산성·유지보수성 우선 Pragmatic Engineering / SOLID Principles / Production-First Engineering*

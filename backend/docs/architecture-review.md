# GoGo Backend Architecture Review

**Reviewer**: Senior Backend Developer Perspective
**Date**: 2026-03-09
**Iteration**: 3/3 (Final)

---

## Executive Summary

전반적으로 Clean Architecture의 핵심 원칙(의존성 역전, 계층 분리)을 의식하고 설계한 흔적이 보인다.
`AuthContext` 포트 인터페이스, 도메인 Repository 인터페이스 등 좋은 패턴이 있지만,
**일관성 부재와 의존성 방향 위반**이 여러 곳에서 발견된다.

**심각도 등급**: CRITICAL > MAJOR > MINOR

---

## 1. 계층 분리 (Layer Separation)

### 현재 구조
```
presentation/api/        → Controllers
application/
  service/               → Business logic
  usecase/               → Standalone use cases
  dto/                   → Request/Response DTOs
  port/                  → Interfaces (AuthContext only)
  auth/                  → JwtService, KakaoOAuthClient, GoogleOAuthClient
domain/
  entity/                → Domain entities (일부 JPA 혼합)
  repository/            → Repository interfaces
infrastructure/
  config/                → SecurityConfig, CorsConfig
  security/              → JwtAuthenticationFilter, SecurityContextHelper
  persistence/           → JPA repositories, JPA entities, Mappers
  external/              → NaverLocalApiClient
```

### CRITICAL: `application/auth/` 패키지의 정체성 혼란

| 클래스 | 현재 위치 | 문제점 | 올바른 위치 |
|--------|----------|--------|------------|
| `JwtService` | `application/auth/` | JWT 라이브러리(jjwt)에 직접 의존하는 **인프라 관심사** | `infrastructure/security/` |
| `KakaoOAuthClient` | `application/auth/` | RestClient로 외부 HTTP 호출하는 **인프라 관심사** | `infrastructure/external/` |
| `GoogleOAuthClient` | `application/auth/` | 위와 동일 | `infrastructure/external/` |

**근거**: Application 계층은 "어떤 기술로 구현하느냐"를 모르는 계층이다. JWT 서명, HTTP 호출은 기술적 세부사항이므로 Infrastructure에 위치해야 한다.

### MAJOR: Domain Entity와 JPA 어노테이션 혼재의 불일치

| Entity | JPA 어노테이션 직접 보유 | 별도 JpaEntity 존재 | Mapper 존재 |
|--------|:---:|:---:|:---:|
| `Place` | O | X | X |
| `User` | O | X | X |
| `Review` | O | X | X |
| `PlaceLike` | O | X | X |
| `RefreshToken` | ? | O (`RefreshTokenJpaEntity`) | X |
| `Group` | **X (순수 POJO)** | O (`GroupJpaEntity`) | O (`GroupMapper`) |
| `Meeting` | **X (순수 POJO)** | O (`MeetingJpaEntity`) | O (`MeetingMapper`) |
| `MeetingVote` | ? | O (`MeetingVoteJpaEntity`) | ? |
| `GroupPlace` | ? | O (`GroupPlaceJpaEntity`) | ? |

**두 가지 전략이 혼재**:
- 전략 A: Domain Entity에 `@Entity` 직접 부착 (Place, User, Review) → 간단하지만 도메인이 JPA에 오염
- 전략 B: Domain Entity는 순수 POJO, 별도 JpaEntity + Mapper (Group, Meeting) → 클린하지만 보일러플레이트 증가

**둘 중 하나를 선택하고 통일해야 한다.** 혼재는 신규 개발자의 혼란을 야기하고, 유지보수 비용을 불필요하게 높인다.

---

## 2. 의존성 방향 (Dependency Direction)

### 올바른 의존성 방향
```
Presentation → Application → Domain ← Infrastructure
                    ↑                        |
                    └────────────────────────┘
```

### CRITICAL: Application → Infrastructure 직접 의존 (DIP 위반)

**`SearchPlacesUseCase` → `NaverLocalApiClient`**
```java
// SearchPlacesUseCase.java (application 계층)
import com.gogo.infrastructure.external.NaverLocalApiClient;  // ← VIOLATION
```
Application UseCase가 Infrastructure 구체 클래스를 직접 import한다.
`NaverLocalApiClient`에 대한 포트 인터페이스(`PlaceSearchPort` 등)가 필요하다.

**`KakaoLoginUseCase` → `KakaoOAuthClient`**
```java
// KakaoLoginUseCase.java (application 계층)
import com.gogo.application.auth.KakaoOAuthClient;  // KakaoOAuthClient가 application에 있어서 컴파일은 OK
```
겉보기에는 같은 계층이라 문제없어 보이지만, `KakaoOAuthClient`의 본질이 Infrastructure이므로 **논리적 DIP 위반**이다.

### MAJOR: Presentation → Infrastructure 직접 의존

**`AuthController` → `AuthenticatedUser`**
```java
// AuthController.java (presentation 계층)
import com.gogo.infrastructure.security.AuthenticatedUser;  // ← VIOLATION
```
`@AuthenticationPrincipal AuthenticatedUser principal`에서 인프라 타입을 직접 사용한다.

### 잘된 점 (Good)
- `AuthContext` 포트 인터페이스 → `SecurityContextHelper` 구현: **교과서적 DIP**
- `PlaceRepository` 도메인 인터페이스 → `PlaceRepositoryImpl` 인프라 구현: **정석 패턴**

---

## 3. 테스트 가능성 (Testability)

### 현재 테스트 현황
```
test/
├── application/service/
│   ├── GroupServiceTest.java
│   ├── MeetingServiceTest.java
│   ├── PlaceCommandServiceTest.java
│   ├── PlaceQueryServiceTest.java
│   └── ReviewServiceTest.java
├── domain/entity/
│   ├── GroupTest.java
│   └── PlaceTest.java
└── presentation/api/
    └── PlacesControllerTest.java
```

### MAJOR: 외부 API 클라이언트에 인터페이스 부재 → 단위 테스트 곤란

| 클래스 | 인터페이스 | 단위 테스트 시 Mock 가능성 |
|--------|:---:|---|
| `NaverLocalApiClient` | X | 구체 클래스 Mock 필요 (Mockito `@Mock`으로 가능하나, DIP 위반) |
| `KakaoOAuthClient` | X | 동일 |
| `GoogleOAuthClient` | X | 동일 |
| `JwtService` | X | 동일 |

`SearchPlacesUseCase`를 테스트하려면 `NaverLocalApiClient`를 Mockito로 Mock해야 하는데,
포트 인터페이스 없이 구체 클래스를 Mock하면 내부 구현 변경 시 테스트도 함께 깨진다.

### MAJOR: UseCase/Auth에 대한 테스트 부재

- `KakaoLoginUseCase`, `GoogleLoginUseCase`, `RefreshTokenUseCase` → **테스트 0개**
- `SearchPlacesUseCase`, `FetchPlacePreviewUseCase` → **테스트 0개**
- `AuthService` → **테스트 0개**
- `PlaceLikeService` → **테스트 0개**

가장 복잡한 인증/토큰 로직에 테스트가 없다는 것은 큰 리스크다.

### MINOR: Controller 테스트 커버리지 부족

Places만 테스트하고 Auth/Groups/Meetings/Reviews Controller 테스트가 없다.

---

## 4. 확장성 (Scalability)

### MAJOR: Service/UseCase 구분 기준 불명확

현재 혼재:
- **Service**: `PlaceQueryService`, `PlaceCommandService`, `GroupService`, `MeetingService`, `AuthService`, `ReviewService`, `PlaceLikeService`
- **UseCase**: `SearchPlacesUseCase`, `FetchPlacePreviewUseCase`, `KakaoLoginUseCase`, `GoogleLoginUseCase`, `RefreshTokenUseCase`

Service와 UseCase의 경계 기준이 문서화되어 있지 않다. 새 기능 추가 시 "Service에 메서드를 추가할 것인가? UseCase를 만들 것인가?"의 판단이 개발자마다 달라질 수 있다.

**현재 암묵적 기준 추정**:
- 외부 API 호출이 필요한 단일 기능 → UseCase
- CRUD + 도메인 로직 → Service

이 기준을 ADR(Architecture Decision Record)로 명시해야 한다.

### MAJOR: `GlobalExceptionHandler`에서 `debug` 필드 노출

```java
// GlobalExceptionHandler.java:39
response.body(Map.of(
    "error", "서버 오류가 발생했습니다.",
    "debug", e.getClass().getSimpleName() + ": " + e.getMessage()  // ← 운영 환경 위험
));
```
운영 환경에서 예외 클래스명과 메시지가 클라이언트에 노출되면 보안 취약점이 된다.
`@Profile("!prod")` 또는 환경 변수 분기가 필요하다.

### MINOR: 비즈니스 예외 타입 부재

모든 비즈니스 오류가 `IllegalArgumentException`/`IllegalStateException`으로 처리된다.
도메인 고유 예외(`PlaceNotFoundException`, `AlreadyConfirmedException` 등)가 없어서:
- 예외 핸들링이 세밀하지 않다 (400/404/409 구분 불가)
- 로깅 시 비즈니스 오류와 프로그래밍 버그 구분이 안 된다

---

## 5. 결합도 (Coupling)

### MAJOR: Service 간 수평 의존

```java
// PlaceCommandService.java
private final PlaceQueryService placeQueryService;  // Command가 Query에 의존
```
`PlaceCommandService.markVisited()`가 `PlaceQueryService.toResponse()`를 호출한다.
CQRS 분리를 시도했다면, Command가 Query를 직접 참조하는 것은 분리 의미를 퇴색시킨다.

**개선안**: `toResponse()` 로직을 별도의 `PlaceResponseMapper` 또는 DTO의 정적 메서드로 추출하여 공유한다.

### MAJOR: `KakaoLoginUseCase.TokenPair`의 범용 사용

```java
// AuthController.java:83 — refresh 응답
KakaoLoginUseCase.TokenPair tokens = refreshTokenUseCase.execute(refreshToken);

// AuthController.java:115 — 범용 OAuth callback
Function<String, KakaoLoginUseCase.TokenPair> loginFn
```
`TokenPair`가 `KakaoLoginUseCase`의 inner record인데, Google 로그인과 Refresh에서도 사용된다.
**Kakao에 종속된 이름의 타입이 범용 인증 흐름에서 사용**되는 것은 잘못된 결합이다.

**개선안**: `TokenPair`를 `application/dto/` 또는 `application/auth/`의 독립 record로 추출한다.

### MINOR: Controller 의존성 개수

`PlacesController`가 5개의 의존성을 주입받는다:
```java
PlaceQueryService, PlaceCommandService, FetchPlacePreviewUseCase, SearchPlacesUseCase, PlaceLikeService
```
`AuthController`가 6개의 의존성을 주입받는다.

현재 규모에서는 관리 가능하지만, 기능 추가 시 급격히 비대해질 수 있다.
Facade 패턴이나 Controller 분리를 고려할 시점이다.

---

## 종합 점수

| 기준 | 점수 (10점 만점) | 등급 |
|------|:---:|:---:|
| 계층 분리 | 5/10 | `application/auth/`가 사실상 인프라, Entity 전략 불일치 |
| 의존성 방향 | 6/10 | AuthContext/Repository 인터페이스는 좋으나 외부 API 포트 부재 |
| 테스트 가능성 | 4/10 | 서비스 테스트는 있으나 인증/UseCase 테스트 전무 |
| 확장성 | 5/10 | Service/UseCase 경계 불명확, 예외 체계 부재 |
| 결합도 | 6/10 | 대부분 인터페이스 의존이나 Service 수평 결합, TokenPair 결합 |
| **종합** | **5.2/10** | **구조적 개선 필요** |

---

## 우선순위별 개선 로드맵

### P0 (즉시)
1. `application/auth/` → `infrastructure/`로 이동 + 포트 인터페이스 도입
2. `SearchPlacesUseCase` → `NaverLocalApiClient` 의존 제거 (포트 도입)
3. `TokenPair`를 독립 DTO로 추출
4. `GlobalExceptionHandler`의 `debug` 필드 운영 환경 노출 차단

### P1 (단기)
5. Domain Entity 전략 통일 (A or B 택일 후 마이그레이션)
6. 비즈니스 예외 계층 도입 (`BusinessException` → `NotFoundException`, `ConflictException` 등)
7. `PlaceCommandService` → `PlaceQueryService` 수평 의존 제거

### P2 (중기)
8. 인증/토큰 UseCase 단위 테스트 추가
9. Controller 테스트 확대
10. Service/UseCase 구분 기준 ADR 작성

---

---

## Iteration 2: 추가 발견사항

### CRITICAL: KakaoLoginUseCase와 GoogleLoginUseCase의 코드 중복 (DRY 위반)

두 UseCase의 `execute()` 메서드가 **90% 동일**하다:

```java
// KakaoLoginUseCase.execute()            // GoogleLoginUseCase.execute()
OAuthTokens = client.exchangeCode(code);  // OAuthTokens = client.exchangeCode(code);
UserInfo = client.getUserInfo(token);      // UserInfo = client.getUserInfo(token);
User user = upsertUser(userInfo);          // User user = upsertUser(userInfo);
String at = jwtService.generate(...);      // String at = jwtService.generate(...);
String rt = UUID.randomUUID()...;          // String rt = UUID.randomUUID()...;
refreshTokenRepository.save(...);          // refreshTokenRepository.save(...);
return new TokenPair(at, rt);              // return new KakaoLoginUseCase.TokenPair(at, rt);
```

`upsertUser()` 로직도 Provider enum만 다르고 완전히 동일하다.
**3번째 OAuth 제공자(Apple, Naver 등)가 추가되면 동일 코드가 3벌이 된다.**

### MAJOR: `User` Entity의 레거시 컬럼명

```java
// User.java:16
@Column(name = "kakao_id", nullable = false)  // ← 실제로는 oauthId인데 컬럼명이 kakao_id
private String oauthId;
```
Google OAuth 추가 후에도 DB 컬럼명이 `kakao_id`로 남아있다. 기능적으로는 동작하지만,
DB를 직접 조회하는 사람에게 혼란을 준다. Flyway/Liquibase 마이그레이션이 필요하다.

### MINOR: `MeetingService.vote()`의 인증 미적용

```java
// MeetingService.java:47
public MeetingResponse vote(Long meetingId, VoteRequest request) {
    // ...
    meetingVoteRepository.findByMeetingIdAndVoterName(meetingId, request.voterName());
```
투표자를 `request.voterName()`으로 받는데, 이는 **클라이언트가 임의 닉네임으로 투표 가능**하다는 뜻이다.
다른 서비스들은 `AuthContext`에서 현재 사용자를 가져오는데, Meeting만 예외다.

---

## Iteration 2: 구체적 코드 개선안

### 개선안 1: 포트 인터페이스 도입 (DIP 수정)

**목표 구조:**
```
application/
  port/
    AuthContext.java              (기존)
    OAuthPort.java                (NEW — OAuth 추상화)
    TokenProvider.java            (NEW — JWT 추상화)
    PlaceSearchPort.java          (NEW — 외부 검색 API 추상화)
infrastructure/
  security/
    JwtTokenProvider.java         (이전 JwtService → TokenProvider 구현)
  external/
    KakaoOAuthAdapter.java        (이전 KakaoOAuthClient → OAuthPort 구현)
    GoogleOAuthAdapter.java       (이전 GoogleOAuthClient → OAuthPort 구현)
    NaverPlaceSearchAdapter.java  (이전 NaverLocalApiClient → PlaceSearchPort 구현)
```

**코드:**

```java
// application/port/OAuthPort.java
package com.gogo.application.port;

public interface OAuthPort {
    String buildAuthorizationUrl();
    OAuthUserInfo authenticate(String code);

    record OAuthUserInfo(String oauthId, String nickname, String profileImageUrl) {}
}
```

```java
// application/port/TokenProvider.java
package com.gogo.application.port;

public interface TokenProvider {
    String generateAccessToken(Long userId, String nickname);
    java.util.Optional<TokenClaims> validateAndExtract(String token);

    record TokenClaims(Long userId, String nickname) {}
}
```

```java
// application/port/PlaceSearchPort.java
package com.gogo.application.port;

import com.gogo.application.dto.PlaceSearchResult;
import java.util.List;

public interface PlaceSearchPort {
    List<PlaceSearchResult> search(String keyword);
}
```

### 개선안 2: OAuth 로그인 중복 제거 (Template Method 또는 전략 패턴)

```java
// application/service/OAuthLoginService.java
package com.gogo.application.service;

@Service
@Transactional
public class OAuthLoginService {

    private final Map<OAuthProvider, OAuthPort> oauthPorts;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;

    public TokenPair login(OAuthProvider provider, String code) {
        OAuthPort port = oauthPorts.get(provider);
        OAuthPort.OAuthUserInfo userInfo = port.authenticate(code);

        User user = upsertUser(userInfo, provider);

        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getNickname());
        String rawRefreshToken = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.create(rawRefreshToken, user.getId(),
                LocalDateTime.now().plusDays(7)));

        return new TokenPair(accessToken, rawRefreshToken);
    }

    private User upsertUser(OAuthPort.OAuthUserInfo info, OAuthProvider provider) {
        return userRepository.findByOauthIdAndProvider(info.oauthId(), provider)
                .map(user -> {
                    user.updateProfile(info.nickname(), info.profileImageUrl());
                    return userRepository.save(user);
                })
                .orElseGet(() -> userRepository.save(
                    User.create(info.oauthId(), info.nickname(),
                                info.profileImageUrl(), provider)));
    }
}
```

**효과**: `KakaoLoginUseCase` + `GoogleLoginUseCase` (146 lines) → `OAuthLoginService` (~40 lines).
4번째 Provider 추가 시 코드 변경 0줄, `OAuthPort` 구현체 1개만 추가하면 된다.

### 개선안 3: TokenPair 독립 추출

```java
// application/dto/TokenPair.java
package com.gogo.application.dto;

public record TokenPair(String accessToken, String refreshToken) {}
```

`KakaoLoginUseCase.TokenPair` 의 모든 참조를 이것으로 교체한다.

### 개선안 4: PlaceCommandService → PlaceQueryService 수평 의존 제거

```java
// 현재: PlaceCommandService가 PlaceQueryService.toResponse()를 호출
// 개선: PlaceResponse.from()의 오버로드를 활용하여 서비스 간 의존 제거

// PlaceCommandService.java — 개선 후
public PlaceResponse markVisited(Long id) {
    Place place = placeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다. id=" + id));
    place.markAsVisited();
    placeRepository.save(place);
    // PlaceQueryService 의존 없이 직접 응답 생성
    Long userId = authContext.currentUserId().orElse(null);
    return PlaceResponse.from(place,
            placeLikeRepository.countByPlaceId(place.getId()),
            userId != null && placeLikeRepository.existsByUserIdAndPlaceId(userId, place.getId()));
}
```

단, 이 경우 `PlaceCommandService`에도 `PlaceLikeRepository` 의존이 추가된다.
더 나은 대안은 `PlaceResponseAssembler`를 도입하는 것이다:

```java
// application/service/PlaceResponseAssembler.java
@Component
public class PlaceResponseAssembler {
    private final PlaceLikeRepository placeLikeRepository;
    private final AuthContext authContext;

    public PlaceResponse toResponse(Place place) {
        Long userId = authContext.currentUserId().orElse(null);
        return PlaceResponse.from(place,
                placeLikeRepository.countByPlaceId(place.getId()),
                userId != null && placeLikeRepository.existsByUserIdAndPlaceId(userId, place.getId()));
    }

    public List<PlaceResponse> toResponses(List<Place> places) {
        return places.stream().map(this::toResponse).toList();
    }
}
```

### 개선안 5: 비즈니스 예외 계층

```java
// domain/exception/BusinessException.java
public abstract class BusinessException extends RuntimeException {
    private final int statusCode;
    protected BusinessException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    public int getStatusCode() { return statusCode; }
}

// domain/exception/NotFoundException.java
public class NotFoundException extends BusinessException {
    public NotFoundException(String message) { super(message, 404); }
}

// domain/exception/ConflictException.java
public class ConflictException extends BusinessException {
    public ConflictException(String message) { super(message, 409); }
}

// domain/exception/UnauthorizedException.java
public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) { super(message, 401); }
}
```

```java
// GlobalExceptionHandler — 개선 후
@ExceptionHandler(BusinessException.class)
public ResponseEntity<Map<String, String>> handleBusiness(BusinessException e) {
    return ResponseEntity.status(e.getStatusCode())
            .body(Map.of("error", e.getMessage()));
}

@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, String>> handleGeneral(Exception e) {
    log.error("Unhandled exception: {}", e.getMessage(), e);
    // debug 필드 제거 — 운영 환경에서 내부 정보 노출하지 않음
    return ResponseEntity.status(500)
            .body(Map.of("error", "서버 오류가 발생했습니다."));
}
```

### 개선안 6: AuthController에서 인프라 타입 제거

```java
// 현재
@PostMapping("/logout")
public ResponseEntity<?> logout(@AuthenticationPrincipal AuthenticatedUser principal, ...)

// 개선: AuthContext를 활용
@PostMapping("/logout")
public ResponseEntity<?> logout(HttpServletResponse response) {
    authContext.currentUserId().ifPresent(authService::logout);
    clearCookies(response);
    return ResponseEntity.ok(Map.of("message", "로그아웃 되었습니다."));
}
```

---

## 개선 후 예상 의존성 그래프

```
presentation/api/
  └── application/service/, application/dto/

application/
  service/     → domain/repository/, application/port/
  usecase/     → application/port/ (NOT infrastructure)
  port/        → (interface only, no external deps)
  dto/         → (pure data, no deps)

domain/
  entity/      → (self-contained)
  repository/  → domain/entity/
  exception/   → (self-contained)

infrastructure/
  security/    → application/port/TokenProvider (implements)
  external/    → application/port/OAuthPort, PlaceSearchPort (implements)
  persistence/ → domain/repository/ (implements)
```

**Application 계층에서 Infrastructure import가 0개**가 되어야 한다.

---

## 수정된 종합 점수 (Iteration 2)

| 기준 | 현재 점수 | 개선 후 예상 | 핵심 개선 사항 |
|------|:---:|:---:|---|
| 계층 분리 | 5/10 | 8/10 | 포트 인터페이스 도입, auth 패키지 이동 |
| 의존성 방향 | 6/10 | 9/10 | Application→Infrastructure 참조 제거 |
| 테스트 가능성 | 4/10 | 7/10 | 포트 Mock으로 UseCase 테스트 가능 |
| 확장성 | 5/10 | 8/10 | OAuthPort로 Provider 추가 용이, 예외 계층 |
| 결합도 | 6/10 | 8/10 | 수평 의존 제거, TokenPair 독립 |
| **종합** | **5.2/10** | **8.0/10** | |

---

---

## Iteration 3: 추가 발견사항 + 최종 마이그레이션 계획

### 재스캔 결과: 추가 발견 4건

#### MAJOR: `ReviewService.addReview()` — 인증 미적용 (MeetingService와 동일 패턴)

```java
// ReviewService.java:22
public ReviewResponse addReview(Long placeId, AddReviewRequest request) {
    Review review = Review.create(placeId, request.authorName(), ...);
    //                              ↑ 클라이언트가 보낸 이름을 그대로 사용
}
```
`authorName`을 request body에서 받는다. **타인 명의로 리뷰 작성이 가능**하다.
`PlaceCommandService`는 `authContext.requireNickname()`을 사용하는데, Review만 예외다.

**영향받는 엔드포인트**: `POST /api/places/{placeId}/reviews` — 인증된 사용자라면 아무 이름으로나 리뷰 가능.

#### MAJOR: `MeetingsController` URL 패턴 불일치

```java
// MeetingsController.java — @RequestMapping 없음, 경로 직접 지정
@PostMapping("/api/groups/{groupId}/meetings")      // 그룹 하위 리소스
@GetMapping("/api/groups/{groupId}/meetings/{id}")   // 그룹 하위 리소스
@PostMapping("/api/meetings/{id}/vote")              // ← 갑자기 최상위 리소스
@PostMapping("/api/meetings/{id}/finalize")           // ← 갑자기 최상위 리소스
```
동일 컨트롤러 내에서 `/api/groups/{gid}/meetings/`과 `/api/meetings/`이 혼재.
REST API 일관성이 깨진다. `groupId` path variable을 받아놓고 사용하지 않는 `getMeeting()`도 있다.

#### MINOR: `DataInitializer` — 운영 환경 분리 없음

```java
// DataInitializer.java — @Component 무조건 활성화
@Component
public class DataInitializer implements CommandLineRunner {
```
`@Profile("dev")` 또는 `@Profile("local")` 없이 운영 환경에서도 seed 데이터 삽입을 시도한다.
현재는 `if (!placeRepository.findAll().isEmpty())` 가드가 있어서 실제 문제는 안 되지만,
운영 DB가 비어있는 경우(새 배포, DB 마이그레이션 후) 테스트 데이터가 삽입될 수 있다.

#### MINOR: `PlaceQueryService.toResponse()` — N+1 쿼리 잠재적 문제

```java
// PlaceQueryService.java:52
PlaceResponse toResponse(Place place) {
    // 장소 1건당 쿼리 2개 발생: countByPlaceId + existsByUserIdAndPlaceId
}
```
`getPlaces()`, `getPopularPlaces()`, `getRecent()`에서 리스트를 순회하며 `toResponse()`를 호출한다.
장소 N개면 **쿼리 2N+1개** 발생. 현재 데이터 규모에서는 문제없으나 데이터 증가 시 성능 병목.

---

### 전체 문제 종합 (Iteration 1~3)

| # | 심각도 | 문제 | 카테고리 | 파일 |
|:-:|:---:|---|:---:|---|
| 1 | CRITICAL | `application/auth/` 인프라 클래스 배치 | 계층분리 | `JwtService`, `*OAuthClient` |
| 2 | CRITICAL | `SearchPlacesUseCase` → `NaverLocalApiClient` DIP 위반 | 의존성 | `SearchPlacesUseCase:4` |
| 3 | CRITICAL | Kakao/Google LoginUseCase 90% 코드 중복 | 결합도 | `*LoginUseCase` |
| 4 | MAJOR | Domain Entity JPA 전략 불일치 | 계층분리 | `domain/entity/*` |
| 5 | MAJOR | `KakaoLoginUseCase.TokenPair` 범용 사용 | 결합도 | `AuthController`, `*UseCase` |
| 6 | MAJOR | `PlaceCommandService` → `PlaceQueryService` 수평 의존 | 결합도 | `PlaceCommandService:17` |
| 7 | MAJOR | `GlobalExceptionHandler` debug 필드 노출 | 확장성 | `GlobalExceptionHandler:39` |
| 8 | MAJOR | Auth UseCase/Service 테스트 전무 | 테스트 | — |
| 9 | MAJOR | `User.oauthId` 컬럼명 `kakao_id` 레거시 | 계층분리 | `User:16` |
| 10 | MAJOR | `ReviewService` 인증 미적용 (authorName 위조 가능) | 확장성 | `ReviewService:22` |
| 11 | MAJOR | `MeetingsController` URL 패턴 불일치 | 확장성 | `MeetingsController` |
| 12 | MINOR | `MeetingService.vote()` 인증 미적용 | 확장성 | `MeetingService:47` |
| 13 | MINOR | `AuthController` → `AuthenticatedUser` 인프라 참조 | 의존성 | `AuthController:9` |
| 14 | MINOR | 비즈니스 예외 타입 부재 | 확장성 | 전역 |
| 15 | MINOR | `DataInitializer` 운영환경 분리 없음 | 확장성 | `DataInitializer:8` |
| 16 | MINOR | `PlaceQueryService.toResponse()` N+1 쿼리 | 확장성 | `PlaceQueryService:52` |
| 17 | MINOR | Service/UseCase 구분 기준 미문서화 | 확장성 | — |

**CRITICAL: 3개 / MAJOR: 8개 / MINOR: 6개 / Total: 17개**

---

### 안전한 마이그레이션 실행 순서

각 단계는 **독립적으로 커밋 가능**하며, 기존 테스트를 깨뜨리지 않는 순서다.

#### Phase 1: 즉시 수정 (기존 코드 변경 없이 추가만)
```
Step 1.1: application/dto/TokenPair.java 생성
Step 1.2: KakaoLoginUseCase.TokenPair → dto.TokenPair 전환 (find & replace)
Step 1.3: GlobalExceptionHandler — debug 필드 제거
Step 1.4: DataInitializer — @Profile("!prod") 추가
→ 커밋: "fix: TokenPair 독립 추출, 운영 환경 보안 수정"
→ 테스트 실행: ./gradlew test (기존 테스트 전부 통과해야 함)
```

#### Phase 2: 포트 인터페이스 도입 (핵심 아키텍처 변경)
```
Step 2.1: application/port/ 에 OAuthPort, TokenProvider, PlaceSearchPort 인터페이스 생성
Step 2.2: JwtService → infrastructure/security/JwtTokenProvider.java로 이동 + TokenProvider 구현
         JwtAuthenticationFilter는 TokenProvider 인터페이스로 변경
Step 2.3: KakaoOAuthClient → infrastructure/external/KakaoOAuthAdapter.java로 이동 + OAuthPort 구현
Step 2.4: GoogleOAuthClient → infrastructure/external/GoogleOAuthAdapter.java로 이동 + OAuthPort 구현
Step 2.5: NaverLocalApiClient → PlaceSearchPort 구현 추가
Step 2.6: SearchPlacesUseCase → PlaceSearchPort 의존으로 변경
Step 2.7: application/auth/ 패키지 삭제
→ 커밋: "refactor: 포트 인터페이스 도입, application→infrastructure 의존 제거"
→ 테스트 실행: ./gradlew test
```

#### Phase 3: OAuth 로그인 통합 + 수평 의존 제거
```
Step 3.1: OAuthLoginService 생성 (Map<OAuthProvider, OAuthPort> 주입)
Step 3.2: KakaoLoginUseCase, GoogleLoginUseCase 삭제
Step 3.3: AuthController에서 OAuthLoginService 사용으로 변경
Step 3.4: PlaceResponseAssembler 도입
Step 3.5: PlaceCommandService → PlaceQueryService 의존 제거
Step 3.6: PlaceQueryService에서도 PlaceResponseAssembler 사용
→ 커밋: "refactor: OAuth 로그인 통합, Service 수평 의존 제거"
→ 테스트 실행: ./gradlew test
```

#### Phase 4: 인증/보안 수정
```
Step 4.1: ReviewService — authorName을 AuthContext에서 가져오도록 변경
Step 4.2: MeetingService.vote() — voterName을 AuthContext에서 가져오도록 변경
Step 4.3: AuthController — @AuthenticationPrincipal 제거, AuthContext 사용
Step 4.4: MeetingsController URL 패턴 통일
→ 커밋: "fix: 리뷰/투표 인증 적용, API URL 일관성 수정"
→ 테스트 실행: ./gradlew test (프론트엔드 API 호출도 확인 필요)
```

#### Phase 5: 예외 계층 + 테스트 (중기)
```
Step 5.1: domain/exception/ 패키지에 BusinessException 계층 생성
Step 5.2: IllegalArgumentException → NotFoundException/ConflictException 등으로 교체
Step 5.3: GlobalExceptionHandler에 BusinessException 핸들러 추가
Step 5.4: KakaoLoginUseCase(→OAuthLoginService) 단위 테스트 작성
Step 5.5: RefreshTokenUseCase 단위 테스트 작성
Step 5.6: AuthService 단위 테스트 작성
→ 커밋: "refactor: 비즈니스 예외 계층 도입 + 인증 테스트 추가"
```

#### Phase 6: Entity 전략 통일 + DB 마이그레이션 (장기)
```
Step 6.1: 전략 결정 — A(JPA 직접) vs B(순수 POJO) 중 택일
         권장: 전략 A (현재 다수가 이미 A 방식, 보일러플레이트 최소화)
Step 6.2: Group/Meeting/GroupPlace/MeetingVote/RefreshToken을 전략 A로 통일
         → JpaEntity 삭제, Mapper 삭제, Domain Entity에 @Entity 추가
Step 6.3: User 테이블 kakao_id → oauth_id 컬럼 리네임 (Flyway 마이그레이션)
Step 6.4: N+1 쿼리 최적화 — 배치 쿼리 또는 DTO 프로젝션
→ 커밋: "refactor: Entity 전략 통일 (전략 A), DB 스키마 정규화"
```

---

### 최종 의존성 그래프 (Phase 6 완료 후)

```
                    ┌─────────────────────┐
                    │  presentation/api/   │
                    │  Controllers         │
                    └──────────┬──────────┘
                               │ depends on
                    ┌──────────▼──────────┐
                    │  application/        │
                    │  service/ + usecase/ │
                    │  dto/ + port/        │
                    └──────┬───────┬──────┘
                           │       │
              implements   │       │ depends on
                           │       │
            ┌──────────────▼─┐   ┌─▼──────────────┐
            │ infrastructure/│   │    domain/       │
            │ security/      │   │    entity/       │
            │ external/      │   │    repository/   │
            │ persistence/   │   │    exception/    │
            └────────────────┘   └─────────────────┘
                     │                    ▲
                     │    implements      │
                     └────────────────────┘

      ✅ Presentation → Application → Domain ← Infrastructure
      ✅ Application → Infrastructure 참조: 0개
      ✅ Domain은 어디에도 의존하지 않음
```

---

### 최종 판정

| 기준 | 현재 | Phase 2 후 | Phase 6 후 |
|------|:---:|:---:|:---:|
| 계층 분리 | 5/10 | 7/10 | 9/10 |
| 의존성 방향 | 6/10 | 9/10 | 9/10 |
| 테스트 가능성 | 4/10 | 6/10 | 8/10 |
| 확장성 | 5/10 | 7/10 | 9/10 |
| 결합도 | 6/10 | 8/10 | 9/10 |
| **종합** | **5.2/10** | **7.4/10** | **8.8/10** |

현재 아키텍처는 **의도는 좋지만 실행의 일관성이 부족**한 상태다.
Phase 2까지만 완료해도 핵심적인 DIP 위반이 해결되어 7.4점까지 올라간다.
전체 6단계를 완료하면 8.8/10의 Clean Architecture에 근접할 수 있다.

---

*Ralph Loop Iteration 3/3 — Final Review Complete.*

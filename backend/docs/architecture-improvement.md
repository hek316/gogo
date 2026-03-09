# GoGo Backend Architecture Improvement

> 문제 분석에서 발견된 17개 이슈(CRITICAL 3, HIGH 2, MEDIUM 3)를 해결하는 개선 아키텍처.
> 단순 패치가 아닌 **설계 철학 전환**을 포함한다.

---

## 설계 철학 (Design Philosophy)

### 1. "Fail Loud, Fail Fast" — 조용한 실패를 제거하라

**문제**: `requireNickname()`이 인증 없이도 `"anonymous"`를 반환한다. 시스템이 문제를 숨긴다.

**철학**: "require"가 이름에 붙으면 **반드시 예외를 던져야 한다**. 실패를 숨기는 편의 메서드는 디버깅을 불가능하게 만든다. 실패가 조용하면 버그를 발견하는 시점이 개발 → 운영으로 밀려난다.

```java
// Before — 거짓말하는 API
default String requireNickname() {
    return currentNickname().orElse("anonymous");  // "require"인데 실패 안 함
}

// After — 정직한 API
default String requireNickname() {
    return currentNickname()
        .orElseThrow(() -> new UnauthorizedException("인증 정보가 없습니다."));
}
```

**원칙**: Optional을 반환하는 메서드(`currentX`)와 예외를 던지는 메서드(`requireX`)를 명확히 분리한다. 중간 지대(기본값 반환)는 허용하지 않는다.

---

### 2. "이름이 다르면 타입도 달라야 한다" — 의미론적 예외 계층

**문제**: "찾을 수 없음", "이미 존재함", "인증 실패" 모두 `IllegalArgumentException` → 400.

**철학**: HTTP 상태 코드는 클라이언트와의 **계약**이다. 같은 400으로 "잘못된 입력"과 "리소스 없음"을 표현하면 클라이언트는 적절한 UX를 만들 수 없다. 도메인 예외는 비즈니스 의미를 담아야 한다.

```
BusinessException (abstract)
├── NotFoundException      → 404  "리소스를 찾을 수 없음"
├── ConflictException      → 409  "상태 충돌 (이미 확정됨, 이미 존재함)"
├── UnauthorizedException  → 401  "인증 필요"
└── InvalidRequestException → 400  "입력값 오류" (기존 IllegalArgumentException 대체)
```

**핵심 규칙**:
- 서비스 계층에서 `IllegalArgumentException`을 직접 던지지 않는다
- 도메인 예외만 사용한다
- `GlobalExceptionHandler`가 예외 타입 → HTTP 상태를 매핑한다
- 프로덕션에서는 내부 정보를 **절대** 노출하지 않는다

```java
// GlobalExceptionHandler — 프로덕션 안전
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ErrorResponse> handle(BusinessException e) {
    return ResponseEntity.status(e.getHttpStatus())
        .body(new ErrorResponse(e.getErrorCode(), e.getMessage()));
}

@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleUnknown(Exception e) {
    log.error("Unhandled: {}", e.getMessage(), e);
    return ResponseEntity.status(500)
        .body(new ErrorResponse("INTERNAL_ERROR", "서버 오류가 발생했습니다."));
    // debug 정보 없음. 로그에만 기록.
}
```

---

### 3. "경계를 넘을 때 변환하라" — 포트 인터페이스 원칙

**문제**: `application/auth/JwtService`가 `io.jsonwebtoken.Claims`(인프라 라이브러리)를 직접 노출한다. Application 계층이 인프라 라이브러리에 결합된다.

**철학**: 계층 경계에는 반드시 **포트(인터페이스)**가 있어야 한다. Application 계층은 "토큰을 만들고 검증할 수 있다"만 알면 된다. JWT인지 PASETO인지는 모른다.

```
Application 계층 (포트 정의)          Infrastructure 계층 (구현)
┌─────────────────────────┐          ┌──────────────────────────┐
│ TokenProvider (interface)│◄─────────│ JwtTokenProvider         │
│  generateAccessToken()  │          │  (io.jsonwebtoken 사용)   │
│  validateAndExtract()   │          │  implements TokenProvider │
│  → TokenClaims (record) │          └──────────────────────────┘
├─────────────────────────┤          ┌──────────────────────────┐
│ OAuthPort (interface)   │◄─────────│ KakaoOAuthAdapter        │
│  authenticate(code)     │          │  (RestTemplate 사용)      │
│  → OAuthUserInfo        │          │  implements OAuthPort     │
│  provider()             │          ├──────────────────────────┤
│  buildAuthorizationUrl()│          │ GoogleOAuthAdapter        │
├─────────────────────────┤          │  implements OAuthPort     │
│ PlaceSearchPort         │◄─────────┤──────────────────────────┤
│  search(keyword)        │          │ NaverLocalApiClient       │
│  → List<PlaceSearchResult>│        │  implements PlaceSearchPort│
└─────────────────────────┘          └──────────────────────────┘
```

**규칙**:
- Application 계층에 `import io.jsonwebtoken.*`이 있으면 설계 오류
- Application은 포트 인터페이스와 도메인 객체만 의존한다
- 포트의 반환 타입은 Application 계층에서 정의한 DTO/record (Claims 노출 금지)

```java
// application/port/TokenProvider.java
public interface TokenProvider {
    String generateAccessToken(Long userId, String nickname);
    Optional<TokenClaims> validateAndExtract(String token);

    record TokenClaims(Long userId, String nickname) {}
}
```

---

### 4. "같은 일을 두 번 쓰면 추상화가 부족한 것" — 전략 패턴 OAuth

**문제**: `KakaoLoginUseCase`(73줄)와 `GoogleLoginUseCase`(~70줄)가 90% 동일한 코드. provider 추가마다 UseCase + Client 파일 2개 증가.

**철학**: 변하는 것(OAuth 프로바이더 고유 로직)과 변하지 않는 것(로그인 흐름)을 분리한다. **전략 패턴**으로 프로바이더를 플러그인화한다.

```java
// 변하지 않는 것: 로그인 흐름 (OAuthLoginService)
@Service
public class OAuthLoginService {

    private final Map<OAuthProvider, OAuthPort> adapters;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    // Spring이 List<OAuthPort>를 주입 → Map으로 변환
    public OAuthLoginService(List<OAuthPort> oauthPorts, ...) {
        this.adapters = oauthPorts.stream()
            .collect(Collectors.toMap(OAuthPort::provider, Function.identity()));
    }

    public TokenPair login(OAuthProvider provider, String code) {
        OAuthPort adapter = adapters.get(provider);  // 전략 선택
        OAuthUserInfo userInfo = adapter.authenticate(code);
        User user = upsertUser(userInfo, provider);
        return generateTokens(user);
    }

    public String getAuthorizationUrl(OAuthProvider provider) {
        return adapters.get(provider).buildAuthorizationUrl();
    }
}
```

**확장 시나리오**: Apple 로그인 추가 = `AppleOAuthAdapter implements OAuthPort` 1개 파일만 추가. Service/Controller 변경 0.

---

### 5. "동시성은 DB에게 맡겨라" — 비관적 잠금 원칙

**문제**: Refresh Token Rotation에서 Read-Check-Write 사이에 Race Condition. 두 요청이 동시에 같은 토큰으로 갱신 가능.

**철학**: 애플리케이션 레벨 검증만으로는 동시성을 해결할 수 없다. **DB 잠금**이 유일한 정답이다. "한 번에 하나"가 보장되어야 하는 곳에는 `SELECT ... FOR UPDATE`를 사용한다.

```java
// domain/repository/RefreshTokenRepository.java
Optional<RefreshToken> findByTokenForUpdate(String token);  // 비관적 잠금

// infrastructure/persistence/RefreshTokenJpaRepository.java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT rt FROM RefreshTokenJpaEntity rt WHERE rt.token = :token")
Optional<RefreshTokenJpaEntity> findByTokenForUpdate(@Param("token") String token);
```

```java
// RefreshTokenUseCase — 개선 후
public TokenPair execute(String rawRefreshToken) {
    // 비관적 잠금으로 조회 → 다른 트랜잭션은 대기
    RefreshToken token = refreshTokenRepository.findByTokenForUpdate(rawRefreshToken)
        .orElseThrow(() -> new UnauthorizedException("유효하지 않은 refresh token입니다."));
    // 이 시점에서 이 row는 잠겨있어서 동시 요청이 불가능
    ...
}
```

**적용 범위**: Refresh Token Rotation, Place Like (unique constraint + catch), Meeting Vote

---

### 6. "신뢰 경계를 존중하라" — SecurityContext 유일 신원 원칙

**문제**: `ReviewService`가 `request.authorName()`을 그대로 사용. 클라이언트가 다른 사람 이름으로 리뷰 작성 가능. `MeetingService`의 `voterName`도 동일.

**철학**: **사용자 신원 정보는 오직 SecurityContext에서만 가져온다.** 클라이언트가 보내는 이름/ID는 절대 신뢰하지 않는다. DTO에 신원 필드가 있으면 설계 오류.

```
클라이언트 요청                서버 처리
┌──────────────┐            ┌────────────────────────┐
│ { "rating": 5,│           │ authorName은 어디서?     │
│   "content":  │──────────►│                        │
│   "좋아요" }  │           │ ✗ request.authorName() │
│              │           │ ✓ authContext.requireNickname() │
└──────────────┘            └────────────────────────┘
```

```java
// Before — 클라이언트를 신뢰 (위험)
public ReviewResponse addReview(Long placeId, AddReviewRequest request) {
    Review review = Review.create(placeId, request.authorName(), ...);
}

// After — SecurityContext만 신뢰 (안전)
public ReviewResponse addReview(Long placeId, AddReviewRequest request) {
    String author = authContext.requireNickname();  // JWT에서 추출
    Review review = Review.create(placeId, author, ...);
}
```

**DTO 마이그레이션 전략**: 프론트엔드 호환을 위해 필드는 남기되 `@JsonIgnore`로 역직렬화에서 제외. 프론트엔드 배포 후 필드 삭제.

---

### 7. "쿼리는 쿼리답게" — N+1 제거 원칙

**문제**: `PlaceQueryService.getPlaces()`가 place마다 like 카운트 + isLiked 쿼리 2번. 100개면 201번.

**철학**: **목록 조회에서 루프 안 쿼리는 금지.** 배치 쿼리 또는 JOIN으로 해결한다.

```java
// Before — N+1 (장소 N개 × 쿼리 2회 = 2N+1)
places.stream().map(place -> {
    long count = placeLikeRepository.countByPlaceId(place.getId());        // 쿼리 1
    boolean liked = placeLikeRepository.existsByUserIdAndPlaceId(uid, id); // 쿼리 2
    return PlaceResponse.from(place, count, liked);
});

// After — 배치 쿼리 (3회 고정)
public List<PlaceResponse> getPlaces(String category) {
    List<Place> places = findPlaces(category);                                // 쿼리 1
    List<Long> placeIds = places.stream().map(Place::getId).toList();

    Map<Long, Long> likeCounts = placeLikeRepository.countByPlaceIds(placeIds); // 쿼리 2
    Set<Long> likedIds = userId != null
        ? placeLikeRepository.findLikedPlaceIds(userId, placeIds)              // 쿼리 3
        : Set.of();

    return places.stream()
        .map(p -> PlaceResponse.from(p,
            likeCounts.getOrDefault(p.getId(), 0L),
            likedIds.contains(p.getId())))
        .toList();
}
```

```java
// Repository — 배치 쿼리
@Query("SELECT pl.placeId, COUNT(pl) FROM PlaceLike pl WHERE pl.placeId IN :ids GROUP BY pl.placeId")
List<Object[]> countByPlaceIds(@Param("ids") List<Long> placeIds);

@Query("SELECT pl.placeId FROM PlaceLike pl WHERE pl.userId = :userId AND pl.placeId IN :ids")
Set<Long> findLikedPlaceIds(@Param("userId") Long userId, @Param("ids") List<Long> placeIds);
```

---

## 개선된 패키지 구조

```
com/gogo/
├── domain/
│   ├── entity/              (변경 없음)
│   ├── repository/          (변경 없음)
│   └── exception/           ★ NEW
│       ├── BusinessException.java        (abstract)
│       ├── NotFoundException.java        (404)
│       ├── ConflictException.java        (409)
│       ├── UnauthorizedException.java    (401)
│       └── InvalidRequestException.java  (400)
│
├── application/
│   ├── port/                ★ EXPANDED
│   │   ├── AuthContext.java             (기존, requireNickname 수정)
│   │   ├── TokenProvider.java           ★ NEW
│   │   ├── OAuthPort.java               ★ NEW
│   │   └── PlaceSearchPort.java         ★ NEW
│   ├── dto/
│   │   ├── TokenPair.java               ★ NEW (독립 record)
│   │   ├── AddReviewRequest.java        (authorName 제거)
│   │   ├── VoteRequest.java             (voterName 제거)
│   │   └── ... (기존)
│   ├── service/
│   │   ├── OAuthLoginService.java       ★ NEW (전략 패턴)
│   │   ├── ReviewService.java           (AuthContext 주입)
│   │   ├── MeetingService.java          (AuthContext 주입)
│   │   ├── PlaceCommandService.java     (PlaceQueryService 의존 제거)
│   │   ├── PlaceQueryService.java       (배치 쿼리로 N+1 제거)
│   │   └── ... (기존)
│   ├── usecase/
│   │   └── auth/
│   │       └── RefreshTokenUseCase.java (TokenProvider + 비관적 잠금)
│   │       (KakaoLoginUseCase 삭제)
│   │       (GoogleLoginUseCase 삭제)
│   └── auth/                            ★ DELETED (전체)
│
├── infrastructure/
│   ├── security/
│   │   ├── JwtTokenProvider.java        ★ NEW (implements TokenProvider)
│   │   ├── JwtAuthenticationFilter.java (TokenProvider 사용)
│   │   └── SecurityContextHelper.java   (기존)
│   ├── external/
│   │   ├── KakaoOAuthAdapter.java       ★ NEW (implements OAuthPort)
│   │   ├── GoogleOAuthAdapter.java      ★ NEW (implements OAuthPort)
│   │   └── NaverLocalApiClient.java     (implements PlaceSearchPort)
│   └── config/
│       ├── DataInitializer.java         (@Profile("!prod") 추가)
│       └── SecurityConfig.java          (TokenProvider 사용)
│
└── presentation/
    └── api/
        └── GlobalExceptionHandler.java  (BusinessException 핸들러)
```

---

## 의존성 흐름 (개선 후)

```
Presentation ──► Application ──► Domain
     │                │              ▲
     │                │              │
     └───► Infrastructure ───────────┘
                │
                ├── implements TokenProvider
                ├── implements OAuthPort
                └── implements PlaceSearchPort
```

**위반 없음 확인**:
- Application → Domain: ✅ (엔티티, 리포지토리)
- Application → Infrastructure: ❌ 없음 (포트 인터페이스만 사용)
- Infrastructure → Application: ❌ 없음 (포트를 implements할 뿐)
- Infrastructure → Domain: ✅ (JPA 구현)
- Presentation → Application: ✅ (서비스 호출)

---

## 변경 전후 비교 요약

| 문제 | Before | After | 원칙 |
|------|--------|-------|------|
| requireNickname 거짓말 | `orElse("anonymous")` | `orElseThrow(UnauthorizedException)` | Fail Loud |
| 예외 구분 불가 | 전부 `IllegalArgumentException` → 400 | 도메인 예외 계층 → 401/404/409 | 의미론적 타입 |
| 디버그 정보 노출 | `"debug": 예외클래스명` | 로그에만 기록, 응답에 없음 | 신뢰 경계 |
| JWT가 Application에 | `application/auth/JwtService` | `infrastructure/security/JwtTokenProvider` | 포트 인터페이스 |
| OAuth 중복 코드 | Kakao/Google UseCase 146줄 중복 | `OAuthLoginService` 40줄 + 전략 패턴 | DRY + 전략 패턴 |
| Refresh Token 경합 | Read-Check-Write 비원자적 | `SELECT FOR UPDATE` 비관적 잠금 | DB에게 맡겨라 |
| 리뷰 이름 위조 | `request.authorName()` | `authContext.requireNickname()` | SecurityContext 유일 신원 |
| N+1 쿼리 | 장소당 2쿼리 | 배치 쿼리 3회 고정 | 루프 안 쿼리 금지 |
| DataInitializer 프로덕션 | `@Component` (항상 실행) | `@Profile("!prod")` | 환경 분리 |
| Command→Query 수평 의존 | `PlaceCommandService` → `PlaceQueryService` | `PlaceLikeRepository` 직접 주입 | CQRS 경계 존중 |

---

## 마이그레이션 순서와 안전장치

```
Phase 1: 예외 계층 + 보안 수정
  ↓ (기존 테스트 100% 통과 — 예외 추가만)
Phase 2: 포트 인터페이스 정의
  ↓ (인터페이스 추가만 — 영향 0)
Phase 3: Infrastructure Adapter 이동 ⚠️ 가장 위험
  ↓ (빈 와이어링 변경 — bootRun 확인 필수)
Phase 4: OAuth 통합 (전략 패턴)
  ↓ (UseCase 2개 삭제 — OAuth 수동 테스트 필수)
Phase 5: 인증 수정 + N+1 제거
  ↓ (DTO 변경 — 프론트엔드 호환 전략 필요)
Phase 6: URL 정리 + 예외 마이그레이션
  ↓ (URL 변경 — deprecated alias 유지)
```

**각 Phase의 안전장치**:
1. 독립 커밋 가능 (이전 Phase 없이도 동작)
2. `./gradlew test` 통과 필수
3. Phase 3, 4 후 `./gradlew bootRun`으로 로컬 기동 확인
4. 프론트엔드 영향 있는 Phase 5, 6은 호환 전략 포함

---

## 핵심 교훈 7가지

1. **"require"는 예외를 던져야 한다** — 메서드 이름이 계약이다
2. **예외 타입이 HTTP 상태 코드를 결정한다** — 비즈니스 의미를 예외로 표현하라
3. **계층 경계에는 인터페이스가 필요하다** — import를 보면 의존 방향을 알 수 있다
4. **중복 코드는 추상화 부족의 신호다** — 전략 패턴으로 변하는 것을 분리하라
5. **동시성은 코드가 아닌 DB가 해결한다** — 비관적 잠금을 두려워하지 마라
6. **사용자 신원은 서버만 결정한다** — 클라이언트 데이터를 절대 신뢰하지 마라
7. **목록 조회에서 루프 안 쿼리는 금지다** — 배치로 바꿔라

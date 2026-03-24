# GoGo Backend — 설계 선택지 가이드

**목적**: 각 아키텍처 결정 포인트에서 가능한 선택지를 비교하고, GoGo에 적합한 선택을 이해하기 위한 학습 자료

---

## 1. Domain Entity 전략

### 선택지 A: Domain Entity에 JPA 어노테이션 직접 부착

현재 Place, User, Review, PlaceLike가 이 방식.

```java
@Entity
@Table(name = "places")
public class Place {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 도메인 로직 + JPA 매핑이 한 클래스에
}
```

**장점**
- 보일러플레이트 최소화 — Mapper, JpaEntity 클래스 불필요
- 코드 추적이 간단 — Entity 1개만 보면 됨
- Spring Data JPA의 변경 감지(dirty checking)를 자연스럽게 활용

**단점**
- Domain이 JPA(jakarta.persistence)에 컴파일 의존
- JPA 제약(`protected` 기본 생성자, 프록시 호환)이 도메인 설계를 제한
- 도메인 모듈만 별도 jar로 분리하면 JPA 의존이 따라감

**적합한 상황**
- 모놀리식 서비스, 도메인 모듈 분리 계획 없음
- 팀 규모 1~4명, Entity 수 20개 이하

### 선택지 B: 순수 POJO Domain + 별도 JpaEntity + Mapper

현재 Group, Meeting이 이 방식.

```java
// domain — JPA 의존 없음
public class Group {
    private Long id;
    private String name;
}

// infrastructure — JPA 전용
@Entity
public class GroupJpaEntity {
    @Id private Long id;
    @Column private String name;
}

// Mapper
public class GroupMapper {
    Group toDomain(GroupJpaEntity e) { ... }
    GroupJpaEntity toJpaEntity(Group g) { ... }
}
```

**장점**
- Domain이 어떤 프레임워크에도 의존하지 않음 — 진정한 Clean Architecture
- 도메인 모듈을 독립 jar로 분리 가능 (멀티모듈)
- JPA 외 다른 저장소(MongoDB, Redis)로 교체 시 Domain 변경 0

**단점**
- Entity 1개당 최소 3개 파일(Domain, JpaEntity, Mapper) — 보일러플레이트 3배
- 변경 감지 불가 — 항상 `save()` 명시 필요
- GroupRepositoryImpl.save()처럼 Mapper 로직이 복잡해짐

**적합한 상황**
- 멀티모듈 프로젝트, 도메인을 공유 라이브러리로 분리할 계획
- 저장소 기술 변경 가능성이 있는 경우 (RDB -> NoSQL)
- 팀 규모 5명 이상

### 선택지 C: JPA Entity + DTO 경계 변환 (권장)

```java
@Entity
public class Place { ... }

// application에서 반환할 때는 항상 DTO
public PlaceResponse getPlace(Long id) {
    Place place = repo.findById(id);
    return PlaceResponse.from(place);  // 경계에서 변환
}
```

**장점**: A의 간결함 + 외부 계층에 Entity 직접 노출하지 않음
**적합한 상황**: 대부분의 Spring Boot 프로젝트

### GoGo 결정: A+C (JPA 직접 + DTO 경계 변환) -> 현재 다수가 이미 이 방식

---

## 2. Application 계층 구성

### 선택지 A: Service 중심

```java
@Service
public class PlaceService {
    public PlaceResponse getPlace(Long id) { ... }
    public PlaceResponse addPlace(AddPlaceRequest req) { ... }
    public void deletePlace(Long id) { ... }
    public List<PlaceSearchResult> search(String keyword) { ... }
}
```

**장점**: 관련 기능이 한 클래스에 모여 있어 찾기 쉬움, 파일 수 최소
**단점**: 기능 추가 시 Service가 비대해짐 ("God Service")
**적합**: 도메인당 기능 5~7개 이하, 빠른 개발 우선

### 선택지 B: UseCase 중심 (1 class = 1 operation)

```java
@Service public class AddPlaceUseCase { PlaceResponse execute(AddPlaceRequest req) { ... } }
@Service public class GetPlaceUseCase { PlaceResponse execute(Long id) { ... } }
```

**장점**: SRP 완벽 충족, 의존성 최소화, OCP 준수
**단점**: 파일 폭발 (도메인당 10~20개), 공통 로직 공유 어려움
**적합**: 대규모 팀, 마이크로서비스

### 선택지 C: CQRS (Command/Query 분리)

```java
@Service public class PlaceQueryService { /* 읽기 전용 */ }
@Service public class PlaceCommandService { /* 쓰기 전용 */ }
```

**장점**: 읽기/쓰기 최적화 독립, `readOnly = true` 성능 최적화
**단점**: Command가 응답 반환 시 Query를 호출하게 됨 (현재 GoGo의 문제점)
**적합**: 읽기/쓰기 비율이 극단적인 경우, 이벤트 소싱

### 선택지 D: 하이브리드 Service + UseCase (권장)

```java
// CRUD는 Service로 묶되
@Service public class PlaceService { getPlace, getPlaces, addPlace, deletePlace }

// 외부 연동이나 복잡한 단일 기능만 UseCase로 분리
@Service public class SearchPlacesUseCase { ... }  // Naver API 연동
```

**장점**: 실용적 타협 — 단순한 것은 간결하게, 복잡한 것은 분리
**적합**: GoGo 같은 중소규모 프로젝트

### GoGo 결정: D (하이브리드) -> 기준을 ADR로 명시

---

## 3. 인증 아키텍처: OAuth 통합 방식

### 선택지 A: Provider별 독립 UseCase (현재)

```java
@Service public class KakaoLoginUseCase { TokenPair execute(String code) { ... } }
@Service public class GoogleLoginUseCase { TokenPair execute(String code) { ... } }
```

**장점**: Provider별 특수 로직 격리 가능
**단점**: 90% 중복 코드, Provider 추가마다 새 클래스
**적합**: Provider별 로직이 근본적으로 다른 경우 (OIDC vs SAML)

### 선택지 B: 전략 패턴 OAuthPort + OAuthLoginService (권장)

```java
public interface OAuthPort {
    OAuthUserInfo authenticate(String code);
}

@Service
public class OAuthLoginService {
    private final Map<OAuthProvider, OAuthPort> ports;
    public TokenPair login(OAuthProvider provider, String code) {
        OAuthUserInfo info = ports.get(provider).authenticate(code);
        User user = upsertUser(info, provider);
        return generateTokens(user);
    }
}
```

**장점**: OCP 충족, 새 Provider = 구현체 1개 추가, 중복 제거
**단점**: Map 기반 라우팅이라 컴파일 타임에 누락 못 잡음
**적합**: GoGo처럼 OAuth 흐름이 동일하고 Provider만 다른 경우

### 선택지 C: 템플릿 메서드 패턴

```java
public abstract class AbstractOAuthLoginUseCase {
    protected abstract OAuthUserInfo fetchUserInfo(String code);
    public final TokenPair execute(String code) { ... }
}
```

**장점**: 공통 흐름 강제 + 변하는 부분만 하위 클래스 구현
**단점**: 상속 기반으로 유연성 낮음, Java 단일 상속 제한
**적합**: Provider별 클래스를 명시적으로 유지하고 싶을 때

### GoGo 결정: B (전략 패턴) -> 90% 중복 제거, 확장성 최고

---

## 4. 예외 처리 전략

### 선택지 A: Java 표준 예외 (현재)

```java
throw new IllegalArgumentException("장소를 찾을 수 없습니다.");
```

**장점**: 별도 예외 클래스 불필요
**단점**: 404/400/409 구분 불가, 비즈니스 오류와 버그 혼재
**적합**: 프로토타입, MVP 초기

### 선택지 B: 도메인 예외 계층 (권장)

```java
public abstract class BusinessException extends RuntimeException {
    private final int statusCode;
}
public class NotFoundException extends BusinessException { /* 404 */ }
public class ConflictException extends BusinessException { /* 409 */ }
```

**장점**: HTTP 상태 코드 1:1 매핑, 로그 분석 용이
**단점**: 예외 클래스 5~10개 필요
**적합**: REST API 제공 서비스, 프론트엔드가 에러에 따라 다른 UI 필요

### 선택지 C: 에러 코드 기반 단일 예외

```java
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;
    public enum ErrorCode {
        PLACE_NOT_FOUND(404, "장소를 찾을 수 없습니다."),
        MEETING_ALREADY_CONFIRMED(409, "이미 확정된 약속입니다.");
    }
}
```

**장점**: 예외 클래스 1개로 모든 오류 처리, 에러 코드 중앙 관리
**단점**: ErrorCode enum이 비대해짐, 타입 기반 catch 불가
**적합**: 프론트엔드와 에러 코드 계약이 필요한 경우

### 선택지 D: Spring `ResponseStatusException`

```java
throw new ResponseStatusException(HttpStatus.NOT_FOUND, "...");
```

**장점**: 별도 예외 클래스 불필요
**단점**: Service에서 Spring Web 의존 -> 계층 오염
**적합**: Controller에서만 사용하는 경우

### GoGo 결정: B (도메인 예외 계층)

---

## 5. 외부 API 연동 전략

### 선택지 A: 구체 클래스 직접 의존 (현재)

```java
public class SearchPlacesUseCase {
    private final NaverLocalApiClient naverClient;  // 구체 클래스
}
```

**장점**: 간단, IDE에서 바로 구현 코드로 이동
**단점**: DIP 위반, 외부 API 교체 시 Application 코드 변경
**적합**: 외부 API 교체 가능성 0%, 프로토타입

### 선택지 B: 포트 인터페이스 — Hexagonal (권장)

```java
// Application Port
public interface PlaceSearchPort {
    List<PlaceSearchResult> search(String keyword);
}

// Infrastructure Adapter
@Component
public class NaverPlaceSearchAdapter implements PlaceSearchPort { ... }
```

**장점**: DIP 충족, API 교체 시 Adapter만 교체, 테스트 용이
**단점**: 인터페이스 + 구현체 파일 2배
**적합**: 테스트 가능성이 중요하거나, API 교체 가능성이 있는 경우

### 선택지 C: `@FeignClient`

```java
@FeignClient(name = "naver-local", url = "...")
public interface NaverLocalApi {
    @GetMapping("/v1/search/local.json")
    NaverSearchResponse search(@RequestParam String query);
}
```

**장점**: 인터페이스 선언만으로 구현 자동 생성
**단점**: Spring Cloud 의존 추가
**적합**: 마이크로서비스 간 통신

### GoGo 결정: B (포트 인터페이스)

---

## 6. Repository 구현 전략

### 선택지 A: 위임 패턴 (현재)

```java
// Domain Interface + JpaRepository + RepositoryImpl 3개 세트
public interface PlaceRepository { Place save(Place place); }
public interface PlaceJpaRepository extends JpaRepository<Place, Long> { }
@Repository
public class PlaceRepositoryImpl implements PlaceRepository {
    private final PlaceJpaRepository jpa;
    public Place save(Place p) { return jpa.save(p); }  // 1줄짜리 위임
}
```

**장점**: Domain이 Spring Data에 의존하지 않음
**단점**: 모든 메서드가 1줄 위임 boilerplate, Entity마다 3개 파일
**적합**: 전략 B(순수 POJO Domain)와 함께 사용, 저장소 교체 가능성 높은 경우

### 선택지 B: Spring Data JPA 직접 확장 (권장)

```java
// RepositoryImpl 불필요!
public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByCategory(String category);
    @Query("SELECT p FROM Place p ORDER BY ...")
    List<Place> findPopularPlaces(int limit);
}
```

**장점**: 코드량 1/3, Impl 클래스 완전 제거
**단점**: Domain이 Spring Data에 의존
**적합**: Entity가 이미 JPA 의존인 경우 (추가 손해 없음)

### 핵심 통찰

> Entity에 `@Entity`를 직접 붙이면서(전략 A), Repository는 위임 패턴으로 분리하는 것은 어중간하다.
> JPA 의존을 허용할 거면 Repository도 Spring Data를 직접 쓰는 게 일관적이다.

### GoGo 결정: B (Spring Data 직접) -> Entity가 이미 JPA 의존이므로

---

## GoGo 최종 결정 요약

| 결정 포인트 | 선택 | 근거 |
|---|---|---|
| Entity 전략 | JPA 직접 + DTO 경계 변환 | 모놀리식, Entity 10개, 실용성 우선 |
| Application 구성 | 하이브리드 Service+UseCase | 기준만 ADR 문서화 |
| OAuth 통합 | 전략 패턴 (OAuthPort) | 90% 중복 제거, Provider 확장 용이 |
| 예외 처리 | 도메인 예외 계층 | HTTP 매핑 명확, 도메인 순수성 유지 |
| 외부 API 연동 | 포트 인터페이스 | DIP 충족, 테스트 용이 |
| Repository | Spring Data 직접 사용 | Entity가 이미 JPA 의존, 위임 무의미 |

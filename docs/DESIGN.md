# DESIGN.md — GoGo 설계 원칙과 결정

이 문서는 GoGo가 **왜 지금 이런 구조인지**를 설명합니다.
`ARCHITECTURE.md`가 "무엇을, 어떻게"라면, 이 문서는 "왜"입니다.
새 기능을 추가하거나 리팩토링할 때, 여기서 설계 의도를 먼저 확인하세요.

---

## 1. 제품 설계 철학

### 핵심 가치: 마찰 없는 공유
GoGo의 가장 중요한 UX 원칙은 **"쓰기 쉬워야 한다"**입니다.
친구들에게 "이 앱 설치하고 가입해"라는 말이 필요 없어야 해요.
초대 링크 하나로 닉네임만 입력하면 그룹에 들어올 수 있고, 함께 장소를 보고 투표할 수 있어야 합니다.

이 철학이 여러 기술 결정에 영향을 줬어요:
- 그룹 참여가 닉네임 기반인 이유
- 미팅 투표가 `userId`가 아닌 `voterName(String)` 기반인 이유
- MVP에서 인증을 뒤로 미룬 이유

### 현재 단계: MVP 이후, 안정화 중
초기 MVP는 인증 없이 닉네임만으로 동작했고, 이후 Kakao/Google OAuth가 추가됐어요.
하지만 그룹과 미팅의 참여 모델은 여전히 닉네임 기반입니다.
완전한 계정 기반 시스템으로 전환하려면 상당한 마이그레이션이 필요하므로, 이 결정은 신중하게 해야 해요.

---

## 2. 주요 아키텍처 결정 (ADR 요약)

### ADR-001: Entity = JPA Entity (별도 도메인 모델 없음)

**결정**: `db/entity/`의 JPA 엔티티가 도메인 모델 역할을 겸한다.

**이유**: 팀 규모와 도메인 복잡도를 고려했을 때, 순수 도메인 객체와 JPA 엔티티를 분리하면 보일러플레이트가 과도하게 늘어난다. 단, Entity는 단순 데이터 홀더가 아니라 도메인 로직을 직접 보유한다.

**실제 적용 모습**:
```java
// 정적 팩토리 메서드 — 생성 규칙을 Entity 안에 캡슐화
Place.create(name, address, ...);

// 상태 변경 메서드 — 유효성 검사 포함
meeting.confirm(placeId);  // 이미 확정됐으면 예외
place.markAsVisited();
```

**주의할 점**: ORM 의존성이 도메인 로직에 스며드는 것은 감수한다. 하지만 `@Transactional`, 지연 로딩 같은 JPA 관심사는 Service 레이어가 책임진다.

---

### ADR-002: Service vs UseCase 구분

**결정**: 비즈니스 로직을 두 종류로 나눈다.

| | `domain/service/` | `domain/usecase/` |
|---|---|---|
| **특징** | DB와 강하게 연결, `@Transactional` 사용 | 독립적, 외부 API 호출 등 단발성 |
| **예시** | `PlaceQueryService`, `GroupService` | `SearchPlacesUseCase`, `FetchPlacePreviewUseCase` |
| **테스트** | Repository를 Mock | 외부 클라이언트를 Mock |

**이유**: DB 트랜잭션이 필요한 로직과 외부 API 호출(Naver, Jsoup 스크래핑)이 섞이면 테스트가 복잡해진다. 구분하면 각각의 책임이 명확해진다.

---

### ADR-003: AuthContext Port 패턴

**결정**: Service가 `SecurityContextHelper`(Spring Security 구현체)를 직접 쓰지 않고, `AuthContext` 인터페이스에만 의존한다.

**이유**: Service 단위 테스트 시 Spring Security 컨텍스트를 셋업할 필요 없이 `AuthContext`를 Mock으로 교체할 수 있다.

**파생 규칙**: DTO에 `userId`, `createdBy`, `authorId` 같은 필드를 절대 넣지 않는다. 사용자 신원은 항상 `authContext.requireUserId()` 또는 `authContext.requireNickname()`으로 가져온다.

---

### ADR-004: 프론트엔드 API 프록시

**결정**: 클라이언트 컴포넌트는 백엔드를 직접 호출하지 않고 `/api/proxy/*`를 경유한다.

**이유**: Vercel(HTTPS) → EC2(HTTP)로 브라우저에서 직접 호출하면 Mixed Content 에러가 발생한다. Next.js API Route가 중간에서 토큰을 HttpOnly Cookie에서 읽어 Bearer 헤더로 변환해준다.

**예외**: Server Component / SSR에서는 서버-서버 통신이므로 `NEXT_PUBLIC_API_URL`로 직접 호출한다.

---

### ADR-005: 에러 처리 전략

**결정**: 도메인 유효성 오류는 `IllegalArgumentException`, 상태 위반은 `IllegalStateException`을 던진다. `GlobalExceptionHandler`가 HTTP 응답 코드로 변환한다.

```
IllegalArgumentException  →  400 Bad Request  (잘못된 입력)
IllegalStateException     →  400 Bad Request  (허용되지 않는 상태 전환)
그 외 Exception           →  500 Internal Server Error
```

**이유**: 커스텀 예외 클래스 계층을 만들면 유지보수 비용이 올라간다. 현재 도메인 복잡도에서는 표준 예외로도 충분하다.

**한계**: 에러 코드(error code)가 없어 클라이언트가 에러 종류를 구분하기 어렵다. 기능이 늘어나면 커스텀 예외와 에러 코드 도입을 고려해야 한다.

---

### ADR-006: 닉네임 기반 그룹/미팅 참여

**결정**: 그룹 멤버와 미팅 투표는 `userId`가 아닌 닉네임(문자열)을 키로 사용한다.

**이유**: 초대 링크를 받은 비로그인 사용자도 즉시 참여할 수 있어야 한다는 제품 원칙에 따른 결정이다. OAuth 로그인 없이도 핵심 기능이 동작한다.

**한계 및 알려진 문제**:
- 같은 닉네임을 여러 사람이 쓸 수 있다 → 중복 방지 없음
- 로그인 계정과 그룹 닉네임이 연결되지 않는다
- 완전한 계정 기반으로 전환하려면 `group_members`, `meeting_votes` 테이블 마이그레이션 필요

---

### ADR-007: 장소 프리뷰 — Jsoup 스크래핑

**결정**: URL에서 장소 정보를 가져올 때 `FetchPlacePreviewUseCase`가 Jsoup으로 OG 태그를 스크래핑한다.

**이유**: 네이버 지도, 카카오맵 등 플랫폼별 공식 API를 개별 연동하는 것보다 OG 메타태그가 대부분의 플랫폼에 존재하므로 범용적으로 동작한다.

**한계**:
- 대상 사이트가 크롤러를 차단하면 실패한다 (타임아웃 5초, 실패 시 null 반환)
- JavaScript 렌더링이 필요한 SPA에서는 OG 태그를 읽지 못할 수 있다

---

## 3. 현재 알려진 설계 부채

아래는 의도적으로 미룬 것들입니다. 기술 부채가 아니라 **의식적인 트레이드오프**입니다.
해결 우선순위는 `docs/exec-plans/tech-debt-tracker.md`에서 관리합니다.

| 항목 | 현재 상태 | 언제 해결할까 |
|------|-----------|--------------|
| 닉네임 ↔ 계정 연결 | 완전히 분리됨 | 사용자 수가 늘어 그룹 관리 문제가 생길 때 |
| 에러 코드 없음 | 메시지 문자열만 반환 | 클라이언트 에러 핸들링이 정교해져야 할 때 |
| N+1 문제 잠재 위험 | EAGER 로딩으로 임시 해결 | 성능 이슈 발생 시 JPQL fetch join으로 교체 |
| 미팅 투표 중복 닉네임 | 동일 닉네임 재투표 시 덮어쓰기 | 인증 기반 투표로 전환 시 |
| 장소 검색 단일 API | Naver Local API 하나만 사용 | 검색 품질 이슈 발생 시 다중 소스 고려 |

---

## 4. 확장 시 고려해야 할 것들

### "이렇게 하면 안 됩니다"

- **그룹 멤버 검증을 Controller에서 하지 마세요.** "이 사람이 이 그룹 멤버인가?" 같은 도메인 규칙은 Service에 있어야 합니다.
- **새 외부 API 클라이언트를 Service에 직접 주입하지 마세요.** `client/` 패키지에 별도 클라이언트를 만들고 UseCase에서 조율하세요.
- **프론트엔드에서 토큰을 직접 다루지 마세요.** `lib/auth/AuthContext.tsx`와 프록시 레이어가 이미 처리합니다.

### 새 기능 추가 체크리스트

1. 새 도메인 개념이 생기면 → Entity 먼저 설계, 정적 팩토리 메서드와 상태 변경 메서드 포함
2. 새 API 엔드포인트 → Controller는 얇게, 로직은 Service/UseCase에
3. 외부 서비스 연동 → `client/` 패키지에 별도 클라이언트, UseCase에서 호출
4. 새 인증 관련 기능 → `AuthContext` 인터페이스를 통해서만
5. 프론트엔드 신규 페이지 → CSS 변수 토큰 준수, 클라이언트 API 호출은 프록시 경유

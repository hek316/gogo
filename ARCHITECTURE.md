# ARCHITECTURE.md — GoGo 시스템 아키텍처

이 문서는 GoGo의 전체 시스템 구조, 레이어 설계, 주요 패턴, 데이터 모델을 설명합니다.
코드 작업 전 `AGENTS.md`와 함께 반드시 읽으세요.

---

## 1. 시스템 전체 구조

```
┌─────────────────────────────────────────────────────────┐
│                     사용자 브라우저                        │
└───────────────────┬─────────────────────────────────────┘
                    │ HTTPS
┌───────────────────▼─────────────────────────────────────┐
│              Frontend (Vercel)                           │
│              Next.js 16 · App Router · React 19          │
│                                                          │
│  Server Component ──→ NEXT_PUBLIC_API_URL (직접 호출)     │
│  Client Component ──→ /api/proxy/* (Next.js API Route)   │
└───────────────────┬─────────────────────────────────────┘
                    │ HTTP (서버-서버)
┌───────────────────▼─────────────────────────────────────┐
│              Backend (EC2 + Docker)                      │
│              Spring Boot 3.5 · Java 21                   │
│                                                          │
│  REST API  ──→  Business Logic  ──→  PostgreSQL          │
│                      │                                   │
│               외부 API 연동                               │
│         (Naver Local · Kakao OAuth · Google OAuth)       │
└─────────────────────────────────────────────────────────┘
```

**배포:**
- Frontend: Vercel (자동 배포, `pnpm build`)
- Backend: AWS EC2, Docker 컨테이너 (`docker-compose.yml`), 이미지는 ECR

---

## 2. 백엔드 아키텍처

### 2-1. 레이어 구조와 의존성 방향

```
┌────────────────────────────────────────┐
│           api/                         │  ← REST Controllers
│  PlacesController, GroupsController    │     HTTP 요청/응답만 처리
│  MeetingsController, ReviewsController │     비즈니스 로직 없음
│  AuthController, HealthController      │
└─────────────────┬──────────────────────┘
                  │ 호출
┌─────────────────▼──────────────────────┐
│           domain/service/              │  ← 비즈니스 로직 (핵심)
│  PlaceQueryService                     │     @Service, @Transactional
│  PlaceCommandService                   │     AuthContext 주입받아 사용
│  PlaceLikeService                      │
│  GroupService, MeetingService          │
│  ReviewService, AuthService            │
│                                        │
│           domain/usecase/              │  ← 독립 유스케이스
│  SearchPlacesUseCase                   │     외부 API 연동 등 단발성 로직
│  FetchPlacePreviewUseCase              │
│  KakaoLoginUseCase, GoogleLoginUseCase │
│  RefreshTokenUseCase                   │
└─────────────────┬──────────────────────┘
                  │ 호출
┌─────────────────▼──────────────────────┐
│           db/repository/               │  ← Spring Data JPA Repositories
│  PlaceRepository, UserRepository       │     인터페이스만 정의
│  GroupRepository, MeetingRepository    │     구현체는 Spring이 자동 생성
│  ReviewRepository, PlaceLikeRepository │
│  MeetingVoteRepository                 │
│  RefreshTokenRepository                │
└─────────────────┬──────────────────────┘
                  │
┌─────────────────▼──────────────────────┐
│           db/entity/                   │  ← JPA Entity (= Domain Model)
│  Place, User, Review, PlaceLike        │     JPA 어노테이션 + 도메인 로직 직접 보유
│  Group, GroupMember, GroupPlace        │     정적 팩토리 메서드 패턴 사용
│  Meeting, MeetingVote                  │
│  RefreshToken                          │
└────────────────────────────────────────┘
```

> **중요:** 의존성은 항상 위→아래 방향만 허용. Controller가 Repository를 직접 참조하는 것은 금지.

### 2-2. 실제 패키지 구조

```
com.gogo/
├── api/                          # Presentation Layer
│   ├── PlacesController.java
│   ├── GroupsController.java
│   ├── MeetingsController.java
│   ├── ReviewsController.java
│   ├── AuthController.java
│   ├── HealthController.java
│   └── GlobalExceptionHandler.java
│
├── domain/                       # Application + Domain Layer
│   ├── service/                  # 상태 있는 비즈니스 로직 (DB 연동)
│   │   ├── PlaceQueryService.java
│   │   ├── PlaceCommandService.java
│   │   ├── PlaceLikeService.java
│   │   ├── GroupService.java
│   │   ├── MeetingService.java
│   │   ├── ReviewService.java
│   │   └── AuthService.java
│   ├── usecase/                  # 독립 유스케이스 (외부 API 등)
│   │   ├── SearchPlacesUseCase.java
│   │   ├── FetchPlacePreviewUseCase.java
│   │   ├── KakaoLoginUseCase.java
│   │   ├── GoogleLoginUseCase.java
│   │   └── RefreshTokenUseCase.java
│   ├── dto/                      # Request/Response DTO
│   ├── port/                     # 인터페이스 (역전 의존성)
│   │   ├── AuthContext.java      ← 핵심: 인증 추상화
│   │   └── TokenProvider.java
│   └── model/
│       └── AuthenticatedUser.java
│
├── db/                           # Infrastructure Layer (DB)
│   ├── entity/                   # JPA Entity
│   └── repository/               # Spring Data JPA Repository
│
├── client/                       # Infrastructure Layer (외부 API)
│   ├── kakao/KakaoOAuthClient.java
│   ├── google/GoogleOAuthClient.java
│   └── naver/NaverLocalApiClient.java
│
└── config/                       # 설정
    ├── SecurityConfig.java
    ├── CorsConfig.java
    ├── RequestLoggingFilter.java
    └── security/
        ├── JwtAuthenticationFilter.java
        ├── JwtService.java
        └── SecurityContextHelper.java    ← AuthContext 구현체
```

### 2-3. 핵심 패턴: AuthContext Port

사용자 신원은 요청 바디가 아닌 JWT에서 추출합니다.

```java
// domain/port/AuthContext.java — 인터페이스 (Service가 의존하는 대상)
public interface AuthContext {
    Optional<Long> currentUserId();
    Optional<String> currentNickname();
    Long requireUserId();       // 없으면 예외
    String requireNickname();   // 없으면 "anonymous"
}

// config/security/SecurityContextHelper.java — 구현체 (Spring Security 연동)
// Service는 AuthContext만 알고, SecurityContextHelper는 모른다
```

**왜 이렇게 하나?**
- Service 레이어 테스트 시 `AuthContext`를 Mock으로 교체 가능
- Spring Security와 Service 로직 간 결합도 제거
- DTO에 `userId`/`createdBy` 필드를 넣지 않아도 됨

### 2-4. Entity 설계 원칙

Entity는 단순 데이터 홀더가 아니라 도메인 로직을 직접 보유합니다.

```java
// 정적 팩토리 메서드로 생성 (new 직접 호출 금지)
Place.create(name, address, category, url, note, imageUrl, createdBy);
Group.create(name, createdBy);  // inviteCode는 내부에서 자동 생성

// 상태 변경은 메서드로
place.markAsVisited();
group.addMember(nickname);
meeting.vote(placeId, nickname);
meeting.confirm(placeId);
```

### 2-5. 보안 설정 요약

```
Public (인증 불필요):
  GET  /api/places, /api/places/**
  ALL  /api/auth/**
  GET  /api/health
  GET  /swagger-ui/**, /v3/api-docs/**

Protected (JWT 필요):
  나머지 모든 엔드포인트
```

JWT: HS256, 액세스 토큰 15분, 리프레시 토큰 7일 (rotation 적용)

---

## 3. 프론트엔드 아키텍처

### 3-1. 디렉토리 구조

```
frontend/src/
├── app/                          # Next.js App Router
│   ├── page.tsx                  # 홈 (장소 목록)
│   ├── layout.tsx                # 루트 레이아웃
│   ├── globals.css               # CSS 변수 토큰 정의
│   │
│   ├── api/proxy/[...path]/      # API 프록시 (클라이언트 → 백엔드)
│   │   └── route.ts
│   ├── auth/
│   │   ├── callback/route.ts     # OAuth 콜백 처리 → HttpOnly Cookie 설정
│   │   ├── login/page.tsx
│   │   └── error/page.tsx
│   │
│   ├── places/[id]/page.tsx      # 장소 상세
│   ├── groups/[id]/page.tsx      # 그룹 상세
│   ├── meetings/[id]/page.tsx    # 미팅 상세
│   ├── explore/page.tsx          # 장소 탐색
│   └── profile/page.tsx         # 프로필
│
├── components/                   # 재사용 UI 컴포넌트
│   ├── AddPlaceForm.tsx
│   ├── BottomNav.tsx
│   └── LikeButton.tsx
│
└── lib/
    ├── api/
    │   ├── config.ts             # API_BASE 설정 (핵심)
    │   ├── places.ts
    │   ├── groups.ts
    │   ├── meetings.ts
    │   └── reviews.ts
    ├── auth/
    │   ├── AuthContext.tsx       # 로그인 상태 전역 관리
    │   └── useRequireAuth.ts
    ├── theme/
    │   └── ThemeContext.tsx      # 다크모드 토글
    └── constants/
        └── categories.ts
```

### 3-2. API 호출 패턴 (핵심)

클라이언트 컴포넌트와 서버 컴포넌트는 API를 다르게 호출합니다.

```typescript
// lib/api/config.ts
export const API_BASE =
  typeof window === 'undefined'
    ? (process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080')  // 서버 → EC2 직접
    : '/api/proxy';  // 브라우저 → Next.js 프록시 경유
```

```
브라우저 (Client Component)
  → /api/proxy/api/places       (Next.js API Route)
  → EC2 백엔드 /api/places       (액세스 토큰 자동 첨부)

서버 (Server Component / SSR)
  → http://ec2.../api/places    (직접 호출, Mixed Content 없음)
```

**왜 프록시를 쓰나?** Vercel(HTTPS) → EC2(HTTP) 직접 호출 시 Mixed Content 에러 발생. 프록시가 이를 우회함.

### 3-3. 인증 흐름

```
1. 사용자가 "카카오/구글 로그인" 클릭
        ↓
2. 백엔드 /api/auth/{provider}/authorize → OAuth 제공자로 리다이렉트
        ↓
3. OAuth 콜백 → 백엔드 /api/auth/{provider}/callback
   (JWT 액세스 토큰 + 리프레시 토큰 발급)
        ↓
4. 프론트엔드 /auth/callback?at=...&rt=... 로 리다이렉트
   (route.ts에서 HttpOnly Cookie로 저장)
        ↓
5. 이후 모든 API 요청: 쿠키 자동 첨부 → 프록시가 Bearer 헤더로 변환
```

### 3-4. 디자인 시스템 핵심 규칙

상세 내용은 `frontend/DESIGN_SYSTEM.md` 참조. 핵심만 요약:

- **컬러**: `bg-bg`, `bg-surface`, `text-primary`, `text-muted` 등 CSS 변수 토큰만 사용. hex/rgba 하드코딩 금지
- **다크모드**: `<html>` 태그에 `.dark` 클래스 토글 (`ThemeContext`)
- **테마**: 화이트 배경 + 라벤더 파스텔 포인트

---

## 4. 데이터 모델

### 4-1. ERD 요약

```
users
  id, kakao_id (oauth provider의 사용자 ID), nickname, profile_image_url, provider (KAKAO|GOOGLE), created_at

places
  id, name, address, category, url, note, image_url
  status (WANT_TO_GO | VISITED)
  created_by (nickname), created_at

place_likes
  id, place_id → places, user_id → users, created_at

reviews
  id, place_id → places, content, rating(1-5)
  author_name (nickname), visited_at, created_at

groups
  id, name, invite_code (unique 8자), created_by, created_at

group_members
  id, group_id → groups, nickname, joined_at

group_places
  id, group_id → groups, place_id → places, shared_by (nickname), shared_at

meetings
  id, group_id → groups, title
  status (VOTING | CONFIRMED)
  confirmed_place_id → places (nullable)
  created_at

meeting_votes
  id, meeting_id → meetings, place_id → places, voter_name (nickname), voted_at

refresh_tokens
  id, user_id → users, token_hash (SHA-256), expires_at, revoked_at, created_at
```

### 4-2. 주요 관계

- `places` ↔ `users`: `place_likes`로 다대다
- `groups` ↔ `places`: `group_places`로 다대다 (그룹 내 공유 장소)
- `meetings` → `groups`: 한 그룹에 여러 미팅
- `meeting_votes`: 닉네임 기반 (비회원도 투표 가능)

---

## 5. 외부 의존성

| 서비스 | 용도 | 설정 위치 |
|--------|------|-----------|
| **Kakao OAuth** | 소셜 로그인 | `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`, `KAKAO_REDIRECT_URI` |
| **Google OAuth** | 소셜 로그인 | `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GOOGLE_REDIRECT_URI` |
| **Naver Local API** | 장소 검색 | `client/naver/NaverLocalApiClient.java` |
| **PostgreSQL** | 주 데이터베이스 | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` |
| **AWS ECR/EC2** | 백엔드 배포 | `docker-compose.yml` |
| **Vercel** | 프론트엔드 배포 | `NEXT_PUBLIC_API_URL` |

---

## 6. 테스트 전략

### 백엔드

- **단위 테스트**: `domain/service/`, `domain/usecase/` — `AuthContext` mock 처리
- **DB**: H2 in-memory (PostgreSQL 호환 모드) — 외부 DB 불필요
- **실행**: `./gradlew test`

```java
// 테스트에서 AuthContext mock 방법
@ExtendWith(MockitoExtension.class)
class PlaceQueryServiceTest {
    @Mock AuthContext authContext;
    @Mock PlaceRepository placeRepository;

    @BeforeEach void setUp() {
        given(authContext.currentUserId()).willReturn(Optional.of(1L));
    }
}
```

### 프론트엔드

- `pnpm build`로 TypeScript 타입 오류 및 빌드 오류 검증
- 런타임 오류는 개발 서버(`pnpm dev`)로 확인

# SECURITY.md — GoGo 보안 정책

이 문서는 GoGo의 보안 모델, 현재 보호 수준, 알려진 위험, 그리고 개발 시 지켜야 할 보안 규칙을 정의합니다.

---

## 1. 인증 아키텍처

### 전체 흐름

```
브라우저  →  OAuth Provider (Kakao/Google)
         ←  authorization code

브라우저  →  백엔드 /api/auth/{provider}/callback?code=...
         ←  프론트엔드로 redirect (?at=ACCESS_TOKEN&rt=REFRESH_TOKEN)

프론트엔드 /auth/callback
  └─ access-token  → HttpOnly Cookie (15분)
  └─ refresh-token → HttpOnly Cookie (7일)

이후 모든 요청: 쿠키 자동 첨부 → 프록시가 Bearer 헤더로 변환
```

### JWT 액세스 토큰

| 항목 | 값 |
|------|----|
| 알고리즘 | HS256 |
| 유효 기간 | 15분 |
| 서명 키 | `JWT_SECRET` 환경 변수 (최소 32바이트 권장) |
| 포함 클레임 | `sub` (userId), `nickname`, `iat`, `exp` |
| 저장 위치 | HttpOnly Cookie (`access-token`) |

### 리프레시 토큰

| 항목 | 값 |
|------|----|
| 생성 방식 | `UUID.randomUUID()` (raw) |
| 저장 방식 | SHA-256 해시값만 DB에 저장 (raw 토큰 미저장) |
| 유효 기간 | 7일 |
| 저장 위치 | HttpOnly Cookie (`refresh-token`, path: `/api/auth`) |
| 로테이션 | 사용 시 즉시 폐기 후 새 토큰 발급 |
| 재사용 감지 | 폐기된 토큰으로 재요청 시 해당 유저의 **모든** 리프레시 토큰 일괄 폐기 |

**재사용 감지 로직 (`RefreshTokenUseCase`):**
```java
if (!token.isValid()) {
    if (token.getRevokedAt() != null) {
        // 이미 폐기된 토큰 재사용 → 탈취 의심 → 전체 세션 무효화
        refreshTokenRepository.revokeAllByUserId(token.getUserId(), LocalDateTime.now());
    }
    throw new IllegalArgumentException("만료되었거나 폐기된 refresh token입니다.");
}
```

### 쿠키 설정

```typescript
// 프론트엔드 /auth/callback/route.ts
{
  httpOnly: true,              // XSS로 JS 접근 불가
  secure: NODE_ENV === 'production',  // HTTPS only (프로덕션)
  sameSite: 'lax',            // CSRF 기본 방어
  maxAge: 15 * 60,            // access-token: 15분
}
{
  httpOnly: true,
  secure: NODE_ENV === 'production',
  sameSite: 'lax',
  maxAge: 7 * 24 * 60 * 60,  // refresh-token: 7일
  path: '/api/auth',          // refresh 엔드포인트에만 전송
}
```

---

## 2. 엔드포인트 접근 제어

```
PUBLIC (인증 불필요):
  GET  /api/places
  GET  /api/places/**
  ALL  /api/auth/**          ← OAuth 플로우
  GET  /api/health
  GET  /swagger-ui/**
  GET  /v3/api-docs/**

PROTECTED (JWT Bearer 필요):
  그 외 모든 엔드포인트
  → 토큰 없으면 401 {"error": "인증이 필요합니다."}
```

---

## 3. CORS 설정

허용된 Origin만 응답합니다. `*` (와일드카드) 없음.

```java
// CorsConfig.java
List<String> origins = [
  "http://localhost:3000",          // 개발 환경
  "https://gogo-sigma.vercel.app",  // 프로덕션 프론트엔드
  FRONTEND_URL 환경변수 (있으면 추가)
]
config.setAllowCredentials(true);   // 쿠키 전송 허용
```

> 새 도메인(프리뷰 배포 등)이 필요하면 `FRONTEND_URL` 환경 변수로 추가하세요. 코드를 수정하지 마세요.

---

## 4. 알려진 위험과 현재 상태

### 🔴 HIGH — OAuth 콜백 시 토큰 URL 노출

**현상**: OAuth 콜백 후 프론트엔드로 리다이렉트할 때 토큰이 쿼리 파라미터에 포함됩니다.
```
https://gogo.vercel.app/auth/callback?at=eyJhb...&rt=uuid-...
```

**위험**: 브라우저 히스토리, 서버 액세스 로그, Referrer 헤더에 토큰이 노출될 수 있습니다.

**현재 대응**: 프론트엔드 `/auth/callback`에서 즉시 HttpOnly Cookie로 이동 후 `/`로 리다이렉트. 토큰이 URL에 머무는 시간이 매우 짧습니다.

**근본 해결 방향**: 백엔드가 프론트엔드로 리다이렉트할 때 토큰을 쿠키로 직접 Set-Cookie하는 방식으로 전환. (현재 미구현, `tech-debt-tracker.md` 등록 필요)

---

### 🟡 MEDIUM — SSRF (FetchPlacePreviewUseCase)

**현상**: 사용자가 입력한 URL을 서버에서 직접 가져옵니다(`Jsoup`).

```java
// FetchPlacePreviewUseCase.java
Document doc = Jsoup.connect(url).get();
```

**위험**: 공격자가 내부 네트워크 주소(`http://169.254.169.254/` 등 EC2 메타데이터)를 URL로 전달하면 서버가 내부 요청을 보낼 수 있습니다.

**현재 대응**: 5초 타임아웃, 실패 시 null 반환으로 안전하게 처리. 치명적 오류 없음.

**권장 조치**: URL 입력 시 허용 스킴(https://) 및 사설 IP 대역 차단 로직 추가.

---

### 🟡 MEDIUM — 닉네임 기반 그룹 참여의 위장 가능성

**현상**: 그룹 참여 시 닉네임을 자유롭게 입력합니다. 타인의 닉네임을 사용해 참여할 수 있습니다.

**위험**: 기존 멤버의 닉네임을 도용해 투표하거나 후기를 남길 수 있습니다.

**현재 대응**: 없음. 의도적인 트레이드오프 (참여 마찰 최소화 우선).

**근본 해결 방향**: 로그인 계정과 그룹 멤버십 연결. `DESIGN.md` ADR-006 참조.

---

### 🟢 LOW — 그룹 초대 코드 브루트포스

**현상**: 초대 코드는 UUID에서 추출한 8자 hex 문자열입니다.

```java
UUID.randomUUID().toString().replace("-", "").substring(0, 8)
// 가능한 조합: 16^8 = 약 43억
```

**위험**: 이론적으로 자동화 요청으로 유효한 코드를 찾을 수 있습니다.

**현재 대응**: 없음 (Rate limiting 미구현).

**권장 조치**: `/api/groups/join` 엔드포인트에 IP 기반 요청 제한 추가.

---

### 🟢 LOW — Rate Limiting 없음

현재 모든 API 엔드포인트에 요청 제한이 없습니다. 남용 시 서버 과부하 또는 OAuth API 쿼터 초과 위험이 있습니다.

**권장 조치**: Spring Boot + Bucket4j 또는 EC2 앞단에 AWS WAF 적용.

---

## 5. 개발 시 보안 체크리스트

새 기능을 개발할 때 아래를 반드시 확인하세요.

### 인증/인가
- [ ] 새 엔드포인트가 인증이 필요한가? → `SecurityConfig`의 `permitAll()` 목록에 명시적으로 추가하지 않으면 자동으로 보호됨
- [ ] 다른 사용자의 리소스에 접근 가능한가? → 소유권 검증 로직 추가 (`createdBy` 비교 등)
- [ ] User identity를 요청 바디에서 받고 있지 않은가? → `AuthContext`에서만 가져올 것

### 데이터 검증
- [ ] 사용자 입력에 `@Valid` + Bean Validation 적용했는가?
- [ ] URL을 입력받아 서버에서 요청하는 경우 → SSRF 방어 고려
- [ ] 에러 메시지에 내부 정보(스택 트레이스, SQL 등)가 노출되지 않는가?

### 환경 변수
- [ ] 시크릿(키, 비밀번호, 토큰)이 코드에 하드코딩되지 않았는가?
- [ ] `.env` 파일이 `.gitignore`에 포함되어 있는가?

### 쿠키/토큰
- [ ] 새 쿠키를 설정한다면 `httpOnly: true`, `secure: true` (prod), `sameSite` 설정 확인
- [ ] 프론트엔드에서 `localStorage`에 토큰을 저장하지 않는가?

---

## 6. 환경 변수 보안 요구사항

| 변수 | 요구사항 |
|------|---------|
| `JWT_SECRET` | 최소 32바이트 이상 랜덤 문자열. 환경마다 다른 값 사용 |
| `KAKAO_CLIENT_SECRET` | Kakao 개발자 콘솔에서 발급. 절대 공개 저장소에 커밋 금지 |
| `GOOGLE_CLIENT_SECRET` | Google Cloud Console에서 발급. 절대 공개 저장소에 커밋 금지 |
| `DB_PASSWORD` | 환경마다 다른 강력한 패스워드 사용 |

---

## 7. 보안 이슈 발견 시

1. 공개 이슈 트래커에 올리지 마세요
2. 팀 내부 채널로 즉시 공유
3. 심각도 분류 (HIGH/MEDIUM/LOW)
4. HIGH는 즉시 핫픽스, MEDIUM/LOW는 `tech-debt-tracker.md`에 등록 후 스프린트에 포함

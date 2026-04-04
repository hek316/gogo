# DB Schema

> 자동 생성 — 수동으로 편집하지 마세요.  
> 출처: `backend/src/main/java/com/gogo/db/entity/` 엔티티 클래스  
> 갱신 시점: 2026-04-04

## 테이블 목록

| 테이블 | 엔티티 클래스 | 설명 |
|--------|--------------|------|
| `users` | `User` | 가입 사용자 (Kakao/Google OAuth) |
| `places` | `Place` | 저장된 장소 |
| `place_likes` | `PlaceLike` | 장소 좋아요 |
| `reviews` | `Review` | 장소 리뷰 |
| `groups` | `Group` | 그룹 |
| `group_members` | `GroupMember` | 그룹 멤버 |
| `group_places` | `GroupPlace` | 그룹에 공유된 장소 |
| `meetings` | `Meeting` | 약속 (투표 진행 중 or 확정) |
| `meeting_candidates` | `Meeting.@ElementCollection` | 약속 후보 장소 목록 |
| `meeting_votes` | `MeetingVote` | 약속 투표 |
| `refresh_tokens` | `RefreshToken` | JWT 리프레시 토큰 |

---

## 테이블 상세

### `users`

```sql
id               BIGINT       PK, AUTO_INCREMENT
kakao_id         VARCHAR      NOT NULL          -- OAuth provider의 사용자 ID (컬럼명이 kakao_id이나 Google도 저장)
nickname         VARCHAR      NOT NULL
profile_image_url VARCHAR     NULL
provider         VARCHAR      NOT NULL          -- ENUM: KAKAO | GOOGLE
created_at       TIMESTAMP    NOT NULL

UNIQUE (kakao_id, provider)
```

> **주의**: 컬럼명이 `kakao_id`이지만 `OAuthProvider.GOOGLE` 유저도 이 컬럼에 저장됨 (tech-debt: 컬럼명 개선 필요)

---

### `places`

```sql
id         BIGINT    PK, AUTO_INCREMENT
name       VARCHAR   NOT NULL
address    VARCHAR   NULL
category   VARCHAR   NULL
url        VARCHAR   NULL
note       TEXT      NULL
image_url  TEXT      NULL
status     VARCHAR   NOT NULL    -- ENUM: WANT_TO_GO | VISITED
created_by VARCHAR   NOT NULL    -- 닉네임 (User.id FK 없음 — 닉네임 기반 설계)
created_at TIMESTAMP NOT NULL
```

> `created_by`는 `users.nickname`과 같은 값이지만 FK가 없음. 의도적인 설계 — [DESIGN.md ADR-006](../DESIGN.md) 참조.

---

### `place_likes`

```sql
id         BIGINT    PK, AUTO_INCREMENT
user_id    BIGINT    NOT NULL    -- users.id 참조 (FK 어노테이션 없음, 논리적 참조)
place_id   BIGINT    NOT NULL    -- places.id 참조
created_at TIMESTAMP NOT NULL

UNIQUE (user_id, place_id)
```

---

### `reviews`

```sql
id          BIGINT   PK, AUTO_INCREMENT
place_id    BIGINT   NOT NULL    -- places.id 참조 (논리적)
author_name VARCHAR  NOT NULL    -- 닉네임 (FK 없음)
rating      INT      NOT NULL    -- 1~5
content     TEXT     NULL
visited_at  DATE     NULL
created_at  TIMESTAMP NOT NULL
```

---

### `groups`

```sql
id          BIGINT    PK, AUTO_INCREMENT
name        VARCHAR   NOT NULL
invite_code VARCHAR   NOT NULL, UNIQUE    -- 8자리 랜덤 코드
created_by  VARCHAR   NOT NULL            -- 닉네임 (FK 없음)
created_at  TIMESTAMP NOT NULL
```

> `Group` 엔티티는 `members`에 `FetchType.EAGER` 사용 중 — tech-debt ARCH-003

---

### `group_members`

```sql
id         BIGINT    PK, AUTO_INCREMENT
group_id   BIGINT    NOT NULL    -- groups.id FK (@JoinColumn)
nickname   VARCHAR   NOT NULL
joined_at  TIMESTAMP NOT NULL
```

---

### `group_places`

```sql
id         BIGINT    PK, AUTO_INCREMENT
group_id   BIGINT    NOT NULL    -- groups.id 참조 (논리적)
place_id   BIGINT    NOT NULL    -- places.id 참조 (논리적)
shared_by  VARCHAR   NOT NULL    -- 닉네임 (FK 없음)
shared_at  TIMESTAMP NOT NULL
```

---

### `meetings`

```sql
id                  BIGINT    PK, AUTO_INCREMENT
group_id            BIGINT    NOT NULL    -- groups.id 참조 (논리적)
title               VARCHAR   NOT NULL
status              VARCHAR   NOT NULL    -- ENUM: VOTING | CONFIRMED
confirmed_place_id  BIGINT    NULL        -- 확정 시 places.id
created_at          TIMESTAMP NOT NULL
```

> `Meeting` 엔티티는 `candidatePlaceIds`에 `FetchType.EAGER` 사용 중 — tech-debt ARCH-004

---

### `meeting_candidates`

`Meeting`의 `@ElementCollection`으로 자동 생성되는 조인 테이블.

```sql
meeting_id  BIGINT    NOT NULL    -- meetings.id FK
place_id    BIGINT    NOT NULL    -- 후보 장소 ID
```

---

### `meeting_votes`

```sql
id          BIGINT    PK, AUTO_INCREMENT
meeting_id  BIGINT    NOT NULL    -- meetings.id 참조 (논리적)
place_id    BIGINT    NOT NULL    -- 투표한 장소 ID
voter_name  VARCHAR   NOT NULL    -- 닉네임 (FK 없음)
voted_at    TIMESTAMP NOT NULL
```

---

### `refresh_tokens`

```sql
id          BIGINT    PK, AUTO_INCREMENT
token_hash  VARCHAR   NOT NULL, UNIQUE    -- SHA-256 해시 (평문 저장 안 함)
user_id     BIGINT    NOT NULL            -- users.id 참조 (논리적)
expires_at  TIMESTAMP NOT NULL
revoked_at  TIMESTAMP NULL               -- NULL이면 유효, 설정되면 폐기됨
created_at  TIMESTAMP NOT NULL
```

> `token_hash`만 DB에 저장. 평문 토큰(`plainToken`)은 `@Transient` — 메모리에만 존재.  
> 리프레시 토큰 탈취 감지: 이미 revoke된 토큰 재사용 시 해당 유저의 모든 토큰 폐기 ([SECURITY.md](../SECURITY.md) 참조)

---

## ERD (텍스트)

```
users ──────────────────────────────────────────────────────
  │                                                         │
  │ (nickname 참조, FK 없음)                                 │ (user_id)
  ▼                                                         ▼
places ◄──── place_likes                           refresh_tokens
  │
  ├──── reviews        (place_id, FK 없음)
  │
  └──── group_places   (place_id, FK 없음)
           │
           │ (group_id, FK 없음)
           ▼
         groups
           │
           ├──── group_members  (group_id, @JoinColumn FK)
           │
           └──── meetings
                   │
                   ├──── meeting_candidates  (@ElementCollection)
                   │
                   └──── meeting_votes      (meeting_id, FK 없음)
```

---

## FK 패턴 정리

GoGo는 대부분의 연관관계를 **논리적 참조** (컬럼 ID 저장, JPA FK 어노테이션 없음)로 처리한다.
실제 DB 레벨 FK 제약이 걸린 곳은 딱 두 곳이다:

| 참조 | FK 방식 | 이유 |
|------|---------|------|
| `group_members.group_id → groups.id` | `@JoinColumn` (JPA FK) | Group ↔ GroupMember 강결합 (CascadeAll, OrphanRemoval) |
| `meeting_candidates.meeting_id → meetings.id` | `@ElementCollection` | 값 컬렉션, Meeting 생명주기와 동일 |
| 나머지 모든 참조 | 논리적 (컬럼 값만) | 닉네임 기반 느슨한 결합 설계 — [DESIGN.md ADR-006](../DESIGN.md) |

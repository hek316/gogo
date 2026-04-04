# tech-debt-tracker.md — GoGo 기술 부채 목록

이 파일은 GoGo의 알려진 기술 부채를 한 곳에서 관리합니다.
새 부채를 발견하면 이 파일에 등록하고, 해결하면 상태를 업데이트하세요.

**등록 규칙:**
- TODO/FIXME 주석을 코드에 남기지 말고 여기에 등록
- 의도적 트레이드오프와 실수로 생긴 부채를 구분해서 기록
- 해결 시 상태를 `OPEN` → `DONE`으로 바꾸고 날짜 기입

---

## 우선순위 기준

| 레벨 | 기준 |
|------|------|
| 🔴 HIGH | 데이터 손실, 보안 취약점, 서비스 중단 위험 |
| 🟡 MEDIUM | 기능 오작동 가능성, 유지보수 난이도 증가 |
| 🟢 LOW | 코드 품질, 개발 경험, 잠재적 성능 이슈 |

---

## 보안 (Security)

| ID | 제목 | 우선순위 | 상태 | 참고 |
|----|------|---------|------|------|
| SEC-001 | OAuth 콜백 시 토큰이 URL 쿼리 파라미터로 노출 | 🔴 HIGH | OPEN | `SECURITY.md` §4 |
| SEC-002 | FetchPlacePreviewUseCase SSRF 방어 없음 | 🟡 MEDIUM | OPEN | `SECURITY.md` §4 |
| SEC-003 | 닉네임 기반 그룹 참여 — 위장 방어 없음 | 🟡 MEDIUM | OPEN | `DESIGN.md` ADR-006 |
| SEC-004 | Rate Limiting 전무 (모든 엔드포인트) | 🟡 MEDIUM | OPEN | `SECURITY.md` §4 |
| SEC-005 | 그룹 초대 코드 브루트포스 방어 없음 | 🟢 LOW | OPEN | `SECURITY.md` §4 |

### SEC-001 상세
**현상**: OAuth 콜백 후 `?at=ACCESS_TOKEN&rt=REFRESH_TOKEN`이 URL에 포함된 채 프론트엔드로 리다이렉트됨. 브라우저 히스토리, 서버 로그, Referrer 헤더에 노출 가능.

**해결 방향**: 백엔드가 OAuth 콜백 처리 후 프론트엔드로 리다이렉트할 때 `Set-Cookie` 헤더로 직접 토큰을 심는 방식으로 전환. URL 파라미터 제거.

**영향 범위**: `AuthController.handleOAuthCallback()` + 프론트엔드 `/auth/callback/route.ts`

---

### SEC-002 상세
**현상**: `FetchPlacePreviewUseCase`가 사용자 입력 URL을 서버에서 직접 fetch. 사설 IP나 클라우드 메타데이터 주소(`169.254.169.254`)를 입력할 경우 SSRF 가능.

**해결 방향**: URL 파싱 후 스킴(`https://`만 허용), 사설 IP 대역 차단 로직 추가.
```java
// 추가할 검증 예시
private void validateUrl(String url) {
    URI uri = URI.create(url);
    if (!"https".equals(uri.getScheme())) throw new IllegalArgumentException(...);
    InetAddress addr = InetAddress.getByName(uri.getHost());
    if (addr.isSiteLocalAddress() || addr.isLoopbackAddress()) throw new IllegalArgumentException(...);
}
```

---

## 안정성 (Reliability)

| ID | 제목 | 우선순위 | 상태 | 참고 |
|----|------|---------|------|------|
| REL-001 | PostgreSQL 데이터가 EC2 로컬 볼륨에만 존재 (백업 없음) | 🔴 HIGH | OPEN | `RELIABILITY.md` §6 |
| REL-002 | EC2 단일 인스턴스 — SPOF | 🔴 HIGH | OPEN | `RELIABILITY.md` §6 |
| REL-003 | 외부 업타임 모니터 없음 (다운돼도 모름) | 🟡 MEDIUM | OPEN | `RELIABILITY.md` §3 |
| REL-004 | 에러 트래킹 없음 (Sentry 등 미구성) | 🟡 MEDIUM | OPEN | `RELIABILITY.md` §3 |
| REL-005 | 스키마 마이그레이션 도구 없음 (Flyway 미도입) | 🟡 MEDIUM | OPEN | `RELIABILITY.md` §5 |
| REL-006 | ECR 이미지 롤백 전략 없음 | 🟡 MEDIUM | OPEN | `RELIABILITY.md` §4 |
| REL-007 | Spring Actuator 미구성 | 🟢 LOW | OPEN | `RELIABILITY.md` §7 |
| REL-008 | Naver API 실패 시 graceful fallback 없음 | 🟢 LOW | OPEN | — |

### REL-001 상세
**현상**: `docker-compose.yml`의 PostgreSQL이 EC2 로컬 볼륨(`gogo-postgres-data`)을 사용. EC2 인스턴스 손실 시 모든 데이터 유실.

**해결 방향 (단계별)**:
1. 단기: `pg_dump` 크론잡 → S3 업로드 (비용 최소)
2. 중기: AWS RDS 전환 (자동 백업 + Multi-AZ)

---

### REL-005 상세
**현상**: 스키마 변경 시 SQL을 수동으로 DB에 직접 실행. 변경 이력 없음. 실수로 인한 데이터 손실 위험.

**해결 방향**: Flyway 도입.
```gradle
implementation 'org.flywaydb:flyway-core'
```
- `resources/db/migration/V1__init.sql` 부터 시작
- `DDL_AUTO`를 `validate`에서 `none`으로 변경 (Flyway가 담당)

---

## 아키텍처 (Architecture)

| ID | 제목 | 우선순위 | 상태 | 참고 |
|----|------|---------|------|------|
| ARCH-001 | 닉네임 ↔ 로그인 계정 완전 분리 | 🟡 MEDIUM | OPEN | `DESIGN.md` ADR-006 |
| ARCH-002 | API 에러 응답에 에러 코드 없음 | 🟡 MEDIUM | OPEN | `DESIGN.md` §3 |
| ARCH-003 | `Group.members` FetchType.EAGER (N+1 잠재 위험) | 🟢 LOW | OPEN | `Group.java:30` |
| ARCH-004 | `Meeting.candidatePlaceIds` FetchType.EAGER | 🟢 LOW | OPEN | `Meeting.java:23` |
| ARCH-005 | 장소 검색 소스 단일화 (Naver API만) | 🟢 LOW | OPEN | `PRODUCT_SENSE.md` §4 |

### ARCH-001 상세
**현상**: `group_members`, `meeting_votes` 테이블이 `user_id` FK 없이 `nickname(String)`만 사용. 로그인 계정과 그룹 참여 이력이 연결되지 않음.

**해결 시점**: 사용자 수 증가로 그룹 관리 문제가 생길 때.

**마이그레이션 난이도**: 높음. `group_members.user_id` 컬럼 추가, 기존 닉네임으로 유저 매핑, 닉네임 중복 처리 정책 필요.

---

### ARCH-002 상세
**현상**: 모든 에러 응답이 `{"error": "메시지"}` 형태. 클라이언트가 에러 종류를 코드로 구분 불가.

**해결 방향**: 에러 코드 enum 도입.
```json
// 현재
{"error": "유효하지 않은 refresh token입니다."}

// 개선 후
{"code": "AUTH_INVALID_REFRESH_TOKEN", "message": "유효하지 않은 refresh token입니다."}
```

---

## 개발 경험 (DX)

| ID | 제목 | 우선순위 | 상태 | 참고 |
|----|------|---------|------|------|
| DX-001 | `SHOW_SQL` 기본값이 `true` — 프로덕션 로그 과다 | 🟡 MEDIUM | OPEN | `application.yml:10` |
| DX-002 | 테스트 커버리지 측정 도구 미설정 | 🟢 LOW | OPEN | — |
| DX-003 | Swagger 문서 자동화 미적용 | 🟢 LOW | OPEN | — |

### DX-001 상세
**현상**: `application.yml`에서 `show-sql: ${SHOW_SQL:true}`로 기본값이 `true`. 환경 변수를 별도 설정하지 않으면 프로덕션에서도 모든 SQL이 로그에 출력됨. 성능 저하 + 민감 데이터 노출 가능.

**해결 방향**: 기본값을 `false`로 변경. 로컬 개발 시 `.env`에 `SHOW_SQL=true` 명시.
```yaml
# 변경 전
show-sql: ${SHOW_SQL:true}
# 변경 후
show-sql: ${SHOW_SQL:false}
```

---

## 완료된 항목

| ID | 제목 | 완료일 | 비고 |
|----|------|--------|------|
| — | — | — | 아직 없음 |

---

## 등록 방법

새 기술 부채 발견 시:
```
| XXX-NNN | 제목 | 🟡 MEDIUM | OPEN | 관련 파일 |
```
형식으로 해당 카테고리 테이블에 추가하고, 필요하면 상세 설명을 아래에 추가하세요.

**코드에 주석으로 남기는 것은 금지합니다.** 주석은 사라지지만 이 파일은 남습니다.

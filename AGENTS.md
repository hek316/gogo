# AGENTS.md — GoGo 에이전트 작업 가이드

이 파일은 AI 에이전트(Claude 등)가 GoGo 코드베이스에서 작업할 때 따라야 하는 규칙과 워크플로우를 정의합니다.
**모든 작업 시작 전 반드시 이 파일을 먼저 읽으세요.**

---

## 1. 작업 시작 전 필수 컨텍스트 로딩

태스크를 받으면 바로 코드를 건드리지 마세요. 아래 순서로 컨텍스트를 먼저 파악하세요.

### 1-1. 항상 읽어야 하는 파일 (모든 작업 공통)

| 파일 | 목적 |
|------|------|
| `CLAUDE.md` | 빌드/실행 명령어, 환경 변수, 전체 아키텍처 요약 |
| `ARCHITECTURE.md` | 레이어 구조, 의존성 방향, 도메인 모델 상세 |
| `docs/DESIGN.md` | 현재 시스템 설계 원칙 및 핵심 의사결정 |

### 1-2. 태스크 유형별 추가 읽기

| 태스크 유형 | 추가로 읽어야 할 파일 |
|-------------|----------------------|
| **신규 기능 개발** | `docs/product-specs/` 해당 스펙 + `docs/exec-plans/active/` |
| **프론트엔드 작업** | `docs/FRONTEND.md` + `frontend/DESIGN_SYSTEM.md` |
| **버그 수정** | `docs/RELIABILITY.md` + `docs/generated/db-schema.md` (DB 관련 시) |
| **보안 관련** | `docs/SECURITY.md` |
| **기술 부채** | `docs/exec-plans/tech-debt-tracker.md` |
| **제품 방향 판단 필요** | `docs/PRODUCT_SENSE.md` + `docs/design-docs/core-beliefs.md` |

### 1-3. LLM 참조 문서 (외부 도구 사용 시)

`docs/references/` 안의 `*-llms.txt` 파일들은 외부 라이브러리/도구의 LLM 친화적 요약본입니다.
해당 도구를 다룰 때 공식 문서 대신 이 파일을 먼저 참조하세요.

---

## 2. 작업 워크플로우

모든 작업은 아래 5단계를 반드시 순서대로 밟으세요.

```
① 컨텍스트 로딩  →  ② 계획 수립  →  ③ 구현  →  ④ 품질 게이트  →  ⑤ 문서 업데이트
```

### ① 컨텍스트 로딩
- 위 1절의 규칙에 따라 관련 문서를 읽는다
- 기존 코드 패턴을 파악한다 (비슷한 기능이 이미 있는지 검색)
- 모르는 게 있으면 추측하지 말고 질문한다

### ② 계획 수립
- 변경할 파일 목록과 변경 이유를 명시한다
- 아키텍처 제약(4절)에 위배되는 부분이 없는지 확인한다
- 복잡한 작업이라면 `docs/exec-plans/active/`에 계획 파일을 먼저 작성한다

### ③ 구현
- 한 번에 너무 많이 바꾸지 않는다. 작은 단위로 커밋 가능한 상태를 유지한다
- 새 패턴을 도입하기 전, 기존 코드에서 유사한 패턴을 찾아 일관성을 지킨다
- 프론트엔드: `frontend/DESIGN_SYSTEM.md`의 컬러 토큰 규칙을 반드시 준수한다

### ④ 품질 게이트 (3절 참조)
- 코드 작성 후 반드시 품질 게이트를 통과해야 한다
- 실패한 게이트가 있으면 다음 단계로 넘어가지 않는다

### ⑤ 문서 업데이트
- 새 API 추가 → `docs/generated/db-schema.md` 또는 API 명세 업데이트
- 아키텍처 결정 → `ARCHITECTURE.md` 또는 `docs/design-docs/` 업데이트
- 완료된 플랜 → `docs/exec-plans/active/` → `docs/exec-plans/completed/`로 이동

---

## 3. 품질 게이트

구현 완료 후 아래 체크리스트를 순서대로 실행하세요. **모두 통과해야 PR/완료 처리합니다.**

### 백엔드 품질 게이트

```bash
# 1. 전체 테스트 통과
cd backend && ./gradlew test

# 2. 빌드 성공
cd backend && ./gradlew build

# 3. 변경된 레이어의 테스트 커버리지 확인
#    비즈니스 로직(application/service, application/usecase)은 단위 테스트 필수
```

**통과 기준:**
- [ ] 모든 기존 테스트 통과 (새 코드가 기존 테스트를 깨트리면 안 됨)
- [ ] 새로운 비즈니스 로직에 단위 테스트 작성 (커버리지 목표 ≥ 80%)
- [ ] 컴파일 경고 없음

### 프론트엔드 품질 게이트

```bash
# 1. 타입 체크
cd frontend && pnpm build

# 2. 개발 서버 정상 실행 확인
cd frontend && pnpm dev
```

**통과 기준:**
- [ ] TypeScript 타입 에러 없음
- [ ] 프로덕션 빌드 성공
- [ ] 하드코딩된 색상값 없음 (hex, rgba 등 — CSS 변수 토큰만 사용)
- [ ] 다크모드 깨짐 없음

### 코드 리뷰 셀프 체크

- [ ] 불필요한 `console.log` / `println` 제거
- [ ] TODO/FIXME 주석은 `docs/exec-plans/tech-debt-tracker.md`에 등록
- [ ] 환경 변수나 시크릿이 코드에 하드코딩되지 않음
- [ ] 새 의존성 추가 시 이유 명시

---

## 4. 아키텍처 제약 — 하지 말아야 할 것들

### 백엔드

| ❌ 금지 | ✅ 올바른 방법 |
|---------|--------------|
| Controller에서 Repository 직접 호출 | Controller → Service/UseCase → Repository |
| Domain Entity에서 외부 서비스 참조 | 외부 서비스는 Infrastructure 레이어에 위치 |
| DTO를 Domain Entity로 직접 사용 | 레이어 경계에서 명시적으로 매핑 |
| `AuthContext` 구현체(`SecurityContextHelper`)를 직접 주입 | `AuthContext` 인터페이스만 주입 |
| 요청 Body에 `userId`/`createdBy` 포함 | User identity는 JWT SecurityContext에서 추출 |
| `@Transactional`을 Domain Entity에 붙이기 | Service/UseCase 레이어에서 트랜잭션 관리 |

### 프론트엔드

| ❌ 금지 | ✅ 올바른 방법 |
|---------|--------------|
| 컬러 하드코딩 (`#7C6FCD`, `rgba(...)`) | CSS 변수 토큰 사용 (`bg-surface`, `text-primary` 등) |
| Client Component에서 백엔드 직접 호출 | `/api/proxy/*` 경유 (Next.js API Route) |
| Server Component에서 proxy 경유 | `NEXT_PUBLIC_API_URL`로 직접 호출 |
| `localStorage`에 JWT 저장 | HttpOnly Cookie (자동 처리됨) |

---

## 5. 문서 구조 빠른 참조

```
gogo/
├── AGENTS.md               ← 지금 읽고 있는 파일
├── ARCHITECTURE.md         ← 시스템 전체 구조 상세
├── CLAUDE.md               ← 빌드 명령어, 환경변수, 아키텍처 요약
│
└── docs/
    ├── DESIGN.md           ← 현재 설계 원칙 (What & Why)
    ├── FRONTEND.md         ← 프론트엔드 패턴, 컴포넌트 가이드
    ├── PLANS.md            ← 로드맵 및 우선순위 개요
    ├── PRODUCT_SENSE.md    ← 제품 방향성, 타겟 유저, 핵심 가치
    ├── QUALITY_SCORE.md    ← 품질 메트릭 현황 및 목표
    ├── RELIABILITY.md      ← 장애 대응, 모니터링, SLA
    ├── SECURITY.md         ← 보안 정책, 위협 모델, 취약점 관리
    │
    ├── design-docs/        ← 설계 의사결정 기록 (ADR 스타일)
    │   ├── index.md        ← 전체 설계 결정 목록
    │   └── core-beliefs.md ← 변하지 않는 핵심 신념
    │
    ├── exec-plans/         ← 실행 계획
    │   ├── active/         ← 진행 중인 플랜
    │   ├── completed/      ← 완료된 플랜 아카이브
    │   └── tech-debt-tracker.md  ← 기술 부채 목록
    │
    ├── generated/          ← 자동 생성 문서 (직접 수정 금지)
    │   └── db-schema.md    ← DB 스키마 (코드에서 자동 생성)
    │
    ├── product-specs/      ← 기능 명세
    │   ├── index.md        ← 전체 기능 목록
    │   └── *.md            ← 개별 기능 스펙
    │
    └── references/         ← 외부 도구 LLM 참조 문서
        └── *-llms.txt      ← 라이브러리별 요약본
```

---

## 6. 에이전트 행동 원칙

1. **추측하지 않는다** — 확실하지 않으면 작업을 멈추고 명확히 한다
2. **작게 움직인다** — 한 번에 한 가지 변경, 검증 후 다음으로
3. **기존 패턴을 따른다** — 새 패턴 도입 전 기존 코드에서 유사 사례를 찾는다
4. **흔적을 남긴다** — 중요한 결정은 `docs/design-docs/`에 기록한다
5. **품질 게이트를 건너뛰지 않는다** — 테스트 실패 상태로 완료 처리하지 않는다

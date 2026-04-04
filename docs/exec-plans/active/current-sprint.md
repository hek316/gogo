# Current Sprint

> 이 파일은 **지금 이번 작업에서 뭘 하고 있는지** 기록합니다.
> 작업 시작 시 업데이트 해주세요. AI는 이 파일을 먼저 읽고 현재 컨텍스트를 파악합니다.

**마지막 업데이트**: 2026-04-04

---

## 지금 하고 있는 작업

**AI 하네스 문서 구조 구축**

GoGo 코드베이스에서 AI(Claude)가 효과적으로 작업할 수 있도록 문서 하네스를 구성 중.

### 완료된 것

- [x] `AGENTS.md` — AI 워크플로우 + 품질 게이트
- [x] `ARCHITECTURE.md` — 시스템 구조 설명
- [x] `docs/DESIGN.md` — 설계 의사결정 ADR 7개
- [x] `docs/PRODUCT_SENSE.md` — 제품 철학 + Anti-goals
- [x] `docs/design-docs/core-beliefs.md` — 핵심 신념 6개
- [x] `docs/design-docs/index.md` — ADR 인덱스
- [x] `docs/SECURITY.md` — 보안 현황 + 위험
- [x] `docs/RELIABILITY.md` — 운영 안정성 현황
- [x] `docs/exec-plans/tech-debt-tracker.md` — 기술 부채 18개
- [x] `docs/QUALITY_SCORE.md` — 테스트 커버리지 분석
- [x] `docs/FRONTEND.md` — 프론트엔드 패턴
- [x] `docs/PLANS.md` — 로드맵
- [x] `docs/generated/db-schema.md` — DB 스키마
- [x] `docs/references/design-system-reference-llms.txt` — 디자인 시스템 참조
- [x] `docs/exec-plans/active/current-sprint.md` — 이 파일

### 남은 것

없음. 하네스 구축 완료.

---

## 다음 작업 후보

`docs/PLANS.md` 2절 "당장 해야 할 것" 기준:

1. **UseCase 테스트 추가** (`KakaoLoginUseCase`, `GoogleLoginUseCase`, `RefreshTokenUseCase`)
2. **SHOW_SQL 기본값 `false`로 변경** — `backend/src/main/resources/application.yml` 10번째 줄
3. **SEC-001 OAuth 콜백 토큰 노출 해결** — 백엔드 Set-Cookie 전환

---

## 작업 방식 안내

새 작업 시작할 때 이 파일의 "지금 하고 있는 작업" 섹션을 교체하고 체크리스트를 업데이트하세요.

```
예시:
## 지금 하고 있는 작업

**KakaoLoginUseCase 테스트 작성**
...
```

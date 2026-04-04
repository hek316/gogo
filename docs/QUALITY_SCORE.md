# QUALITY_SCORE.md — GoGo 품질 현황

이 문서는 GoGo의 현재 코드 품질 수준을 측정 가능한 방식으로 기록합니다.
주요 변경이 있을 때마다 업데이트하고, 목표 대비 현황을 추적하세요.

**마지막 업데이트**: 2026-04-04

---

## 1. 종합 품질 점수

| 영역 | 현재 | 목표 | 상태 |
|------|------|------|------|
| 백엔드 테스트 커버리지 (핵심 레이어) | ~60% | 80% | 🟡 |
| 백엔드 빌드 | 통과 | 통과 | ✅ |
| 프론트엔드 빌드 (타입 오류) | 통과 | 통과 | ✅ |
| 알려진 HIGH 보안 이슈 | 1건 | 0건 | 🔴 |
| 테스트 없는 핵심 컴포넌트 | 7개 | 0개 | 🟡 |
| 프로덕션 SQL 로그 노출 | 있음 | 없음 | 🟡 |

---

## 2. 테스트 커버리지 현황

### 2-1. 백엔드 테스트 파일 목록

| 파일 | 테스트 수 | 커버 내용 |
|------|----------|----------|
| `PlaceQueryServiceTest` | 2 | 전체 조회, 카테고리 필터 |
| `PlaceCommandServiceTest` | 4 | 장소 추가, 방문 처리, 삭제 |
| `GroupServiceTest` | 6 | 그룹 생성, 참여, 장소 공유 등 |
| `MeetingServiceTest` | 7 | 생성, 투표, 확정, 예외 케이스 |
| `ReviewServiceTest` | 3 | 후기 작성, 조회 |
| `PlacesControllerTest` | 2 | 장소 목록/상세 API |
| `PlaceTest` (Entity) | 4 | 생성 규칙, 상태 전환 |
| `GroupTest` (Entity) | 4 | 생성 규칙, 멤버 추가 |
| **합계** | **32** | |

### 2-2. 테스트가 없는 컴포넌트

| 컴포넌트 | 종류 | 위험도 | 비고 |
|----------|------|--------|------|
| `AuthService` | Service | 🟡 중간 | OAuth URL 생성 로직 |
| `PlaceLikeService` | Service | 🟡 중간 | 좋아요 토글 |
| `KakaoLoginUseCase` | UseCase | 🔴 높음 | 로그인 + 토큰 발급 핵심 로직 |
| `GoogleLoginUseCase` | UseCase | 🔴 높음 | 로그인 + 토큰 발급 핵심 로직 |
| `RefreshTokenUseCase` | UseCase | 🔴 높음 | 리프레시 토큰 로테이션 |
| `SearchPlacesUseCase` | UseCase | 🟢 낮음 | Naver API 래핑 |
| `FetchPlacePreviewUseCase` | UseCase | 🟢 낮음 | Jsoup 스크래핑 |

> `KakaoLoginUseCase`, `GoogleLoginUseCase`, `RefreshTokenUseCase`는 인증의 핵심 경로이므로 테스트 작성이 가장 시급합니다.

### 2-3. 커버리지 측정 도구

현재 JaCoCo 미설정. 커버리지 수치는 수동 추산입니다.

**JaCoCo 설정 방법 (`build.gradle`에 추가):**
```gradle
plugins {
    id 'jacoco'
}

jacocoTestReport {
    dependsOn test
    reports {
        xml.required = true
        html.required = true
    }
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = 'CLASS'
            includes = ['com.gogo.domain.service.*', 'com.gogo.domain.usecase.*']
            limit {
                minimum = 0.80  // 핵심 레이어 80% 강제
            }
        }
    }
}
```

설정 후 실행:
```bash
./gradlew test jacocoTestReport
# 리포트: build/reports/jacoco/test/html/index.html
```

---

## 3. 테스트 품질 기준

### 현재 테스트 스타일

GoGo 테스트는 **Mockito + AssertJ** 기반 단위 테스트입니다.

```java
// 좋은 테스트의 예 (MeetingServiceTest)
@Test
void 후보_장소_없이_생성시_예외() {
    assertThatThrownBy(() -> meetingService.createMeeting(
            new CreateMeetingRequest(1L, "약속", List.of())))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("후보 장소");
}
```

**테스트 작성 규칙:**
- 테스트 메서드명은 한국어로 `행위_결과` 형식 (예: `투표_성공`, `후보_장소_없이_생성시_예외`)
- `given()/when/then` 구조 유지
- Service 테스트: `AuthContext`는 반드시 Mock으로 처리
- 각 테스트는 독립적이어야 함 (다른 테스트 결과에 의존 금지)

**테스트에서 하지 말아야 할 것:**
- 실제 DB 연결 (H2 in-memory 사용)
- 외부 API 실제 호출 (Naver, Kakao, Google 전부 Mock)
- `Thread.sleep()` 사용

---

## 4. 빌드 상태

### 백엔드

```bash
cd backend && ./gradlew test
```

| 체크 항목 | 현재 상태 |
|----------|----------|
| 전체 테스트 통과 | ✅ |
| 컴파일 경고 | 확인 필요 |
| 불필요한 import | 일부 존재 가능 |

### 프론트엔드

```bash
cd frontend && pnpm build
```

| 체크 항목 | 현재 상태 |
|----------|----------|
| TypeScript 타입 오류 | ✅ 없음 |
| 프로덕션 빌드 성공 | ✅ |
| 프론트엔드 테스트 | ❌ 없음 |

> 프론트엔드 테스트가 전혀 없습니다. 현재는 `pnpm build`의 타입 체크가 유일한 자동화된 검증입니다.

---

## 5. 코드 품질 관찰

### 잘 되어 있는 것

- **Entity 설계**: 정적 팩토리 메서드 + 도메인 로직 보유 (단순 getter/setter 아님)
- **테스트 독립성**: 모든 Service 테스트가 H2 + Mock 기반, 외부 의존 없음
- **에러 처리 일관성**: `GlobalExceptionHandler`로 에러 응답 형식 통일
- **요청 추적**: MDC 기반 `requestId`로 로그 추적 가능
- **보안**: HttpOnly Cookie, 리프레시 토큰 rotation + 재사용 감지 구현

### 개선이 필요한 것

- **`SHOW_SQL` 기본값 `true`**: 프로덕션 로그 과다, 성능 영향 (→ `DX-001`)
- **`FetchType.EAGER` 2곳**: `Group.members`, `Meeting.candidatePlaceIds` (→ `ARCH-003`, `ARCH-004`)
- **UseCase 테스트 부재**: 인증 관련 UseCase 3개 커버리지 없음 (→ 이 문서 §2-2)
- **프론트엔드 테스트 없음**: 컴포넌트 동작 검증 불가

---

## 6. 품질 개선 로드맵

### 단기 (다음 스프린트)

- [ ] `KakaoLoginUseCase` 단위 테스트 작성
- [ ] `GoogleLoginUseCase` 단위 테스트 작성
- [ ] `RefreshTokenUseCase` 단위 테스트 작성
- [ ] `SHOW_SQL` 기본값 `false`로 변경 (`DX-001`)
- [ ] JaCoCo 설정 추가, 80% 강제

### 중기

- [ ] `PlaceLikeService`, `AuthService` 테스트 추가
- [ ] `SearchPlacesUseCase`, `FetchPlacePreviewUseCase` 테스트 추가
- [ ] 프론트엔드 핵심 컴포넌트 테스트 (Jest + Testing Library)
- [ ] `Group.members` EAGER → LAZY + fetch join으로 전환

### 장기

- [ ] E2E 테스트 (Playwright) — 핵심 사용자 여정 자동화
- [ ] CI/CD에서 커버리지 게이트 적용 (80% 미달 시 PR 차단)

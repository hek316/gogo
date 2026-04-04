# RELIABILITY.md — GoGo 안정성 가이드

이 문서는 GoGo의 장애 대응 방법, 현재 모니터링 수준, 알려진 단일 장애 지점(SPOF), 그리고 서비스 회복 절차를 정의합니다.

---

## 1. 현재 인프라 구성

```
사용자
  │
  ├─→ Vercel (Frontend)          ─ 관리형 서비스, 자동 HA
  │
  └─→ EC2 단일 인스턴스
        ├── Docker: backend 컨테이너   (포트 80 → 8080)
        └── Docker: postgres 컨테이너  (볼륨: gogo-postgres-data)
              └── Volume이 EC2 로컬 디스크에 존재
```

**현재 아키텍처의 솔직한 한계:**
- EC2 단일 인스턴스 → 인스턴스 종료 시 전체 서비스 중단
- PostgreSQL 데이터가 EC2 로컬 볼륨 → 인스턴스 손실 시 데이터 손실 위험
- 로드 밸런서 없음 → 스케일 아웃 불가

이것은 현재 단계에서의 의도적인 단순화입니다. 사용자 수가 늘어나면 RDS + ALB + ASG로 전환해야 합니다.

---

## 2. 현재 내장된 안정성 장치

### 2-1. 헬스체크 (`docker-compose.yml`)

```yaml
healthcheck:
  test: ["CMD", "sh", "-c", "wget -qO- http://127.0.0.1:8080/api/health || exit 1"]
  interval: 30s
  timeout: 5s
  retries: 3
  start_period: 60s   # Spring Boot 기동 여유 시간
```

`GET /api/health` 엔드포인트가 응답하지 않으면 Docker가 컨테이너를 unhealthy로 표시합니다.

### 2-2. 자동 재시작

```yaml
restart: unless-stopped
```

컨테이너가 crash하면 자동으로 재시작됩니다. 단, 명시적으로 `docker stop`한 경우는 재시작하지 않습니다.

### 2-3. 요청 로깅 (`RequestLoggingFilter`)

모든 HTTP 요청에 고유 `requestId`(8자 UUID)가 부여되고 MDC에 등록됩니다.

```
▶ [a3f1b2c4] GET /api/places from 1.2.3.4
◀ [a3f1b2c4] /api/places → 200 (45ms)
```

장애 발생 시 `requestId`로 특정 요청의 전체 흐름을 추적할 수 있습니다.

### 2-4. 로그 로테이션

```yaml
logging:
  driver: json-file
  options:
    max-size: "10m"
    max-file: "3"   # 최대 30MB 유지
```

디스크 풀 방지를 위해 로그 파일이 자동 순환됩니다.

### 2-5. JVM 메모리 설정

```
-XX:+UseContainerSupport       # 컨테이너 메모리 한도 인식
-XX:MaxRAMPercentage=75.0       # 컨테이너 메모리의 75%만 힙으로 사용
```

EC2 메모리 부족으로 OOMKiller가 동작하는 것을 방지합니다.

### 2-6. 외부 API 타임아웃

| 서비스 | 타임아웃 | 실패 시 동작 |
|--------|---------|------------|
| Jsoup (URL 프리뷰) | 5,000ms | null 반환, 예외 삼킴 |
| Vercel 프록시 → EC2 | 8,000ms | 504 반환 (Vercel 10s 한도 이내) |
| Naver Local API | Spring 기본값 | 예외 전파 |

---

## 3. 현재 모니터링 현황

| 항목 | 상태 | 비고 |
|------|------|------|
| 헬스체크 엔드포인트 | ✅ 있음 | `GET /api/health` |
| 요청 로그 | ✅ 있음 | `RequestLoggingFilter` |
| 에러 로그 | ✅ 있음 | `GlobalExceptionHandler` |
| 메트릭 수집 | ❌ 없음 | Prometheus/Actuator 미구성 |
| 알림(Alerting) | ❌ 없음 | — |
| 업타임 모니터링 | ❌ 없음 | 외부 핑 서비스 없음 |
| DB 백업 | ❌ 없음 | 로컬 볼륨만 존재 |
| 에러 트래킹 | ❌ 없음 | Sentry 등 미구성 |

> 현재는 로그를 직접 확인하는 방식에 의존합니다. 서비스가 중단돼도 스스로 감지하지 못합니다.

---

## 4. 장애 대응 절차

### 4-1. 서비스가 응답하지 않을 때

```bash
# 1. 컨테이너 상태 확인
docker ps
docker inspect backend --format='{{.State.Health.Status}}'

# 2. 최근 로그 확인
docker logs backend --tail 100

# 3. 헬스체크 직접 확인
curl http://localhost/api/health

# 4. 컨테이너 재시작
docker restart backend

# 5. 재시작 후에도 실패하면 이미지 재배포
docker pull 207567788967.dkr.ecr.ap-northeast-2.amazonaws.com/gogo-server:latest
docker-compose up -d
```

### 4-2. DB 연결 오류 (`HikariPool`, `FATAL: connection refused`)

```bash
# PostgreSQL 컨테이너 상태 확인
docker ps | grep postgres
docker logs gogo-postgres --tail 50

# 재시작
docker restart gogo-postgres

# 재시작 후 백엔드도 재시작 (커넥션 풀 초기화)
docker restart backend
```

### 4-3. 배포 후 서비스 불안정

```bash
# 직전 버전 이미지 태그 확인 후 롤백
docker tag 207567788967.dkr.ecr.ap-northeast-2.amazonaws.com/gogo-server:previous \
           207567788967.dkr.ecr.ap-northeast-2.amazonaws.com/gogo-server:latest
docker-compose up -d
```

> 현재 이전 버전 태그 관리 정책이 없습니다. ECR에서 직전 이미지 다이제스트로 롤백해야 합니다.

### 4-4. 프론트엔드 장애

Vercel 관리형 서비스이므로 직접 개입할 수 없습니다.
- [Vercel Status](https://www.vercel-status.com/) 확인
- 환경 변수(`NEXT_PUBLIC_API_URL`) 오설정 확인
- Vercel 대시보드에서 직전 배포로 롤백

### 4-5. 특정 요청에서 에러가 반복될 때

`requestId`로 로그를 추적합니다:

```bash
# requestId로 특정 요청 전체 흐름 확인
docker logs backend 2>&1 | grep "a3f1b2c4"
```

---

## 5. DDL 전략 — 데이터 안전

```yaml
# 프로덕션 (docker-compose.yml)
DDL_AUTO: validate     # 스키마 변경 절대 자동 적용 안 함

# 로컬 개발 (application.yml 기본값)
DDL_AUTO: update       # 자동으로 컬럼 추가 허용
```

**프로덕션에서 스키마 변경이 필요하면:**
1. 마이그레이션 SQL을 직접 작성해 DB에 적용
2. 백엔드 배포
3. `validate` 모드가 새 스키마와 맞는지 확인

> Flyway/Liquibase 미도입 상태입니다. 스키마 변경 이력이 관리되지 않습니다. 사용자 증가 전에 도입을 권장합니다.

---

## 6. 알려진 단일 장애 지점 (SPOF)

| 지점 | 현재 위험도 | 해결 방향 |
|------|------------|----------|
| EC2 단일 인스턴스 | 🔴 높음 | ALB + Auto Scaling Group |
| PostgreSQL 로컬 볼륨 | 🔴 높음 | AWS RDS (Multi-AZ) |
| Naver Local API | 🟡 중간 | 검색 실패 시 graceful fallback |
| ECR 이미지 단일 태그 | 🟡 중간 | 태그 버전 관리 정책 수립 |
| 스키마 마이그레이션 수동 | 🟡 중간 | Flyway 도입 |

---

## 7. 다음에 만들어야 할 것들

서비스가 커질 때 우선순위 순으로:

1. **외부 업타임 모니터** — UptimeRobot 같은 무료 서비스라도 `/api/health`를 1분마다 핑해서 다운 시 알림
2. **DB 백업** — 일 1회 `pg_dump` → S3 업로드 크론잡
3. **Sentry 에러 트래킹** — `GlobalExceptionHandler`에 Sentry SDK 연결, 500 에러 즉시 알림
4. **Spring Actuator + 메트릭** — `/actuator/health`, `/actuator/metrics` 활성화
5. **Flyway 마이그레이션** — 스키마 변경 이력 관리
6. **RDS 전환** — PostgreSQL을 EC2 로컬에서 RDS Multi-AZ로

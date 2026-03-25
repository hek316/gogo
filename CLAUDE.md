# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

GoGo is a place-sharing and meetup-planning app (Korean: "가고 싶은 장소 기록"). Users save places, share them in groups, and vote on meeting dates. Monorepo with two independent apps:

- **Backend** (`/backend`): Spring Boot 3.5, Java 21, JPA, PostgreSQL
- **Frontend** (`/frontend`): Next.js 16 (App Router), React 19, TypeScript, Tailwind CSS 4, pnpm

## Frontend Design System

**IMPORTANT**: 프론트엔드 코드 작성 시 반드시 `/frontend/DESIGN_SYSTEM.md`를 참조하세요.

- **테마**: 화이트 배경 + 라벤더 파스텔 포인트 + 다크모드 지원
- **컬러**: 하드코딩 hex/rgba 금지. CSS 변수 기반 토큰만 사용 (`bg-bg`, `text-primary`, `bg-surface` 등)
- **다크모드**: `class` 전략 (`.dark` on `<html>`), `ThemeContext`로 토글
- **상세 규칙**: `/frontend/DESIGN_SYSTEM.md` 참조

## Build & Run Commands

### Backend
```bash
cd backend
./gradlew build          # build + test
./gradlew test           # all tests (H2 in-memory, no DB needed)
./gradlew test --tests "com.gogo.application.service.PlaceQueryServiceTest"  # single test class
./gradlew bootRun        # run locally (requires .env with DB/JWT/OAuth vars)
```

### Frontend
```bash
cd frontend
pnpm install
pnpm dev                 # dev server on localhost:3000
pnpm build               # production build
```

## Architecture

### Backend Layered Architecture (Clean Architecture style)

```
presentation/api/     → Controllers (REST endpoints)
application/
  service/            → Business logic services (PlaceQueryService, PlaceCommandService, GroupService, etc.)
  usecase/            → Standalone use cases (SearchPlacesUseCase, FetchPlacePreviewUseCase, auth/*)
  dto/                → Request/Response DTOs
  port/               → Interfaces for infrastructure (AuthContext)
  auth/               → JWT and OAuth clients (JwtService, KakaoOAuthClient, GoogleOAuthClient)
domain/
  entity/             → JPA entities with domain logic (Place, User, Group, Review, PlaceLike, etc.)
  repository/         → Repository interfaces
infrastructure/
  config/             → SecurityConfig, CorsConfig, DataInitializer
  security/           → JwtAuthenticationFilter, SecurityContextHelper, AuthenticatedUser
  persistence/        → JPA repository implementations, JPA entities for Group/Meeting (still mapped)
  external/           → NaverLocalApiClient, external API DTOs
  filter/             → RequestLoggingFilter
```

Key patterns:
- Domain entities (Place, User, Review, PlaceLike) have JPA annotations directly — no separate JPA entity classes
- Group and Meeting still use separate JpaEntity classes with mappers
- `AuthContext` interface in `application/port/` is implemented by `SecurityContextHelper` — use cases inject `AuthContext`, not the implementation
- User identity comes from SecurityContext (JWT), not request bodies — DTOs like `AddPlaceRequest` don't have `createdBy` fields
- Tests use H2 (PostgreSQL mode) with `create-drop` — no external DB needed. Mock `AuthContext` directly in service tests

### Frontend API Proxy Pattern

Client components call `/api/proxy/*` which routes through Next.js server to the backend. Server components call the backend directly via `NEXT_PUBLIC_API_URL`. This is configured in `src/lib/api/config.ts` — the `API_BASE` export handles both cases automatically.

Auth tokens are stored as HttpOnly cookies (set via `/auth/callback` route). The proxy reads the `access-token` cookie and forwards it as a Bearer header.

### Auth Flow (Kakao/Google OAuth)

Browser → Backend `/api/auth/{provider}/authorize` → OAuth provider → Backend `/api/auth/{provider}/callback` → redirects to Frontend `/auth/callback?at=...&rt=...` → sets HttpOnly cookies → client ready.

JWT: HS256, 15min access token, 7-day refresh token with rotation.

## Environment Variables

Backend requires (see `.env.example`): `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`, `KAKAO_REDIRECT_URI`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GOOGLE_REDIRECT_URI`, `FRONTEND_URL`

Frontend requires: `NEXT_PUBLIC_API_URL` (backend URL, defaults to `http://localhost:8080`)

## Deployment

- Frontend: Vercel
- Backend: Docker on EC2 (image pushed to ECR, deployed via `docker-compose.yml`)

## API Endpoints

Public (no auth): `/api/auth/**`, `GET /api/places/**`, `/api/health`, Swagger (`/swagger-ui/**`, `/v3/api-docs/**`)

All other endpoints require JWT Bearer token.

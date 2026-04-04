# FRONTEND.md — GoGo 프론트엔드 가이드

이 문서는 GoGo 프론트엔드의 패턴, 규칙, 주의사항을 정리합니다.
프론트엔드 작업 전 반드시 읽고, 세부 디자인 토큰은 `frontend/DESIGN_SYSTEM.md`를 함께 참조하세요.

---

## 1. 디자인 시스템 핵심 규칙

### 컬러 토큰 — 절대 hex/rgba 하드코딩 금지

GoGo는 Tailwind CSS v4의 `@theme {}` 블록으로 CSS 변수 기반 컬러 토큰을 정의합니다 (`globals.css`).

**사용 가능한 주요 토큰:**

| 토큰 | 용도 |
|------|------|
| `bg-bg` | 페이지 배경 |
| `bg-bg-secondary` | 보조 배경 |
| `bg-surface` | 카드, 입력 필드 배경 |
| `bg-surface-hover` | hover 상태 배경 |
| `bg-primary` | 주요 버튼, 강조색 (라벤더) |
| `bg-primary-subtle` | 연한 강조 배경 |
| `text-text-main` | 기본 텍스트 |
| `text-text-secondary` | 보조 텍스트 |
| `text-text-muted` | 흐린 텍스트, 플레이스홀더 |
| `text-primary` | 강조 텍스트 (라벤더) |
| `text-text-on-primary` | primary 배경 위의 텍스트 (흰색) |
| `border-border` | 기본 테두리 |
| `border-border-hover` | hover 테두리 |
| `bg-accent-bg` / `text-accent-text` | 좋아요, 핑크 계열 강조 |
| `bg-danger` / `text-danger` | 삭제, 에러 상태 |
| `bg-success` / `text-success` | 완료, 성공 상태 |

```tsx
// ✅ 올바른 사용
<div className="bg-surface text-text-main border border-border rounded-2xl">

// ❌ 절대 금지
<div style={{ background: '#F0EDF8', color: '#2D264B' }}>
<div className="bg-[#F0EDF8]">
```

**예외 — 카테고리 그라디언트**: `CATEGORY_GRADIENT`는 현재 hex 값이 포함된 Tailwind arbitrary value를 사용합니다. 이는 카테고리별 고유 색상이 토큰화되기 어렵기 때문입니다. 기존 패턴을 유지하되, 새 카테고리 추가 시 `lib/constants/categories.ts`에 gradient 클래스를 추가하세요.

### 다크모드

`<html>` 태그에 `.dark` 클래스 토글로 동작합니다. `ThemeContext`가 전담합니다.

- 컴포넌트에서 직접 `.dark` 조건 분기 금지
- 다크모드는 CSS 변수가 자동으로 처리하므로, 토큰을 올바르게 쓰면 별도 다크모드 스타일 불필요
- FOUC(깜빡임) 방지를 위해 `layout.tsx`의 인라인 스크립트가 HTML 파싱 전에 `.dark` 클래스를 적용함

### 폰트

Pretendard Variable. body에 전역 적용됨. 별도 설정 불필요.

### 레이아웃 기본 제약

```tsx
// 모든 페이지의 콘텐츠 최대 너비
<div className="max-w-2xl mx-auto px-4">
  ...
</div>

// BottomNav 높이만큼 하단 패딩 (layout.tsx에서 main에 적용됨)
<main className="pb-16">
```

---

## 2. 컴포넌트 작성 규칙

### Server Component vs Client Component

Next.js App Router의 기본은 Server Component입니다. 아래 경우에만 `'use client'`를 추가하세요.

| `'use client'` 필요 | Server Component 유지 |
|--------------------|----------------------|
| `useState`, `useEffect` 사용 | 데이터 fetch만 하는 경우 |
| 이벤트 핸들러 (`onClick` 등) | 정적 마크업 렌더링 |
| 브라우저 API (`localStorage` 등) | SEO가 중요한 페이지 |
| Context 소비 (`useAuth`, `useTheme`) | 레이아웃 컴포넌트 |

```tsx
// ✅ Server Component (기본)
// app/places/[id]/page.tsx
export default async function PlacePage({ params }: { params: { id: string } }) {
  const place = await getPlace(Number(params.id));  // 서버에서 직접 fetch
  return <PlaceDetail place={place} />;
}

// ✅ Client Component (상호작용 필요)
// components/LikeButton.tsx
'use client';
export default function LikeButton({ ... }) {
  const [liked, setLiked] = useState(initialLiked);
  ...
}
```

### 무거운 컴포넌트는 dynamic import

```tsx
// ✅ 폼처럼 큰 컴포넌트는 dynamic으로 lazy load
const AddPlaceForm = dynamic(() => import('@/components/AddPlaceForm'), {
  ssr: false,  // 클라이언트 전용 컴포넌트
});
```

### 컴포넌트 파일 위치

```
app/                  ← 페이지 단위 컴포넌트 (라우트에 귀속)
components/           ← 여러 페이지에서 재사용되는 컴포넌트
lib/                  ← 비즈니스 로직, API 함수, Context
```

---

## 3. API 호출 패턴

### 기본 원칙

`lib/api/config.ts`의 `apiFetch`를 항상 사용하세요. 직접 `fetch`를 쓰지 마세요.

```typescript
// ✅ 올바른 방법
import { apiFetch } from '@/lib/api/config';
const places = await apiFetch<Place[]>('/api/places');

// ❌ 직접 fetch 금지
const res = await fetch('http://localhost:8080/api/places');
```

### Server vs Client에서의 호출 경로

```typescript
// lib/api/config.ts
export const API_BASE =
  typeof window === 'undefined'
    ? process.env.NEXT_PUBLIC_API_URL   // 서버: EC2 직접
    : '/api/proxy';                      // 브라우저: Next.js 프록시 경유
```

`apiFetch`가 이를 자동으로 처리하므로, 컴포넌트에서 호출 경로를 신경 쓰지 않아도 됩니다.

### Server Component에서 데이터 fetch

```tsx
// app/groups/[id]/page.tsx
export default async function GroupPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  const group = await getGroup(Number(id));  // 서버에서 직접 실행
  return <GroupDetail group={group} />;
}
```

### Client Component에서 데이터 fetch

```tsx
'use client';
export default function PlaceList() {
  const [places, setPlaces] = useState<Place[]>([]);

  useEffect(() => {
    getPlaces().then(setPlaces);
  }, []);
}
```

---

## 4. 인증 패턴

### 로그인 상태 확인

```tsx
'use client';
import { useAuth } from '@/lib/auth/AuthContext';

export default function MyComponent() {
  const { user, loading } = useAuth();

  if (loading) return <Spinner />;
  if (!user) return <LoginPrompt />;

  return <div>안녕, {user.nickname}!</div>;
}
```

### 로그인 필수 페이지

```tsx
'use client';
import { useRequireAuth } from '@/lib/auth/useRequireAuth';

export default function ProfilePage() {
  const { user, loading } = useRequireAuth();
  // 비로그인 시 자동으로 /auth/login 으로 리다이렉트

  if (loading) return <Spinner />;
  return <div>{user!.nickname}의 프로필</div>;
}
```

### 로그인/로그아웃

```tsx
const { loginWithKakao, loginWithGoogle, logout } = useAuth();

// OAuth 로그인 (백엔드로 리다이렉트)
<button onClick={loginWithKakao}>카카오로 로그인</button>

// 로그아웃
<button onClick={() => logout()}>로그아웃</button>
```

---

## 5. 자주 쓰는 패턴

### 낙관적 업데이트 (Optimistic Update)

사용자 경험을 위해 API 응답을 기다리지 않고 UI를 먼저 업데이트합니다. `LikeButton`이 참고 구현입니다.

```tsx
const handleLike = async () => {
  const nextLiked = !liked;

  // 1. UI 먼저 업데이트
  setLiked(nextLiked);
  setCount(c => nextLiked ? c + 1 : c - 1);

  try {
    // 2. API 호출
    await likePlace(placeId);
  } catch {
    // 3. 실패 시 롤백
    setLiked(!nextLiked);
    setCount(c => nextLiked ? c - 1 : c + 1);
  }
};
```

### 드롭다운 메뉴 외부 클릭 닫기

```tsx
const menuRef = useRef<HTMLDivElement>(null);

useEffect(() => {
  if (!menuOpen) return;
  const handler = (e: MouseEvent) => {
    if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
      setMenuOpen(false);
    }
  };
  document.addEventListener('mousedown', handler);
  return () => document.removeEventListener('mousedown', handler);
}, [menuOpen]);
```

### 테마 사용

```tsx
import { useTheme } from '@/lib/theme/ThemeContext';

const { resolved, toggle } = useTheme();
// resolved: 'light' | 'dark' (현재 실제 테마)
// toggle(): 라이트 ↔ 다크 전환
```

---

## 6. 접근성 (a11y) 기준

GoGo는 기본적인 접근성을 지킵니다. 새 컴포넌트 작성 시 아래를 확인하세요.

```tsx
// ✅ 아이콘 버튼에 aria-label 필수
<button aria-label="좋아요 취소">
  <Heart aria-hidden="true" />
</button>

// ✅ 토글 버튼에 aria-pressed
<button aria-pressed={liked}>좋아요</button>

// ✅ 현재 페이지 표시
<Link aria-current={active ? 'page' : undefined}>홈</Link>

// ✅ 장식용 아이콘에 aria-hidden
<MapPin size={18} aria-hidden="true" />

// ✅ 로딩 상태에 aria-label
<div role="status" aria-label="로딩 중">...</div>
```

---

## 7. 금지 사항

| ❌ 하지 말 것 | ✅ 대신 |
|-------------|--------|
| `localStorage`에 JWT 저장 | HttpOnly Cookie (자동 처리) |
| hex/rgba 컬러 직접 사용 | CSS 변수 토큰 사용 |
| `fetch()` 직접 호출 | `apiFetch()` 사용 |
| Client Component에서 백엔드 직접 호출 | `/api/proxy` 경유 자동 처리됨 |
| `window.location.href`로 내부 이동 | `next/navigation`의 `useRouter` |
| 다크모드 `.dark:` prefix 남발 | CSS 변수 토큰이 자동으로 처리 |
| Context 없이 `AuthContext` 직접 import | `useAuth()` 훅 사용 |

---

## 8. 카테고리 추가 방법

새 장소 카테고리가 생기면 `lib/constants/categories.ts` 한 곳만 수정합니다.

```typescript
// lib/constants/categories.ts
export const CATEGORY_LABEL: Record<string, string> = {
  CAFE: '카페',
  RESTAURANT: '식당',
  BAR: '바/펍',
  ACTIVITY: '액티비티',
  ETC: '기타',
  SHOPPING: '쇼핑',  // 새 카테고리 추가
};

export const CATEGORY_GRADIENT: Record<string, string> = {
  // ...
  SHOPPING: 'from-[#E91E63] to-[#F48FB1]',  // gradient 추가
};
```

백엔드 `PlaceStatus` enum도 함께 추가해야 합니다.

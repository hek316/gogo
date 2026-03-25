# GoGo Design System — AI Rules

이 파일은 AI가 프론트엔드 코드를 작성할 때 반드시 따라야 하는 디자인 시스템 규칙입니다.

## Color Tokens (필수 사용)

하드코딩된 hex/rgba 값 사용 금지. 반드시 아래 토큰만 사용하세요.

### 배경 (Background)
| Token | 용도 | Light | Dark |
|-------|------|-------|------|
| `bg-bg` | 페이지/모달 배경 | `#FFFFFF` | `#1A1726` |
| `bg-bg-secondary` | 카드/섹션 배경 | `#F6F4FB` | `#221F30` |
| `bg-surface` | 입력필드/탭 비활성 배경 | `#F0EDF8` | `#2A2640` |
| `bg-surface-hover` | surface hover 상태 | `#E8E4F3` | `#353050` |

### 텍스트 (Text)
| Token | 용도 |
|-------|------|
| `text-text-main` | 제목, 본문 |
| `text-text-secondary` | 보조 텍스트 |
| `text-text-muted` | placeholder, 부가 정보 |
| `text-text-on-primary` | primary 배경 위 텍스트 (항상 white/dark) |

### 포인트 컬러 (Primary = Lavender)
| Token | 용도 |
|-------|------|
| `bg-primary` / `text-primary` | CTA 버튼, 활성 탭, 링크, 아이콘 강조 |
| `bg-primary-hover` | primary hover |
| `bg-primary-subtle` | 뱃지/태그 배경, 선택 상태 배경 |

### 악센트 (Accent = Pink)
| Token | 용도 |
|-------|------|
| `text-accent` / `bg-accent` | 좋아요 하트, Beta 뱃지 |
| `bg-accent-bg` | 좋아요 활성 배경 |
| `text-accent-text` | 좋아요 활성 텍스트 |

### 위험/삭제 (Danger)
| Token | 용도 |
|-------|------|
| `text-danger` | 삭제 버튼, 에러 메시지 |
| `bg-danger-bg` | 삭제 hover 배경 |

### 테두리 (Border)
| Token | 용도 |
|-------|------|
| `border-border` | 기본 테두리 |
| `border-border-hover` | hover 테두리 |

## 버튼 패턴

```
// Primary CTA (가장 중요한 액션)
className="bg-primary hover:bg-primary-hover text-text-on-primary"

// Secondary CTA (폼 제출, 확인 등)
className="bg-text-main hover:bg-text-secondary text-text-on-primary"

// Ghost / Cancel
className="bg-surface text-text-muted"

// Danger
className="text-danger border border-danger/30 hover:bg-danger-bg"

// Active tab / filter
className="bg-text-main text-text-on-primary shadow-sm"

// Inactive tab / filter
className="bg-bg text-text-muted border border-border hover:border-primary"
```

## 입력 필드 패턴

```
className="w-full border border-border rounded-[12px] px-5 py-3 text-sm
  focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary bg-bg"
```

## 카드 패턴

```
// 기본 카드
className="bg-bg-secondary rounded-[20px] p-6 border border-border shadow-sm"

// 표면 카드 (폼 내부 등)
className="bg-surface rounded-[16px] p-4 border border-border"
```

## 모달 패턴

```
// 오버레이
className="fixed inset-0 bg-black/40 flex items-end sm:items-center justify-center z-50"

// 모달 본체
className="bg-bg w-full sm:max-w-md rounded-t-[28px] sm:rounded-[28px] p-6 shadow-lg border-t border-border"

// 닫기 버튼
className="w-8 h-8 flex items-center justify-center rounded-full bg-surface text-primary hover:bg-surface-hover"
```

## 다크모드 규칙

1. **자동 지원**: CSS 변수가 `.dark` 클래스에서 자동으로 변환됨
2. **하드코딩 금지**: `#FFFFFF`, `bg-white` 대신 `bg-bg` 사용
3. **투명도 활용**: 다크모드 호환을 위해 `border-primary/30` 같은 투명도 패턴 활용
4. **특수 케이스**: 카카오 버튼(`#FEE500`), Google 버튼(`#FFFFFF`) 등 브랜드 컬러는 inline style로 유지
5. **다크모드 전용 스타일**: `dark:` prefix로 추가 가능 (예: `dark:bg-amber-900/30`)

## Legacy 토큰 (호환용, 신규 코드에서 사용 금지)

아래 토큰은 하위 호환을 위해 유지되지만, 신규 코드에서는 사용하지 마세요:
- `mint` → `primary` 사용
- `green` → `text-main` 사용
- `green-mid` → `text-secondary` 사용
- `card-bg` → `surface` 사용

## 카테고리 그라데이션

카테고리 색상은 `lib/constants/categories.ts`의 `CATEGORY_GRADIENT`를 사용합니다.
이 값들은 이미지 대체용으로만 사용되며, 다크모드에서도 동일합니다.

## 타이포그래피

- Font: Pretendard Variable
- letter-spacing: `-0.02em` (기본)
- 제목: `font-bold tracking-tight` 또는 `tracking-[-0.03em]`
- Round corners: `rounded-[12px]` (입력), `rounded-[16px]` (버튼), `rounded-[20px]` (카드), `rounded-[28px]` (모달)

## 스피너

```
className="w-8 h-8 border-[3px] border-surface border-t-primary rounded-full animate-spin"
```

## 금지 사항

1. `bg-white` 사용 금지 → `bg-bg` 사용
2. 하드코딩 색상 (`#F8F7FB`, `#2D264B`, `#9D8DC2` 등) 금지
3. `rgba(45,38,75,...)` 패턴 금지 → 토큰 사용
4. `shadow-[0_4px_20px_rgba(...)]` 같은 하드코딩 shadow 금지 → `shadow-sm`, `shadow-md`, `shadow-lg` 사용
5. 새 컴포넌트에서 legacy 토큰 (`mint`, `green`, `green-mid`) 사용 금지

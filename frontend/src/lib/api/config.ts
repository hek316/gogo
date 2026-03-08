// 서버 컴포넌트(SSR): NEXT_PUBLIC_API_URL로 EC2 직접 호출 (서버-서버는 HTTP 무방)
// 클라이언트 컴포넌트: /api/proxy 경유 → Vercel 서버 → EC2 (Mixed Content 해결)
export const API_BASE =
  typeof window === 'undefined'
    ? (process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080')
    : '/api/proxy';

export async function apiFetch<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    credentials: 'include',
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.error || `API 요청 실패: ${res.status}`);
  }
  return res.json();
}

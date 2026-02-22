// 서버 컴포넌트(SSR): BACKEND_URL로 EC2 직접 호출 (서버-서버는 HTTP 무방)
// 클라이언트 컴포넌트: /api/proxy 경유 → Vercel 서버 → EC2 (Mixed Content 해결)
export const API_BASE =
  typeof window === 'undefined'
    ? (process.env.BACKEND_URL || 'http://localhost:8080')
    : '/api/proxy';

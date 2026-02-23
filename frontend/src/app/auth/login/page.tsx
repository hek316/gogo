'use client';

import { useAuth } from '@/lib/auth/AuthContext';

export default function LoginPage() {
  const { loginWithKakao } = useAuth();

  return (
    <div className="min-h-screen bg-bg flex flex-col items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <div className="text-center mb-10">
          <h1 className="text-3xl font-bold text-text-main tracking-tight mb-2">GoGo</h1>
          <p className="text-sm text-text-muted">친구들과 가고 싶은 장소를 기록해보세요</p>
        </div>

        <button
          onClick={loginWithKakao}
          className="w-full flex items-center justify-center gap-3 rounded-[16px] py-4 text-sm font-semibold transition hover:brightness-95 active:scale-[0.98]"
          style={{ backgroundColor: '#FEE500', color: '#191919' }}
        >
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path fillRule="evenodd" clipRule="evenodd"
              d="M9 0.5C4.029 0.5 0 3.696 0 7.637c0 2.54 1.687 4.775 4.242 6.04L3.19 17.5l4.22-2.803A10.6 10.6 0 009 14.773c4.971 0 9-3.195 9-7.136C18 3.696 13.971.5 9 .5z"
              fill="#191919"/>
          </svg>
          카카오로 계속하기
        </button>
      </div>
    </div>
  );
}

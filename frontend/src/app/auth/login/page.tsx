'use client';

import { useAuth } from '@/lib/auth/AuthContext';

export default function LoginPage() {
  const { loginWithKakao, loginWithGoogle } = useAuth();

  return (
    <div className="min-h-screen bg-bg flex flex-col items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <div className="text-center mb-10">
          <h1 className="text-3xl font-bold text-text-main tracking-tight mb-2">GoGo</h1>
          <p className="text-sm text-text-muted">친구들과 가고 싶은 장소를 기록해보세요</p>
        </div>

        <div className="flex flex-col gap-3">
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

          <button
            onClick={loginWithGoogle}
            className="w-full flex items-center justify-center gap-3 rounded-[16px] py-4 text-sm font-semibold transition hover:bg-gray-50 active:scale-[0.98] border border-gray-200"
            style={{ backgroundColor: '#FFFFFF', color: '#191919' }}
          >
            <svg width="18" height="18" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg">
              <path d="M17.64 9.205c0-.639-.057-1.252-.164-1.841H9v3.481h4.844a4.14 4.14 0 01-1.796 2.716v2.259h2.908c1.702-1.567 2.684-3.875 2.684-6.615z" fill="#4285F4"/>
              <path d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332A8.997 8.997 0 009 18z" fill="#34A853"/>
              <path d="M3.964 10.71A5.41 5.41 0 013.682 9c0-.593.102-1.17.282-1.71V4.958H.957A8.996 8.996 0 000 9c0 1.452.348 2.827.957 4.042l3.007-2.332z" fill="#FBBC05"/>
              <path d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0A8.997 8.997 0 00.957 4.958L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58z" fill="#EA4335"/>
            </svg>
            Google로 계속하기
          </button>
        </div>
      </div>
    </div>
  );
}

'use client';

import { useSearchParams } from 'next/navigation';
import { useRouter } from 'next/navigation';
import { Suspense } from 'react';

function ErrorContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const message = searchParams.get('message') || '로그인 중 오류가 발생했습니다.';

  return (
    <div className="min-h-screen bg-bg flex flex-col items-center justify-center px-4 text-center">
      <div className="text-4xl mb-4">😕</div>
      <h1 className="text-xl font-bold text-text-main mb-2">로그인 실패</h1>
      <p className="text-sm text-text-muted mb-8">{message}</p>
      <button
        onClick={() => router.replace('/auth/login')}
        className="bg-green text-white rounded-[16px] px-8 py-3 text-sm font-medium hover:bg-green-mid transition"
      >
        다시 시도하기
      </button>
    </div>
  );
}

export default function AuthErrorPage() {
  return (
    <Suspense>
      <ErrorContent />
    </Suspense>
  );
}

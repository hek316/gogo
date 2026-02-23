import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';

interface Props {
  searchParams: Promise<{ at?: string; rt?: string; error?: string }>;
}

export default async function AuthCallbackPage({ searchParams }: Props) {
  const params = await searchParams;

  if (params.error || !params.at || !params.rt) {
    redirect(`/auth/error?message=${encodeURIComponent(params.error || '인증에 실패했습니다.')}`);
  }

  const cookieStore = await cookies();

  cookieStore.set('access-token', params.at, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    maxAge: 15 * 60, // 15 minutes
    path: '/',
  });

  cookieStore.set('refresh-token', params.rt, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    maxAge: 7 * 24 * 60 * 60, // 7 days
    path: '/',
  });

  redirect('/');
}

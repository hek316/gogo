import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import { NextRequest } from 'next/server';

export async function GET(req: NextRequest) {
  const { searchParams } = req.nextUrl;
  const at = searchParams.get('at');
  const rt = searchParams.get('rt');
  const error = searchParams.get('error');

  if (error || !at || !rt) {
    redirect(`/auth/error?message=${encodeURIComponent(error || '인증에 실패했습니다.')}`);
  }

  const cookieStore = await cookies();

  cookieStore.set('access-token', at, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    maxAge: 15 * 60,
    path: '/',
  });

  cookieStore.set('refresh-token', rt, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    maxAge: 7 * 24 * 60 * 60,
    path: '/',
  });

  redirect('/');
}

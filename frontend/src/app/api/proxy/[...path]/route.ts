import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

async function handler(
  req: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
) {
  const { path } = await params;
  const search = req.nextUrl.search;
  const url = `${BACKEND_URL}/${path.join('/')}${search}`;

  const cookieStore = await cookies();
  const accessToken = cookieStore.get('access-token')?.value;

  const headers = new Headers();
  req.headers.forEach((value, key) => {
    if (!['host', 'connection', 'transfer-encoding', 'origin'].includes(key.toLowerCase())) {
      headers.set(key, value);
    }
  });

  if (accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`);
  }

  const init: RequestInit = { method: req.method, headers };

  if (!['GET', 'HEAD'].includes(req.method)) {
    init.body = await req.arrayBuffer();
  }

  const res = await fetch(url, init);
  const data = await res.arrayBuffer();

  const responseHeaders = new Headers({
    'Content-Type': res.headers.get('Content-Type') || 'application/json',
  });

  // Forward Set-Cookie headers from backend (handle multiple cookies)
  const setCookies = typeof (res.headers as Headers & { getSetCookie?: () => string[] }).getSetCookie === 'function'
    ? (res.headers as Headers & { getSetCookie: () => string[] }).getSetCookie()
    : res.headers.get('set-cookie') ? [res.headers.get('set-cookie')!] : [];
  for (const cookie of setCookies) {
    responseHeaders.append('Set-Cookie', cookie);
  }

  return new NextResponse(data, {
    status: res.status,
    headers: responseHeaders,
  });
}

export const GET = handler;
export const POST = handler;
export const PUT = handler;
export const PATCH = handler;
export const DELETE = handler;

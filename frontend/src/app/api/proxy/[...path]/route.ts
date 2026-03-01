import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

const BACKEND_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

async function handler(
  req: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
) {
  try {
    const { path } = await params;
    const search = req.nextUrl.search;
    const url = `${BACKEND_URL}/${path.join('/')}${search}`;

    console.log(`[Proxy] ${req.method} ${url} (BACKEND_URL=${BACKEND_URL})`);

    const cookieStore = await cookies();
    const accessToken = cookieStore.get('access-token')?.value;

    const headers = new Headers();
    const EXCLUDED = ['host', 'connection', 'transfer-encoding', 'origin', 'content-length'];
    req.headers.forEach((value, key) => {
      if (!EXCLUDED.includes(key.toLowerCase())) {
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
    console.log(`[Proxy] response status: ${res.status}`);

    const data = await res.arrayBuffer();

    const responseHeaders = new Headers({
      'Content-Type': res.headers.get('Content-Type') || 'application/json',
    });

    const setCookie = res.headers.get('Set-Cookie');
    if (setCookie) {
      responseHeaders.set('Set-Cookie', setCookie);
    }

    return new NextResponse(data, {
      status: res.status,
      headers: responseHeaders,
    });
  } catch (error) {
    console.error(`[Proxy] ERROR`, error);
    return NextResponse.json(
      { error: 'Proxy error', detail: String(error) },
      { status: 503 }
    );
  }
}

export const GET = handler;
export const POST = handler;
export const PUT = handler;
export const PATCH = handler;
export const DELETE = handler;

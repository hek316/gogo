import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

async function handler(
  req: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
) {
  const { path } = await params;
  const search = req.nextUrl.search;
  const url = `${BACKEND_URL}/api/${path.join('/')}${search}`;

  const headers = new Headers();
  req.headers.forEach((value, key) => {
    if (!['host', 'connection', 'transfer-encoding'].includes(key.toLowerCase())) {
      headers.set(key, value);
    }
  });

  const init: RequestInit = { method: req.method, headers };

  if (!['GET', 'HEAD'].includes(req.method)) {
    init.body = await req.arrayBuffer();
  }

  const res = await fetch(url, init);
  const data = await res.arrayBuffer();

  return new NextResponse(data, {
    status: res.status,
    headers: {
      'Content-Type': res.headers.get('Content-Type') || 'application/json',
    },
  });
}

export const GET = handler;
export const POST = handler;
export const PUT = handler;
export const PATCH = handler;
export const DELETE = handler;

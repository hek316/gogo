const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export interface Review {
  id: number;
  placeId: number;
  authorName: string;
  rating: number;
  content: string;
  visitedAt: string;
  createdAt: string;
}

export async function getReviews(placeId: number): Promise<Review[]> {
  const res = await fetch(`${API_URL}/api/places/${placeId}/reviews`, { cache: 'no-store' });
  if (!res.ok) throw new Error('후기를 불러오지 못했습니다.');
  return res.json();
}

export async function addReview(placeId: number, data: {
  authorName: string;
  rating: number;
  content: string;
  visitedAt: string;
}): Promise<Review> {
  const res = await fetch(`${API_URL}/api/places/${placeId}/reviews`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || '후기 작성에 실패했습니다.');
  }
  return res.json();
}

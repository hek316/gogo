import { apiFetch } from './config';

export interface Review {
  id: number;
  placeId: number;
  authorName: string;
  rating: number;
  content: string;
  visitedAt: string;
  createdAt: string;
}

export function getReviews(placeId: number): Promise<Review[]> {
  return apiFetch(`/api/places/${placeId}/reviews`, { cache: 'no-store' });
}

export function addReview(placeId: number, data: {
  authorName: string;
  rating: number;
  content: string;
  visitedAt: string;
}): Promise<Review> {
  return apiFetch(`/api/places/${placeId}/reviews`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
}

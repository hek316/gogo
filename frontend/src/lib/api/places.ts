import { apiFetch } from './config';

export type PlaceStatus = 'WANT_TO_GO' | 'VISITED';

export interface Place {
  id: number;
  name: string;
  address: string;
  category: string;
  url?: string;
  note?: string;
  imageUrl?: string;
  status: PlaceStatus;
  createdBy: string;
  createdAt: string;
  likeCount: number;
  isLiked: boolean;
}

export interface AddPlaceRequest {
  name: string;
  address: string;
  category: string;
  url?: string;
  note?: string;
  imageUrl?: string;
}

export interface PlacePreview {
  title: string | null;
  imageUrl: string | null;
  address: string | null;
  description: string | null;
}

export interface PlaceSearchResult {
  name: string;
  address: string | null;
  mapUrl: string | null;
  category: string | null;
  phone: string | null;
}

export function getPlaces(category?: string): Promise<Place[]> {
  const params = category ? `?category=${category}` : '';
  return apiFetch(`/api/places${params}`, { cache: 'no-store' });
}

export function getPlace(id: number): Promise<Place> {
  return apiFetch(`/api/places/${id}`, { cache: 'no-store' });
}

export function addPlace(data: AddPlaceRequest): Promise<Place> {
  return apiFetch('/api/places', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
}

export function markVisited(id: number): Promise<Place> {
  return apiFetch(`/api/places/${id}/visit`, { method: 'PATCH' });
}

export async function deletePlace(id: number): Promise<void> {
  await apiFetch(`/api/places/${id}`, { method: 'DELETE' });
}

export function getPopularPlaces(limit = 10): Promise<Place[]> {
  return apiFetch(`/api/places/popular?limit=${limit}`, { cache: 'no-store' });
}

export function getRecentPlaces(limit = 20): Promise<Place[]> {
  return apiFetch(`/api/places/recent?limit=${limit}`, { cache: 'no-store' });
}

export async function fetchPlacePreview(url: string): Promise<PlacePreview> {
  try {
    return await apiFetch(`/api/places/preview?url=${encodeURIComponent(url)}`);
  } catch {
    return { title: null, imageUrl: null, address: null, description: null };
  }
}

export async function searchPlaces(keyword: string): Promise<PlaceSearchResult[]> {
  if (!keyword.trim()) return [];
  try {
    return await apiFetch(`/api/places/search?keyword=${encodeURIComponent(keyword)}`);
  } catch {
    return [];
  }
}

export function likePlace(id: number): Promise<void> {
  return apiFetch(`/api/places/${id}/like`, { method: 'POST' });
}

export function unlikePlace(id: number): Promise<void> {
  return apiFetch(`/api/places/${id}/like`, { method: 'DELETE' });
}

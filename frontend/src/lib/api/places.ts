import { API_BASE as API_URL } from './config';

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

export async function getPlaces(category?: string): Promise<Place[]> {
  const params = category ? `?category=${category}` : '';
  const res = await fetch(`${API_URL}/api/places${params}`, { cache: 'no-store', credentials: 'include' });
  if (!res.ok) throw new Error('장소 목록을 불러오지 못했습니다.');
  return res.json();
}

export async function addPlace(data: AddPlaceRequest): Promise<Place> {
  const res = await fetch(`${API_URL}/api/places`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
    credentials: 'include',
  });
  if (!res.ok) throw new Error('장소 추가에 실패했습니다.');
  return res.json();
}

export async function markVisited(id: number): Promise<Place> {
  const res = await fetch(`${API_URL}/api/places/${id}/visit`, {
    method: 'PATCH',
    credentials: 'include',
  });
  if (!res.ok) throw new Error('방문 완료 처리에 실패했습니다.');
  return res.json();
}

export async function deletePlace(id: number): Promise<void> {
  const res = await fetch(`${API_URL}/api/places/${id}`, {
    method: 'DELETE',
    credentials: 'include',
  });
  if (!res.ok) throw new Error('장소 삭제에 실패했습니다.');
}

export async function getPopularPlaces(limit = 10): Promise<Place[]> {
  const res = await fetch(`${API_URL}/api/places/popular?limit=${limit}`, { cache: 'no-store' });
  if (!res.ok) throw new Error('인기 장소를 불러오지 못했습니다.');
  return res.json();
}

export async function getRecentPlaces(limit = 20): Promise<Place[]> {
  const res = await fetch(`${API_URL}/api/places/recent?limit=${limit}`, { cache: 'no-store' });
  if (!res.ok) throw new Error('최신 장소를 불러오지 못했습니다.');
  return res.json();
}

export async function fetchPlacePreview(url: string): Promise<PlacePreview> {
  const res = await fetch(`${API_URL}/api/places/preview?url=${encodeURIComponent(url)}`);
  if (!res.ok) return { title: null, imageUrl: null, address: null, description: null };
  return res.json();
}

export async function searchPlaces(keyword: string): Promise<PlaceSearchResult[]> {
  if (!keyword.trim()) return [];
  const res = await fetch(`${API_URL}/api/places/search?keyword=${encodeURIComponent(keyword)}`, {
    credentials: 'include',
  });
  if (!res.ok) return [];
  return res.json();
}

export async function likePlace(id: number): Promise<void> {
  const res = await fetch(`${API_URL}/api/places/${id}/like`, {
    method: 'POST',
    credentials: 'include',
  });
  if (!res.ok) throw new Error('좋아요 처리에 실패했습니다.');
}

export async function unlikePlace(id: number): Promise<void> {
  const res = await fetch(`${API_URL}/api/places/${id}/like`, {
    method: 'DELETE',
    credentials: 'include',
  });
  if (!res.ok) throw new Error('좋아요 취소에 실패했습니다.');
}

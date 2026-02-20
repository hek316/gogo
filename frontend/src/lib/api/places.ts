const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export type PlaceStatus = 'WANT_TO_GO' | 'VISITED';

export interface Place {
  id: number;
  name: string;
  address: string;
  category: string;
  url?: string;
  note?: string;
  status: PlaceStatus;
  createdBy: string;
  createdAt: string;
}

export interface AddPlaceRequest {
  name: string;
  address: string;
  category: string;
  url?: string;
  note?: string;
  createdBy: string;
}

export async function getPlaces(category?: string): Promise<Place[]> {
  const params = category ? `?category=${category}` : '';
  const res = await fetch(`${API_URL}/api/places${params}`, { cache: 'no-store' });
  if (!res.ok) throw new Error('장소 목록을 불러오지 못했습니다.');
  return res.json();
}

export async function addPlace(data: AddPlaceRequest): Promise<Place> {
  const res = await fetch(`${API_URL}/api/places`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error('장소 추가에 실패했습니다.');
  return res.json();
}

export async function markVisited(id: number): Promise<Place> {
  const res = await fetch(`${API_URL}/api/places/${id}/visit`, { method: 'PATCH' });
  if (!res.ok) throw new Error('방문 완료 처리에 실패했습니다.');
  return res.json();
}

export async function deletePlace(id: number): Promise<void> {
  const res = await fetch(`${API_URL}/api/places/${id}`, { method: 'DELETE' });
  if (!res.ok) throw new Error('장소 삭제에 실패했습니다.');
}

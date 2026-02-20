import { Place } from './places';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export interface GroupMember {
  id: number;
  nickname: string;
  joinedAt: string;
}

export interface Group {
  id: number;
  name: string;
  inviteCode: string;
  createdBy: string;
  members: GroupMember[];
  createdAt: string;
}

export interface GroupPlace {
  id: number;
  groupId: number;
  place: Place;
  sharedBy: string;
  sharedAt: string;
}

export async function createGroup(name: string, createdBy: string): Promise<Group> {
  const res = await fetch(`${API_URL}/api/groups`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, createdBy }),
  });
  if (!res.ok) throw new Error('그룹 생성에 실패했습니다.');
  return res.json();
}

export async function joinGroup(inviteCode: string, nickname: string): Promise<Group> {
  const res = await fetch(`${API_URL}/api/groups/join`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ inviteCode, nickname }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || '그룹 참여에 실패했습니다.');
  }
  return res.json();
}

export async function getGroup(id: number): Promise<Group> {
  const res = await fetch(`${API_URL}/api/groups/${id}`, { cache: 'no-store' });
  if (!res.ok) throw new Error('그룹을 불러오지 못했습니다.');
  return res.json();
}

export async function getGroupPlaces(groupId: number): Promise<GroupPlace[]> {
  const res = await fetch(`${API_URL}/api/groups/${groupId}/places`, { cache: 'no-store' });
  if (!res.ok) throw new Error('그룹 장소를 불러오지 못했습니다.');
  return res.json();
}

export async function sharePlaceToGroup(groupId: number, placeId: number, sharedBy: string): Promise<GroupPlace> {
  const res = await fetch(`${API_URL}/api/groups/${groupId}/places`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ placeId, sharedBy }),
  });
  if (!res.ok) throw new Error('장소 공유에 실패했습니다.');
  return res.json();
}

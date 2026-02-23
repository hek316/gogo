import { Place } from './places';

import { API_BASE as API_URL } from './config';

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

export async function createGroup(name: string): Promise<Group> {
  const res = await fetch(`${API_URL}/api/groups`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name }),
    credentials: 'include',
  });
  if (!res.ok) throw new Error('그룹 생성에 실패했습니다.');
  return res.json();
}

export async function joinGroup(inviteCode: string): Promise<Group> {
  const res = await fetch(`${API_URL}/api/groups/join`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ inviteCode }),
    credentials: 'include',
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || '그룹 참여에 실패했습니다.');
  }
  return res.json();
}

export async function getGroup(id: number): Promise<Group> {
  const res = await fetch(`${API_URL}/api/groups/${id}`, {
    cache: 'no-store',
    credentials: 'include',
  });
  if (!res.ok) throw new Error('그룹을 불러오지 못했습니다.');
  return res.json();
}

export async function getGroupPlaces(groupId: number): Promise<GroupPlace[]> {
  const res = await fetch(`${API_URL}/api/groups/${groupId}/places`, {
    cache: 'no-store',
    credentials: 'include',
  });
  if (!res.ok) throw new Error('그룹 장소를 불러오지 못했습니다.');
  return res.json();
}

export async function sharePlaceToGroup(groupId: number, placeId: number): Promise<GroupPlace> {
  const res = await fetch(`${API_URL}/api/groups/${groupId}/places`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ placeId }),
    credentials: 'include',
  });
  if (!res.ok) throw new Error('장소 공유에 실패했습니다.');
  return res.json();
}

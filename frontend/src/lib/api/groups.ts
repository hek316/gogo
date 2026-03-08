import { Place } from './places';
import { apiFetch } from './config';

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

export function createGroup(name: string): Promise<Group> {
  return apiFetch('/api/groups', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name }),
  });
}

export function joinGroup(inviteCode: string): Promise<Group> {
  return apiFetch('/api/groups/join', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ inviteCode }),
  });
}

export function getGroup(id: number): Promise<Group> {
  return apiFetch(`/api/groups/${id}`, { cache: 'no-store' });
}

export function getGroupPlaces(groupId: number): Promise<GroupPlace[]> {
  return apiFetch(`/api/groups/${groupId}/places`, { cache: 'no-store' });
}

export function sharePlaceToGroup(groupId: number, placeId: number): Promise<GroupPlace> {
  return apiFetch(`/api/groups/${groupId}/places`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ placeId }),
  });
}

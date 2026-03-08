import { apiFetch } from './config';

export interface VoteResult {
  placeId: number;
  voteCount: number;
  voters: string[];
}

export interface Meeting {
  id: number;
  groupId: number;
  title: string;
  candidatePlaceIds: number[];
  status: 'VOTING' | 'CONFIRMED';
  confirmedPlaceId: number | null;
  voteResults: VoteResult[];
  createdAt: string;
}

export function createMeeting(groupId: number, title: string, candidatePlaceIds: number[]): Promise<Meeting> {
  return apiFetch(`/api/groups/${groupId}/meetings`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title, candidatePlaceIds }),
  });
}

export function getMeeting(groupId: number, meetingId: number): Promise<Meeting> {
  return apiFetch(`/api/groups/${groupId}/meetings/${meetingId}`, { cache: 'no-store' });
}

export function vote(meetingId: number, placeId: number, voterName: string): Promise<Meeting> {
  return apiFetch(`/api/meetings/${meetingId}/vote`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ placeId, voterName }),
  });
}

export function finalizeMeeting(meetingId: number, confirmedPlaceId: number): Promise<Meeting> {
  return apiFetch(`/api/meetings/${meetingId}/finalize`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ confirmedPlaceId }),
  });
}

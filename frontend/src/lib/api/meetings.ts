const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

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

export async function createMeeting(groupId: number, title: string, candidatePlaceIds: number[]): Promise<Meeting> {
  const res = await fetch(`${API_URL}/api/groups/${groupId}/meetings`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title, candidatePlaceIds }),
  });
  if (!res.ok) throw new Error('약속 생성에 실패했습니다.');
  return res.json();
}

export async function getMeeting(groupId: number, meetingId: number): Promise<Meeting> {
  const res = await fetch(`${API_URL}/api/groups/${groupId}/meetings/${meetingId}`, { cache: 'no-store' });
  if (!res.ok) throw new Error('약속을 불러오지 못했습니다.');
  return res.json();
}

export async function vote(meetingId: number, placeId: number, voterName: string): Promise<Meeting> {
  const res = await fetch(`${API_URL}/api/meetings/${meetingId}/vote`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ placeId, voterName }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || '투표에 실패했습니다.');
  }
  return res.json();
}

export async function finalizeMeeting(meetingId: number, confirmedPlaceId: number): Promise<Meeting> {
  const res = await fetch(`${API_URL}/api/meetings/${meetingId}/finalize`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ confirmedPlaceId }),
  });
  if (!res.ok) throw new Error('확정에 실패했습니다.');
  return res.json();
}

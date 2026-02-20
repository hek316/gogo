'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter, useSearchParams } from 'next/navigation';
import { getMeeting, vote, finalizeMeeting, Meeting, VoteResult } from '@/lib/api/meetings';
import { getPlaces, Place } from '@/lib/api/places';

export default function MeetingPage() {
  const { id } = useParams<{ id: string }>();
  const searchParams = useSearchParams();
  const groupId = Number(searchParams.get('groupId') || '0');
  const router = useRouter();

  const [meeting, setMeeting] = useState<Meeting | null>(null);
  const [places, setPlaces] = useState<Record<number, Place>>({});
  const [loading, setLoading] = useState(true);
  const [nickname, setNickname] = useState('');
  const [nicknameSet, setNicknameSet] = useState(false);
  const [voting, setVoting] = useState<number | null>(null);

  useEffect(() => {
    Promise.all([getMeeting(groupId, Number(id)), getPlaces()])
      .then(([m, ps]) => {
        setMeeting(m);
        const map: Record<number, Place> = {};
        ps.forEach(p => { map[p.id] = p; });
        setPlaces(map);
      })
      .finally(() => setLoading(false));
  }, [id, groupId]);

  const totalVotes = meeting?.voteResults.reduce((sum, r) => sum + r.voteCount, 0) || 0;

  const handleVote = async (placeId: number) => {
    if (!nicknameSet || !nickname.trim() || !meeting) return;
    setVoting(placeId);
    try {
      const updated = await vote(meeting.id, placeId, nickname);
      setMeeting(updated);
    } catch (err: any) {
      alert(err.message);
    } finally {
      setVoting(null);
    }
  };

  const handleFinalize = async (placeId: number) => {
    if (!meeting) return;
    if (!confirm(`"${places[placeId]?.name || placeId}"으로 약속을 확정할까요?`)) return;
    try {
      const updated = await finalizeMeeting(meeting.id, placeId);
      setMeeting(updated);
    } catch (err: any) {
      alert(err.message);
    }
  };

  const myVote = (result: VoteResult) => result.voters.includes(nickname);

  if (loading) return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="w-8 h-8 border-4 border-indigo-200 border-t-indigo-600 rounded-full animate-spin" />
    </div>
  );

  if (!meeting) return <div className="p-8 text-center text-gray-400">약속을 찾을 수 없습니다.</div>;

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b border-gray-100 sticky top-0 z-10">
        <div className="max-w-2xl mx-auto px-4 py-4 flex items-center gap-3">
          <button onClick={() => router.back()} className="text-gray-400 hover:text-gray-600">←</button>
          <div className="flex-1 min-w-0">
            <h1 className="text-lg font-bold text-gray-900 truncate">{meeting.title}</h1>
            <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${
              meeting.status === 'CONFIRMED' ? 'bg-green-100 text-green-700' : 'bg-amber-100 text-amber-700'
            }`}>
              {meeting.status === 'CONFIRMED' ? '✓ 확정됨' : '🗳️ 투표 중'}
            </span>
          </div>
        </div>
      </header>

      <main className="max-w-2xl mx-auto px-4 py-6 space-y-5">
        {/* 닉네임 설정 */}
        {!nicknameSet && meeting.status === 'VOTING' && (
          <div className="bg-indigo-50 rounded-2xl p-4">
            <p className="text-sm font-medium text-indigo-700 mb-2">투표하려면 닉네임을 입력하세요</p>
            <div className="flex gap-2">
              <input
                placeholder="닉네임"
                value={nickname}
                onChange={e => setNickname(e.target.value)}
                className="flex-1 border border-indigo-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
              />
              <button
                onClick={() => nickname.trim() && setNicknameSet(true)}
                className="bg-indigo-600 hover:bg-indigo-700 text-white text-sm px-4 py-2 rounded-xl transition"
              >
                확인
              </button>
            </div>
          </div>
        )}

        {nicknameSet && meeting.status === 'VOTING' && (
          <div className="flex items-center gap-2 bg-white rounded-xl px-4 py-2 border border-gray-100 text-sm text-gray-500">
            <span>👤 {nickname}</span>
            <button onClick={() => setNicknameSet(false)} className="ml-auto text-xs text-gray-400 hover:text-gray-600">변경</button>
          </div>
        )}

        {/* 투표 결과 */}
        <div className="space-y-3">
          {meeting.voteResults.map(result => {
            const place = places[result.placeId];
            const pct = totalVotes > 0 ? Math.round((result.voteCount / totalVotes) * 100) : 0;
            const isConfirmed = meeting.confirmedPlaceId === result.placeId;
            const iVoted = nicknameSet && myVote(result);

            return (
              <div key={result.placeId}
                className={`bg-white rounded-2xl p-4 shadow-sm border transition ${
                  isConfirmed ? 'border-green-400 ring-2 ring-green-200' : 'border-gray-100'
                }`}>
                <div className="flex items-start justify-between gap-3 mb-3">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <h3 className="font-semibold text-gray-900 truncate">
                        {place?.name || `장소 #${result.placeId}`}
                      </h3>
                      {isConfirmed && <span className="text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded-full flex-shrink-0">확정!</span>}
                    </div>
                    {place?.address && <p className="text-sm text-gray-500 truncate">{place.address}</p>}
                  </div>
                  <div className="text-right flex-shrink-0">
                    <p className="text-2xl font-bold text-indigo-600">{result.voteCount}</p>
                    <p className="text-xs text-gray-400">표</p>
                  </div>
                </div>

                {/* 득표수 바 */}
                <div className="h-2 bg-gray-100 rounded-full mb-2 overflow-hidden">
                  <div className="h-full bg-indigo-500 rounded-full transition-all duration-500"
                    style={{ width: `${pct}%` }} />
                </div>

                {/* 투표자 목록 */}
                {result.voters.length > 0 && (
                  <div className="flex flex-wrap gap-1 mb-3">
                    {result.voters.map(v => (
                      <span key={v} className={`text-xs px-2 py-0.5 rounded-full ${
                        v === nickname ? 'bg-indigo-100 text-indigo-700 font-medium' : 'bg-gray-100 text-gray-500'
                      }`}>{v}</span>
                    ))}
                  </div>
                )}

                {/* 투표/확정 버튼 */}
                {meeting.status === 'VOTING' && (
                  <div className="flex gap-2">
                    {nicknameSet && (
                      <button onClick={() => handleVote(result.placeId)}
                        disabled={voting !== null}
                        className={`flex-1 text-sm py-2 rounded-xl transition ${
                          iVoted
                            ? 'bg-indigo-600 text-white'
                            : 'bg-indigo-50 hover:bg-indigo-100 text-indigo-600'
                        }`}>
                        {voting === result.placeId ? '...' : iVoted ? '✓ 투표함' : '투표'}
                      </button>
                    )}
                    <button onClick={() => handleFinalize(result.placeId)}
                      className="text-sm px-3 py-2 bg-green-50 hover:bg-green-100 text-green-700 rounded-xl transition">
                      확정
                    </button>
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {meeting.status === 'CONFIRMED' && (
          <div className="bg-green-50 rounded-2xl p-4 text-center">
            <p className="text-2xl mb-1">🎉</p>
            <p className="font-semibold text-green-800">
              {places[meeting.confirmedPlaceId!]?.name || '장소'} 확정!
            </p>
          </div>
        )}
      </main>
    </div>
  );
}

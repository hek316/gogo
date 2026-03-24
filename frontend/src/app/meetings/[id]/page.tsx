'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter, useSearchParams } from 'next/navigation';
import { getMeeting, vote, finalizeMeeting, Meeting, VoteResult } from '@/lib/api/meetings';
import { getPlaces, Place } from '@/lib/api/places';
import { ChevronLeft, Sparkles } from 'lucide-react';

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
    <div className="min-h-screen flex items-center justify-center bg-bg">
      <div className="w-8 h-8 border-4 border-surface border-t-primary rounded-full animate-spin" />
    </div>
  );

  if (!meeting) return <div className="p-8 text-center text-text-muted">약속을 찾을 수 없습니다.</div>;

  return (
    <div className="min-h-screen bg-bg">
      <header className="bg-bg border-b border-border sticky top-0 z-10">
        <div className="max-w-2xl mx-auto px-4 py-4 flex items-center gap-3">
          <button onClick={() => router.back()} className="text-text-muted hover:text-text-main">
            <ChevronLeft size={20} strokeWidth={1.5} />
          </button>
          <div className="flex-1 min-w-0">
            <h1 className="text-lg font-semibold text-text-main truncate">{meeting.title}</h1>
            <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${
              meeting.status === 'CONFIRMED'
                ? 'bg-primary-subtle text-primary'
                : 'bg-amber-50 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400'
            }`}>
              {meeting.status === 'CONFIRMED' ? '✓ 확정됨' : '투표 중'}
            </span>
          </div>
        </div>
      </header>

      <main className="max-w-2xl mx-auto px-4 py-6 space-y-5">
        {!nicknameSet && meeting.status === 'VOTING' && (
          <div className="bg-bg-secondary border border-border rounded-[20px] p-6 shadow-sm">
            <p className="text-sm font-medium text-text-main mb-2">투표하려면 닉네임을 입력하세요</p>
            <div className="flex gap-2">
              <input
                placeholder="닉네임"
                value={nickname}
                onChange={e => setNickname(e.target.value)}
                className="flex-1 border border-border rounded-[12px] px-4 py-2.5 text-sm focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary bg-bg"
              />
              <button
                onClick={() => nickname.trim() && setNicknameSet(true)}
                className="bg-text-main hover:bg-text-secondary text-text-on-primary text-sm px-4 py-2 rounded-[12px] font-medium"
              >
                확인
              </button>
            </div>
          </div>
        )}

        {nicknameSet && meeting.status === 'VOTING' && (
          <div className="flex items-center gap-2 bg-bg-secondary rounded-[20px] px-4 py-2.5 border border-border shadow-sm text-sm text-text-muted">
            <span>{nickname}</span>
            <button onClick={() => setNicknameSet(false)} className="ml-auto text-xs text-text-muted hover:text-text-main font-medium">변경</button>
          </div>
        )}

        <div className="space-y-3">
          {meeting.voteResults.map(result => {
            const place = places[result.placeId];
            const pct = totalVotes > 0 ? Math.round((result.voteCount / totalVotes) * 100) : 0;
            const isConfirmed = meeting.confirmedPlaceId === result.placeId;
            const iVoted = nicknameSet && myVote(result);

            return (
              <div key={result.placeId}
                className={`bg-bg-secondary rounded-[20px] p-6 border transition ${
                  iVoted ? 'ring-1 ring-primary/40 bg-primary-subtle border-primary/20' : isConfirmed ? 'border-primary/30' : 'border-border shadow-sm'
                }`}>
                <div className="flex items-start justify-between gap-3 mb-3">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <h3 className="font-semibold text-text-main truncate">
                        {place?.name || `장소 #${result.placeId}`}
                      </h3>
                      {isConfirmed && <span className="text-xs bg-primary-subtle text-primary px-2 py-0.5 rounded-full flex-shrink-0 font-medium">확정!</span>}
                    </div>
                    {place?.address && <p className="text-sm text-text-muted truncate">{place.address}</p>}
                  </div>
                  <div className="text-right flex-shrink-0">
                    <p className="text-2xl font-bold text-primary">{result.voteCount}</p>
                    <p className="text-xs text-text-muted">표</p>
                  </div>
                </div>

                <div className="h-1.5 bg-surface rounded-full mb-2 overflow-hidden">
                  <div className="h-full bg-primary rounded-full transition-all duration-500"
                    style={{ width: `${pct}%` }} />
                </div>

                {result.voters.length > 0 && (
                  <div className="flex flex-wrap gap-1 mb-3">
                    {result.voters.map(v => (
                      <span key={v} className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                        v === nickname ? 'bg-primary-subtle text-primary' : 'bg-surface text-text-muted'
                      }`}>{v}</span>
                    ))}
                  </div>
                )}

                {meeting.status === 'VOTING' && (
                  <div className="flex gap-2">
                    {nicknameSet && (
                      <button onClick={() => handleVote(result.placeId)}
                        disabled={voting !== null}
                        className={`flex-1 text-sm py-2 rounded-[12px] font-medium transition ${
                          iVoted
                            ? 'bg-text-main text-text-on-primary'
                            : 'bg-surface text-primary hover:bg-primary-subtle'
                        }`}>
                        {voting === result.placeId ? '...' : iVoted ? '✓ 투표함' : '투표'}
                      </button>
                    )}
                    <button onClick={() => handleFinalize(result.placeId)}
                      className="text-sm px-4 py-2 bg-text-main hover:bg-text-secondary text-text-on-primary rounded-[12px] font-medium">
                      확정
                    </button>
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {meeting.status === 'CONFIRMED' && (
          <div className="bg-gradient-to-br from-primary to-primary-hover rounded-[20px] p-5 text-center text-white">
            <div className="flex justify-center mb-2">
              <Sparkles size={32} strokeWidth={1.5} stroke="white" />
            </div>
            <p className="font-bold text-lg">
              {places[meeting.confirmedPlaceId!]?.name || '장소'} 확정!
            </p>
          </div>
        )}
      </main>
    </div>
  );
}

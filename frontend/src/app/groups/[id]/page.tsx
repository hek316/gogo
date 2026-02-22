'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getGroup, getGroupPlaces, sharePlaceToGroup, Group, GroupPlace } from '@/lib/api/groups';
import { getPlaces, Place } from '@/lib/api/places';
import { createMeeting } from '@/lib/api/meetings';
import { ChevronLeft, Plus, Inbox } from 'lucide-react';

const CATEGORY_LABEL: Record<string, string> = {
  CAFE: '카페', RESTAURANT: '식당', BAR: '바/펍', ACTIVITY: '액티비티', ETC: '기타',
};

export default function GroupDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const groupId = Number(id);

  const [group, setGroup] = useState<Group | null>(null);
  const [groupPlaces, setGroupPlaces] = useState<GroupPlace[]>([]);
  const [myPlaces, setMyPlaces] = useState<Place[]>([]);
  const [loading, setLoading] = useState(true);
  const [shareModal, setShareModal] = useState(false);
  const [meetingModal, setMeetingModal] = useState(false);
  const [shareForm, setShareForm] = useState({ placeId: '', sharedBy: '' });
  const [meetingForm, setMeetingForm] = useState({ title: '', candidatePlaceIds: [] as number[] });
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    Promise.all([getGroup(groupId), getGroupPlaces(groupId), getPlaces()])
      .then(([g, gp, places]) => { setGroup(g); setGroupPlaces(gp); setMyPlaces(places); })
      .finally(() => setLoading(false));
  }, [groupId]);

  const copyInviteCode = () => {
    if (!group) return;
    navigator.clipboard.writeText(group.inviteCode);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleShare = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const gp = await sharePlaceToGroup(groupId, Number(shareForm.placeId), shareForm.sharedBy);
      setGroupPlaces(prev => [...prev, gp]);
      setShareModal(false);
      setShareForm({ placeId: '', sharedBy: '' });
    } catch (err: any) {
      alert(err.message);
    }
  };

  const toggleCandidate = (placeId: number) => {
    setMeetingForm(f => ({
      ...f,
      candidatePlaceIds: f.candidatePlaceIds.includes(placeId)
        ? f.candidatePlaceIds.filter(id => id !== placeId)
        : [...f.candidatePlaceIds, placeId],
    }));
  };

  const handleCreateMeeting = async (e: React.FormEvent) => {
    e.preventDefault();
    if (meetingForm.candidatePlaceIds.length < 2) {
      alert('후보 장소를 2개 이상 선택해주세요.');
      return;
    }
    try {
      const meeting = await createMeeting(groupId, meetingForm.title, meetingForm.candidatePlaceIds);
      setMeetingModal(false);
      setMeetingForm({ title: '', candidatePlaceIds: [] });
      router.push(`/meetings/${meeting.id}?groupId=${groupId}`);
    } catch (err: any) {
      alert(err.message);
    }
  };

  if (loading) return (
    <div className="min-h-screen flex items-center justify-center bg-bg">
      <div className="w-8 h-8 border-4 border-surface border-t-mint rounded-full animate-spin" />
    </div>
  );

  if (!group) return <div className="p-8 text-center text-text-muted">그룹을 찾을 수 없습니다.</div>;

  const sharedPlaceIds = new Set(groupPlaces.map(gp => gp.place.id));
  const unsharedPlaces = myPlaces.filter(p => !sharedPlaceIds.has(p.id));

  return (
    <div className="min-h-screen bg-bg">
      <header className="bg-white border-b border-border sticky top-0 z-10">
        <div className="max-w-2xl mx-auto px-4 py-4 flex items-center gap-3">
          <button onClick={() => router.push('/groups')} className="text-text-muted hover:text-text-main">
            <ChevronLeft size={20} strokeWidth={1.5} />
          </button>
          <h1 className="text-xl font-semibold text-text-main flex-1 truncate">{group.name}</h1>
          {groupPlaces.length >= 2 && (
            <button onClick={() => setMeetingModal(true)}
              className="text-sm bg-mint hover:bg-green-mid text-white px-3 py-1.5 rounded-[16px] font-medium">
              약속 만들기
            </button>
          )}
        </div>
      </header>

      <main className="max-w-2xl mx-auto px-4 py-6 space-y-6">
        {/* 초대 코드 */}
        <div className="bg-surface rounded-[16px] p-4 flex items-center justify-between border border-border">
          <div>
            <p className="text-xs text-text-muted font-medium mb-0.5">초대 코드</p>
            <p className="text-2xl font-mono font-bold text-text-main tracking-widest">{group.inviteCode}</p>
          </div>
          <button onClick={copyInviteCode}
            className="bg-mint hover:bg-green-mid text-white font-medium text-sm px-4 py-2 rounded-[12px]">
            {copied ? '✓ 복사됨' : '복사'}
          </button>
        </div>

        {/* 멤버 목록 */}
        <div className="bg-white rounded-[20px] p-6 border border-border shadow-sm">
          <h2 className="font-semibold text-text-main mb-3">멤버 ({group.members.length}명)</h2>
          {group.members.length === 0 ? (
            <p className="text-sm text-text-muted">아직 참여한 멤버가 없어요. 초대 코드를 공유해보세요!</p>
          ) : (
            <div className="flex flex-wrap gap-2">
              {group.members.map(m => (
                <span key={m.id} className="bg-surface text-green text-sm px-3 py-1 rounded-full font-medium">
                  {m.nickname}
                </span>
              ))}
            </div>
          )}
        </div>

        {/* 공유 장소 목록 */}
        <div>
          <div className="flex items-center justify-between mb-3">
            <h2 className="font-semibold text-text-main">공유 장소 ({groupPlaces.length})</h2>
            <button onClick={() => setShareModal(true)}
              className="text-sm bg-green hover:bg-green-mid text-white px-4 py-1.5 rounded-[16px] font-medium flex items-center gap-1">
              <Plus size={14} strokeWidth={1.5} />
              장소 공유
            </button>
          </div>

          {groupPlaces.length < 2 && (
            <p className="text-xs text-text-muted mb-2">※ 장소를 2개 이상 공유하면 약속 만들기가 가능해요</p>
          )}

          {groupPlaces.length === 0 ? (
            <div className="bg-white rounded-[20px] p-8 text-center text-text-muted border border-border">
              <div className="flex justify-center mb-2">
                <Inbox size={32} strokeWidth={1.5} className="text-text-muted" />
              </div>
              <p className="text-sm">아직 공유된 장소가 없어요</p>
            </div>
          ) : (
            <div className="space-y-3">
              {groupPlaces.map(gp => (
                <div key={gp.id} className="bg-white rounded-[20px] p-6 border border-border hover:shadow-[0_6px_20px_rgba(0,212,170,0.14)] transition-all duration-300">
                  <div className="flex items-start gap-3">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <span className="text-xs text-text-muted">{CATEGORY_LABEL[gp.place.category] ?? '기타'}</span>
                        <span className={`text-xs px-2 py-0.5 rounded-full flex-shrink-0 font-medium ${
                          gp.place.status === 'VISITED'
                            ? 'bg-mint/20 text-green'
                            : 'bg-surface text-text-muted'
                        }`}>
                          {gp.place.status === 'VISITED' ? '방문완료' : '가고싶어'}
                        </span>
                      </div>
                      <p className="font-semibold text-text-main truncate">{gp.place.name}</p>
                      {gp.place.address && <p className="text-sm text-text-muted truncate">{gp.place.address}</p>}
                      <p className="text-xs text-text-muted mt-1">{gp.sharedBy}님이 공유</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>

      {/* 장소 공유 모달 */}
      {shareModal && (
        <div className="fixed inset-0 bg-black/40 flex items-end sm:items-center justify-center z-50">
          <div className="bg-[#F0FDF9] w-full sm:max-w-md rounded-t-[28px] sm:rounded-[28px] p-6 shadow-lg border-t border-border">
            <div className="flex justify-between items-center mb-5">
              <h2 className="text-lg font-semibold text-text-main">장소 공유하기</h2>
              <button onClick={() => setShareModal(false)}
                className="w-8 h-8 flex items-center justify-center rounded-full bg-surface text-green hover:bg-[#A7F3D0] text-sm font-bold">
                ✕
              </button>
            </div>
            <form onSubmit={handleShare} className="space-y-3">
              <select required value={shareForm.placeId}
                onChange={e => setShareForm(f => ({ ...f, placeId: e.target.value }))}
                className="w-full border border-border rounded-[12px] px-5 py-3 text-sm focus:outline-none focus:ring-1 focus:ring-mint focus:border-mint bg-white">
                <option value="">공유할 장소 선택</option>
                {unsharedPlaces.map(p => (
                  <option key={p.id} value={p.id}>{CATEGORY_LABEL[p.category]} {p.name}</option>
                ))}
              </select>
              <input required placeholder="닉네임"
                value={shareForm.sharedBy}
                onChange={e => setShareForm(f => ({ ...f, sharedBy: e.target.value }))}
                className="w-full border border-border rounded-[12px] px-5 py-3 text-sm focus:outline-none focus:ring-1 focus:ring-mint focus:border-mint bg-white"
              />
              <button type="submit"
                className="w-full bg-green hover:bg-green-mid text-white rounded-[16px] py-3.5 text-sm font-medium">
                공유하기
              </button>
            </form>
          </div>
        </div>
      )}

      {/* 약속 만들기 모달 */}
      {meetingModal && (
        <div className="fixed inset-0 bg-black/40 flex items-end sm:items-center justify-center z-50">
          <div className="bg-[#F0FDF9] w-full sm:max-w-md rounded-t-[28px] sm:rounded-[28px] p-6 shadow-lg border-t border-border max-h-[80vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-5">
              <h2 className="text-lg font-semibold text-text-main">약속 만들기</h2>
              <button onClick={() => setMeetingModal(false)}
                className="w-8 h-8 flex items-center justify-center rounded-full bg-surface text-green hover:bg-[#A7F3D0] text-sm font-bold">
                ✕
              </button>
            </div>
            <form onSubmit={handleCreateMeeting} className="space-y-4">
              <input required placeholder="약속 이름 (예: 이번 주 약속)"
                value={meetingForm.title}
                onChange={e => setMeetingForm(f => ({ ...f, title: e.target.value }))}
                className="w-full border border-border rounded-[12px] px-5 py-3 text-sm focus:outline-none focus:ring-1 focus:ring-mint focus:border-mint bg-white"
              />
              <div>
                <p className="text-sm font-medium text-text-main mb-2">후보 장소 선택 (2개 이상)</p>
                <div className="space-y-2">
                  {groupPlaces.map(gp => (
                    <label key={gp.id}
                      className={`flex items-center gap-3 p-3 rounded-[12px] border cursor-pointer transition ${
                        meetingForm.candidatePlaceIds.includes(gp.place.id)
                          ? 'border-mint/40 bg-surface/50'
                          : 'border-border hover:border-mint/50'
                      }`}>
                      <input type="checkbox"
                        checked={meetingForm.candidatePlaceIds.includes(gp.place.id)}
                        onChange={() => toggleCandidate(gp.place.id)}
                        className="hidden"
                      />
                      <span className="text-sm text-text-muted">{CATEGORY_LABEL[gp.place.category] ?? '기타'}</span>
                      <span className="text-sm font-medium text-text-main">{gp.place.name}</span>
                      {meetingForm.candidatePlaceIds.includes(gp.place.id) && (
                        <span className="ml-auto text-mint text-sm font-bold">✓</span>
                      )}
                    </label>
                  ))}
                </div>
              </div>
              <button type="submit"
                className="w-full bg-green hover:bg-green-mid text-white rounded-[16px] py-3.5 text-sm font-medium">
                약속 만들기
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

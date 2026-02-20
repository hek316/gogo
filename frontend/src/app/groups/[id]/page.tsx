'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getGroup, getGroupPlaces, sharePlaceToGroup, Group, GroupPlace } from '@/lib/api/groups';
import { getPlaces, Place } from '@/lib/api/places';

const CATEGORY_LABEL: Record<string, string> = {
  CAFE: '☕', RESTAURANT: '🍽️', BAR: '🍺', ACTIVITY: '🎯', ETC: '📍',
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
  const [shareForm, setShareForm] = useState({ placeId: '', sharedBy: '' });
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

  if (loading) return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="w-8 h-8 border-4 border-indigo-200 border-t-indigo-600 rounded-full animate-spin" />
    </div>
  );

  if (!group) return <div className="p-8 text-center text-gray-400">그룹을 찾을 수 없습니다.</div>;

  const sharedPlaceIds = new Set(groupPlaces.map(gp => gp.place.id));
  const unsharedPlaces = myPlaces.filter(p => !sharedPlaceIds.has(p.id));

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b border-gray-100 sticky top-0 z-10">
        <div className="max-w-2xl mx-auto px-4 py-4 flex items-center gap-3">
          <button onClick={() => router.push('/groups')} className="text-gray-400 hover:text-gray-600">←</button>
          <h1 className="text-xl font-bold text-gray-900 flex-1 truncate">{group.name}</h1>
        </div>
      </header>

      <main className="max-w-2xl mx-auto px-4 py-6 space-y-6">
        {/* 초대 코드 */}
        <div className="bg-indigo-50 rounded-2xl p-4 flex items-center justify-between">
          <div>
            <p className="text-xs text-indigo-400 font-medium mb-0.5">초대 코드</p>
            <p className="text-2xl font-mono font-bold text-indigo-700 tracking-widest">{group.inviteCode}</p>
          </div>
          <button onClick={copyInviteCode}
            className="bg-indigo-600 hover:bg-indigo-700 text-white text-sm px-4 py-2 rounded-xl transition">
            {copied ? '✓ 복사됨' : '복사'}
          </button>
        </div>

        {/* 멤버 목록 */}
        <div className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100">
          <h2 className="font-semibold text-gray-700 mb-3">👥 멤버 ({group.members.length}명)</h2>
          {group.members.length === 0 ? (
            <p className="text-sm text-gray-400">아직 참여한 멤버가 없어요. 초대 코드를 공유해보세요!</p>
          ) : (
            <div className="flex flex-wrap gap-2">
              {group.members.map(m => (
                <span key={m.id} className="bg-gray-100 text-gray-700 text-sm px-3 py-1 rounded-full">
                  {m.nickname}
                </span>
              ))}
            </div>
          )}
        </div>

        {/* 공유 장소 목록 */}
        <div>
          <div className="flex items-center justify-between mb-3">
            <h2 className="font-semibold text-gray-700">📍 공유 장소 ({groupPlaces.length})</h2>
            <button onClick={() => setShareModal(true)}
              className="text-sm bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-1.5 rounded-xl transition">
              + 장소 공유
            </button>
          </div>
          {groupPlaces.length === 0 ? (
            <div className="bg-white rounded-2xl p-8 text-center text-gray-400 border border-gray-100">
              <p className="text-3xl mb-2">📭</p>
              <p className="text-sm">아직 공유된 장소가 없어요</p>
            </div>
          ) : (
            <div className="space-y-3">
              {groupPlaces.map(gp => (
                <div key={gp.id} className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100">
                  <div className="flex items-start gap-3">
                    <span className="text-xl">{CATEGORY_LABEL[gp.place.category] ?? '📍'}</span>
                    <div className="flex-1 min-w-0">
                      <p className="font-semibold text-gray-900 truncate">{gp.place.name}</p>
                      {gp.place.address && <p className="text-sm text-gray-500 truncate">{gp.place.address}</p>}
                      <p className="text-xs text-gray-300 mt-1">{gp.sharedBy}님이 공유</p>
                    </div>
                    <span className={`text-xs px-2 py-0.5 rounded-full flex-shrink-0 ${
                      gp.place.status === 'VISITED' ? 'bg-green-100 text-green-700' : 'bg-indigo-50 text-indigo-500'
                    }`}>
                      {gp.place.status === 'VISITED' ? '방문완료' : '가고싶어'}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>

      {/* 장소 공유 모달 */}
      {shareModal && (
        <div className="fixed inset-0 bg-black/50 flex items-end sm:items-center justify-center z-50">
          <div className="bg-white w-full sm:max-w-md rounded-t-2xl sm:rounded-2xl p-6 shadow-xl">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-lg font-bold text-gray-900">장소 공유하기</h2>
              <button onClick={() => setShareModal(false)} className="text-gray-400 hover:text-gray-600">✕</button>
            </div>
            <form onSubmit={handleShare} className="space-y-3">
              <select required value={shareForm.placeId}
                onChange={e => setShareForm(f => ({ ...f, placeId: e.target.value }))}
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400">
                <option value="">공유할 장소 선택</option>
                {unsharedPlaces.map(p => (
                  <option key={p.id} value={p.id}>{CATEGORY_LABEL[p.category]} {p.name}</option>
                ))}
              </select>
              <input required placeholder="닉네임"
                value={shareForm.sharedBy}
                onChange={e => setShareForm(f => ({ ...f, sharedBy: e.target.value }))}
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
              />
              <button type="submit"
                className="w-full bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl py-2.5 text-sm font-medium transition">
                공유하기
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

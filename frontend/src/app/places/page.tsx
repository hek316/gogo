'use client';

import { useEffect, useRef, useState } from 'react';
import { getPlaces, markVisited, deletePlace, Place } from '@/lib/api/places';
import AddPlaceForm from '@/components/AddPlaceForm';
import { useAuth } from '@/lib/auth/AuthContext';
import { MapPin, MoreHorizontal } from 'lucide-react';

const CATEGORY_LABEL: Record<string, string> = {
  CAFE: '카페',
  RESTAURANT: '식당',
  BAR: '바/펍',
  ACTIVITY: '액티비티',
  ETC: '기타',
};

const CATEGORY_GRADIENT: Record<string, string> = {
  CAFE:       'from-[#8B5E3C] to-[#C8936C]',
  RESTAURANT: 'from-[#E8593A] to-[#F5A574]',
  BAR:        'from-[#2D264B] to-[#6B5B9E]',
  ACTIVITY:   'from-[#2E7D32] to-[#66BB6A]',
  ETC:        'from-[#9D8DC2] to-[#C4B8E0]',
};

type StatusFilter = 'ALL' | 'WANT_TO_GO' | 'VISITED';

function PlaceListItem({
  place,
  onVisit,
  onDelete,
}: {
  place: Place;
  onVisit: (id: number) => void;
  onDelete: (id: number) => void;
}) {
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);
  const gradient = CATEGORY_GRADIENT[place.category] ?? CATEGORY_GRADIENT.ETC;

  useEffect(() => {
    if (!menuOpen) return;
    const handler = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, [menuOpen]);

  return (
    <div className="bg-white rounded-2xl border border-[rgba(45,38,75,0.07)] shadow-[0_2px_8px_rgba(45,38,75,0.04)] flex items-stretch overflow-hidden">
      {/* 카테고리 색상 사각형 */}
      <div className={`w-14 flex-shrink-0 bg-gradient-to-br ${gradient} flex items-center justify-center`}>
        <MapPin size={18} strokeWidth={1.5} className="text-white/80" />
      </div>

      {/* 내용 */}
      <div className="flex-1 min-w-0 px-4 py-3">
        <div className="flex items-start justify-between gap-2">
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 mb-0.5">
              <h3 className="font-semibold text-[#2D264B] text-sm truncate">{place.name}</h3>
              <span className={`flex-shrink-0 text-[9px] font-bold px-1.5 py-0.5 rounded-full ${
                place.status === 'VISITED'
                  ? 'bg-[#2D264B] text-white'
                  : 'bg-[#EFEDF7] text-[#9D8DC2]'
              }`}>
                {place.status === 'VISITED' ? '방문완료' : '가고싶어'}
              </span>
            </div>
            {place.address && (
              <p className="text-xs text-[rgba(45,38,75,0.4)] truncate">{place.address}</p>
            )}
            {place.note && (
              <p className="text-xs text-[rgba(45,38,75,0.35)] mt-1 truncate">{place.note}</p>
            )}
          </div>

          {/* ··· 메뉴 */}
          <div ref={menuRef} className="relative flex-shrink-0">
            <button
              onClick={() => setMenuOpen(v => !v)}
              className="w-7 h-7 flex items-center justify-center rounded-full hover:bg-[#EFEDF7] transition-colors"
            >
              <MoreHorizontal size={15} strokeWidth={1.5} stroke="rgba(45,38,75,0.4)" />
            </button>
            {menuOpen && (
              <div className="absolute right-0 top-8 z-20 bg-white rounded-xl shadow-[0_8px_32px_rgba(45,38,75,0.15)] border border-[rgba(45,38,75,0.08)] py-1 min-w-[140px]">
                {place.status === 'WANT_TO_GO' && (
                  <button
                    onClick={() => { setMenuOpen(false); onVisit(place.id); }}
                    className="w-full text-left text-xs px-4 py-2.5 text-[#2D264B] hover:bg-[#EFEDF7] transition-colors"
                  >
                    방문완료로 변경
                  </button>
                )}
                <button
                  onClick={() => { setMenuOpen(false); onDelete(place.id); }}
                  className="w-full text-left text-xs px-4 py-2.5 text-red-400 hover:bg-red-50 transition-colors"
                >
                  삭제
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default function PlacesPage() {
  const { user } = useAuth();
  const [places, setPlaces] = useState<Place[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');

  const load = async () => {
    setLoading(true);
    try {
      const data = await getPlaces();
      setPlaces(data);
    } catch {
      setPlaces([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleVisit = async (id: number) => {
    if (!user) {
      window.location.href = '/auth/login';
      return;
    }
    try {
      const updated = await markVisited(id);
      setPlaces(ps => ps.map(p => p.id === id ? updated : p));
    } catch {
      alert('방문 완료 처리에 실패했습니다. 잠시 후 다시 시도해주세요.');
    }
  };

  const handleDelete = async (id: number) => {
    if (!user) {
      window.location.href = '/auth/login';
      return;
    }
    if (!confirm('삭제할까요?')) return;
    try {
      await deletePlace(id);
      setPlaces(ps => ps.filter(p => p.id !== id));
    } catch {
      alert('삭제에 실패했습니다. 잠시 후 다시 시도해주세요.');
    }
  };

  const filtered = statusFilter === 'ALL'
    ? places
    : places.filter(p => p.status === statusFilter);

  const wantCount = places.filter(p => p.status === 'WANT_TO_GO').length;
  const visitedCount = places.filter(p => p.status === 'VISITED').length;

  return (
    <div className="min-h-screen bg-[#F8F7FB]">
      <header className="bg-white border-b border-[rgba(45,38,75,0.08)] sticky top-0 z-10">
        <div className="max-w-2xl mx-auto px-5 py-4 flex items-center justify-between">
          <h1 className="text-lg font-bold text-[#2D264B] tracking-tight">내 장소</h1>
          <span className="text-xs text-[rgba(45,38,75,0.4)]">총 {places.length}개</span>
        </div>
      </header>

      <main className="max-w-2xl mx-auto px-4 py-5">
        {/* 통계 바 */}
        {!loading && places.length > 0 && (
          <div className="grid grid-cols-3 gap-2 mb-5">
            {[
              { label: '전체', count: places.length, color: 'text-[#2D264B]', bg: 'bg-white' },
              { label: '가고싶어', count: wantCount, color: 'text-[#9D8DC2]', bg: 'bg-[#EFEDF7]' },
              { label: '방문완료', count: visitedCount, color: 'text-[#2D264B]', bg: 'bg-[#2D264B]/5' },
            ].map(({ label, count, color, bg }) => (
              <div key={label} className={`${bg} rounded-2xl px-4 py-3 border border-[rgba(45,38,75,0.07)]`}>
                <p className={`text-xl font-bold ${color}`}>{count}</p>
                <p className="text-[10px] text-[rgba(45,38,75,0.4)] mt-0.5">{label}</p>
              </div>
            ))}
          </div>
        )}

        {/* 상태 탭 */}
        <div className="flex gap-2 mb-5">
          {([
            { value: 'ALL', label: '전체' },
            { value: 'WANT_TO_GO', label: '가고싶어' },
            { value: 'VISITED', label: '방문완료' },
          ] as { value: StatusFilter; label: string }[]).map(tab => (
            <button
              key={tab.value}
              onClick={() => setStatusFilter(tab.value)}
              className={`px-4 py-2 rounded-full text-xs font-semibold transition-all ${
                statusFilter === tab.value
                  ? 'bg-[#2D264B] text-white shadow-sm'
                  : 'bg-white text-[rgba(45,38,75,0.45)] border border-[rgba(45,38,75,0.1)] hover:border-[#9D8DC2]'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* 리스트 */}
        {loading ? (
          <div className="flex justify-center py-20">
            <div className="w-8 h-8 border-[3px] border-[rgba(157,141,194,0.2)] border-t-[#9D8DC2] rounded-full animate-spin" />
          </div>
        ) : filtered.length === 0 ? (
          <div className="text-center py-20">
            <div className="text-5xl mb-4">🗺️</div>
            <p className="font-semibold text-[#2D264B] tracking-[-0.02em]">
              {statusFilter === 'ALL' ? '아직 저장한 장소가 없어요' : '해당 장소가 없어요'}
            </p>
            <p className="text-sm text-[rgba(45,38,75,0.4)] mt-1">
              {statusFilter === 'ALL' ? '+ 버튼으로 첫 장소를 추가해보세요!' : '다른 탭을 확인해보세요'}
            </p>
          </div>
        ) : (
          <div className="flex flex-col gap-2.5 pb-28">
            {filtered.map(place => (
              <PlaceListItem
                key={place.id}
                place={place}
                onVisit={handleVisit}
                onDelete={handleDelete}
              />
            ))}
          </div>
        )}
      </main>

      <AddPlaceForm onAdded={place => setPlaces(ps => [place, ...ps])} />
    </div>
  );
}

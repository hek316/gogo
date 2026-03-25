'use client';

import { useEffect, useRef, useState } from 'react';
import { getPlaces, markVisited, deletePlace, Place } from '@/lib/api/places';
import AddPlaceForm from '@/components/AddPlaceForm';
import { useAuth } from '@/lib/auth/AuthContext';
import { MapPin, MoreHorizontal } from 'lucide-react';
import { CATEGORY_GRADIENT } from '@/lib/constants/categories';

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
    <div className="bg-bg rounded-2xl border border-border shadow-sm flex items-stretch overflow-hidden">
      <div className={`w-14 flex-shrink-0 bg-gradient-to-br ${gradient} flex items-center justify-center`}>
        <MapPin size={18} strokeWidth={1.5} className="text-white/80" />
      </div>

      <div className="flex-1 min-w-0 px-4 py-3">
        <div className="flex items-start justify-between gap-2">
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 mb-0.5">
              <h3 className="font-semibold text-text-main text-sm truncate">{place.name}</h3>
              <span className={`flex-shrink-0 text-[9px] font-bold px-1.5 py-0.5 rounded-full ${
                place.status === 'VISITED'
                  ? 'bg-text-main text-text-on-primary'
                  : 'bg-surface text-primary'
              }`}>
                {place.status === 'VISITED' ? '방문완료' : '가고싶어'}
              </span>
            </div>
            {place.address && (
              <p className="text-xs text-text-muted truncate">{place.address}</p>
            )}
            {place.note && (
              <p className="text-xs text-text-muted mt-1 truncate">{place.note}</p>
            )}
          </div>

          <div ref={menuRef} className="relative flex-shrink-0">
            <button
              onClick={() => setMenuOpen(v => !v)}
              className="w-7 h-7 flex items-center justify-center rounded-full hover:bg-surface transition-colors"
            >
              <MoreHorizontal size={15} strokeWidth={1.5} className="text-text-muted" />
            </button>
            {menuOpen && (
              <div className="absolute right-0 top-8 z-20 bg-bg rounded-xl shadow-lg border border-border py-1 min-w-[140px]">
                {place.status === 'WANT_TO_GO' && (
                  <button
                    onClick={() => { setMenuOpen(false); onVisit(place.id); }}
                    className="w-full text-left text-xs px-4 py-2.5 text-text-main hover:bg-surface transition-colors"
                  >
                    방문완료로 변경
                  </button>
                )}
                <button
                  onClick={() => { setMenuOpen(false); onDelete(place.id); }}
                  className="w-full text-left text-xs px-4 py-2.5 text-danger hover:bg-danger-bg transition-colors"
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
    <div className="min-h-screen bg-bg">
      <header className="bg-bg border-b border-border sticky top-0 z-10">
        <div className="max-w-2xl mx-auto px-5 py-4 flex items-center justify-between">
          <h1 className="text-lg font-bold text-text-main tracking-tight">내 장소</h1>
          <span className="text-xs text-text-muted">총 {places.length}개</span>
        </div>
      </header>

      <main className="max-w-2xl mx-auto px-4 py-5">
        {!loading && places.length > 0 && (
          <div className="grid grid-cols-3 gap-2 mb-5">
            {[
              { label: '전체', count: places.length, color: 'text-text-main', bg: 'bg-bg-secondary' },
              { label: '가고싶어', count: wantCount, color: 'text-primary', bg: 'bg-surface' },
              { label: '방문완료', count: visitedCount, color: 'text-text-main', bg: 'bg-bg-secondary' },
            ].map(({ label, count, color, bg }) => (
              <div key={label} className={`${bg} rounded-2xl px-4 py-3 border border-border`}>
                <p className={`text-xl font-bold ${color}`}>{count}</p>
                <p className="text-[10px] text-text-muted mt-0.5">{label}</p>
              </div>
            ))}
          </div>
        )}

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
                  ? 'bg-text-main text-text-on-primary shadow-sm'
                  : 'bg-bg text-text-muted border border-border hover:border-primary'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {loading ? (
          <div className="flex justify-center py-20">
            <div className="w-8 h-8 border-[3px] border-surface border-t-primary rounded-full animate-spin" />
          </div>
        ) : filtered.length === 0 ? (
          <div className="text-center py-20">
            <div className="text-5xl mb-4">🗺️</div>
            <p className="font-semibold text-text-main tracking-[-0.02em]">
              {statusFilter === 'ALL' ? '아직 저장한 장소가 없어요' : '해당 장소가 없어요'}
            </p>
            <p className="text-sm text-text-muted mt-1">
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

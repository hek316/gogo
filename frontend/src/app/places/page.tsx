'use client';

import { useEffect, useState } from 'react';
import { getPlaces, markVisited, deletePlace, Place } from '@/lib/api/places';
import AddPlaceForm from '@/components/AddPlaceForm';
import { MapPin } from 'lucide-react';

const CATEGORY_LABEL: Record<string, string> = {
  CAFE: '카페',
  RESTAURANT: '식당',
  BAR: '바/펍',
  ACTIVITY: '액티비티',
  ETC: '기타',
};

export default function PlacesPage() {
  const [places, setPlaces] = useState<Place[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('');

  const load = async (category?: string) => {
    setLoading(true);
    try {
      const data = await getPlaces(category || undefined);
      setPlaces(data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(filter); }, [filter]);

  const handleVisit = async (id: number) => {
    const updated = await markVisited(id);
    setPlaces(ps => ps.map(p => p.id === id ? updated : p));
  };

  const handleDelete = async (id: number) => {
    if (!confirm('삭제할까요?')) return;
    await deletePlace(id);
    setPlaces(ps => ps.filter(p => p.id !== id));
  };

  return (
    <div className="min-h-screen bg-bg">
      <header className="bg-white border-b border-border sticky top-0 z-10">
        <div className="max-w-2xl mx-auto px-6 py-5 flex items-center justify-between">
          <h1 className="text-xl font-bold text-text-main tracking-tight">GoGo</h1>
          <span className="text-sm text-text-muted">가고 싶은 장소 기록</span>
        </div>
      </header>

      <main className="max-w-2xl mx-auto px-4 py-6">
        {/* Category filter */}
        <div className="flex gap-2 overflow-x-auto pb-2 mb-6">
          {[{ value: '', label: '전체' }, ...Object.entries(CATEGORY_LABEL).map(([v, l]) => ({ value: v, label: l }))].map(c => (
            <button
              key={c.value}
              onClick={() => setFilter(c.value)}
              className={`flex-shrink-0 px-4 py-1.5 rounded-full text-sm font-medium transition ${
                filter === c.value
                  ? 'bg-green text-white'
                  : 'bg-white text-text-muted border border-border hover:border-mint'
              }`}
            >
              {c.label}
            </button>
          ))}
        </div>

        {/* Place list */}
        {loading ? (
          <div className="flex justify-center py-20">
            <div className="w-8 h-8 border-4 border-surface border-t-mint rounded-full animate-spin" />
          </div>
        ) : places.length === 0 ? (
          <div className="text-center py-20 text-text-muted">
            <div className="flex justify-center mb-3">
              <MapPin size={40} strokeWidth={1.5} className="text-mint" />
            </div>
            <p className="font-medium">아직 등록된 장소가 없어요</p>
            <p className="text-sm mt-1">아래 + 버튼으로 장소를 추가해보세요</p>
          </div>
        ) : (
          <div className="grid grid-cols-2 gap-3">
            {places.map((place, index) => (
              <div
                key={place.id}
                className={`bg-white rounded-[20px] p-4 border border-border hover:shadow-[0_4px_16px_rgba(0,212,170,0.15)] transition-all duration-300 ${
                  index === 0 ? 'col-span-2 p-6' : ''
                }`}
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-xs text-text-muted">{CATEGORY_LABEL[place.category] ?? place.category}</span>
                      {place.status === 'VISITED' && (
                        <span className="text-xs bg-mint text-white px-2 py-0.5 rounded-full font-medium">방문완료</span>
                      )}
                      {place.status === 'WANT_TO_GO' && (
                        <span className="text-xs bg-surface text-green px-2 py-0.5 rounded-full font-medium">가고싶어</span>
                      )}
                    </div>
                    <h3 className="font-semibold text-text-main truncate">{place.name}</h3>
                    {place.address && <p className="text-sm text-text-muted mt-0.5 truncate">{place.address}</p>}
                    {index === 0 && place.note && <p className="text-sm text-text-muted mt-1 line-clamp-2">{place.note}</p>}
                    <p className="text-xs text-text-muted mt-2">by {place.createdBy}</p>
                  </div>
                  <div className="flex flex-col gap-1.5 flex-shrink-0">
                    {place.status === 'WANT_TO_GO' && (
                      <button
                        onClick={() => handleVisit(place.id)}
                        className="text-xs bg-green hover:bg-green-mid text-white px-3 py-1.5 rounded-lg font-medium"
                      >
                        방문완료
                      </button>
                    )}
                    <button
                      onClick={() => handleDelete(place.id)}
                      className="text-xs bg-red-50 hover:bg-red-100 text-red-400 px-3 py-1.5 rounded-lg"
                    >
                      삭제
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      <AddPlaceForm onAdded={place => setPlaces(ps => [place, ...ps])} />
    </div>
  );
}

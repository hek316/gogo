'use client';

import { useEffect, useState } from 'react';
import { getPlaces, markVisited, deletePlace, Place } from '@/lib/api/places';
import AddPlaceForm from '@/components/AddPlaceForm';

const CATEGORY_LABEL: Record<string, string> = {
  CAFE: '☕ 카페',
  RESTAURANT: '🍽️ 식당',
  BAR: '🍺 바/펍',
  ACTIVITY: '🎯 액티비티',
  ETC: '📍 기타',
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
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b border-gray-100 sticky top-0 z-10">
        <div className="max-w-2xl mx-auto px-4 py-4 flex items-center justify-between">
          <h1 className="text-xl font-bold text-indigo-600">🗺️ GoGo</h1>
          <span className="text-sm text-gray-500">가고 싶은 장소 기록</span>
        </div>
      </header>

      <main className="max-w-2xl mx-auto px-4 py-6">
        {/* Category filter */}
        <div className="flex gap-2 overflow-x-auto pb-2 mb-6 scrollbar-hide">
          {[{ value: '', label: '전체' }, ...Object.entries(CATEGORY_LABEL).map(([v, l]) => ({ value: v, label: l }))].map(c => (
            <button
              key={c.value}
              onClick={() => setFilter(c.value)}
              className={`flex-shrink-0 px-4 py-1.5 rounded-full text-sm font-medium transition ${
                filter === c.value
                  ? 'bg-indigo-600 text-white'
                  : 'bg-white text-gray-600 border border-gray-200 hover:border-indigo-300'
              }`}
            >
              {c.label}
            </button>
          ))}
        </div>

        {/* Place list */}
        {loading ? (
          <div className="flex justify-center py-20">
            <div className="w-8 h-8 border-4 border-indigo-200 border-t-indigo-600 rounded-full animate-spin" />
          </div>
        ) : places.length === 0 ? (
          <div className="text-center py-20 text-gray-400">
            <p className="text-4xl mb-3">📍</p>
            <p className="font-medium">아직 등록된 장소가 없어요</p>
            <p className="text-sm mt-1">아래 + 버튼으로 장소를 추가해보세요</p>
          </div>
        ) : (
          <div className="space-y-3">
            {places.map(place => (
              <div key={place.id} className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-xs text-gray-400">{CATEGORY_LABEL[place.category] ?? place.category}</span>
                      {place.status === 'VISITED' && (
                        <span className="text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded-full font-medium">방문완료</span>
                      )}
                    </div>
                    <h3 className="font-semibold text-gray-900 truncate">{place.name}</h3>
                    {place.address && <p className="text-sm text-gray-500 mt-0.5 truncate">{place.address}</p>}
                    {place.note && <p className="text-sm text-gray-400 mt-1 line-clamp-2">{place.note}</p>}
                    <p className="text-xs text-gray-300 mt-2">by {place.createdBy}</p>
                  </div>
                  <div className="flex flex-col gap-1.5 flex-shrink-0">
                    {place.status === 'WANT_TO_GO' && (
                      <button
                        onClick={() => handleVisit(place.id)}
                        className="text-xs bg-indigo-50 hover:bg-indigo-100 text-indigo-600 px-3 py-1.5 rounded-lg transition"
                      >
                        방문완료
                      </button>
                    )}
                    <button
                      onClick={() => handleDelete(place.id)}
                      className="text-xs bg-red-50 hover:bg-red-100 text-red-400 px-3 py-1.5 rounded-lg transition"
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

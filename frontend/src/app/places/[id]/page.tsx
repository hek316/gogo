'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getPlaces, markVisited, Place } from '@/lib/api/places';
import { getReviews, addReview, Review } from '@/lib/api/reviews';

const STARS = [1, 2, 3, 4, 5];

export default function PlaceDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const placeId = Number(id);

  const [place, setPlace] = useState<Place | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ authorName: '', rating: 5, content: '', visitedAt: new Date().toISOString().slice(0, 10) });
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    Promise.all([getPlaces(), getReviews(placeId)])
      .then(([places, revs]) => {
        setPlace(places.find(p => p.id === placeId) || null);
        setReviews(revs);
      })
      .finally(() => setLoading(false));
  }, [placeId]);

  const handleVisit = async () => {
    if (!place) return;
    const updated = await markVisited(place.id);
    setPlace(updated);
  };

  const handleReview = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const r = await addReview(placeId, form);
      setReviews(prev => [r, ...prev]);
      setShowForm(false);
      setForm({ authorName: '', rating: 5, content: '', visitedAt: new Date().toISOString().slice(0, 10) });
    } catch (err: any) {
      alert(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const avgRating = reviews.length > 0
    ? (reviews.reduce((s, r) => s + r.rating, 0) / reviews.length).toFixed(1)
    : null;

  if (loading) return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="w-8 h-8 border-4 border-indigo-200 border-t-indigo-600 rounded-full animate-spin" />
    </div>
  );

  if (!place) return <div className="p-8 text-center text-gray-400">장소를 찾을 수 없습니다.</div>;

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b border-gray-100 sticky top-0 z-10">
        <div className="max-w-2xl mx-auto px-4 py-4 flex items-center gap-3">
          <button onClick={() => router.back()} className="text-gray-400 hover:text-gray-600">←</button>
          <h1 className="text-lg font-bold text-gray-900 flex-1 truncate">{place.name}</h1>
          {place.status === 'WANT_TO_GO' && (
            <button onClick={handleVisit}
              className="text-sm bg-indigo-50 hover:bg-indigo-100 text-indigo-600 px-3 py-1.5 rounded-xl transition">
              방문완료
            </button>
          )}
          {place.status === 'VISITED' && (
            <span className="text-xs bg-green-100 text-green-700 px-2 py-1 rounded-full font-medium">방문완료</span>
          )}
        </div>
      </header>

      <main className="max-w-2xl mx-auto px-4 py-6 space-y-5">
        {/* 장소 정보 */}
        <div className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
          {place.address && <p className="text-gray-500 mb-1">📍 {place.address}</p>}
          {place.note && <p className="text-gray-600 text-sm mt-2">{place.note}</p>}
          {avgRating && (
            <div className="flex items-center gap-1 mt-3">
              <span className="text-yellow-400">★</span>
              <span className="font-bold text-gray-800">{avgRating}</span>
              <span className="text-gray-400 text-sm">({reviews.length}개 후기)</span>
            </div>
          )}
        </div>

        {/* 후기 섹션 */}
        <div>
          <div className="flex items-center justify-between mb-3">
            <h2 className="font-semibold text-gray-700">후기 ({reviews.length})</h2>
            <button onClick={() => setShowForm(s => !s)}
              className="text-sm bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-1.5 rounded-xl transition">
              {showForm ? '취소' : '+ 후기 작성'}
            </button>
          </div>

          {showForm && (
            <form onSubmit={handleReview} className="bg-indigo-50 rounded-2xl p-4 mb-4 space-y-3">
              <input required placeholder="닉네임"
                value={form.authorName}
                onChange={e => setForm(f => ({ ...f, authorName: e.target.value }))}
                className="w-full border border-indigo-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400 bg-white" />
              <div className="flex gap-1">
                {STARS.map(s => (
                  <button key={s} type="button" onClick={() => setForm(f => ({ ...f, rating: s }))}
                    className={`text-2xl transition ${s <= form.rating ? 'text-yellow-400' : 'text-gray-200'}`}>
                    ★
                  </button>
                ))}
              </div>
              <textarea placeholder="후기를 작성해주세요"
                value={form.content}
                onChange={e => setForm(f => ({ ...f, content: e.target.value }))}
                rows={3}
                className="w-full border border-indigo-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400 resize-none bg-white" />
              <input type="date"
                value={form.visitedAt}
                onChange={e => setForm(f => ({ ...f, visitedAt: e.target.value }))}
                className="w-full border border-indigo-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400 bg-white" />
              <button type="submit" disabled={submitting}
                className="w-full bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl py-2 text-sm font-medium transition disabled:opacity-50">
                {submitting ? '제출 중...' : '후기 등록'}
              </button>
            </form>
          )}

          {reviews.length === 0 ? (
            <div className="bg-white rounded-2xl p-8 text-center text-gray-400 border border-gray-100">
              <p className="text-3xl mb-2">📝</p>
              <p className="text-sm">아직 후기가 없어요</p>
            </div>
          ) : (
            <div className="space-y-3">
              {reviews.map(r => (
                <div key={r.id} className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100">
                  <div className="flex items-center justify-between mb-2">
                    <span className="font-medium text-gray-800">{r.authorName}</span>
                    <div className="flex items-center gap-1">
                      {STARS.map(s => (
                        <span key={s} className={`text-sm ${s <= r.rating ? 'text-yellow-400' : 'text-gray-200'}`}>★</span>
                      ))}
                    </div>
                  </div>
                  {r.content && <p className="text-sm text-gray-600">{r.content}</p>}
                  <p className="text-xs text-gray-300 mt-2">{r.visitedAt} 방문</p>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

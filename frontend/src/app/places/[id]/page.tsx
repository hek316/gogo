'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getPlaces, markVisited, Place } from '@/lib/api/places';
import { getReviews, addReview, Review } from '@/lib/api/reviews';
import { ChevronLeft, Inbox, ExternalLink } from 'lucide-react';

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
    <div className="min-h-screen flex items-center justify-center bg-bg">
      <div className="w-8 h-8 border-4 border-surface border-t-mint rounded-full animate-spin" />
    </div>
  );

  if (!place) return <div className="p-8 text-center text-text-muted">장소를 찾을 수 없습니다.</div>;

  return (
    <div className="min-h-screen bg-bg">
      <header className="bg-white border-b border-border sticky top-0 z-10">
        <div className="max-w-2xl mx-auto px-4 py-4 flex items-center gap-3">
          <button onClick={() => router.back()} className="text-text-muted hover:text-text-main">
            <ChevronLeft size={20} strokeWidth={1.5} />
          </button>
          <h1 className="text-lg font-semibold text-text-main flex-1 truncate">{place.name}</h1>
          {place.status === 'WANT_TO_GO' && (
            <button onClick={handleVisit}
              className="text-sm bg-green hover:bg-green-mid text-white px-3 py-1.5 rounded-[16px] font-medium">
              방문완료
            </button>
          )}
          {place.status === 'VISITED' && (
            <span className="text-xs bg-mint text-white px-2 py-1 rounded-full font-medium">방문완료</span>
          )}
        </div>
      </header>

      <main className="max-w-2xl mx-auto px-4 py-6 space-y-5">
        {/* Hero image or category gradient */}
        {(() => {
          const CATEGORY_GRADIENT: Record<string, string> = {
            CAFE: 'from-[#8B5E3C] to-[#C8936C]',
            RESTAURANT: 'from-[#E8593A] to-[#F5A574]',
            BAR: 'from-[#2D264B] to-[#6B5B9E]',
            ACTIVITY: 'from-[#2E7D32] to-[#66BB6A]',
            ETC: 'from-[#9D8DC2] to-[#C4B8E0]',
          };
          const gradient = CATEGORY_GRADIENT[place.category] ?? CATEGORY_GRADIENT.ETC;
          return place.imageUrl ? (
            <div className="w-full h-48 rounded-[20px] overflow-hidden shadow-sm">
              <img src={place.imageUrl} alt={place.name} className="w-full h-full object-cover" />
            </div>
          ) : (
            <div className={`w-full h-48 rounded-[20px] bg-gradient-to-br ${gradient} shadow-sm`} />
          );
        })()}

        {/* 장소 정보 */}
        <div className="bg-white rounded-[20px] p-6 border border-border shadow-sm">
          {place.address && <p className="text-text-muted mb-1">{place.address}</p>}
          {place.note && <p className="text-text-main text-sm mt-2">{place.note}</p>}
          {avgRating && (
            <div className="flex items-center gap-1 mt-3">
              <span className="text-mint">★</span>
              <span className="font-semibold text-text-main">{avgRating}</span>
              <span className="text-text-muted text-sm">({reviews.length}개 후기)</span>
            </div>
          )}
          {place.url && (
            <a
              href={place.url}
              target="_blank"
              rel="noopener noreferrer"
              className="mt-3 inline-flex items-center gap-1.5 text-sm text-mint font-medium hover:underline"
            >
              <ExternalLink size={14} />
              지도에서 보기
            </a>
          )}
        </div>

        {/* 후기 섹션 */}
        <div>
          <div className="flex items-center justify-between mb-3">
            <h2 className="font-semibold text-text-main">후기 ({reviews.length})</h2>
            <button onClick={() => setShowForm(s => !s)}
              className="text-sm bg-green hover:bg-green-mid text-white px-4 py-1.5 rounded-[16px] font-medium">
              {showForm ? '취소' : '+ 후기 작성'}
            </button>
          </div>

          {showForm && (
            <div className="bg-surface rounded-[20px] p-6 mb-4 border border-border">
              <form onSubmit={handleReview} className="space-y-3">
                <input required placeholder="닉네임"
                  value={form.authorName}
                  onChange={e => setForm(f => ({ ...f, authorName: e.target.value }))}
                  className="w-full border border-border rounded-[12px] px-5 py-3 text-sm focus:outline-none focus:ring-1 focus:ring-mint focus:border-mint bg-white" />
                <div className="flex gap-1">
                  {STARS.map(s => (
                    <button key={s} type="button" onClick={() => setForm(f => ({ ...f, rating: s }))}
                      className={`text-2xl transition ${s <= form.rating ? 'text-mint' : 'text-border'}`}>
                      ★
                    </button>
                  ))}
                </div>
                <textarea placeholder="후기를 작성해주세요"
                  value={form.content}
                  onChange={e => setForm(f => ({ ...f, content: e.target.value }))}
                  rows={3}
                  className="w-full border border-border rounded-[12px] px-5 py-3 text-sm focus:outline-none focus:ring-1 focus:ring-mint focus:border-mint resize-none bg-white" />
                <input type="date"
                  value={form.visitedAt}
                  onChange={e => setForm(f => ({ ...f, visitedAt: e.target.value }))}
                  className="w-full border border-border rounded-[12px] px-5 py-3 text-sm focus:outline-none focus:ring-1 focus:ring-mint focus:border-mint bg-white" />
                <button type="submit" disabled={submitting}
                  className="w-full bg-green hover:bg-green-mid text-white rounded-[16px] py-3 text-sm font-medium disabled:opacity-50">
                  {submitting ? '제출 중...' : '후기 등록'}
                </button>
              </form>
            </div>
          )}

          {reviews.length === 0 ? (
            <div className="bg-white rounded-[20px] p-8 text-center text-text-muted border border-border">
              <div className="flex justify-center mb-2">
                <Inbox size={32} strokeWidth={1.5} className="text-text-muted" />
              </div>
              <p className="text-sm">아직 후기가 없어요</p>
            </div>
          ) : (
            <div className="space-y-3">
              {reviews.map(r => (
                <div key={r.id} className="bg-white rounded-[20px] p-6 border border-border shadow-sm">
                  <div className="flex items-center justify-between mb-2">
                    <span className="font-semibold text-text-main">{r.authorName}</span>
                    <div className="flex items-center gap-0.5">
                      {STARS.map(s => (
                        <span key={s} className={`text-sm ${s <= r.rating ? 'text-mint' : 'text-border'}`}>★</span>
                      ))}
                    </div>
                  </div>
                  {r.content && <p className="text-sm text-text-main">{r.content}</p>}
                  <p className="text-xs text-text-muted mt-2">{r.visitedAt} 방문</p>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

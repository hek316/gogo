'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { MapPin, Compass, ArrowRight } from 'lucide-react';
import { getPopularPlaces, getRecentPlaces, Place } from '@/lib/api/places';
import LikeButton from '@/components/LikeButton';
import { CATEGORY_LABEL, CATEGORY_GRADIENT, CATEGORIES } from '@/lib/constants/categories';

type SortMode = 'recent' | 'popular';

export default function ExplorePage() {
  const [sort, setSort] = useState<SortMode>('recent');
  const [category, setCategory] = useState('');
  const [places, setPlaces] = useState<Place[]>([]);
  const [loading, setLoading] = useState(true);

  const load = async (mode: SortMode) => {
    setLoading(true);
    try {
      const data = mode === 'popular'
        ? await getPopularPlaces(30)
        : await getRecentPlaces(30);
      setPlaces(data);
    } catch {
      setPlaces([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(sort); }, [sort]);

  const filtered = category
    ? places.filter(p => p.category === category)
    : places;

  return (
    <div className="min-h-screen bg-bg">
      {/* Header */}
      <div className="max-w-2xl mx-auto px-4 pt-10 pb-4">
        <div className="flex items-center gap-2 mb-1">
          <Compass size={18} strokeWidth={1} className="text-primary" />
          <p className="text-[10px] font-semibold text-primary tracking-[0.14em] uppercase">Explore</p>
        </div>
        <h1 className="text-2xl font-bold text-text-main tracking-[-0.04em]">장소 탐색</h1>
        <p className="text-sm text-text-muted mt-1 tracking-[-0.02em]">
          모든 장소를 자유롭게 둘러보세요
        </p>
      </div>

      {/* Sort tabs + Category filter (sticky) */}
      <div className="sticky top-0 z-10 bg-bg/95 backdrop-blur-sm border-b border-border">
        <div className="max-w-2xl mx-auto px-4 pt-3 pb-3">
          {/* Sort tabs */}
          <div className="inline-flex bg-surface rounded-2xl p-1 gap-1 mb-3">
            {(['recent', 'popular'] as SortMode[]).map(mode => (
              <button
                key={mode}
                onClick={() => setSort(mode)}
                className={`px-4 py-1.5 rounded-xl text-xs font-semibold transition-all ${
                  sort === mode
                    ? 'bg-text-main text-text-on-primary shadow-sm'
                    : 'text-text-muted hover:text-text-main'
                }`}
              >
                {mode === 'recent' ? '최신순' : '인기순'}
              </button>
            ))}
          </div>

          {/* Category filter */}
          <div className="flex gap-2 overflow-x-auto pb-0.5 scrollbar-hide">
            {CATEGORIES.map(c => (
              <button
                key={c.value}
                onClick={() => setCategory(c.value)}
                className={`flex-shrink-0 px-3.5 py-1.5 rounded-full text-xs font-medium transition-all border ${
                  category === c.value
                    ? 'bg-primary text-text-on-primary border-primary shadow-md'
                    : 'bg-bg text-text-muted border-border hover:border-primary'
                }`}
              >
                {c.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Place grid */}
      <main className="max-w-2xl mx-auto px-4 pt-5 pb-28">
        {loading ? (
          <div className="flex justify-center py-20">
            <div className="w-8 h-8 border-[3px] border-surface border-t-primary rounded-full animate-spin" />
          </div>
        ) : filtered.length === 0 ? (
          <div className="text-center py-20">
            <div className="w-14 h-14 rounded-full bg-surface flex items-center justify-center mx-auto mb-4">
              <MapPin size={24} strokeWidth={1} className="text-primary" />
            </div>
            <p className="font-semibold text-text-main tracking-[-0.02em]">아직 장소가 없어요</p>
            <p className="text-sm text-text-muted mt-1">첫 번째로 장소를 등록해보세요</p>
            <Link
              href="/places"
              className="mt-4 inline-flex items-center gap-1.5 bg-primary text-text-on-primary text-xs font-semibold px-4 py-2.5 rounded-full"
            >
              장소 등록하기 <ArrowRight size={11} strokeWidth={1} />
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-2 gap-3">
            {filtered.map(place => {
              const gradient = CATEGORY_GRADIENT[place.category] ?? CATEGORY_GRADIENT.ETC;
              return (
                <div key={place.id} className="group bg-bg rounded-[20px] border border-border shadow-sm hover:-translate-y-0.5 hover:shadow-md transition-all duration-200 overflow-hidden">
                  <Link href={`/places/${place.id}`} className="block">
                    <div className="relative h-[72px]">
                      {place.imageUrl ? (
                        <img src={place.imageUrl} alt={place.name} className="w-full h-full object-cover" />
                      ) : (
                        <div className={`w-full h-full bg-gradient-to-br ${gradient}`} />
                      )}
                      <span className="absolute top-2 left-2 bg-black/25 backdrop-blur-sm text-white text-[9px] font-semibold px-2 py-0.5 rounded-full">
                        {CATEGORY_LABEL[place.category] ?? place.category}
                      </span>
                    </div>
                  </Link>

                  <div className="px-3 py-2.5">
                    <Link href={`/places/${place.id}`}>
                      <h3 className="font-semibold text-text-main text-xs tracking-[-0.02em] truncate">
                        {place.name}
                      </h3>
                      {place.address && (
                        <p className="text-[10px] text-text-muted mt-0.5 truncate">
                          {place.address}
                        </p>
                      )}
                    </Link>

                    <div className="flex items-center justify-between mt-2">
                      <p className="text-[9px] text-text-muted">by {place.createdBy}</p>
                      <LikeButton
                        placeId={place.id}
                        initialLiked={place.isLiked}
                        initialCount={place.likeCount}
                        size="sm"
                      />
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {/* CTA */}
        {!loading && filtered.length > 0 && (
          <div className="mt-8 bg-text-main rounded-[24px] px-6 py-5 text-center">
            <p className="text-white font-semibold text-sm tracking-[-0.02em]">나도 장소 기록하기</p>
            <p className="text-white/50 text-xs mt-1">로그인 후 장소를 저장하고 좋아요를 눌러보세요</p>
            <Link
              href="/places"
              className="mt-3 inline-flex items-center gap-1.5 bg-primary text-text-on-primary text-xs font-semibold px-4 py-2 rounded-full hover:shadow-lg transition"
            >
              시작하기 <ArrowRight size={10} strokeWidth={1} />
            </Link>
          </div>
        )}
      </main>
    </div>
  );
}

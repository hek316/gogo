import Link from 'next/link';
import { MapPin, Users, ArrowRight, Compass, Heart } from 'lucide-react';
import { getPopularPlaces, Place } from '@/lib/api/places';
import { CATEGORY_GRADIENT, CATEGORY_LABEL } from '@/lib/constants/categories';

function PlaceCard({ place }: { place: Place }) {
  const gradient = CATEGORY_GRADIENT[place.category] ?? CATEGORY_GRADIENT.ETC;
  return (
    <Link
      href={`/places/${place.id}`}
      className="flex-shrink-0 w-[148px] rounded-[20px] overflow-hidden border border-border shadow-sm hover:-translate-y-0.5 hover:shadow-md transition-all duration-200"
    >
      <div className="relative w-full h-[96px]">
        {place.imageUrl ? (
          <img src={place.imageUrl} alt={place.name} className="w-full h-full object-cover" />
        ) : (
          <div className={`w-full h-full bg-gradient-to-br ${gradient}`} />
        )}
        <span className="absolute top-2 left-2 bg-black/30 backdrop-blur-sm text-white text-[9px] font-semibold px-2 py-0.5 rounded-full">
          {CATEGORY_LABEL[place.category] ?? place.category}
        </span>
        {place.likeCount > 0 && (
          <span className="absolute bottom-2 right-2 flex items-center gap-1 bg-black/30 backdrop-blur-sm text-white text-[9px] font-semibold px-1.5 py-0.5 rounded-full">
            <Heart size={8} fill="currentColor" className="text-accent" />
            {place.likeCount}
          </span>
        )}
      </div>
      <div className="bg-bg px-3 py-2.5">
        <p className="text-text-main text-xs font-semibold leading-tight line-clamp-1">{place.name}</p>
        {place.address && (
          <p className="text-text-muted text-[10px] mt-0.5 leading-tight line-clamp-1">{place.address}</p>
        )}
      </div>
    </Link>
  );
}

export default async function Home() {
  let popularPlaces: Place[] = [];
  try {
    popularPlaces = await getPopularPlaces(8);
  } catch {
    // silently fail
  }

  return (
    <div className="min-h-screen bg-bg">
      <div className="max-w-2xl mx-auto px-4 pt-10 pb-8 space-y-4">

        {/* Hero */}
        <div className="relative">
          <div className="bg-text-main rounded-[32px] px-7 py-8 overflow-hidden relative">
            <div className="absolute -top-14 -right-14 w-48 h-48 rounded-full bg-primary/10 pointer-events-none" />
            <div className="absolute -bottom-10 left-10 w-28 h-28 rounded-full bg-primary/5 pointer-events-none" />

            <p className="text-primary text-[10px] font-semibold tracking-[0.16em] uppercase">
              Place &amp; Meeting
            </p>
            <h1 className="text-[48px] font-bold tracking-[-0.05em] text-white leading-none mt-1">
              GoGo
            </h1>
            <p className="text-white/40 mt-2 text-sm tracking-[-0.02em] leading-relaxed max-w-[200px]">
              가고 싶은 곳,<br />함께 결정하는 약속
            </p>

            <div className="mt-5 flex items-center gap-3">
              <Link
                href="/places"
                className="inline-flex items-center gap-2 bg-primary text-text-on-primary text-xs font-semibold px-4 py-2.5 rounded-full hover:-translate-y-0.5 hover:shadow-lg transition-all duration-200"
              >
                장소 등록하기 <ArrowRight size={11} strokeWidth={1} />
              </Link>
            </div>
          </div>

          <div className="absolute -bottom-3.5 right-7 bg-accent text-text-main text-[9px] font-bold px-3 py-1.5 rounded-full shadow-lg tracking-[0.08em] uppercase z-10">
            Beta
          </div>
        </div>

        {/* Quick nav grid */}
        <div className="grid grid-cols-5 gap-3 pt-1">
          <Link
            href="/places"
            className="col-span-3 bg-surface rounded-[28px] p-5 border border-border shadow-sm hover:-translate-y-0.5 hover:shadow-md transition-all duration-200"
          >
            <div className="w-8 h-8 rounded-xl bg-bg/70 flex items-center justify-center mb-4 shadow-sm">
              <MapPin size={15} strokeWidth={1} className="text-primary" />
            </div>
            <p className="font-semibold text-text-main text-sm tracking-[-0.03em]">장소 기록</p>
            <p className="text-text-muted text-xs mt-1 tracking-[-0.02em]">가고 싶은 곳 저장</p>
          </Link>

          <Link
            href="/groups"
            className="col-span-2 bg-primary rounded-[28px] p-5 shadow-md hover:-translate-y-0.5 hover:shadow-lg transition-all duration-200"
          >
            <div className="w-8 h-8 rounded-xl bg-white/15 flex items-center justify-center mb-4">
              <Users size={15} strokeWidth={1} stroke="white" />
            </div>
            <p className="font-semibold text-white text-sm tracking-[-0.03em]">그룹 & 약속</p>
            <p className="text-white/55 text-xs mt-1 tracking-[-0.02em]">투표로 결정</p>
          </Link>
        </div>

        {/* Explore shortcut */}
        <Link
          href="/explore"
          className="flex items-center justify-between bg-bg-secondary rounded-[24px] px-5 py-4 border border-border shadow-sm hover:-translate-y-0.5 transition-all duration-200"
        >
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-surface flex items-center justify-center">
              <Compass size={16} strokeWidth={1} className="text-primary" />
            </div>
            <div>
              <p className="font-semibold text-text-main text-sm tracking-[-0.03em]">장소 탐색</p>
              <p className="text-text-muted text-xs mt-0.5">모두의 장소를 둘러보세요</p>
            </div>
          </div>
          <ArrowRight size={14} strokeWidth={1.5} className="text-text-muted" />
        </Link>

        {/* Popular places feed */}
        {popularPlaces.length > 0 && (
          <div>
            <div className="flex items-center justify-between mb-3 px-0.5">
              <div className="flex items-center gap-2">
                <p className="text-text-main text-sm font-semibold tracking-[-0.03em]">지금 뜨는 장소</p>
                <Heart size={12} fill="var(--color-accent)" stroke="none" />
              </div>
              <Link
                href="/explore"
                className="text-primary text-xs font-semibold flex items-center gap-1 hover:underline"
              >
                더 보기 <ArrowRight size={11} strokeWidth={1.5} />
              </Link>
            </div>
            <div className="flex gap-3 overflow-x-auto pb-2 -mx-4 px-4 scrollbar-hide">
              {popularPlaces.map(place => (
                <PlaceCard key={place.id} place={place} />
              ))}
            </div>
          </div>
        )}

      </div>
    </div>
  );
}

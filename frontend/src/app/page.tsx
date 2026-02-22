import Link from 'next/link';
import { MapPin, Users, ArrowRight } from 'lucide-react';
import { getPopularPlaces, Place } from '@/lib/api/places';

const CATEGORY_GRADIENT: Record<string, string> = {
  CAFE:       'from-[#8B5E3C] to-[#C8936C]',
  RESTAURANT: 'from-[#E8593A] to-[#F5A574]',
  BAR:        'from-[#2D264B] to-[#6B5B9E]',
  ACTIVITY:   'from-[#2E7D32] to-[#66BB6A]',
  ETC:        'from-[#9D8DC2] to-[#C4B8E0]',
};

const CATEGORY_LABEL: Record<string, string> = {
  CAFE: '카페', RESTAURANT: '식당', BAR: '바/펍', ACTIVITY: '액티비티', ETC: '기타',
};

function PlaceCard({ place }: { place: Place }) {
  const gradient = CATEGORY_GRADIENT[place.category] ?? CATEGORY_GRADIENT.ETC;
  return (
    <Link
      href={`/places/${place.id}`}
      className="flex-shrink-0 w-[148px] rounded-[20px] overflow-hidden border border-[rgba(45,38,75,0.08)] shadow-[0_4px_16px_rgba(45,38,75,0.07)] hover:-translate-y-0.5 hover:shadow-[0_8px_24px_rgba(45,38,75,0.14)] transition-all duration-200"
    >
      {/* Image or gradient banner */}
      <div className="relative w-full h-[96px]">
        {place.imageUrl ? (
          <img
            src={place.imageUrl}
            alt={place.name}
            className="w-full h-full object-cover"
          />
        ) : (
          <div className={`w-full h-full bg-gradient-to-br ${gradient}`} />
        )}
        <span className="absolute top-2 left-2 bg-black/30 backdrop-blur-sm text-white text-[9px] font-semibold px-2 py-0.5 rounded-full">
          {CATEGORY_LABEL[place.category] ?? place.category}
        </span>
      </div>
      {/* Info */}
      <div className="bg-white px-3 py-2.5">
        <p className="text-[#2D264B] text-xs font-semibold leading-tight line-clamp-1">{place.name}</p>
        {place.address && (
          <p className="text-[rgba(45,38,75,0.4)] text-[10px] mt-0.5 leading-tight line-clamp-1">{place.address}</p>
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
    // silently fail — feed hidden if backend unreachable
  }

  return (
    <div className="min-h-screen bg-[#F8F7FB]">
      <div className="max-w-2xl mx-auto px-4 pt-10 pb-8 space-y-4">

        {/* Hero — compact */}
        <div className="relative">
          <div className="bg-[#2D264B] rounded-[32px] px-7 py-8 overflow-hidden relative">
            <div className="absolute -top-14 -right-14 w-48 h-48 rounded-full bg-[#9D8DC2]/10 pointer-events-none" />
            <div className="absolute -bottom-10 left-10 w-28 h-28 rounded-full bg-[#9D8DC2]/06 pointer-events-none" />

            <p className="text-[#9D8DC2] text-[10px] font-semibold tracking-[0.16em] uppercase">
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
                className="inline-flex items-center gap-2 bg-[#9D8DC2] text-white text-xs font-semibold px-4 py-2.5 rounded-full hover:-translate-y-0.5 hover:shadow-[0_8px_24px_rgba(157,141,194,0.6)] transition-all duration-200"
              >
                장소 등록하기 <ArrowRight size={11} strokeWidth={1} />
              </Link>
            </div>
          </div>

          <div className="absolute -bottom-3.5 right-7 bg-[#FFB5C5] text-[#2D264B] text-[9px] font-bold px-3 py-1.5 rounded-full shadow-[0_4px_16px_rgba(255,181,197,0.5)] tracking-[0.08em] uppercase z-10">
            Beta
          </div>
        </div>

        {/* Quick nav grid */}
        <div className="grid grid-cols-5 gap-3 pt-1">
          <Link
            href="/places"
            className="col-span-3 bg-[#EFEDF7] rounded-[28px] p-5 border border-[rgba(45,38,75,0.07)] shadow-[0_10px_30px_rgba(45,38,75,0.06)] hover:-translate-y-0.5 hover:shadow-[0_16px_40px_rgba(45,38,75,0.11)] transition-all duration-200"
          >
            <div className="w-8 h-8 rounded-xl bg-white/70 flex items-center justify-center mb-4 shadow-[0_2px_8px_rgba(45,38,75,0.07)]">
              <MapPin size={15} strokeWidth={1} stroke="#9D8DC2" />
            </div>
            <p className="font-semibold text-[#2D264B] text-sm tracking-[-0.03em]">장소 기록</p>
            <p className="text-[rgba(45,38,75,0.4)] text-xs mt-1 tracking-[-0.02em]">가고 싶은 곳 저장</p>
          </Link>

          <Link
            href="/groups"
            className="col-span-2 bg-[#9D8DC2] rounded-[28px] p-5 shadow-[0_10px_30px_rgba(157,141,194,0.28)] hover:-translate-y-0.5 hover:shadow-[0_16px_40px_rgba(157,141,194,0.42)] transition-all duration-200"
          >
            <div className="w-8 h-8 rounded-xl bg-white/15 flex items-center justify-center mb-4">
              <Users size={15} strokeWidth={1} stroke="white" />
            </div>
            <p className="font-semibold text-white text-sm tracking-[-0.03em]">그룹 & 약속</p>
            <p className="text-white/55 text-xs mt-1 tracking-[-0.02em]">투표로 결정</p>
          </Link>
        </div>

        {/* Popular places feed */}
        {popularPlaces.length > 0 && (
          <div>
            <div className="flex items-center justify-between mb-3 px-0.5">
              <p className="text-[#2D264B] text-sm font-semibold tracking-[-0.03em]">지금 뜨는 장소</p>
              <Link
                href="/places"
                className="text-[#9D8DC2] text-xs font-semibold flex items-center gap-1 hover:underline"
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

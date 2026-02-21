import Link from 'next/link';
import { MapPin, Users, ArrowRight, Calendar, CheckCheck } from 'lucide-react';

const FLOW_STEPS = [
  { n: '01', label: '장소 등록', Icon: MapPin },
  { n: '02', label: '그룹 생성', Icon: Users },
  { n: '03', label: '약속', Icon: Calendar },
  { n: '04', label: '투표 확정', Icon: CheckCheck },
];

export default function Home() {
  return (
    <div className="min-h-screen bg-[#F8F7FB]">
      <div className="max-w-2xl mx-auto px-4 pt-10 pb-8 space-y-3">

        {/* Hero — layered composition, dark base */}
        <div className="relative">
          <div className="bg-[#2D264B] rounded-[32px] px-7 py-9 overflow-hidden relative">
            {/* Decorative depth layers */}
            <div className="absolute -top-14 -right-14 w-48 h-48 rounded-full bg-[#9D8DC2]/10 pointer-events-none" />
            <div className="absolute -bottom-10 left-10 w-28 h-28 rounded-full bg-[#9D8DC2]/06 pointer-events-none" />
            <div className="absolute top-6 right-8 w-1 h-16 rounded-full bg-[#9D8DC2]/20 pointer-events-none" />

            <p className="text-[#9D8DC2] text-[10px] font-semibold tracking-[0.16em] uppercase">
              Place &amp; Meeting
            </p>
            <h1 className="text-[56px] font-bold tracking-[-0.05em] text-white leading-none mt-2">
              GoGo
            </h1>
            <p className="text-white/40 mt-3 text-sm tracking-[-0.02em] leading-relaxed max-w-[200px]">
              가고 싶은 곳,<br />함께 결정하는 약속
            </p>

            <Link
              href="/places"
              className="mt-7 inline-flex items-center gap-2 bg-[#9D8DC2] text-white text-xs font-semibold px-4 py-2.5 rounded-full hover:-translate-y-0.5 hover:shadow-[0_8px_24px_rgba(157,141,194,0.6)] transition-all duration-200 ease-[cubic-bezier(0.25,0.1,0.25,1)]"
            >
              장소 등록하기 <ArrowRight size={11} strokeWidth={1} />
            </Link>
          </div>

          {/* Floating accent badge — overlaps hero bottom edge */}
          <div className="absolute -bottom-3.5 right-7 bg-[#FFB5C5] text-[#2D264B] text-[9px] font-bold px-3 py-1.5 rounded-full shadow-[0_4px_16px_rgba(255,181,197,0.5)] tracking-[0.08em] uppercase z-10">
            Beta
          </div>
        </div>

        {/* Asymmetric 5-col grid */}
        <div className="grid grid-cols-5 gap-3 pt-1.5">

          {/* Places card — 3/5 */}
          <Link
            href="/places"
            className="col-span-3 bg-[#EFEDF7] rounded-[32px] p-6 border border-[rgba(45,38,75,0.07)] shadow-[0_10px_30px_rgba(45,38,75,0.06)] hover:-translate-y-0.5 hover:shadow-[0_16px_40px_rgba(45,38,75,0.11)] transition-all duration-200 ease-[cubic-bezier(0.25,0.1,0.25,1)]"
          >
            <div className="w-9 h-9 rounded-2xl bg-white/70 flex items-center justify-center mb-5 shadow-[0_2px_8px_rgba(45,38,75,0.07)]">
              <MapPin size={16} strokeWidth={1} stroke="#9D8DC2" />
            </div>
            <p className="font-semibold text-[#2D264B] text-sm tracking-[-0.03em]">장소 기록</p>
            <p className="text-[rgba(45,38,75,0.4)] text-xs mt-1 tracking-[-0.02em]">가고 싶은 곳 저장</p>
          </Link>

          {/* Groups card — 2/5, primary purple */}
          <Link
            href="/groups"
            className="col-span-2 bg-[#9D8DC2] rounded-[32px] p-6 shadow-[0_10px_30px_rgba(157,141,194,0.28)] hover:-translate-y-0.5 hover:shadow-[0_16px_40px_rgba(157,141,194,0.42)] transition-all duration-200 ease-[cubic-bezier(0.25,0.1,0.25,1)]"
          >
            <div className="w-9 h-9 rounded-2xl bg-white/15 flex items-center justify-center mb-5">
              <Users size={16} strokeWidth={1} stroke="white" />
            </div>
            <p className="font-semibold text-white text-sm tracking-[-0.03em]">그룹 & 약속</p>
            <p className="text-white/55 text-xs mt-1 tracking-[-0.02em]">투표로 결정</p>
          </Link>

        </div>

        {/* Flow — minimal step indicator */}
        <div className="bg-[#EFEDF7] rounded-[32px] px-6 py-5 border border-[rgba(45,38,75,0.07)]">
          <p className="text-[9px] font-semibold text-[rgba(45,38,75,0.3)] uppercase tracking-[0.16em] mb-4">
            Flow
          </p>
          <div className="flex items-start">
            {FLOW_STEPS.map(({ n, label, Icon }, i) => (
              <div key={n} className="flex items-start flex-1">
                <div className="flex flex-col items-center gap-1.5 flex-1">
                  <span className="text-[9px] font-bold text-[rgba(45,38,75,0.28)] tracking-[0.08em]">
                    {n}
                  </span>
                  <div
                    className={`w-7 h-7 rounded-xl flex items-center justify-center ${
                      i === FLOW_STEPS.length - 1
                        ? 'bg-[#9D8DC2] shadow-[0_4px_12px_rgba(157,141,194,0.4)]'
                        : 'bg-white/70'
                    }`}
                  >
                    <Icon
                      size={13}
                      strokeWidth={1}
                      stroke={i === FLOW_STEPS.length - 1 ? 'white' : '#9D8DC2'}
                    />
                  </div>
                  <p className="text-[10px] text-[rgba(45,38,75,0.5)] text-center leading-tight tracking-[-0.01em]">
                    {label}
                  </p>
                </div>
                {i < FLOW_STEPS.length - 1 && (
                  <div className="w-2.5 h-px bg-[rgba(45,38,75,0.1)] mt-[18px] flex-shrink-0" />
                )}
              </div>
            ))}
          </div>
        </div>

      </div>
    </div>
  );
}

'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Home, Compass, MapPin, Users } from 'lucide-react';

const NAV_ITEMS = [
  { href: '/', label: '홈', Icon: Home },
  { href: '/explore', label: '탐색', Icon: Compass },
  { href: '/places', label: '내장소', Icon: MapPin },
  { href: '/groups', label: '그룹', Icon: Users },
];

export default function BottomNav() {
  const pathname = usePathname();

  return (
    <nav className="fixed bottom-0 left-0 right-0 bg-[#F8F7FB]/90 backdrop-blur-md border-t border-[rgba(45,38,75,0.07)] z-20">
      <div className="max-w-2xl mx-auto flex">
        {NAV_ITEMS.map(({ href, label, Icon }) => {
          const active = href === '/'
            ? pathname === '/'
            : pathname.startsWith(href);
          return (
            <Link key={href} href={href}
              className={`flex-1 flex flex-col items-center pt-0.5 pb-3 text-xs transition relative ${
                active ? 'text-[#9D8DC2]' : 'text-[rgba(45,38,75,0.35)] hover:text-[#2D264B]'
              }`}>
              {active && <span className="absolute top-0 left-1/2 -translate-x-1/2 w-8 h-0.5 rounded-full bg-[#9D8DC2]" />}
              <span className="mb-1 mt-2"><Icon size={20} strokeWidth={1} /></span>
              <span className={`font-medium tracking-[-0.02em] ${active ? 'text-[#9D8DC2]' : ''}`}>{label}</span>
            </Link>
          );
        })}
      </div>
    </nav>
  );
}

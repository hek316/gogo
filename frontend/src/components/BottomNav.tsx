'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

const NAV_ITEMS = [
  { href: '/', label: '홈', icon: '🏠' },
  { href: '/places', label: '장소', icon: '📍' },
  { href: '/groups', label: '그룹', icon: '👥' },
];

export default function BottomNav() {
  const pathname = usePathname();

  return (
    <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-100 z-20">
      <div className="max-w-2xl mx-auto flex">
        {NAV_ITEMS.map(item => {
          const active = item.href === '/'
            ? pathname === '/'
            : pathname.startsWith(item.href);
          return (
            <Link key={item.href} href={item.href}
              className={`flex-1 flex flex-col items-center py-2.5 text-xs transition ${
                active ? 'text-indigo-600' : 'text-gray-400 hover:text-gray-600'
              }`}>
              <span className="text-xl mb-0.5">{item.icon}</span>
              <span className="font-medium">{item.label}</span>
            </Link>
          );
        })}
      </div>
    </nav>
  );
}

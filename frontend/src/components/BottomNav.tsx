'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Home, Compass, MapPin, Users, User, Moon, Sun } from 'lucide-react';
import { useAuth } from '@/lib/auth/AuthContext';
import { useTheme } from '@/lib/theme/ThemeContext';

const NAV_ITEMS = [
  { href: '/', label: '홈', Icon: Home },
  { href: '/explore', label: '탐색', Icon: Compass },
  { href: '/places', label: '내장소', Icon: MapPin },
  { href: '/groups', label: '그룹', Icon: Users },
];

export default function BottomNav() {
  const pathname = usePathname();
  const { user } = useAuth();
  const { toggle, resolved } = useTheme();
  const profileHref = user ? '/profile' : '/auth/login';
  const profileLabel = user ? user.nickname : '로그인';

  return (
    <nav className="fixed bottom-0 left-0 right-0 bg-bg/90 backdrop-blur-md border-t border-border z-20">
      <div className="max-w-2xl mx-auto flex">
        {NAV_ITEMS.map(({ href, label, Icon }) => {
          const active = href === '/'
            ? pathname === '/'
            : pathname.startsWith(href);
          return (
            <Link key={href} href={href}
              className={`flex-1 flex flex-col items-center pt-0.5 pb-3 text-xs transition relative ${
                active ? 'text-primary' : 'text-text-muted hover:text-text-main'
              }`}>
              {active && <span className="absolute top-0 left-1/2 -translate-x-1/2 w-8 h-0.5 rounded-full bg-primary" />}
              <span className="mb-1 mt-2"><Icon size={20} strokeWidth={1} /></span>
              <span className={`font-medium tracking-[-0.02em] ${active ? 'text-primary' : ''}`}>{label}</span>
            </Link>
          );
        })}
        {/* 다크모드 토글 */}
        <button
          onClick={toggle}
          className="flex-1 flex flex-col items-center pt-0.5 pb-3 text-xs transition text-text-muted hover:text-text-main"
        >
          <span className="mb-1 mt-2">
            {resolved === 'dark' ? <Sun size={20} strokeWidth={1} /> : <Moon size={20} strokeWidth={1} />}
          </span>
          <span className="font-medium tracking-[-0.02em]">{resolved === 'dark' ? '라이트' : '다크'}</span>
        </button>
        {/* 프로필 / 로그인 탭 */}
        <Link href={profileHref}
          className={`flex-1 flex flex-col items-center pt-0.5 pb-3 text-xs transition relative ${
            pathname.startsWith('/auth') || pathname === '/profile'
              ? 'text-primary'
              : 'text-text-muted hover:text-text-main'
          }`}>
          {(pathname.startsWith('/auth') || pathname === '/profile') && (
            <span className="absolute top-0 left-1/2 -translate-x-1/2 w-8 h-0.5 rounded-full bg-primary" />
          )}
          <span className="mb-1 mt-2">
            {user?.profileImageUrl ? (
              <img src={user.profileImageUrl} alt="" className="w-5 h-5 rounded-full object-cover" />
            ) : (
              <User size={20} strokeWidth={1} />
            )}
          </span>
          <span className={`font-medium tracking-[-0.02em] truncate max-w-[48px] ${pathname.startsWith('/auth') || pathname === '/profile' ? 'text-primary' : ''}`}>
            {profileLabel}
          </span>
        </Link>
      </div>
    </nav>
  );
}

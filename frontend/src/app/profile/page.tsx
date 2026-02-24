'use client';

import { useAuth } from '@/lib/auth/AuthContext';
import { useRequireAuth } from '@/lib/auth/useRequireAuth';
import { User } from 'lucide-react';

export default function ProfilePage() {
  useRequireAuth();
  const { user, logout } = useAuth();

  if (!user) return null;

  return (
    <div className="min-h-screen bg-bg pb-24 px-4 pt-12">
      <div className="max-w-sm mx-auto">
        <h1 className="text-xl font-bold text-text-main mb-8">프로필</h1>

        <div className="flex flex-col items-center gap-4 mb-10">
          {user.profileImageUrl ? (
            <img
              src={user.profileImageUrl}
              alt={user.nickname}
              className="w-20 h-20 rounded-full object-cover"
            />
          ) : (
            <div className="w-20 h-20 rounded-full bg-gray-100 flex items-center justify-center">
              <User size={36} strokeWidth={1} className="text-gray-400" />
            </div>
          )}
          <p className="text-lg font-semibold text-text-main">{user.nickname}</p>
        </div>

        <button
          onClick={logout}
          className="w-full py-3 rounded-[14px] text-sm font-semibold text-red-500 border border-red-200 hover:bg-red-50 transition"
        >
          로그아웃
        </button>
      </div>
    </div>
  );
}

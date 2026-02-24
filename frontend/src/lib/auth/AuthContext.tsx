'use client';

import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { API_BASE } from '@/lib/api/config';

export interface AuthUser {
  id: number;
  nickname: string;
  profileImageUrl: string;
}

interface AuthContextValue {
  user: AuthUser | null;
  loading: boolean;
  loginWithKakao: () => void;
  loginWithGoogle: () => void;
  logout: () => Promise<void>;
  refresh: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchMe = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/auth/me`, { credentials: 'include' });
      if (res.ok) {
        setUser(await res.json());
      } else {
        setUser(null);
      }
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMe();
  }, []);

  const BACKEND_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

  const loginWithKakao = () => {
    window.location.href = `${BACKEND_URL}/api/auth/kakao/authorize`;
  };

  const loginWithGoogle = () => {
    window.location.href = `${BACKEND_URL}/api/auth/google/authorize`;
  };

  const logout = async () => {
    await fetch(`${API_BASE}/api/auth/logout`, {
      method: 'POST',
      credentials: 'include',
    });
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, loginWithKakao, loginWithGoogle, logout, refresh: fetchMe }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}

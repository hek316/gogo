'use client';

import { createContext, useContext, useEffect, useState, useCallback, ReactNode } from 'react';
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
  logout: () => Promise<void>;
  refresh: () => Promise<void>;
  fetchWithAuth: (input: string, init?: RequestInit) => Promise<Response>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);

  const tryRefreshToken = useCallback(async (): Promise<boolean> => {
    try {
      const res = await fetch(`${API_BASE}/api/auth/refresh`, {
        method: 'POST',
        credentials: 'include',
      });
      return res.ok;
    } catch {
      return false;
    }
  }, []);

  const fetchWithAuth = useCallback(async (input: string, init?: RequestInit): Promise<Response> => {
    const res = await fetch(input, { ...init, credentials: 'include' });
    if (res.status === 401) {
      const refreshed = await tryRefreshToken();
      if (refreshed) {
        return fetch(input, { ...init, credentials: 'include' });
      }
      setUser(null);
    }
    return res;
  }, [tryRefreshToken]);

  const fetchMe = useCallback(async () => {
    try {
      const res = await fetch(`${API_BASE}/api/auth/me`, { credentials: 'include' });
      if (res.status === 401) {
        const refreshed = await tryRefreshToken();
        if (refreshed) {
          const retryRes = await fetch(`${API_BASE}/api/auth/me`, { credentials: 'include' });
          if (retryRes.ok) {
            setUser(await retryRes.json());
            return;
          }
        }
        setUser(null);
      } else if (res.ok) {
        setUser(await res.json());
      } else {
        setUser(null);
      }
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, [tryRefreshToken]);

  useEffect(() => {
    fetchMe();
  }, [fetchMe]);

  const loginWithKakao = () => {
    window.location.href = `${API_BASE}/api/auth/kakao/authorize`;
  };

  const logout = async () => {
    await fetch(`${API_BASE}/api/auth/logout`, {
      method: 'POST',
      credentials: 'include',
    });
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, loginWithKakao, logout, refresh: fetchMe, fetchWithAuth }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}

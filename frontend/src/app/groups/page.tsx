'use client';

import { useState } from 'react';
import { createGroup, joinGroup, Group } from '@/lib/api/groups';
import { useRouter } from 'next/navigation';
import { ChevronLeft } from 'lucide-react';

export default function GroupsPage() {
  const router = useRouter();
  const [tab, setTab] = useState<'create' | 'join'>('create');
  const [loading, setLoading] = useState(false);

  const [createForm, setCreateForm] = useState({ name: '' });
  const [joinForm, setJoinForm] = useState({ inviteCode: '' });
  const [error, setError] = useState('');

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true); setError('');
    try {
      const group = await createGroup(createForm.name);
      router.push(`/groups/${group.id}`);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleJoin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true); setError('');
    try {
      const group = await joinGroup(joinForm.inviteCode);
      router.push(`/groups/${group.id}`);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-bg">
      <header className="bg-bg border-b border-border sticky top-0 z-10">
        <div className="max-w-2xl mx-auto px-4 py-4 flex items-center gap-3">
          <button onClick={() => router.push('/places')} className="text-text-muted hover:text-text-main">
            <ChevronLeft size={20} strokeWidth={1.5} />
          </button>
          <h1 className="text-xl font-bold text-text-main tracking-tight">그룹</h1>
        </div>
      </header>

      <main className="max-w-md mx-auto px-4 py-10">
        <div className="flex bg-surface rounded-[16px] p-1 mb-8">
          {(['create', 'join'] as const).map(t => (
            <button key={t} onClick={() => setTab(t)}
              className={`flex-1 py-2 rounded-[12px] text-sm font-medium transition ${
                tab === t ? 'bg-bg text-text-main shadow-sm' : 'text-text-muted'
              }`}>
              {t === 'create' ? '그룹 만들기' : '초대 코드로 참여'}
            </button>
          ))}
        </div>

        {tab === 'create' ? (
          <form onSubmit={handleCreate} className="space-y-4">
            <div>
              <label className="text-sm font-medium text-text-main mb-1.5 block">그룹 이름</label>
              <input required placeholder="예) 성수동 탐방대"
                value={createForm.name}
                onChange={e => setCreateForm(f => ({ ...f, name: e.target.value }))}
                className="w-full border border-border rounded-[12px] px-5 py-3 text-sm focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary bg-bg"
              />
            </div>
            {error && <p className="text-sm text-danger">{error}</p>}
            <button type="submit" disabled={loading}
              className="w-full disabled:opacity-50 bg-text-main hover:bg-text-secondary text-text-on-primary rounded-[16px] py-3.5 text-sm font-medium">
              {loading ? '생성 중...' : '그룹 만들기'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleJoin} className="space-y-4">
            <div>
              <label className="text-sm font-medium text-text-main mb-1.5 block">초대 코드</label>
              <input required placeholder="8자리 코드 입력"
                value={joinForm.inviteCode}
                onChange={e => setJoinForm(f => ({ ...f, inviteCode: e.target.value }))}
                className="w-full border border-border rounded-[12px] px-5 py-3 text-sm font-mono focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary bg-bg"
              />
            </div>
            {error && <p className="text-sm text-danger">{error}</p>}
            <button type="submit" disabled={loading}
              className="w-full disabled:opacity-50 bg-text-main hover:bg-text-secondary text-text-on-primary rounded-[16px] py-3.5 text-sm font-medium">
              {loading ? '참여 중...' : '그룹 참여하기'}
            </button>
          </form>
        )}
      </main>
    </div>
  );
}

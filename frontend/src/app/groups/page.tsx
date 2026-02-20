'use client';

import { useState } from 'react';
import { createGroup, joinGroup, Group } from '@/lib/api/groups';
import { useRouter } from 'next/navigation';

export default function GroupsPage() {
  const router = useRouter();
  const [tab, setTab] = useState<'create' | 'join'>('create');
  const [loading, setLoading] = useState(false);

  const [createForm, setCreateForm] = useState({ name: '', createdBy: '' });
  const [joinForm, setJoinForm] = useState({ inviteCode: '', nickname: '' });
  const [error, setError] = useState('');

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true); setError('');
    try {
      const group = await createGroup(createForm.name, createForm.createdBy);
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
      const group = await joinGroup(joinForm.inviteCode, joinForm.nickname);
      router.push(`/groups/${group.id}`);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b border-gray-100 sticky top-0 z-10">
        <div className="max-w-2xl mx-auto px-4 py-4 flex items-center gap-3">
          <button onClick={() => router.push('/places')} className="text-gray-400 hover:text-gray-600">←</button>
          <h1 className="text-xl font-bold text-indigo-600">👥 그룹</h1>
        </div>
      </header>

      <main className="max-w-md mx-auto px-4 py-10">
        {/* Tab */}
        <div className="flex bg-white rounded-2xl border border-gray-200 p-1 mb-8">
          {(['create', 'join'] as const).map(t => (
            <button key={t} onClick={() => setTab(t)}
              className={`flex-1 py-2 rounded-xl text-sm font-medium transition ${
                tab === t ? 'bg-indigo-600 text-white' : 'text-gray-500 hover:text-gray-700'
              }`}>
              {t === 'create' ? '✨ 그룹 만들기' : '🔗 초대 코드로 참여'}
            </button>
          ))}
        </div>

        {tab === 'create' ? (
          <form onSubmit={handleCreate} className="space-y-4">
            <div>
              <label className="text-sm font-medium text-gray-700 mb-1 block">그룹 이름</label>
              <input required placeholder="예) 성수동 탐방대"
                value={createForm.name}
                onChange={e => setCreateForm(f => ({ ...f, name: e.target.value }))}
                className="w-full border border-gray-200 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-indigo-400"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-gray-700 mb-1 block">닉네임</label>
              <input required placeholder="예) 홍길동"
                value={createForm.createdBy}
                onChange={e => setCreateForm(f => ({ ...f, createdBy: e.target.value }))}
                className="w-full border border-gray-200 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-indigo-400"
              />
            </div>
            {error && <p className="text-sm text-red-500">{error}</p>}
            <button type="submit" disabled={loading}
              className="w-full bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white rounded-xl py-3 font-medium transition">
              {loading ? '생성 중...' : '그룹 만들기'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleJoin} className="space-y-4">
            <div>
              <label className="text-sm font-medium text-gray-700 mb-1 block">초대 코드</label>
              <input required placeholder="8자리 코드 입력"
                value={joinForm.inviteCode}
                onChange={e => setJoinForm(f => ({ ...f, inviteCode: e.target.value }))}
                className="w-full border border-gray-200 rounded-xl px-4 py-3 font-mono focus:outline-none focus:ring-2 focus:ring-indigo-400"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-gray-700 mb-1 block">닉네임</label>
              <input required placeholder="예) 김철수"
                value={joinForm.nickname}
                onChange={e => setJoinForm(f => ({ ...f, nickname: e.target.value }))}
                className="w-full border border-gray-200 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-indigo-400"
              />
            </div>
            {error && <p className="text-sm text-red-500">{error}</p>}
            <button type="submit" disabled={loading}
              className="w-full bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white rounded-xl py-3 font-medium transition">
              {loading ? '참여 중...' : '그룹 참여하기'}
            </button>
          </form>
        )}
      </main>
    </div>
  );
}

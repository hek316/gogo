'use client';

import { useState } from 'react';
import { addPlace, AddPlaceRequest, Place } from '@/lib/api/places';

const CATEGORIES = [
  { value: 'CAFE', label: '☕ 카페' },
  { value: 'RESTAURANT', label: '🍽️ 식당' },
  { value: 'BAR', label: '🍺 바/펍' },
  { value: 'ACTIVITY', label: '🎯 액티비티' },
  { value: 'ETC', label: '📍 기타' },
];

interface Props {
  onAdded: (place: Place) => void;
}

export default function AddPlaceForm({ onAdded }: Props) {
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState<AddPlaceRequest>({
    name: '', address: '', category: 'CAFE', url: '', note: '', createdBy: '',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const place = await addPlace({ ...form, url: form.url || undefined, note: form.note || undefined });
      onAdded(place);
      setForm({ name: '', address: '', category: 'CAFE', url: '', note: '', createdBy: '' });
      setOpen(false);
    } catch (err) {
      alert('장소 추가에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (!open) {
    return (
      <button
        onClick={() => setOpen(true)}
        className="fixed bottom-6 right-6 bg-indigo-600 hover:bg-indigo-700 text-white rounded-full w-14 h-14 text-2xl shadow-lg flex items-center justify-center transition"
      >
        +
      </button>
    );
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-end sm:items-center justify-center z-50">
      <div className="bg-white w-full sm:max-w-md rounded-t-2xl sm:rounded-2xl p-6 shadow-xl">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-bold text-gray-900">장소 추가</h2>
          <button onClick={() => setOpen(false)} className="text-gray-400 hover:text-gray-600 text-xl">✕</button>
        </div>
        <form onSubmit={handleSubmit} className="space-y-3">
          <input
            required
            placeholder="장소 이름 *"
            value={form.name}
            onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
            className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
          <input
            placeholder="주소"
            value={form.address}
            onChange={e => setForm(f => ({ ...f, address: e.target.value }))}
            className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
          <select
            value={form.category}
            onChange={e => setForm(f => ({ ...f, category: e.target.value }))}
            className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
          >
            {CATEGORIES.map(c => <option key={c.value} value={c.value}>{c.label}</option>)}
          </select>
          <input
            placeholder="URL (선택)"
            value={form.url}
            onChange={e => setForm(f => ({ ...f, url: e.target.value }))}
            className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
          <textarea
            placeholder="메모 (선택)"
            value={form.note}
            onChange={e => setForm(f => ({ ...f, note: e.target.value }))}
            rows={2}
            className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400 resize-none"
          />
          <input
            required
            placeholder="닉네임 *"
            value={form.createdBy}
            onChange={e => setForm(f => ({ ...f, createdBy: e.target.value }))}
            className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white rounded-xl py-2.5 text-sm font-medium transition"
          >
            {loading ? '추가 중...' : '추가하기'}
          </button>
        </form>
      </div>
    </div>
  );
}

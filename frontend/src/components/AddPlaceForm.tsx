'use client';

import { useState } from 'react';
import { addPlace, fetchPlacePreview, AddPlaceRequest, Place, PlacePreview } from '@/lib/api/places';
import { Loader2, ImageIcon, CheckCircle2 } from 'lucide-react';

const CATEGORIES = [
  { value: 'CAFE', label: '카페' },
  { value: 'RESTAURANT', label: '식당' },
  { value: 'BAR', label: '바/펍' },
  { value: 'ACTIVITY', label: '액티비티' },
  { value: 'ETC', label: '기타' },
];

interface Props {
  onAdded: (place: Place) => void;
}

export default function AddPlaceForm({ onAdded }: Props) {
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [preview, setPreview] = useState<PlacePreview | null>(null);
  const [autoFilled, setAutoFilled] = useState<{ name?: boolean; address?: boolean }>({});
  const [form, setForm] = useState<AddPlaceRequest & { imageUrl?: string }>({
    name: '', address: '', category: 'CAFE', url: '', note: '', imageUrl: '', createdBy: '',
  });

  const handleUrlBlur = async () => {
    const url = form.url?.trim();
    if (!url || (!url.startsWith('http://') && !url.startsWith('https://'))) return;
    setPreviewLoading(true);
    setPreview(null);
    try {
      const result = await fetchPlacePreview(url);
      setPreview(result);
      // 자동 적용: 빈 필드에만 채움 (현재 form 값 기준으로 미리 계산)
      const filled: { name?: boolean; address?: boolean } = {};
      if (!form.name && result.title) filled.name = true;
      if (!form.address && result.address) filled.address = true;
      setForm(f => ({
        ...f,
        name: f.name || result.title || f.name,
        address: f.address || result.address || f.address,
        imageUrl: f.imageUrl || result.imageUrl || f.imageUrl,
      }));
      if (filled.name || filled.address) {
        setAutoFilled(filled);
        setTimeout(() => setAutoFilled({}), 1500);
      }
    } finally {
      setPreviewLoading(false);
    }
  };

  const applyPreview = () => {
    if (!preview) return;
    setForm(f => ({
      ...f,
      name: preview.title || f.name,
      address: preview.address || f.address,
      imageUrl: preview.imageUrl || f.imageUrl,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const place = await addPlace({
        ...form,
        url: form.url || undefined,
        note: form.note || undefined,
        imageUrl: form.imageUrl || undefined,
      });
      onAdded(place);
      setForm({ name: '', address: '', category: 'CAFE', url: '', note: '', imageUrl: '', createdBy: '' });
      setPreview(null);
      setOpen(false);
    } catch {
      alert('장소 추가에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (!open) {
    return (
      <button
        onClick={() => setOpen(true)}
        className="fixed bottom-20 right-5 bg-mint hover:bg-green text-white rounded-full w-14 h-14 text-2xl flex items-center justify-center font-bold shadow-[0_4px_20px_rgba(0,212,170,0.45)] hover:scale-[1.02] transition-all duration-150"
      >
        +
      </button>
    );
  }

  return (
    <div className="fixed inset-0 bg-black/40 flex items-end sm:items-center justify-center z-50">
      <div className="bg-[#F0FDF9] w-full sm:max-w-md rounded-t-[28px] sm:rounded-[28px] p-6 shadow-lg border-t border-border max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center mb-5">
          <h2 className="text-lg font-semibold text-text-main">장소 추가</h2>
          <button onClick={() => { setOpen(false); setPreview(null); }}
            className="w-8 h-8 flex items-center justify-center rounded-full bg-surface text-green hover:bg-[#A7F3D0] text-sm font-bold">
            ✕
          </button>
        </div>
        <form onSubmit={handleSubmit} className="space-y-3">

          {/* URL with preview */}
          <div className="space-y-2">
            <input
              placeholder="네이버/카카오 지도 URL (선택)"
              value={form.url}
              onChange={e => setForm(f => ({ ...f, url: e.target.value }))}
              onBlur={handleUrlBlur}
              className="w-full border border-border rounded-[12px] px-5 py-3 text-sm focus:outline-none focus:ring-1 focus:ring-mint focus:border-mint bg-white"
            />
            {previewLoading && (
              <div className="flex items-center gap-2 px-1 text-xs text-text-muted">
                <Loader2 size={13} className="animate-spin" />
                미리보기 불러오는 중...
              </div>
            )}
            {preview && (preview.title || preview.imageUrl) && (
              <div className="bg-white rounded-[16px] border border-border overflow-hidden">
                {preview.imageUrl && (
                  <img src={preview.imageUrl} alt="" className="w-full h-32 object-cover" />
                )}
                {!preview.imageUrl && (
                  <div className="w-full h-20 bg-surface flex items-center justify-center">
                    <ImageIcon size={24} className="text-text-muted" />
                  </div>
                )}
                <div className="px-4 py-3">
                  {preview.title && <p className="text-sm font-semibold text-text-main line-clamp-1">{preview.title}</p>}
                  {preview.address && <p className="text-xs text-text-muted mt-0.5">{preview.address}</p>}
                  {preview.description && <p className="text-xs text-text-muted mt-1 line-clamp-2">{preview.description}</p>}
                  <button
                    type="button"
                    onClick={applyPreview}
                    className="mt-2 flex items-center gap-1.5 text-xs text-green font-semibold hover:underline"
                  >
                    <CheckCircle2 size={13} />
                    {(form.name || form.address) ? '정보 업데이트' : '이 정보로 채우기'}
                  </button>
                </div>
              </div>
            )}
          </div>

          <input
            required
            placeholder="장소 이름 *"
            value={form.name}
            onChange={e => { setAutoFilled(f => ({ ...f, name: false })); setForm(f => ({ ...f, name: e.target.value })); }}
            className={`w-full border rounded-[12px] px-5 py-3 text-sm focus:outline-none focus:ring-1 focus:ring-mint focus:border-mint transition-colors duration-300 ${autoFilled.name ? 'bg-mint/10 border-mint' : 'bg-white border-border'}`}
          />
          <input
            placeholder="주소"
            value={form.address}
            onChange={e => { setAutoFilled(f => ({ ...f, address: false })); setForm(f => ({ ...f, address: e.target.value })); }}
            className={`w-full border rounded-[12px] px-5 py-3 text-sm focus:outline-none focus:ring-1 focus:ring-mint focus:border-mint transition-colors duration-300 ${autoFilled.address ? 'bg-mint/10 border-mint' : 'bg-white border-border'}`}
          />
          <select
            value={form.category}
            onChange={e => setForm(f => ({ ...f, category: e.target.value }))}
            className="w-full border border-border rounded-[12px] px-5 py-3 text-sm focus:outline-none focus:ring-1 focus:ring-mint focus:border-mint bg-white"
          >
            {CATEGORIES.map(c => <option key={c.value} value={c.value}>{c.label}</option>)}
          </select>
          <textarea
            placeholder="메모 (선택)"
            value={form.note}
            onChange={e => setForm(f => ({ ...f, note: e.target.value }))}
            rows={2}
            className="w-full border border-border rounded-[12px] px-5 py-3 text-sm focus:outline-none focus:ring-1 focus:ring-mint focus:border-mint resize-none bg-white"
          />
          <input
            required
            placeholder="닉네임 *"
            value={form.createdBy}
            onChange={e => setForm(f => ({ ...f, createdBy: e.target.value }))}
            className="w-full border border-border rounded-[12px] px-5 py-3 text-sm focus:outline-none focus:ring-1 focus:ring-mint focus:border-mint bg-white"
          />
          <button
            type="submit"
            disabled={loading}
            className="w-full disabled:opacity-50 bg-green hover:bg-green-mid text-white rounded-[16px] py-3.5 text-sm font-medium transition"
          >
            {loading ? '추가 중...' : '추가하기'}
          </button>
          <button
            type="button"
            onClick={() => { setOpen(false); setPreview(null); }}
            className="w-full bg-surface text-text-muted rounded-[16px] py-3 text-sm font-medium"
          >
            취소
          </button>
        </form>
      </div>
    </div>
  );
}

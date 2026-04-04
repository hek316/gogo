'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import Image from 'next/image';
import { addPlace, fetchPlacePreview, searchPlaces, AddPlaceRequest, Place, PlacePreview, PlaceSearchResult } from '@/lib/api/places';
import { useAuth } from '@/lib/auth/AuthContext';
import { Loader2, ImageIcon, CheckCircle2, Search } from 'lucide-react';

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
  const { user, loginWithKakao } = useAuth();
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [preview, setPreview] = useState<PlacePreview | null>(null);
  const [autoFilled, setAutoFilled] = useState<{ name?: boolean; address?: boolean }>({});
  const [form, setForm] = useState<AddPlaceRequest & { imageUrl?: string }>({
    name: '', address: '', category: 'CAFE', url: '', note: '', imageUrl: '',
  });

  const [searchResults, setSearchResults] = useState<PlaceSearchResult[]>([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [showDropdown, setShowDropdown] = useState(false);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const nameInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setShowDropdown(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleNameChange = useCallback((value: string) => {
    setForm(f => ({ ...f, name: value }));
    setAutoFilled(f => ({ ...f, name: false }));

    if (debounceRef.current) clearTimeout(debounceRef.current);

    if (!value.trim()) {
      setSearchResults([]);
      setShowDropdown(false);
      return;
    }

    debounceRef.current = setTimeout(async () => {
      setSearchLoading(true);
      try {
        const results = await searchPlaces(value);
        setSearchResults(results);
        setShowDropdown(results.length > 0);
      } finally {
        setSearchLoading(false);
      }
    }, 300);
  }, []);

  const handleSelectPlace = async (result: PlaceSearchResult) => {
    setShowDropdown(false);
    setSearchResults([]);
    setForm(f => ({
      ...f,
      name: result.name,
      address: result.address || f.address,
      url: result.mapUrl || f.url,
    }));
    setAutoFilled({ name: true, address: !!result.address });
    setTimeout(() => setAutoFilled({}), 1500);

    if (result.mapUrl) {
      setPreviewLoading(true);
      try {
        const prev = await fetchPlacePreview(result.mapUrl);
        if (prev.imageUrl) {
          setForm(f => ({ ...f, imageUrl: prev.imageUrl || f.imageUrl }));
          setPreview(prev);
        }
      } finally {
        setPreviewLoading(false);
      }
    }
  };

  const handleUrlBlur = async () => {
    const url = form.url?.trim();
    if (!url || (!url.startsWith('http://') && !url.startsWith('https://'))) return;
    setPreviewLoading(true);
    setPreview(null);
    try {
      const result = await fetchPlacePreview(url);
      setPreview(result);
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
      setForm({ name: '', address: '', category: 'CAFE', url: '', note: '', imageUrl: '' });
      setPreview(null);
      setSearchResults([]);
      setOpen(false);
    } catch {
      alert('장소 추가에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setOpen(false);
    setPreview(null);
    setSearchResults([]);
    setShowDropdown(false);
  };

  if (!open) {
    return (
      <button
        onClick={() => {
          if (!user) { loginWithKakao(); return; }
          setOpen(true);
        }}
        aria-label="장소 추가"
        className="fixed bottom-20 right-5 bg-primary hover:bg-primary-hover text-text-on-primary rounded-full w-14 h-14 text-2xl flex items-center justify-center font-bold shadow-lg hover:scale-[1.02] transition-colors duration-150"
      >
        +
      </button>
    );
  }

  return (
    <div className="fixed inset-0 bg-black/40 flex items-end sm:items-center justify-center z-50" role="dialog" aria-modal="true" aria-labelledby="add-place-title">
      <div className="bg-bg w-full sm:max-w-md rounded-t-[28px] sm:rounded-[28px] p-6 shadow-lg border-t border-border max-h-[90vh] overflow-y-auto" style={{ overscrollBehavior: 'contain' }}>
        <div className="flex justify-between items-center mb-5">
          <h2 id="add-place-title" className="text-lg font-semibold text-text-main">장소 추가</h2>
          <button onClick={handleClose} aria-label="닫기"
            className="w-8 h-8 flex items-center justify-center rounded-full bg-surface text-primary hover:bg-surface-hover text-sm font-bold">
            ✕
          </button>
        </div>
        <form onSubmit={handleSubmit} className="space-y-3">

          <div className="relative" ref={dropdownRef}>
            <div className="mb-1">
              <span className="text-xs font-medium text-text-muted">장소 이름</span>
              <span className="ml-1 text-xs text-primary font-semibold">필수</span>
            </div>
            <div className="relative">
              <input
                ref={nameInputRef}
                required
                placeholder="어떤 장소를 추가할까요?…"
                value={form.name}
                onChange={e => handleNameChange(e.target.value)}
                onFocus={() => { if (searchResults.length > 0) setShowDropdown(true); }}
                className={`w-full border-2 rounded-[12px] px-5 py-4 pr-10 text-base focus:outline-none focus:border-primary transition-colors duration-300 ${autoFilled.name ? 'bg-primary-subtle border-primary' : 'bg-bg border-border'}`}
              />
              <div className="absolute right-3 top-1/2 -translate-y-1/2 text-text-muted pointer-events-none">
                {searchLoading
                  ? <Loader2 size={15} className="animate-spin" />
                  : <Search size={15} />
                }
              </div>
            </div>
            {showDropdown && (
              <div className="absolute z-10 mt-1 w-full bg-bg border border-border rounded-[12px] shadow-lg overflow-hidden max-h-60 overflow-y-auto">
                {searchResults.map((result, i) => (
                  <button
                    key={i}
                    type="button"
                    onMouseDown={() => handleSelectPlace(result)}
                    className="w-full text-left px-4 py-3 hover:bg-surface transition-colors border-b border-border last:border-0"
                  >
                    <p className="text-sm font-medium text-text-main line-clamp-1">{result.name}</p>
                    {result.address && (
                      <p className="text-xs text-text-muted mt-0.5 line-clamp-1">{result.address}</p>
                    )}
                    {result.category && (
                      <p className="text-xs text-primary mt-0.5">{result.category}</p>
                    )}
                  </button>
                ))}
              </div>
            )}
          </div>

          {previewLoading && (
            <div className="flex items-center gap-2 px-1 text-xs text-text-muted" role="status" aria-live="polite">
              <Loader2 size={13} className="animate-spin" aria-hidden="true" />
              미리보기 불러오는 중…
            </div>
          )}
          {preview && (preview.title || preview.imageUrl) && (
            <div className="bg-bg-secondary rounded-[16px] border border-border overflow-hidden">
              {preview.imageUrl && (
                <div className="relative w-full h-32">
                  <Image src={preview.imageUrl} alt="" fill className="object-cover" sizes="(max-width: 448px) 100vw, 448px" />
                </div>
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
                  className="mt-2 flex items-center gap-1.5 text-xs text-primary font-semibold hover:underline"
                >
                  <CheckCircle2 size={13} />
                  {(form.name || form.address) ? '정보 업데이트' : '이 정보로 채우기'}
                </button>
              </div>
            </div>
          )}

          <p className="text-xs font-medium text-text-muted mt-1">장소 정보</p>
          <input
            placeholder="주소…" aria-label="주소"
            value={form.address}
            onChange={e => { setAutoFilled(f => ({ ...f, address: false })); setForm(f => ({ ...f, address: e.target.value })); }}
            className={`w-full border rounded-[12px] px-5 py-3 text-sm focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary transition-colors duration-300 ${autoFilled.address ? 'bg-primary-subtle border-primary' : 'bg-bg border-border'}`}
          />
          <select
            value={form.category} aria-label="카테고리"
            onChange={e => setForm(f => ({ ...f, category: e.target.value }))}
            className="w-full border border-border rounded-[12px] px-5 py-3 text-sm focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary bg-bg"
          >
            {CATEGORIES.map(c => <option key={c.value} value={c.value}>{c.label}</option>)}
          </select>

          <div className="flex items-center gap-3 my-1">
            <div className="flex-1 h-px bg-border" />
            <span className="text-xs text-text-muted whitespace-nowrap">또는 URL로 가져오기</span>
            <div className="flex-1 h-px bg-border" />
          </div>
          <input
            type="url"
            placeholder="네이버/카카오 지도 URL 붙여넣기…" aria-label="지도 URL"
            value={form.url}
            onChange={e => setForm(f => ({ ...f, url: e.target.value }))}
            onBlur={handleUrlBlur}
            className="w-full border border-border rounded-[12px] px-5 py-3 text-sm focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary bg-bg"
          />

          <textarea
            placeholder="메모 (선택)…" aria-label="메모"
            value={form.note}
            onChange={e => setForm(f => ({ ...f, note: e.target.value }))}
            rows={2}
            className="w-full border border-border rounded-[12px] px-5 py-3 text-sm focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary resize-none bg-bg"
          />
          <button
            type="submit"
            disabled={loading}
            className="w-full disabled:opacity-50 bg-text-main hover:bg-text-secondary text-text-on-primary rounded-[16px] py-3.5 text-sm font-medium transition"
          >
            {loading ? '추가 중…' : '추가하기'}
          </button>
          <button
            type="button"
            onClick={handleClose}
            className="w-full bg-surface text-text-muted rounded-[16px] py-3 text-sm font-medium"
          >
            취소
          </button>
        </form>
      </div>
    </div>
  );
}

'use client';

import { useState } from 'react';
import { Heart } from 'lucide-react';
import { likePlace, unlikePlace } from '@/lib/api/places';
import { useAuth } from '@/lib/auth/AuthContext';

interface LikeButtonProps {
  placeId: number;
  initialLiked: boolean;
  initialCount: number;
  size?: 'sm' | 'md';
}

export default function LikeButton({ placeId, initialLiked, initialCount, size = 'sm' }: LikeButtonProps) {
  const { user } = useAuth();
  const [liked, setLiked] = useState(initialLiked);
  const [count, setCount] = useState(initialCount);
  const [loading, setLoading] = useState(false);

  const iconSize = size === 'sm' ? 13 : 16;
  const textSize = size === 'sm' ? 'text-[10px]' : 'text-xs';

  const handleClick = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    if (!user) {
      // 비로그인: 카카오 로그인 페이지로 이동
      window.location.href = '/auth/login';
      return;
    }

    if (loading) return;

    // 낙관적 업데이트
    const nextLiked = !liked;
    setLiked(nextLiked);
    setCount(c => nextLiked ? c + 1 : c - 1);
    setLoading(true);

    try {
      if (nextLiked) {
        await likePlace(placeId);
      } else {
        await unlikePlace(placeId);
      }
    } catch {
      // 롤백
      setLiked(!nextLiked);
      setCount(c => nextLiked ? c - 1 : c + 1);
    } finally {
      setLoading(false);
    }
  };

  return (
    <button
      onClick={handleClick}
      className={`flex items-center gap-1 px-2 py-1 rounded-full transition-all ${
        liked
          ? 'bg-[#FFE4EC] text-[#E05178]'
          : 'bg-[rgba(45,38,75,0.06)] text-[rgba(45,38,75,0.4)] hover:bg-[#FFE4EC] hover:text-[#E05178]'
      }`}
    >
      <Heart
        size={iconSize}
        strokeWidth={1.5}
        fill={liked ? 'currentColor' : 'none'}
        className="transition-transform active:scale-125"
      />
      {count > 0 && (
        <span className={`${textSize} font-semibold leading-none`}>{count}</span>
      )}
    </button>
  );
}

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
      window.location.href = '/auth/login';
      return;
    }

    if (loading) return;

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
          ? 'bg-accent-bg text-accent-text'
          : 'bg-surface text-text-muted hover:bg-accent-bg hover:text-accent-text'
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

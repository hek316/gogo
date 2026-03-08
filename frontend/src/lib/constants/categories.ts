export const CATEGORY_LABEL: Record<string, string> = {
  CAFE: '카페',
  RESTAURANT: '식당',
  BAR: '바/펍',
  ACTIVITY: '액티비티',
  ETC: '기타',
};

export const CATEGORY_GRADIENT: Record<string, string> = {
  CAFE:       'from-[#8B5E3C] to-[#C8936C]',
  RESTAURANT: 'from-[#E8593A] to-[#F5A574]',
  BAR:        'from-[#2D264B] to-[#6B5B9E]',
  ACTIVITY:   'from-[#2E7D32] to-[#66BB6A]',
  ETC:        'from-[#9D8DC2] to-[#C4B8E0]',
};

export const CATEGORIES = [
  { value: '', label: '전체' },
  ...Object.entries(CATEGORY_LABEL).map(([v, l]) => ({ value: v, label: l })),
];

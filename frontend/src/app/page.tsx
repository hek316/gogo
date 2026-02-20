import Link from 'next/link';

export default function Home() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 to-purple-50">
      <div className="max-w-2xl mx-auto px-4 pt-16 pb-8 text-center">
        <div className="mb-8">
          <p className="text-6xl mb-4">🗺️</p>
          <h1 className="text-4xl font-extrabold text-indigo-700 mb-2">GoGo</h1>
          <p className="text-gray-500 text-lg">친구들과 가고 싶은 장소를 기록하고<br />함께 약속을 정해보세요</p>
        </div>

        <div className="grid gap-4 mt-10">
          <Link href="/places"
            className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 flex items-center gap-4 hover:shadow-md transition text-left">
            <span className="text-3xl">📍</span>
            <div>
              <p className="font-bold text-gray-900">장소 기록</p>
              <p className="text-sm text-gray-500">가고 싶은 곳을 등록하고 방문 후기도 남겨보세요</p>
            </div>
            <span className="ml-auto text-gray-300">→</span>
          </Link>

          <Link href="/groups"
            className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 flex items-center gap-4 hover:shadow-md transition text-left">
            <span className="text-3xl">👥</span>
            <div>
              <p className="font-bold text-gray-900">그룹 & 약속</p>
              <p className="text-sm text-gray-500">그룹을 만들고 친구들과 약속 장소를 투표로 결정해요</p>
            </div>
            <span className="ml-auto text-gray-300">→</span>
          </Link>
        </div>

        <div className="mt-10 bg-white rounded-2xl p-5 border border-gray-100 text-left shadow-sm">
          <h2 className="font-bold text-gray-700 mb-3">사용 방법</h2>
          <ol className="space-y-2 text-sm text-gray-500">
            <li className="flex gap-2"><span className="text-indigo-500 font-bold">1.</span> 📍 장소 탭에서 가고 싶은 곳 등록</li>
            <li className="flex gap-2"><span className="text-indigo-500 font-bold">2.</span> 👥 그룹 탭에서 그룹 생성 후 친구 초대</li>
            <li className="flex gap-2"><span className="text-indigo-500 font-bold">3.</span> 그룹에 장소 공유 → 약속 만들기</li>
            <li className="flex gap-2"><span className="text-indigo-500 font-bold">4.</span> 🗳️ 투표로 최종 장소 확정!</li>
          </ol>
        </div>
      </div>
    </div>
  );
}

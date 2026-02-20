import type { Metadata } from "next";
import { Geist } from "next/font/google";
import "./globals.css";
import BottomNav from "@/components/BottomNav";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "GoGo - 친구들과 가고 싶은 장소 기록",
  description: "가고 싶은 장소를 기록하고 친구들과 약속을 정해보세요",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body className={`${geistSans.variable} antialiased bg-gray-50`}>
        <div className="pb-16">
          {children}
        </div>
        <BottomNav />
      </body>
    </html>
  );
}

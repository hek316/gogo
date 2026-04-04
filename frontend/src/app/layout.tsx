import type { Metadata, Viewport } from "next";
import "./globals.css";
import BottomNav from "@/components/BottomNav";
import { AuthProvider } from "@/lib/auth/AuthContext";
import { ThemeProvider } from "@/lib/theme/ThemeContext";

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  themeColor: [
    { media: "(prefers-color-scheme: light)", color: "#FFFFFF" },
    { media: "(prefers-color-scheme: dark)", color: "#1A1726" },
  ],
};

export const metadata: Metadata = {
  title: {
    default: "GoGo - 친구들과 가고 싶은 장소 기록",
    template: "%s | GoGo",
  },
  description: "가고 싶은 장소를 기록하고 친구들과 약속을 정해보세요",
  openGraph: {
    title: "GoGo - 친구들과 가고 싶은 장소 기록",
    description: "가고 싶은 장소를 기록하고 친구들과 약속을 정해보세요",
    type: "website",
    locale: "ko_KR",
  },
  robots: { index: true, follow: true },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko" suppressHydrationWarning>
      <head>
        <script dangerouslySetInnerHTML={{ __html: `
          (function() {
            try {
              var theme = localStorage.getItem('gogo-theme');
              if (theme === 'dark' || (!theme && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
                document.documentElement.classList.add('dark');
              }
            } catch(e) {}
          })();
        `}} />
      </head>
      <body className="antialiased bg-bg text-text-main">
        <a href="#main" className="sr-only focus:not-sr-only focus:fixed focus:top-4 focus:left-4 focus:z-50 focus:bg-primary focus:text-text-on-primary focus:px-4 focus:py-2 focus:rounded-lg focus:text-sm focus:font-medium">
          본문으로 건너뛰기
        </a>
        <ThemeProvider>
          <AuthProvider>
            <main id="main" className="pb-16">
              {children}
            </main>
            <BottomNav />
          </AuthProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}

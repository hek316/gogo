You are a senior product designer (20+ years) specializing in premium web applications (not marketing pages).

This project is a functional web app, NOT a landing page.
Refactor the entire UI from the existing Mint system into a "Sophisticated Lavender" premium design system.

ABSOLUTE RULE:
No AI-generic layouts. No marketing-style hero sections. No cute or playful UI.

[Product Direction]

* Type: Web App (Dashboard-style UI)
* Mood: Cold, refined, premium, minimal
* Reference feel: Linear, Notion, Apple, Superhuman
* High usability over decorative visuals

[Color System]

* App Background: #F8F7FB
* Primary: #9D8DC2 (Muted Lavender)
* Text & Primary CTA: #2D264B (Deep Midnight Purple)
* Surface / Cards: #EFEDF7
* Accent: #FFB5C5 (VERY limited, only badges/alerts)

[Typography]

* Font: Pretendard
* Headings: letter-spacing -0.05em, weight 600~700
* Body: letter-spacing -0.02em
* Slightly dense text spacing (premium product feel)

[Layout Rules - Critical for Web App]

* Use structured dashboard layout (not hero-centric)
* Clear hierarchy: Header → Content Grid → Functional Cards
* Avoid overly spacious landing-page padding
* Maintain professional information density
* Subtle asymmetry allowed for sophistication

[Component Design System]
Cards:

* rounded-[32px]
* 1px border: rgba(45, 38, 75, 0.08)
* Inner shadow (inset) for depth
* Soft colored shadow: rgba(45, 38, 75, 0.08)
* No flat, lifeless cards

Buttons:

* No emoji
* Minimal lucide-react icons only (1px stroke)
* Hover: translateY(-2px) + soft blur shadow
* Transition: 200ms ease, very subtle

Icons:

* Use lucide-react only
* Consistent size (16px / 20px)
* Minimal and sharp (no illustration style)

[Page-Specific Improvements]
/ (Home Dashboard):

* Not a marketing hero
* Use bento grid with functional widgets
* Slight overlapping layers to avoid flatness
* Example: stats card overlapping a larger container

/places (Core Feature Page):

* Premium place cards with thin borders + inner gloss effect
* Subtle hover elevation
* Editorial, refined layout (not cute, not playful)

[Anti-AI Design Constraints]

* No emoji anywhere
* No generic centered hero + subtitle + button layout
* No bright gradient spam
* No startup-template aesthetics
* Must feel like a real production SaaS UI

Tech:
Next.js + Tailwind CSS
Write clean, scalable, production-level component code.

---
name: Pitch Precision
colors:
  surface: '#faf8ff'
  surface-dim: '#d2d9f4'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3ff'
  surface-container: '#eaedff'
  surface-container-high: '#e2e7ff'
  surface-container-highest: '#dae2fd'
  on-surface: '#131b2e'
  on-surface-variant: '#434655'
  inverse-surface: '#283044'
  inverse-on-surface: '#eef0ff'
  outline: '#737686'
  outline-variant: '#c3c6d7'
  surface-tint: '#0053db'
  primary: '#004ac6'
  on-primary: '#ffffff'
  primary-container: '#2563eb'
  on-primary-container: '#eeefff'
  inverse-primary: '#b4c5ff'
  secondary: '#006c4a'
  on-secondary: '#ffffff'
  secondary-container: '#82f5c1'
  on-secondary-container: '#00714e'
  tertiary: '#005b7c'
  on-tertiary: '#ffffff'
  tertiary-container: '#00759f'
  on-tertiary-container: '#e1f2ff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dbe1ff'
  primary-fixed-dim: '#b4c5ff'
  on-primary-fixed: '#00174b'
  on-primary-fixed-variant: '#003ea8'
  secondary-fixed: '#85f8c4'
  secondary-fixed-dim: '#68dba9'
  on-secondary-fixed: '#002114'
  on-secondary-fixed-variant: '#005137'
  tertiary-fixed: '#c4e7ff'
  tertiary-fixed-dim: '#7bd0ff'
  on-tertiary-fixed: '#001e2c'
  on-tertiary-fixed-variant: '#004c69'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
typography:
  display:
    fontFamily: Plus Jakarta Sans
    fontSize: 44px
    fontWeight: '800'
    lineHeight: 52px
    letterSpacing: -0.03em
  display-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '800'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 26px
    fontWeight: '700'
    lineHeight: 34px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.015em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 26px
    letterSpacing: 0em
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 22px
    letterSpacing: 0em
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
    letterSpacing: 0.005em
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 11px
    fontWeight: '700'
    lineHeight: 14px
    letterSpacing: 0.04em
  tabular-stat:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '700'
    lineHeight: 24px
    letterSpacing: -0.01em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-md: 1.5rem
  gutter-lg: 2rem
  margin: 1rem
  margin-md: 1.5rem
  margin-lg: 2.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style
The design system delivers an airy, athletic, and impeccably structured atmosphere tailored for modern football followers, tacticians, and enthusiasts. Balancing editorial poise with active sports performance, it pairs porcelain-toned purity with the energetic vibrancy of electric indigo and stadium emerald. 

The aesthetic is Modern Precision: high-clarity surfaces, generous breathing space, soft architectural borders, and deliberate micro-interactions. The experience rejects the clutter of legacy sports scoreboards, opting instead for effortless legibility, swift glanceability, and tactile warmth that feels native and premium across all screen environments.

## Colors
The palette balances pure light foundations with rich functional highlights:

- **Surfaces & Canvases:** The main background relies on a subtle porcelain tint (`#f8fafc`), layered with elevated cards and panels finished in pure white (`#ffffff`). Subtle slate borders (`#e2e8f0`) provide tactile structural definition without visual noise.
- **Primary (Electric Indigo / Sapphire):** Set at `#2563eb`, driving focal actions, active score callouts, active tab indicators, and live match accents.
- **Secondary (Pitch Mint / Emerald):** Anchored at `#059669`, providing crisp confirmation states, live-time match indicators, positive momentum stats, and form guides.
- **Tertiary (Aero Sky):** Set at `#38bdf8` for subtle data highlights, secondary match metrics, and assistive visual badges.
- **Neutrals:** Text and iconography rely on deep slate (`#0f172a`) for titles and high-emphasis data, shifting to slate body (`#334155`) and muted captions (`#64748b`) to preserve typographic hierarchy and WCAG AAA compliance.

## Typography
Plus Jakarta Sans is utilized system-wide, selected for its crisp geometric terminals, open apertures, and energetic sports-editorial feel. 

- **Numerical & Stat Rendering:** All live scores, match clocks, player squad numbers, and tabular league tables must utilize `font-variant-numeric: tabular-nums` to eliminate layout jitter during live telemetry updates.
- **Hierarchy:** High-emphasis headlines employ tighter negative tracking (`-0.01em` to `-0.03em`) to anchor sports headlines and match fixtures. Tiny meta tags and badge labels employ uppercase tracking (`0.04em`) to safeguard legibility under compact form factors.

## Layout & Spacing
The layout model employs a fluid 8pt baseline rhythm mapped to dynamic responsive grids:

- **Breakpoints & Layout:**
  - **Mobile (<768px):** 4-column layout with `margin: 1rem` and `gutter: 1rem`. Focuses on vertically stacked match cards, horizontal scrolling pill carousels, and edge-to-edge stat views.
  - **Tablet (768px–1024px):** 8-column layout with `margin-md: 1.5rem` and `gutter-md: 1.5rem`. Enables side-by-side fixtures and modular tables.
  - **Desktop (>1024px):** 12-column layout capped at a maximum inner container width of `1280px` with `margin-lg: 2.5rem`. Accommodates a dedicated sticky sidebar navigation, primary match feed, and real-time live commentary drawer.
- **Rhythm Rules:** Component internals enforce tight groupings (`space-xs` and `space-sm` for badge groupings and metadata pairs), while module cards and live fixture groups maintain relaxed boundaries with `space-md` through `space-xl`.

## Elevation & Depth
Elevation rejects heavy, murky drop shadows in favor of a layered, architectural approach combining surface tints, crisp slate borders, and ultra-soft ambient light:

- **Flat/Resting Layer:** Background canvas sits at `#f8fafc`. Surface cards sit at `#ffffff` bounded by a 1px solid border in `#e2e8f0`.
- **Card Ambient Hover / Active Match:** `box-shadow: 0 4px 20px -2px rgba(15, 23, 42, 0.05), 0 2px 6px -1px rgba(15, 23, 42, 0.02)`.
- **Interactive Flyouts & Floating Bars:** Bottom navigation bars, modal sheets, and quick-filter trays employ subtle translucent backing (`rgba(255, 255, 255, 0.85)`) coupled with a backdrop blur of `12px` and a bounding border of `#e2e8f0`.
- **Match Priority Glow:** For active live games or critical alerts, a restrained focus halo using `rgba(37, 99, 235, 0.08)` or `rgba(5, 150, 105, 0.1)` accentuates the container boundary without obscuring surrounding content.

## Shapes
A roundedness level of `2` provides an approachable, contemporary sports aesthetic while preserving structural discipline:

- **Primary Cards & Containers:** Standardized at `0.75rem` (12px) to frame match fixtures, team sheets, and stats tables seamlessly.
- **Buttons & Action Controls:** Core buttons utilize `0.5rem` (8px) for crisp interaction targets, while compact chips and quick-filter pills employ `1rem` (or fully pill-shaped radii) to denote swift touchability.
- **Score Indicators & Avatars:** Team crest badges and player avatars are enclosed in rounded squares (`0.5rem`) or circles, giving equal weight to contrasting club crest geometries.

## Components

### Buttons
- **Primary:** Filled in `#2563eb` with crisp white text (`#ffffff`), `0.5rem` radius, `0.75rem 1.25rem` padding. Subtle transform on tap (`scale: 0.98`).
- **Secondary / Ghost:** White background (`#ffffff`), 1px solid `#e2e8f0`, text in `#0f172a`. Hover transitions border to `#cbd5e1` with a micro-tint of `#f8fafc`.
- **Live / Accent:** Emerald green pill (`#059669`) reserved for "Watch Live", "Match In Play", or real-time broadcast activations.

### Chips & Filter Pills
- Rendered with a full pill radius (`9999px`), `0.375rem 0.875rem` padding, and `label-md` typography.
- Unselected: Background `#ffffff`, border 1px solid `#e2e8f0`, text `#334155`.
- Selected: Background `#2563eb`, border 1px solid `#2563eb`, text `#ffffff`.

### Match Cards (Custom Football Component)
- Background `#ffffff`, 1px solid `#e2e8f0`, radius `0.75rem` (12px).
- Internal layout: Team emblems (32x32px), team names (`headline-sm`), central score module featuring tabular numerals (`tabular-stat`).
- Live match state incorporates an animated emerald badge (`#059669`) with a soft pulsing indicator alongside the live match minute (`label-sm`).

### Lists & League Tables
- Clean horizontal rows divided by 1px subtle rules (`#f1f5f9`).
- Hover states transition cleanly to `#f8fafc`.
- Form guides use mini status dots: Emerald (`#059669` for Win), Slate (`#94a3b8` for Draw), and Rose (`#e11d48` for Loss) with `0.25rem` corner rounding.

### Input Fields & Search Bars
- Background `#ffffff`, border 1px solid `#cbd5e1`, radius `0.5rem`, text `#0f172a`.
- Focus state: Border transitions to `#2563eb` with a 3px soft focus ring (`rgba(37, 99, 235, 0.12)`).
- Search prefix icons utilize muted slate (`#94a3b8`).

### Checkboxes & Radio Controls
- Checkbox: `18x18px`, `0.25rem` radius, 1.5px border `#cbd5e1`. Checked state fills `#2563eb` with an inset white checkmark.
- Radio: Circular with concentric `#2563eb` fill dot upon selection.
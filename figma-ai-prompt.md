# Figma AI Prompt — Rank Football (GoalStream) UI Redesign

Paste the block below into Figma AI.

---

```
ROLE
You are a senior product designer at a mobile sports-streaming startup. Design a complete, pixel-ready UI for "Rank Football" (internal name "GoalStream"), a free Android football app where users browse live scores, fixtures, leagues, standings, and watch streamable matches with a built-in video player.

OBJECTIVE
Design an interface that is visually ELECTRIFYING (stadium-atmosphere, premium sports energy) yet trivially easy to navigate for a first-time user. Users should reach a live match or start playback in 2 taps or fewer. Everything must feel fast, bold, and modern.

TARGET
- Primary: Android phone (360×800dp portrait frame).
- Secondary frames: tablet (800×1280), Android TV / leanback (1920×1080 remote-first).
- Dark UI only. No light theme.

────────────────────────────────────────────
1. BRAND & DESIGN SYSTEM
────────────────────────────────────────────

COLOR PALETTE (use exactly):
- StadiumBlack #0A0A0F — app background
- CardDark #12121C — cards
- SurfaceDark #1A1A28 — surfaces/sheets
- SurfaceMuted #1E1E2E — pressed/hover states
- PitchGreen #00C853 — primary brand action color
- NeonGreen #69FF47 — highlights, glow, energy accents
- TextWhite #F0F0F5 — primary text
- TextGrey #6B6B85 — secondary text
- LiveRed #FF1744 — LIVE badges, alerts, errors
- GoalYellow #FFD600 — goals, yellow cards, hype accents
- BorderSubtle: white at ~6% opacity — hairline dividers

TYPOGRAPHY:
- Display/headlines/titles: "Barlow Condensed" (Bold, ExtraBold, Black), tight letter-spacing, ALL-CAPS for big hero numbers and section titles. Gives broadcast-ticker energy.
- Body/labels: "DM Sans" (Regular/Medium/SemiBold).
- Live score numbers: oversized Black Barlow Condensed (e.g. 48–64pt).
- Do not use script or serif fonts anywhere.

VISUAL LANGUAGE:
- Deep black pitch-like background; cards with subtle gradients (CardDark → slightly lighter top edge) and 1px hairline borders, generous corner radius (16–20dp cards, 24dp sheets, 12dp small chips).
- NeonGreen glow accents on active/live/primary elements only — never as a fill for large surfaces.
- Section headers with a small green tick/pitch-line motif.
- Minimal, line-based sport icons (ball, whistle, calendar, trophy, star). No clipart.
- Everything feels "stadium at night": dark, focused, electric green energy.

COMPONENT LIBRARY — define reusable components:
1. MatchCard — horizontal: competition name + minute (top), home/away crests + score in the middle, status chip (LIVE red pulsing / NS grey / FT white) bottom-right. Tappable.
2. LiveBadge — small red pill with a pulsing dot + "LIVE".
3. StatusChips — NS / 1H / 2H / HT / ET / FT / AET / PEN with distinct colors (live = LiveRed, finished = TextGrey, upcoming = PitchGreen outline).
4. CompetitionHeader — league crest + name + season.
5. Countdown — HH:MM:SS mono-styled timer in GoalYellow for kick-off.
6. SearchBar — full-width rounded pill with search icon + mic hint.
7. AI suggestion chips — gradient-bordered chips for "AI" search suggestions.
8. BottomNavItem — 5 items (see navigation).
9. HeroBanner — large match-of-the-day card with team crests, kick-off countdown, "WATCH" CTA button.
10. StreamCard — title, quality badge (HD/SD), language, source, play button, favorites heart.
11. VideoPlayer shell — dark player with overlay controls (play/pause, 10s seek, PiP, cast, fullscreen, quality/speed), progress bar with buffering state, ad pill "Ad".
12. ChatBubble — match chat message bubbles.
13. TimelineEvent — goal (green dot + GoalYellow text), yellow/red card, substitution, kickoff icons on a vertical line.
14. ToggleChips — segmented filter (Today / Tomorrow / Weekend).
15. Skeleton shimmer for loading states.

SPACING: 4dp grid; standard margins 20dp; card gap 12dp; section spacing 24dp.

────────────────────────────────────────────
2. NAVIGATION MODEL
────────────────────────────────────────────

Bottom navigation (phone) with 5 tabs, always visible except on the Watch/player screen:
1. HOME — feed
2. LIVE — live scores wall
3. FIXTURES — upcoming matches
4. LEAGUES — browse competitions
5. SETTINGS (user/profile)

Active tab = PitchGreen icon + NeonGreen indicator; inactive = TextGrey.

From Live/Home/Fixtures/Leagues: tapping a match opens MATCH DETAIL (secondary screen with back button) → tapping WATCH opens the full-screen PLAYER.
Global search is a floating search pill on Home and a search icon on top bars.
TV frame: remote-friendly vertical rail (left) instead of bottom bar; large focus states (20% scale-up + green outline).

────────────────────────────────────────────
3. SCREENS — FULL FLOW (design all, in this order)
────────────────────────────────────────────

S1. ONBOARDING (3 swipeable pages + final screen)
- Page 1: big hero image of a packed floodlit stadium; headline "The Stadium in Your Pocket"; subtext about live scores + streams.
- Page 2: goal celebration visual; headline "Feel Every Goal"; subtext about instant goal alerts.
- Page 3: crests collage; headline "Follow Your Club"; subtext about favorites + reminders.
- Dots progress indicator, "Skip" top-right, "Continue" (PitchGreen filled button) and "Next".
- Final: language picker (English, Arabic, French, Portuguese, Swahili) + "Get Started".

S2. HOME
- Top bar: app wordmark "RANK FOOTBALL" (Barlow Condensed, black weight) + bell icon with unread-dot badge + avatar.
- Floating search pill under top bar ("Search teams, leagues, matches…").
- HERO BANNER (matches of the day): large gradient card, home vs away crests, league chip, kick-off countdown in GoalYellow, big "WATCH NOW" green button.
- FAVORITES STRIP: horizontally scrollable circular team crests (only if user has favorites) with a "+ Add teams" ghost tile.
- "LIVE NOW" section header with live counter → horizontal scroll of compact MatchCards.
- "TODAY'S FIXTURES" section → grouped vertical list by competition.
- "TRENDING LEAGUES" → crest tiles.
- Banner ad slot (full-width, muted SurfaceDark) at bottom above nav.

S3. LIVE
- Sticky header with segmented chips: ALL / LIVE / FINISHED.
- Live count banner: "5 matches live now" with pulsing dot.
- Vertical wall of MatchCards; LIVE cards have red pulsing badge + minute ticking; tapping opens the player directly (fast path).
- Goal highlight toast: transient overlay card "⚽ GOAL! Team A 2-1 Team B" that slides in from top and auto-dismisses.
- Empty state: "No live matches right now — check fixtures" + CTA.

S4. FIXTURES
- Date chips: Today / Tomorrow / Sat / Sun / + (week view).
- Matches grouped by competition (CompetitionHeader then MatchCards).
- Upcoming matches show countdown instead of score.
- Filter icon to choose league.
- Pull-to-refresh.

S5. LEAGUES
- Grid of league cards (crest, name, country flag, country). Search/filter by country.
- Popular first (Premier League, La Liga, UCL, etc.), then "All Leagues" alphabetical grid.
- Tapping a league → S6.

S6. STANDINGS (secondary screen)
- Tabs: TABLE / FIXTURES / TOP SCORERS.
- Table: rank, crest, team, P W D L, GD, PTS. Top 4 rows subtly green-tinted (UCL spots), bottom 2 red-tinted. Current team row highlighted with NeonGreen left rail.
- Fixtures tab reuses MatchCards.

S7. SEARCH
- Big search field with instant results; "AI" chip for smart suggestions (Gemini).
- Result groups: Matches, Teams, Leagues. Team cards tappable → team view with fixtures + follow button.
- Recent searches + trending searches (grey text, tap to fill).
- Empty/typing state with AI-powered suggestion pills ("Top derby this weekend", "Champions League fixtures"…).

S8. MATCH DETAIL (secondary)
- Header: league chip, "Live" status or countdown.
- Score block: crests, giant score (e.g. 2 — 1), minute, stadium + attendance line.
- Tabs: TIMELINE / CHAT / INFO (and STATS where available).
- Timeline: vertical event list (goal = GoalYellow text + green dot; cards; subs; kickoff).
- Chat: live match chat panel — message list + input bar (user message right-aligned PitchGreen, others left). Rate-limit hint text.
- Favorite heart + "Remind me" toggle (sets pre-match notification).
- Primary sticky CTA: "▶ WATCH STREAM" (PitchGreen, full-width, bottom).

S9. PLAYER / WATCH (full-screen, immersive)
- Dark chrome; hides all nav. Top: back arrow, match title, quality badge, cast icon, PiP icon.
- Center: playback controls (10s back, play/pause, 10s forward) — large translucent circles with NeonGreen icon when playing.
- Bottom: progress bar with elapsed/total, settings gear (quality/speed), fullscreen toggle.
- Stream list drawer (if multiple sources): StreamCards with source, quality, language, "HD" badges.
- Buffering = animated shimmer/spinner + "Connecting to stream…".
- Live badge if match is live; ad overlay pill when an ad plays.
- Cast/Chromecast + picture-in-picture states shown as mock overlays.

S10. SETTINGS
- List groups: Account (sign-in), Preferences (language, notifications, dark mode toggle locked-on, ad-free/coins upsell card), Favorites, Reminders, Widgets, Review the app, Share, About, Legal.
- An "upgrade / coins" upsell card (RewardedInterstitialManager unlocks content) with a NeonGreen gradient border — premium feel.

S11. NOTIFICATIONS (optional sheet)
- Bell sheet: goal alerts, match reminders, kick-off; unread highlighted; mark-all-read action.

S12. WIDGET (small phone mock with two widget sizes)
- 4×1: home crest + score + minute. 4×2: two matches, green accent.

────────────────────────────────────────────
4. STATES TO DESIGN FOR EVERY SCREEN
────────────────────────────────────────────
- Loading: skeleton shimmer cards (grey blocks pulsing), not spinners.
- Empty: friendly illustration (trophy/stadium line art), message, primary CTA.
- Error: subtle card with retry button; offline banner strip at top ("You're offline — showing cached scores").
- Pull-to-refresh.
- Ad slots clearly framed.

────────────────────────────────────────────
5. MOTION & MICRO-INTERACTIONS (document as notes)
- LIVE badge pulse (1s ease-in-out).
- Goal toast slide+fade (top, 3s).
- Tab switch: icon bounce + underline grow.
- Card press: 1–2% scale-down + surface lift.
- Countdown ticks visually (every second, slight weight shift).
- Nav transitions: slide-in from right for detail screens; player fades in full-screen.
- Skeleton shimmer sweep 1.2s loop.
Everything 200–350ms, ease-out. No bounce loops except the LIVE pulse.

────────────────────────────────────────────
6. ACCESSIBILITY & PLATFORM
- All text AA contrast on StadiumBlack (TextGrey is acceptable for secondary at 14pt+).
- Touch targets ≥ 48dp.
- TV focus states prominent (NeonGreen 2dp outline + scale).
- Android 16 dp status/nav bar, edge-to-edge, transparent status bar.

────────────────────────────────────────────
7. DELIVERABLES
- A full Figma file with 3 frames: PHONE FLOW (all screens connected with arrows), TABLET, TV.
- One shared COMPONENT LIBRARY page (all components above as reusable variants with light/dark state + pressed/disabled states).
- One DESIGN SYSTEM page: color palette swatches, type scale, spacing tokens, glow effect recipe.
- For each screen: a 1-line annotation describing behavior and state.
- Provide the exact color hex codes and a font pairing note (Barlow Condensed + DM Sans).
```

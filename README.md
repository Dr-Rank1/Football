# Football

**Watch Football Online Free** — a free Android app for browsing and watching streamable football matches. Built with Kotlin, Jetpack Compose, Media3 ExoPlayer, and API-Football.

Live scores, fixtures, leagues, and streams in one place. Only matches with configured stream URLs appear in the app.

## Features

- **Home, Live, Fixtures, Leagues** — four-tab navigation with a polished dark UI
- **Stream catalog** — Firebase Realtime Database (`/streams/{fixtureId}`) or optional `STREAM_CATALOG_URL`
- **Search** — teams, leagues, and matches with AI-powered suggestions (Gemini)
- **Favorites & reminders** — follow teams and get notified before kick-off
- **Widgets** — live score home-screen widget
- **Ads** — AdMob integration (test units in debug)

## Setup

1. Open the project in Android Studio (Ladybug or newer).
2. Copy `local.properties.example` to `local.properties` and set:
   - `sdk.dir` — path to your Android SDK
   - `API_FOOTBALL_KEY` — from [api-football.com](https://www.api-football.com/)
   - `GEMINI_API_KEY` — optional, for search suggestions
   - `STREAM_CATALOG_URL` — optional JSON catalog URL
3. Add `app/google-services.json` from your Firebase project (`com.rank.football`).
4. Sync Gradle and run on a device or emulator (API 24+).

## Architecture

- **MVVM** with Repository pattern
- **Jetpack Compose** UI
- **Media3 ExoPlayer** for HLS/DASH/MP4 playback
- **Room** for local cache (migrations only)
- **Retrofit + OkHttp** for API-Football
- **Firebase** Auth, Realtime Database, FCM

## Stream URLs

API-Football does not provide stream URLs. Configure streams in Firebase under `/streams/{fixtureId}` with `streamUrl`, `title`, `quality`, and `language`, or point `STREAM_CATALOG_URL` at a JSON catalog.

## License

Private project — all rights reserved unless stated otherwise.

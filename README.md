# Rank Football (GoalStream)

A free, electrifying Android app for browsing live scores, fixtures, leagues, and watching streamable football matches. Built with Kotlin, Jetpack Compose, Media3 ExoPlayer, and API-Football.

Live scores, fixtures, leagues, and streams in one place. Only matches with configured stream URLs appear in the app.

## Features

- **Home, Live, Fixtures, Leagues**: Four-tab navigation with a polished dark UI (StadiumBlack and PitchGreen accents).
- **Stream Catalog**: Firebase Realtime Database (`/streams/{fixtureId}`) or optional `STREAM_CATALOG_URL`.
- **Search**: Search teams, leagues, and matches with AI-powered suggestions (Gemini).
- **Match Details**: Real-time scores, timelines (goals, cards, substitutions), and match chat.
- **Favorites & Reminders**: Follow teams and get notified before kick-off.
- **Widgets**: Live score home-screen widget.
- **Ads**: AdMob integration (test units in debug mode).
- **Cast Support**: Stream matches directly to your TV using Chromecast.

## Setup Instructions

1. Open the project in Android Studio (Ladybug or newer).
2. Copy `local.properties.example` to `local.properties` and set the following properties:
   - `sdk.dir`: Path to your Android SDK.
   - `API_FOOTBALL_KEY`: Your API key from api-football.com.
   - `GEMINI_API_KEY`: Optional, for AI-powered search suggestions.
   - `STREAM_CATALOG_URL`: Optional JSON catalog URL for streams.
3. Add `app/google-services.json` from your Firebase project (package name: `com.rank.football`).
4. Sync Gradle and run on a device or emulator (API 24+).

## Architecture

- **MVVM Architecture** utilizing the Repository pattern.
- **Jetpack Compose** for building the native UI.
- **Media3 ExoPlayer** for robust HLS/DASH/MP4 video playback.
- **Room Database** for local caching (migrations only).
- **Retrofit + OkHttp** for API-Football network requests.
- **Firebase** integration including Authentication, Realtime Database, and Firebase Cloud Messaging (FCM).

## Stream URLs Configuration

API-Football does not provide stream URLs directly. You must configure streams in Firebase under `/streams/{fixtureId}` containing the following fields: `streamUrl`, `title`, `quality`, and `language`. Alternatively, you can point the `STREAM_CATALOG_URL` variable at a remote JSON catalog.

## Design System

The application features a premium sports energy design system focused on a dark theme ("StadiumBlack") with "PitchGreen" and "NeonGreen" highlights. The typography uses Barlow Condensed for display headlines and DM Sans for body text to give a broadcast-ticker energy. Check out the Figma AI Prompt in the repository for more details on the UI and design language.

## License

Private project. All rights reserved unless stated otherwise.

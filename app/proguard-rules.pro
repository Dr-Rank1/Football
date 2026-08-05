# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }

# Gson-serialized models used by Retrofit converters
-keep class com.rank.football.data.model.** { *; }
-keep class com.rank.football.data.firebase.** { *; }
-keep class com.rank.football.streaming.** { *; }

# Manifest-instantiated components (widgets, receivers, services) — instantiated by name
-keep class com.rank.football.widget.LiveScoreWidget { *; }
-keep class com.rank.football.widget.WidgetConfigureActivity { *; }
-keep class com.rank.football.widget.WidgetRefreshReceiver { *; }
-keep class com.rank.football.util.MatchReminderReceiver { *; }
-keep class com.rank.football.pip.PipActionsReceiver { *; }
-keep class com.rank.football.cast.CastOptionsProvider { *; }
-keep class com.rank.football.notifications.GoalStreamFirebaseService { *; }
-keep class com.rank.football.GoalStreamApp { *; }

# ExoPlayer / Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Firebase
-keep class com.google.firebase.** { *; }

# AdMob
-keep class com.google.android.gms.ads.** { *; }

# Gemini
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# NanoHTTPD
-keep class fi.iki.elonen.** { *; }

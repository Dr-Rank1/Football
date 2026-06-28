# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep class com.goalstream.app.data.model.** { *; }

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

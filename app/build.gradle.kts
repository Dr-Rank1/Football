import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.rank.football"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.rank.football"
        minSdk = 24
        targetSdk = 36
        versionCode = 6
        versionName = "5.1.0"

        buildConfigField(
            "String",
            "API_FOOTBALL_KEY",
            "\"${localProperties.getProperty("API_FOOTBALL_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "STREAM_CATALOG_URL",
            "\"${localProperties.getProperty("STREAM_CATALOG_URL", "")}\""
        )
        buildConfigField(
            "String",
            "ADMOB_BANNER_ID",
            "\"${localProperties.getProperty("ADMOB_BANNER_ID", "ca-app-pub-3940256099942544/6300978111")}\""
        )
        buildConfigField(
            "String",
            "ADMOB_INTERSTITIAL_ID",
            "\"${localProperties.getProperty("ADMOB_INTERSTITIAL_ID", "ca-app-pub-3940256099942544/1033173712")}\""
        )
        buildConfigField(
            "String",
            "ADMOB_REWARDED_ID",
            "\"${localProperties.getProperty("ADMOB_REWARDED_ID", "ca-app-pub-3940256099942544/5224354917")}\""
        )
        buildConfigField(
            "String",
            "ADMOB_REWARDED_INTERSTITIAL_ID",
            "\"${localProperties.getProperty("ADMOB_REWARDED_INTERSTITIAL_ID", "ca-app-pub-3940256099942544/5354046379")}\""
        )

        manifestPlaceholders["admobAppId"] = localProperties.getProperty(
            "ADMOB_APP_ID",
            "ca-app-pub-3940256099942544~3347511713"
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    bundle {
        language { enableSplit = true }
        density { enableSplit = true }
        abi { enableSplit = true }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        // Partially translated locales fall back to the default strings at runtime.
        disable += "MissingTranslation"
        // The Kotlin compiler (via @OptIn/@file:OptIn) is the authority on Media3's
        // UnstableApi opt-in; this lint check cannot see Kotlin OptIn annotations.
        disable += "UnsafeOptInUsageError"
    }
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.11.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-process:2.10.0")

    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    implementation("androidx.work:work-runtime-ktx:2.11.2")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("com.airbnb.android:lottie-compose:6.4.0")

    implementation("androidx.media3:media3-exoplayer:1.10.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.10.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.10.1")
    implementation("androidx.media3:media3-ui:1.10.1")
    implementation("androidx.media3:media3-datasource-okhttp:1.10.1")
    implementation("androidx.media3:media3-datasource:1.10.1")

    implementation("androidx.camera:camera-camera2:1.6.1")
    implementation("androidx.camera:camera-lifecycle:1.6.1")
    implementation("androidx.camera:camera-view:1.6.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.rometools:rome:1.18.0")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("com.google.android.gms:play-services-ads:25.3.0")

    implementation("com.google.android.gms:play-services-cast-framework:22.3.1")
    implementation("androidx.mediarouter:mediarouter:1.7.0")
    implementation("androidx.appcompat:appcompat:1.7.1")

    implementation(platform("com.google.firebase:firebase-bom:34.16.0"))
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-messaging")

    implementation("com.google.android.play:review-ktx:2.0.1")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("androidx.core:core-splashscreen:1.2.0")

    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")

    implementation(platform("androidx.compose:compose-bom:2026.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.core:core-ktx:1.17.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}

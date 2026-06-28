package com.rank.football.ui.accessibility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalContext
import android.view.accessibility.AccessibilityManager

/** Global reduced-motion flag for skipping animations. */
val LocalReduceMotion = staticCompositionLocalOf { false }

/** Returns true when the user has enabled reduced motion / animations. */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    val manager = context.getSystemService(AccessibilityManager::class.java)
    return manager?.isEnabled == true &&
        manager.getRecommendedTimeoutMillis(100, AccessibilityManager.FLAG_CONTENT_CONTROLS) > 100
}

/** Spells out a score for TalkBack, e.g. "Two goals to one". */
fun scoreContentDescription(home: Int, away: Int): String {
    fun word(n: Int) = when (n) {
        0 -> "zero"; 1 -> "one"; 2 -> "two"; 3 -> "three"; 4 -> "four"
        5 -> "five"; else -> n.toString()
    }
    return "${word(home)} goals to ${word(away)}"
}

package com.rank.football.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = PitchGreen,
    onPrimary = StadiumBlack,
    secondary = NeonGreen,
    onSecondary = StadiumBlack,
    background = StadiumBlack,
    onBackground = TextWhite,
    surface = SurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextGrey,
    error = LiveRed
)

@Composable
fun GoalStreamTheme(content: @Composable () -> Unit) {
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = false
        )
        systemUiController.setNavigationBarColor(
            color = StadiumBlack,
            darkIcons = false
        )
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = GoalStreamTypography,
        content = content
    )
}

package com.rank.football.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rank.football.ads.RewardedAdManager
import com.rank.football.data.model.displayScore
import com.rank.football.data.model.isLive
import com.rank.football.ui.screen.WatchScreen
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.TextWhite
import com.rank.football.util.Result
import com.rank.football.viewmodel.PlayerViewModel
import com.rank.football.viewmodel.PlayerViewModelFactory
import androidx.compose.ui.platform.LocalContext

/** Full-screen TV watch experience with D-pad controls and persistent score overlay. */
@Composable
fun TvWatchScreen(
    fixtureId: Int,
    onBack: () -> Unit,
    onEnterFullscreen: () -> Unit,
    onExitFullscreen: () -> Unit,
    rewardedAdManager: RewardedAdManager
) {
    val context = LocalContext.current
    val viewModel: PlayerViewModel = viewModel(
        factory = PlayerViewModelFactory(
            context.applicationContext as android.app.Application,
            fixtureId
        )
    )
    val fixtureResult by viewModel.fixture.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumBlack)
            .onKeyEvent { event ->
                when (event.key) {
                    Key.DirectionLeft -> { viewModel.seekBy(-10_000); true }
                    Key.DirectionRight -> { viewModel.seekBy(10_000); true }
                    Key.DirectionCenter -> { viewModel.togglePlayPause(); true }
                    Key.Back -> { onBack(); true }
                    else -> false
                }
            }
    ) {
        WatchScreen(
            fixtureId = fixtureId,
            onBack = onBack,
            onEnterFullscreen = onEnterFullscreen,
            onExitFullscreen = onExitFullscreen,
            rewardedAdManager = rewardedAdManager,
            isTvMode = true,
            viewModel = viewModel
        )
        (fixtureResult as? Result.Success)?.data?.let { f ->
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .background(StadiumBlack.copy(alpha = 0.8f))
                    .padding(12.dp)
            ) {
                Text(f.displayScore(), color = TextWhite, fontSize = 28.sp)
                if (f.isLive()) {
                    Text(
                        "LIVE ${f.fixture.status.elapsed ?: 0}'",
                        color = LiveRed,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

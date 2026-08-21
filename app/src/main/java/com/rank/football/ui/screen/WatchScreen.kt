@file:OptIn(ExperimentalMaterial3Api::class, androidx.media3.common.util.UnstableApi::class)

package com.rank.football.ui.screen

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.util.Rational
import android.view.ContextThemeWrapper
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.mediarouter.app.MediaRouteButton
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.google.android.gms.cast.framework.CastButtonFactory
import com.rank.football.R
import com.rank.football.ads.RewardedAdManager
import com.rank.football.cast.CastManager
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.displayScore
import com.rank.football.data.model.isFinished
import com.rank.football.data.model.isLive
import com.rank.football.data.model.isUpcoming
import com.rank.football.data.model.kickOffTime
import com.rank.football.pip.PipActionHandler
import com.rank.football.pip.PipHelper
import com.rank.football.ui.components.GoalAlert
import com.rank.football.ui.components.GoalAlertBus
import com.rank.football.ui.components.LiveBadge
import com.rank.football.ui.components.MatchDetailTabs
import com.rank.football.ui.theme.BarlowCondensed
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.DmSans
import com.rank.football.ui.theme.GoalYellow
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.SurfaceDark
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
import com.rank.football.util.Result
import com.rank.football.viewmodel.PlaybackStreamSource
import com.rank.football.viewmodel.PlayerState
import com.rank.football.viewmodel.PlayerViewModel
import com.rank.football.viewmodel.PlayerViewModelFactory
import kotlinx.coroutines.delay

@Composable
@OptIn(androidx.media3.common.util.UnstableApi::class)
fun WatchScreen(
    fixtureId: Int,
    onBack: () -> Unit,
    onEnterFullscreen: () -> Unit,
    onExitFullscreen: () -> Unit,
    rewardedAdManager: RewardedAdManager,
    onAdPlayingChanged: (Boolean) -> Unit = {},
    isTvMode: Boolean = false,
    isTablet: Boolean = false,
    viewModel: PlayerViewModel = viewModel(
        factory = PlayerViewModelFactory(
            LocalContext.current.applicationContext as android.app.Application,
            fixtureId
        )
    )
) {
    val context = LocalContext.current
    val activity = context as Activity
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val fixtureResult by viewModel.fixture.collectAsState()
    val playbackSources by viewModel.playbackSources.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val events by viewModel.events.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val lineups by viewModel.lineups.collectAsState()
    val newGoal by viewModel.newGoalDetected.collectAsState()
    val hdUnlocked by viewModel.hdUnlocked.collectAsState()
    val buffered by viewModel.bufferedPercentage.collectAsState()
    val sourceSwitchMessage by viewModel.sourceSwitchMessage.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    var controlsKey by remember { mutableIntStateOf(0) }
    var dismissOffset by remember { mutableFloatStateOf(0f) }
    var showSources by remember { mutableStateOf(false) }
    var matchFavorite by remember { mutableStateOf(false) }

    val fixture = (fixtureResult as? Result.Success)?.data
    val isLiveMatch = fixture?.isLive() == true
    val isPlayingVideo = playerState is PlayerState.Playing || playerState is PlayerState.Buffering
    val hasStream = playbackSources.isNotEmpty()

    fun selectSource(source: PlaybackStreamSource) {
        if (source.requiresRewardedAd && !hdUnlocked) {
            onAdPlayingChanged(true)
            rewardedAdManager.show(
                activity,
                onRewarded = {
                    viewModel.unlockHd()
                    viewModel.selectPlaybackSource(source)
                },
                onDismissed = { onAdPlayingChanged(false) },
                onUnavailable = {
                    onAdPlayingChanged(false)
                    viewModel.unlockHd()
                    viewModel.selectPlaybackSource(source)
                }
            )
            return
        }
        viewModel.selectPlaybackSource(source)
    }

    LaunchedEffect(isLandscape) {
        if (isLandscape) onEnterFullscreen() else onExitFullscreen()
    }

    LaunchedEffect(showControls, controlsKey, playerState) {
        if (showControls && isPlayingVideo) {
            delay(4000)
            showControls = false
        }
    }

    LaunchedEffect(isLiveMatch) {
        if (!isLiveMatch) return@LaunchedEffect
        while (true) {
            viewModel.refreshEvents()
            delay(60_000)
        }
    }

    LaunchedEffect(newGoal) {
        val goal = newGoal ?: return@LaunchedEffect
        val f = fixture ?: return@LaunchedEffect
        GoalAlertBus.emit(
            GoalAlert(
                fixtureId = f.fixture.id,
                scorer = goal.player?.name ?: "Goal",
                team = goal.team.name,
                minute = goal.time.elapsed ?: 0,
                score = f.displayScore(),
                competition = f.league.name
            )
        )
        viewModel.clearGoalCelebration()
    }

    LaunchedEffect(sourceSwitchMessage) {
        if (sourceSwitchMessage != null) {
            delay(2_500)
            viewModel.clearSourceSwitchMessage()
        }
    }

    BackHandler {
        viewModel.pause()
        onBack()
    }

    DisposableEffect(viewModel) {
        PipActionHandler.onRewind = { viewModel.seekBy(-10_000) }
        PipActionHandler.onPlayPause = { viewModel.togglePlayPause() }
        PipActionHandler.onFastForward = { viewModel.seekBy(10_000) }
        onDispose {
            viewModel.savePlaybackPosition()
            PipActionHandler.clear()
        }
    }

    var lastCastUrl by remember { mutableStateOf<String?>(null) }
    fun castCurrentStream(force: Boolean = false) {
        val url = viewModel.currentStreamUrl() ?: return
        if (!force && url == lastCastUrl) return
        val f = fixture
        val title = f?.let { "${it.teams.home.name} vs ${it.teams.away.name}" } ?: "Match"
        val poster = f?.teams?.home?.logo.orEmpty()
        CastManager.loadMedia(url, title, poster, isLive = f?.isLive() == true)
        lastCastUrl = url
    }

    DisposableEffect(Unit) {
        val listener = CastManager.attachSessionListener { castCurrentStream(force = true) }
        onDispose { CastManager.detachSessionListener(listener) }
    }

    LaunchedEffect(playbackSources) {
        if (CastManager.isConnected()) {
            castCurrentStream()
        }
    }

    val scale = (1f - (dismissOffset / 1200f)).coerceIn(0.85f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumBlack)
            .graphicsLayer {
                translationY = dismissOffset.coerceAtLeast(0f)
                scaleX = scale
                scaleY = scale
                alpha = (1f - dismissOffset / 900f).coerceIn(0.4f, 1f)
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dismissOffset > 180f) {
                            viewModel.pause()
                            onBack()
                        } else {
                            dismissOffset = 0f
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val next = dismissOffset + dragAmount * if (dragAmount > 0) 0.85f else 0.35f
                        dismissOffset = next.coerceAtLeast(0f)
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isLandscape) Modifier.weight(1f) else Modifier.aspectRatio(16f / 9f))
                .background(Color.Black)
                .onGloballyPositioned { coords ->
                    val topLeft = coords.localToWindow(Offset.Zero)
                    (activity as? com.rank.football.MainActivity)?.updatePipSourceRect(
                        android.graphics.Rect(
                            topLeft.x.toInt(),
                            topLeft.y.toInt(),
                            (topLeft.x + coords.size.width).toInt(),
                            (topLeft.y + coords.size.height).toInt()
                        )
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        showControls = !showControls
                        controlsKey++
                    })
                }
        ) {
            if (hasStream && playerState !is PlayerState.NoStream) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = viewModel.exoPlayer
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        }
                    },
                    update = { it.player = viewModel.exoPlayer }
                )
            } else {
                PitchLinesOverlay(modifier = Modifier.fillMaxSize())
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.55f),
                            0.28f to Color.Transparent,
                            0.72f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.78f)
                        )
                    )
            )

            when (playerState) {
                is PlayerState.Loading, is PlayerState.Buffering -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(0.42f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = PitchGreen,
                        trackColor = TextWhite.copy(alpha = 0.12f),
                        strokeCap = StrokeCap.Round
                    )
                }
                is PlayerState.NoStream -> {
                    NoBroadcastOverlay(
                        fixture = fixture,
                        onRetry = { viewModel.retryFromFirstSource() },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp)
                    )
                }
                is PlayerState.Error, is PlayerState.AllSourcesExhausted -> {
                    StreamErrorState(
                        onRetry = { viewModel.retryFromFirstSource() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> Unit
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showControls || !isPlayingVideo,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    PlayerTopBar(
                        fixture = fixture,
                        hasSources = playbackSources.size > 1,
                        onBack = {
                            viewModel.pause()
                            onBack()
                        },
                        onSources = { showSources = true },
                        isMuted = isMuted,
                        onToggleMute = { viewModel.toggleMute() },
                        onPip = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val params = PictureInPictureParams.Builder()
                                    .setAspectRatio(Rational(16, 9))
                                    .setActions(
                                        PipHelper.buildPipActions(
                                            activity,
                                            viewModel.exoPlayer.isPlaying
                                        )
                                    )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    params.setAutoEnterEnabled(true)
                                }
                                activity.enterPictureInPictureMode(params.build())
                            }
                        },
                        onFullscreen = {
                            activity.requestedOrientation = if (isLandscape) {
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            }
                        },
                        isLandscape = isLandscape
                    )

                    if (hasStream && playerState !is PlayerState.NoStream) {
                        Spacer(modifier = Modifier.weight(1f))
                        PlayerCenterControls(
                            isPlaying = playerState is PlayerState.Playing,
                            isLive = isLiveMatch,
                            onRewind = { viewModel.seekBy(-10_000) },
                            onPlayPause = { viewModel.togglePlayPause() },
                            onForward = { viewModel.seekBy(10_000) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (isPlayingVideo && buffered in 1..99) {
                        LinearProgressIndicator(
                            progress = { buffered / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = PitchGreen,
                            trackColor = TextWhite.copy(alpha = 0.12f),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }

            sourceSwitchMessage?.let { message ->
                Text(
                    text = message,
                    color = TextWhite,
                    fontFamily = DmSans,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 52.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceDark.copy(alpha = 0.92f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            if (!isLandscape) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TextWhite.copy(alpha = 0.28f))
                )
            }
        }

        if (!isLandscape) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
            ) {
                fixture?.let { f ->
                    MatchCenterCard(
                        fixture = f,
                        isFavorite = matchFavorite,
                        onToggleFavorite = { matchFavorite = !matchFavorite }
                    )
                }

                if (playbackSources.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WifiTethering,
                            contentDescription = null,
                            tint = PitchGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.watch_sources).uppercase(),
                            color = TextGrey,
                            fontFamily = BarlowCondensed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                        playbackSources.forEach { source ->
                            StreamSourceChip(
                                source = source,
                                selected = source.url == viewModel.currentStreamUrl(),
                                onSelect = { selectSource(source) }
                            )
                        }
                    }
                }

                MatchDetailTabs(
                    events = events,
                    statistics = statistics,
                    lineups = lineups,
                    currentMinute = fixture?.fixture?.status?.elapsed ?: 0,
                    fixtureId = fixtureId,
                    fixture = fixture,
                    onEventSeek = { minute ->
                        val duration = viewModel.exoPlayer.duration
                        if (duration > 0) {
                            val pos = (duration * (minute / 90f)).toLong()
                            viewModel.seekTo(pos)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showSources && playbackSources.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { showSources = false },
            containerColor = StadiumBlack,
            dragHandle = { BottomSheetDefaults.DragHandle(color = TextWhite.copy(alpha = 0.3f)) }
        ) {
            Text(
                text = stringResource(R.string.watch_sources).uppercase(),
                color = TextGrey,
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Column(modifier = Modifier.padding(bottom = 24.dp, top = 8.dp)) {
                playbackSources.forEach { source ->
                    val selected = source.url == viewModel.currentStreamUrl()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectSource(source)
                                showSources = false
                            }
                            .background(
                                if (selected) PitchGreen.copy(alpha = 0.08f) else Color.Transparent
                            )
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = source.label,
                                color = TextWhite,
                                fontFamily = DmSans,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = source.quality,
                                color = if (selected) PitchGreen else TextGrey,
                                fontFamily = BarlowCondensed,
                                fontSize = 12.sp
                            )
                        }
                        if (selected) {
                            Icon(Icons.Default.Check, null, tint = PitchGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerTopBar(
    fixture: FixtureItem?,
    hasSources: Boolean,
    onBack: () -> Unit,
    onSources: () -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onPip: () -> Unit,
    onFullscreen: () -> Unit,
    isLandscape: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
        }
        fixture?.let { f ->
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${f.teams.home.name} vs ${f.teams.away.name}",
                    color = TextWhite,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = f.league.name,
                        color = TextGrey,
                        fontFamily = DmSans,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 180.dp)
                    )
                    if (f.isLive()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        LiveBadge(minute = f.fixture.status.elapsed)
                    }
                }
            }
        } ?: Spacer(modifier = Modifier.weight(1f))

        if (hasSources) {
            TextButton(onClick = onSources) {
                Text(
                    text = "SOURCES",
                    color = PitchGreen,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        }
        IconButton(onClick = onToggleMute) {
            Icon(
                if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Mute",
                tint = TextWhite
            )
        }
        AndroidView(
            factory = { ctx ->
                runCatching {
                    val themed = ContextThemeWrapper(ctx, androidx.appcompat.R.style.Theme_AppCompat)
                    MediaRouteButton(themed).also { btn ->
                        CastButtonFactory.setUpMediaRouteButton(ctx.applicationContext, btn)
                    }
                }.getOrElse { View(ctx) }
            },
            modifier = Modifier.size(36.dp)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            IconButton(onClick = onPip) {
                Icon(Icons.Default.PictureInPictureAlt, contentDescription = "PiP", tint = TextWhite)
            }
        }
        IconButton(onClick = onFullscreen) {
            Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = TextWhite)
        }
    }
}

@Composable
private fun PlayerCenterControls(
    isPlaying: Boolean,
    isLive: Boolean,
    onRewind: () -> Unit,
    onPlayPause: () -> Unit,
    onForward: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isLive) {
            IconButton(onClick = onRewind) {
                Icon(Icons.Default.Replay10, contentDescription = "Back 10s", tint = TextWhite, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
        }
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(PitchGreen)
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = StadiumBlack,
                modifier = Modifier.size(36.dp)
            )
        }
        if (!isLive) {
            Spacer(modifier = Modifier.width(20.dp))
            IconButton(onClick = onForward) {
                Icon(Icons.Default.Forward10, contentDescription = "Forward 10s", tint = TextWhite, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun MatchCenterCard(
    fixture: FixtureItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    val statusLabel = when {
        fixture.isLive() -> fixture.fixture.status.long ?: "LIVE"
        fixture.isFinished() -> fixture.fixture.status.short
        fixture.isUpcoming() -> stringResource(R.string.watch_kickoff, fixture.kickOffTime())
        else -> fixture.fixture.status.short
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .border(1.dp, TextWhite.copy(alpha = 0.06f), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fixture.league.name.uppercase(),
                color = PitchGreen,
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.2.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = statusLabel,
                color = if (fixture.isLive()) LiveRed else TextGrey,
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.6.sp
            )
            MatchFavoriteToggle(isFavorite = isFavorite, onToggle = onToggleFavorite)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MatchTeamBlock(
                name = fixture.teams.home.name,
                logo = fixture.teams.home.logo,
                modifier = Modifier.weight(1f)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = fixture.displayScore(),
                    color = TextWhite,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Black,
                    fontSize = 42.sp,
                    letterSpacing = 1.sp
                )
                if (fixture.isLive()) {
                    LiveBadge(minute = fixture.fixture.status.elapsed, large = true)
                } else if (fixture.isUpcoming()) {
                    Text(
                        text = fixture.kickOffTime(),
                        color = GoalYellow,
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            MatchTeamBlock(
                name = fixture.teams.away.name,
                logo = fixture.teams.away.logo,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MatchTeamBlock(
    name: String,
    logo: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = logo,
            contentDescription = name,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(TextWhite.copy(alpha = 0.06f))
                .padding(8.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            color = TextWhite,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NoBroadcastOverlay(
    fixture: FixtureItem?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (fixture != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = fixture.teams.home.logo,
                    contentDescription = fixture.teams.home.name,
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = "  VS  ",
                    color = TextGrey,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                AsyncImage(
                    model = fixture.teams.away.logo,
                    contentDescription = fixture.teams.away.name,
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        Text(
            text = stringResource(R.string.watch_no_stream_title),
            color = TextWhite,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.watch_no_stream_body),
            color = TextGrey,
            fontFamily = DmSans,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onRetry) {
            Text(
                text = stringResource(R.string.watch_check_again),
                color = PitchGreen,
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PitchLinesOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val insetX = w * 0.045f
        val insetY = h * 0.05f
        val stroke = Stroke(width = w * 0.006f, cap = StrokeCap.Round)
        val line = Color.White.copy(alpha = 0.06f)

        val outer = Path().apply {
            addRoundRect(
                RoundRect(insetX, insetY, w - insetX, h - insetY, w * 0.02f, w * 0.02f)
            )
        }
        drawPath(outer, line, style = stroke)

        val midY = h / 2f
        drawLine(line, Offset(insetX, midY), Offset(w - insetX, midY), stroke.width, StrokeCap.Round)
        drawCircle(line, radius = w * 0.12f, center = Offset(w / 2f, midY), style = stroke)

        val boxW = w * 0.16f
        val boxH = h * 0.24f
        val penalty = Path().apply {
            moveTo(insetX, midY - boxH / 2f)
            lineTo(insetX + boxW, midY - boxH / 2f)
            lineTo(insetX + boxW, midY + boxH / 2f)
            lineTo(insetX, midY + boxH / 2f)
            moveTo(w - insetX, midY - boxH / 2f)
            lineTo(w - insetX - boxW, midY - boxH / 2f)
            lineTo(w - insetX - boxW, midY + boxH / 2f)
            lineTo(w - insetX, midY + boxH / 2f)
        }
        drawPath(penalty, line, style = stroke)
    }
}

@Composable
private fun MatchFavoriteToggle(
    isFavorite: Boolean,
    onToggle: () -> Unit
) {
    IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "Favorite",
            tint = if (isFavorite) LiveRed else TextGrey
        )
    }
}

@Composable
private fun StreamSourceChip(
    source: PlaybackStreamSource,
    selected: Boolean,
    onSelect: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onSelect,
        label = { Text("${source.label} · ${source.quality}") },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = SurfaceDark,
            labelColor = TextWhite,
            selectedContainerColor = PitchGreen.copy(alpha = 0.28f),
            selectedLabelColor = TextWhite
        )
    )
}

@Composable
private fun StreamErrorState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(StadiumBlack.copy(alpha = 0.92f))
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.stream_unavailable),
            color = TextWhite,
            fontFamily = DmSans,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = PitchGreen, contentColor = StadiumBlack)
        ) {
            Text(stringResource(R.string.retry), fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold)
        }
    }
}

@file:OptIn(ExperimentalMaterial3Api::class, androidx.media3.common.util.UnstableApi::class)

package com.rank.football.ui.screen

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.util.Rational
import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.rank.football.data.model.isLive
import com.rank.football.pip.PipActionHandler
import com.rank.football.pip.PipHelper
import com.rank.football.ui.components.GoalAlert
import com.rank.football.ui.components.GoalAlertBus
import com.rank.football.ui.components.LiveBadge
import com.rank.football.ui.components.MatchDetailTabs
import com.rank.football.ui.theme.BarlowCondensed
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.NeonGreen
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
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

    var showControls by remember { mutableStateOf(true) }
    var controlsKey by remember { mutableIntStateOf(0) }
    var dismissOffset by remember { mutableFloatStateOf(0f) }
    var showSources by remember { mutableStateOf(false) }
    var matchFavorite by remember { mutableStateOf(false) }

    val fixture = (fixtureResult as? Result.Success)?.data
    val isLiveMatch = fixture?.isLive() == true

    LaunchedEffect(isLandscape) {
        if (isLandscape) onEnterFullscreen() else onExitFullscreen()
    }

    LaunchedEffect(showControls, controlsKey) {
        if (showControls) {
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
                        // Rubber-band: resist upward, ease downward
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
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        showControls = !showControls
                        controlsKey++
                    })
                }
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = viewModel.exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                update = { it.player = viewModel.exoPlayer }
            )

            PitchLinesOverlay(modifier = Modifier.fillMaxSize())

            when (playerState) {
                is PlayerState.Loading, is PlayerState.Buffering -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(0.45f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = PitchGreen,
                        trackColor = TextWhite.copy(alpha = 0.12f),
                        strokeCap = StrokeCap.Round
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

            fixture?.let { f ->
                VideoScoreboardOverlay(
                    fixture = f,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StadiumBlack.copy(alpha = 0.75f))
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.pause(); onBack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextWhite)
                            }
                            fixture?.let { f ->
                                Column(modifier = Modifier.padding(start = 2.dp)) {
                                    Text(
                                        text = "${f.teams.home.name} vs ${f.teams.away.name}",
                                        color = TextWhite,
                                        fontFamily = BarlowCondensed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 200.dp)
                                    )
                                    if (f.isLive()) {
                                        LiveBadge(minute = f.fixture.status.elapsed)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            if (playbackSources.size > 1) {
                                Text(
                                    text = "SOURCES",
                                    color = PitchGreen,
                                    fontFamily = BarlowCondensed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier
                                        .clickable { showSources = true }
                                        .padding(horizontal = 8.dp)
                                )
                            }
                            IconButton(onClick = { viewModel.toggleMute() }) {
                                Icon(
                                    if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                    null,
                                    tint = PitchGreen
                                )
                            }
                            IconButton(onClick = { viewModel.togglePlayPause() }) {
                                Icon(Icons.Default.PlayArrow, null, tint = PitchGreen)
                            }
                            AndroidView(
                                factory = { ctx ->
                                    MediaRouteButton(ctx).also { btn ->
                                        CastButtonFactory.setUpMediaRouteButton(ctx.applicationContext, btn)
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                IconButton(onClick = {
                                    val params = PictureInPictureParams.Builder()
                                        .setAspectRatio(Rational(16, 9))
                                        .setActions(
                                            PipHelper.buildPipActions(
                                                activity,
                                                viewModel.exoPlayer.isPlaying
                                            )
                                        )
                                        .build()
                                    activity.enterPictureInPictureMode(params)
                                }) {
                                    Icon(Icons.Default.PictureInPictureAlt, null, tint = PitchGreen)
                                }
                            }
                            IconButton(onClick = {
                                activity.requestedOrientation = if (isLandscape) {
                                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                } else {
                                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                }
                            }) {
                                Icon(Icons.Default.Fullscreen, null, tint = PitchGreen)
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TextWhite.copy(alpha = 0.35f))
            )
        }

        if (!isLandscape) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
            fixture?.let { f ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(f.league.name, color = TextGrey, style = MaterialTheme.typography.labelMedium)
                        Text(
                            "${f.teams.home.name} vs ${f.teams.away.name}",
                            color = TextWhite,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        f.displayScore(),
                        color = NeonGreen,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (f.isLive()) LiveBadge(minute = f.fixture.status.elapsed)
                    MatchFavoriteToggle(
                        isFavorite = matchFavorite,
                        onToggle = { matchFavorite = !matchFavorite }
                    )
                }

                MatchDetailTabs(
                    events = events,
                    statistics = statistics,
                    lineups = lineups,
                    currentMinute = f.fixture.status.elapsed ?: 0,
                    fixtureId = fixtureId,
                    fixture = f,
                    onEventSeek = { minute ->
                        val duration = viewModel.exoPlayer.duration
                        if (duration > 0) {
                            val pos = (duration * (minute / 90f)).toLong()
                            viewModel.seekTo(pos)
                        }
                    }
                )
            }

            if (playbackSources.size > 1) {
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(playbackSources, key = { it.url }) { source ->
                        StreamSourceChip(
                            source = source,
                            selected = source.url == viewModel.currentStreamUrl(),
                            onSelect = { viewModel.selectPlaybackSource(source) }
                        )
                    }
                }
            }
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
                text = "SOURCES",
                color = TextGrey,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                playbackSources.forEach { source ->
                    val selected = source.url == viewModel.currentStreamUrl()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectPlaybackSource(source)
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
                                text = "${source.label} · ${source.quality}",
                                color = TextWhite,
                                fontWeight = FontWeight.Medium
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
private fun PitchLinesOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val insetX = w * 0.045f
        val insetY = h * 0.05f
        val stroke = Stroke(width = w * 0.006f, cap = StrokeCap.Round)
        val line = Color.White.copy(alpha = 0.04f)

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

        val goalW = w * 0.05f
        val goalH = h * 0.16f
        val goal = Path().apply {
            moveTo(insetX, midY - goalH / 2f)
            lineTo(insetX + goalW, midY - goalH / 2f)
            lineTo(insetX + goalW, midY + goalH / 2f)
            lineTo(insetX, midY + goalH / 2f)
            moveTo(w - insetX, midY - goalH / 2f)
            lineTo(w - insetX - goalW, midY - goalH / 2f)
            lineTo(w - insetX - goalW, midY + goalH / 2f)
            lineTo(w - insetX, midY + goalH / 2f)
        }
        drawPath(goal, line, style = stroke)
    }
}

@Composable
private fun MatchFavoriteToggle(
    isFavorite: Boolean,
    onToggle: () -> Unit
) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "Favorite",
            tint = if (isFavorite) LiveRed else TextGrey
        )
    }
}

@Composable
private fun VideoScoreboardOverlay(
    fixture: FixtureItem,
    modifier: Modifier = Modifier
) {
    val isLive = fixture.isLive()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f))
                )
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ScoreboardTeamSide(
                name = fixture.teams.home.name,
                logo = fixture.teams.home.logo,
                alignment = Alignment.Start,
                modifier = Modifier.weight(1f)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Text(
                    text = fixture.displayScore(),
                    color = TextWhite,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    letterSpacing = 2.sp
                )
                if (isLive) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LiveBadge(minute = fixture.fixture.status.elapsed)
                }
            }
            ScoreboardTeamSide(
                name = fixture.teams.away.name,
                logo = fixture.teams.away.logo,
                alignment = Alignment.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ScoreboardTeamSide(
    name: String,
    logo: String?,
    alignment: Alignment.Horizontal,
    modifier: Modifier = Modifier
) {
    val abbreviated = name.split(" ").firstOrNull()?.take(3)?.uppercase() ?: name.take(3).uppercase()
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (alignment == Alignment.End) Arrangement.End else Arrangement.Start
    ) {
        if (alignment == Alignment.End) {
            Text(
                text = abbreviated,
                color = TextWhite,
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(8.dp))
            AsyncImage(
                model = logo,
                contentDescription = name,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(TextGrey.copy(alpha = 0.25f)),
                contentScale = ContentScale.Fit
            )
        } else {
            AsyncImage(
                model = logo,
                contentDescription = name,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(TextGrey.copy(alpha = 0.25f)),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = abbreviated,
                color = TextWhite,
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
            containerColor = StadiumBlack,
            labelColor = TextWhite,
            selectedContainerColor = PitchGreen.copy(alpha = 0.3f),
            selectedLabelColor = TextWhite
        )
    )
}

@Composable
private fun StreamErrorState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .background(StadiumBlack.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.stream_unavailable), color = LiveRed)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

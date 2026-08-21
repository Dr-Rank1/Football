@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.rank.football.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.rank.football.GoalStreamApp
import com.rank.football.analytics.LocalAnalytics
import com.rank.football.data.local.AppDatabase
import com.rank.football.data.local.WatchHistoryEntry
import com.rank.football.data.api.RetrofitClient
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.FixtureStatisticsItem
import com.rank.football.data.model.LineupItem
import com.rank.football.data.model.StreamSource
import com.rank.football.data.model.FixtureEventItem
import com.rank.football.data.repository.FootballRepository
import com.rank.football.streaming.AdaptiveBitrateManager
import com.rank.football.streaming.StreamHealthMonitor
import com.rank.football.streaming.StreamHealthReport
import com.rank.football.streaming.StreamProxyServer
import com.rank.football.util.NetworkMonitor
import com.rank.football.util.NetworkType
import com.rank.football.util.Result
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PlayerState {
    data object Loading : PlayerState()
    data object Playing : PlayerState()
    data object Buffering : PlayerState()
    data class Error(val message: String) : PlayerState()
    data object AllSourcesExhausted : PlayerState()
    data object NoStream : PlayerState()
}

@OptIn(androidx.media3.common.util.UnstableApi::class)
class PlayerViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "GoalStream_Player"
    }

    private val footballRepository = FootballRepository(application)
    private val app = application as GoalStreamApp
    private val streamRepository = app.streamRepository

    private val fixtureId: Int = savedStateHandle.get<Int>("fixtureId") ?: 0

    private val _fixture = MutableStateFlow<Result<FixtureItem>>(Result.Loading)
    val fixture: StateFlow<Result<FixtureItem>> = _fixture.asStateFlow()

    private val _legacyStreams = MutableStateFlow<List<StreamSource>>(emptyList())
    val streams: StateFlow<List<StreamSource>> = _legacyStreams.asStateFlow()

    private val _playbackSources = MutableStateFlow<List<PlaybackStreamSource>>(emptyList())
    val playbackSources: StateFlow<List<PlaybackStreamSource>> = _playbackSources.asStateFlow()

    private val _selectedStream = MutableStateFlow<StreamSource?>(null)
    val selectedStream: StateFlow<StreamSource?> = _selectedStream.asStateFlow()

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Loading)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _volume = MutableStateFlow(1f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _hdUnlocked = MutableStateFlow(false)
    val hdUnlocked: StateFlow<Boolean> = _hdUnlocked.asStateFlow()

    private val _sourceSwitchMessage = MutableStateFlow<String?>(null)
    val sourceSwitchMessage: StateFlow<String?> = _sourceSwitchMessage.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _showSpeedSelector = MutableStateFlow(false)
    val showSpeedSelector: StateFlow<Boolean> = _showSpeedSelector.asStateFlow()

    private val _events = MutableStateFlow<List<FixtureEventItem>>(emptyList())
    val events: StateFlow<List<FixtureEventItem>> = _events.asStateFlow()

    private val _statistics = MutableStateFlow<Result<List<FixtureStatisticsItem>>>(Result.Loading)
    val statistics: StateFlow<Result<List<FixtureStatisticsItem>>> = _statistics.asStateFlow()

    private val _lineups = MutableStateFlow<Result<List<LineupItem>>>(Result.Loading)
    val lineups: StateFlow<Result<List<LineupItem>>> = _lineups.asStateFlow()

    private val _bufferedPercentage = MutableStateFlow(0)
    val bufferedPercentage: StateFlow<Int> = _bufferedPercentage.asStateFlow()

    private val database = AppDatabase.getInstance(application)
    private var watchStartTime = System.currentTimeMillis()
    private var previousGoalCount = 0

    private val _newGoalDetected = MutableStateFlow<FixtureEventItem?>(null)
    val newGoalDetected: StateFlow<FixtureEventItem?> = _newGoalDetected.asStateFlow()

    private var currentSourceIndex = 0
    private var allSources: List<PlaybackStreamSource> = emptyList()
    private var networkType: NetworkType = NetworkType.WIFI
    private var useProxyForCurrent = false

    private val dataSourceFactory = OkHttpDataSource.Factory(
        RetrofitClient.createStreamOkHttpClient()
    )

    val exoPlayer: ExoPlayer = buildPlayer(application)

    private val streamProxy = StreamProxyServer()
    private val healthMonitor = StreamHealthMonitor(application, exoPlayer, viewModelScope)
    private val abrManager = AdaptiveBitrateManager(application, exoPlayer, healthMonitor, viewModelScope)
    val streamHealthReport: StateFlow<StreamHealthReport> = healthMonitor.report

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Coroutine error", throwable)
    }

    /** App-scoped IO scope for the final watch-history write in [onCleared]. */
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        val savedPosition = savedStateHandle.get<Long>("playbackPosition") ?: 0L
        if (savedPosition > 0) {
            try {
                exoPlayer.seekTo(savedPosition)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore position", e)
            }
        }
        observeNetwork(application)
        loadFixture()
        loadMatchDetails()
        recordWatchStart(application)
        startBufferMonitor()
        streamProxy.startServer()
        healthMonitor.start()
        abrManager.start()
    }

    /** Wraps a remote stream URL through the local proxy server. */
    private fun proxiedUrl(url: String): String = streamProxy.buildProxyUrl(url)

    /** Inserts a watch history row when playback session begins. */
    private fun recordWatchStart(application: Application) {
        viewModelScope.launch(exceptionHandler) {
            val match = footballRepository.getFixtureById(fixtureId) ?: return@launch
            database.watchHistoryDao().insert(
                WatchHistoryEntry(
                    fixtureId = fixtureId,
                    homeTeam = match.teams.home.name,
                    awayTeam = match.teams.away.name,
                    leagueId = match.league.id,
                    leagueName = match.league.name,
                    watchedAt = System.currentTimeMillis(),
                    watchDurationSeconds = 0
                )
            )
            LocalAnalytics.log(application, "match_opened", mapOf("fixtureId" to fixtureId, "league" to match.league.name))
        }
    }

    /** Polls ExoPlayer buffer percentage every 2 seconds. */
    private fun startBufferMonitor() {
        viewModelScope.launch(exceptionHandler) {
            while (true) {
                try {
                    _bufferedPercentage.value = exoPlayer.bufferedPercentage
                } catch (_: Exception) {
                }
                kotlinx.coroutines.delay(2000)
            }
        }
    }

    /** Clears the goal celebration trigger after it has been shown. */
    fun clearGoalCelebration() {
        _newGoalDetected.value = null
    }

    private fun buildPlayer(application: Application): ExoPlayer {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(20_000, 60_000, 2_000, 4_000)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        return ExoPlayer.Builder(application)
            .setLoadControl(loadControl)
            .build()
            .apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _playerState.value = when (playbackState) {
                            Player.STATE_BUFFERING -> PlayerState.Buffering
                            Player.STATE_READY -> {
                                if (isPlaying) PlayerState.Playing else PlayerState.Loading
                            }
                            Player.STATE_IDLE -> PlayerState.Loading
                            Player.STATE_ENDED -> PlayerState.Playing
                            else -> PlayerState.Loading
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) _playerState.value = PlayerState.Playing
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e(TAG, "ExoPlayer error ${error.errorCodeName}: ${error.message}", error)
                        handlePlaybackError()
                    }
                })
            }
    }

    private fun observeNetwork(application: Application) {
        viewModelScope.launch(exceptionHandler) {
            NetworkMonitor.observeNetwork(application).collect { type ->
                networkType = type
                reorderSources()
            }
        }
    }

    private fun loadFixture() {
        viewModelScope.launch(exceptionHandler) {
            _fixture.value = Result.Loading
            _playerState.value = PlayerState.Loading
            try {
                app.syncStreamCatalog()
                val match = footballRepository.getFixtureById(fixtureId)
                if (match != null) {
                    _fixture.value = Result.Success(match)
                } else {
                    _fixture.value = Result.Error("Match not found")
                }
                val sources = streamRepository.getStreamsForFixture(fixtureId)
                Log.i(TAG, "Streams for $fixtureId: ${sources.size} catalog=${streamRepository.streamCount()}")
                _legacyStreams.value = sources
                allSources = sources.mapIndexed { index, source ->
                    source.toPlaybackSource(index + 1)
                }
                currentSourceIndex = 0
                useProxyForCurrent = false
                reorderSources()
                playCurrentSource()
            } catch (e: Exception) {
                Log.e(TAG, "loadFixture failed", e)
                _fixture.value = Result.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun loadMatchDetails() {
        viewModelScope.launch(exceptionHandler) {
            try {
                _events.value = footballRepository.getFixtureEvents(fixtureId)
            } catch (e: Exception) {
                Log.e(TAG, "Events load failed", e)
            }
            try {
                _statistics.value = Result.Loading
                _statistics.value = Result.Success(footballRepository.getFixtureStatistics(fixtureId))
            } catch (e: Exception) {
                _statistics.value = Result.Error(e.message ?: "Stats unavailable")
            }
            try {
                _lineups.value = Result.Loading
                _lineups.value = Result.Success(footballRepository.getFixtureLineups(fixtureId))
            } catch (e: Exception) {
                _lineups.value = Result.Error(e.message ?: "Lineups unavailable")
            }
        }
    }

    fun refreshEvents() {
        viewModelScope.launch(exceptionHandler) {
            try {
                val updated = footballRepository.getFixtureEvents(fixtureId)
                val goals = updated.filter { it.type.contains("Goal", true) }
                if (goals.size > previousGoalCount && previousGoalCount > 0) {
                    _newGoalDetected.value = goals.lastOrNull()
                }
                previousGoalCount = goals.size
                _events.value = updated
            } catch (e: Exception) {
                Log.e(TAG, "Events refresh failed", e)
            }
        }
    }

    private fun StreamSource.toPlaybackSource(serverNumber: Int): PlaybackStreamSource {
        val type = when {
            streamUrl.contains(".m3u8", ignoreCase = true) -> StreamType.HLS
            streamUrl.contains(".mpd", ignoreCase = true) -> StreamType.DASH
            else -> StreamType.MP4
        }
        return PlaybackStreamSource(
            url = streamUrl,
            quality = quality,
            label = title.ifBlank { "Server $serverNumber" },
            type = type,
            requiresRewardedAd = requiresRewardedAd
        )
    }

    private fun reorderSources() {
        val ordered = when (networkType) {
            NetworkType.MOBILE -> allSources.sortedBy { if (it.quality.contains("HD", true)) 1 else 0 }
            NetworkType.WIFI, NetworkType.NONE -> allSources.sortedBy { if (it.quality.contains("HD", true)) 0 else 1 }
        }
        _playbackSources.value = ordered
        if (ordered.isNotEmpty() && currentSourceIndex >= ordered.size) {
            currentSourceIndex = 0
        }
    }

    fun selectStream(source: StreamSource) {
        if (source.requiresRewardedAd && !_hdUnlocked.value) {
            _selectedStream.value = source
            return
        }
        _selectedStream.value = source
        val index = _playbackSources.value.indexOfFirst { it.url == source.streamUrl }
        if (index >= 0) {
            currentSourceIndex = index
            playCurrentSource()
        }
    }

    fun selectPlaybackSource(source: PlaybackStreamSource) {
        if (source.requiresRewardedAd && !_hdUnlocked.value) return
        val index = _playbackSources.value.indexOfFirst { it.url == source.url }
        if (index >= 0) {
            currentSourceIndex = index
            useProxyForCurrent = false
            playCurrentSource()
        }
    }

    /** URL of the stream currently selected for local / Cast playback. */
    fun currentStreamUrl(): String? = _playbackSources.value.getOrNull(currentSourceIndex)?.url

    fun unlockHd() {
        _hdUnlocked.value = true
        _selectedStream.value?.let { selectStream(it) }
    }

    private fun playCurrentSource() {
        val sources = _playbackSources.value
        if (sources.isEmpty()) {
            _playerState.value = PlayerState.NoStream
            return
        }
        val source = sources.getOrNull(currentSourceIndex) ?: return
        _playerState.value = PlayerState.Loading
        try {
            val mediaSource = createMediaSource(source, useProxyForCurrent)
            Log.i(TAG, "Playing ${source.label} proxy=$useProxyForCurrent url=${source.url}")
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            exoPlayer.setPlaybackSpeed(_playbackSpeed.value)
        } catch (e: Exception) {
            Log.e(TAG, "playCurrentSource failed: ${source.url}", e)
            handlePlaybackError()
        }
    }

    private fun createMediaSource(source: PlaybackStreamSource, proxied: Boolean): MediaSource {
        val uri = if (proxied) proxiedUrl(source.url) else source.url
        val mediaItem = MediaItem.fromUri(uri)
        return when (source.type) {
            StreamType.HLS -> HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true)
                .createMediaSource(mediaItem)
            StreamType.DASH -> DashMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            StreamType.MP4 -> ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        }
    }

    private fun handlePlaybackError() {
        val failed = _playbackSources.value.getOrNull(currentSourceIndex)
        Log.w(TAG, "Stream failed proxy=$useProxyForCurrent: ${failed?.url}")

        if (!useProxyForCurrent && failed != null) {
            useProxyForCurrent = true
            playCurrentSource()
            return
        }

        useProxyForCurrent = false
        currentSourceIndex++
        val next = _playbackSources.value.getOrNull(currentSourceIndex)
        if (next != null) {
            _sourceSwitchMessage.value = "Switching to ${next.label}..."
            try {
                exoPlayer.stop()
                exoPlayer.seekTo(0)
                playCurrentSource()
            } catch (e: Exception) {
                Log.e(TAG, "Fallback play failed", e)
                _playerState.value = PlayerState.AllSourcesExhausted
            }
        } else {
            _playerState.value = PlayerState.AllSourcesExhausted
        }
    }

    fun clearSourceSwitchMessage() {
        _sourceSwitchMessage.value = null
    }

    fun retryFromFirstSource() {
        viewModelScope.launch(exceptionHandler) {
            currentSourceIndex = 0
            useProxyForCurrent = false
            _playerState.value = PlayerState.Loading
            app.syncStreamCatalog()
            val sources = streamRepository.getStreamsForFixture(fixtureId)
            _legacyStreams.value = sources
            allSources = sources.mapIndexed { index, source ->
                source.toPlaybackSource(index + 1)
            }
            reorderSources()
            playCurrentSource()
        }
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        applyVolume()
    }

    fun setVolume(value: Float) {
        _volume.value = value.coerceIn(0f, 1f)
        if (_volume.value > 0f) _isMuted.value = false
        applyVolume()
    }

    private fun applyVolume() {
        try {
            exoPlayer.volume = if (_isMuted.value) 0f else _volume.value
        } catch (e: Exception) {
            Log.e(TAG, "setVolume failed", e)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        _showSpeedSelector.value = false
        try {
            exoPlayer.setPlaybackSpeed(speed)
        } catch (e: Exception) {
            Log.e(TAG, "setPlaybackSpeed failed", e)
        }
    }

    fun toggleSpeedSelector(show: Boolean) {
        _showSpeedSelector.value = show
    }

    fun seekBy(deltaMs: Long) {
        try {
            val newPos = (exoPlayer.currentPosition + deltaMs).coerceAtLeast(0)
            exoPlayer.seekTo(newPos)
        } catch (e: Exception) {
            Log.e(TAG, "seekBy failed", e)
        }
    }

    fun seekTo(positionMs: Long) {
        try {
            exoPlayer.seekTo(positionMs.coerceAtLeast(0))
        } catch (e: Exception) {
            Log.e(TAG, "seekTo failed", e)
        }
    }

    fun savePlaybackPosition() {
        savedStateHandle["playbackPosition"] = exoPlayer.currentPosition
    }

    fun pause() {
        try {
            exoPlayer.playWhenReady = false
        } catch (e: Exception) {
            Log.e(TAG, "pause failed", e)
        }
    }

    fun togglePlayPause() {
        try {
            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
        } catch (e: Exception) {
            Log.e(TAG, "togglePlayPause failed", e)
        }
    }

    override fun onCleared() {
        savePlaybackPosition()
        val duration = ((System.currentTimeMillis() - watchStartTime) / 1000).toInt()
        ioScope.launch {
            try {
                database.watchHistoryDao().updateDuration(fixtureId, duration)
            } catch (e: Exception) {
                Log.e(TAG, "update watch duration failed", e)
            }
        }
        healthMonitor.stop()
        abrManager.stop()
        try {
            exoPlayer.release()
        } catch (e: Exception) {
            Log.e(TAG, "release failed", e)
        }
        streamProxy.stopServer()
        super.onCleared()
    }
}

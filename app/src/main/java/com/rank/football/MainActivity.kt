package com.rank.football

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rank.football.data.model.FixtureItem
import com.rank.football.ui.components.GoalToastHost
import com.rank.football.ui.components.LiveGoalMonitor
import com.rank.football.ui.components.LiveMatchBus
import com.rank.football.ui.components.MiniPipPlayer
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rank.football.ads.AppOpenAdManager
import com.rank.football.ads.InterstitialAdManager
import com.rank.football.ads.RewardedAdManager
import com.rank.football.data.local.AppPreferences
import com.rank.football.data.local.OnboardingPreference
import com.rank.football.pip.PipHelper
import com.rank.football.review.InAppReviewManager
import com.rank.football.ui.components.NetworkBanner
import com.rank.football.ui.components.StreamBottomBar
import com.rank.football.ui.components.StreamNavItem
import com.rank.football.ui.privacy.ConsentScreen
import com.rank.football.ui.screen.FixturesScreen
import com.rank.football.ui.screen.HomeScreen
import com.rank.football.ui.screen.LeaguesScreen
import com.rank.football.ui.screen.LiveScreen
import com.rank.football.ui.screen.OnboardingScreen
import com.rank.football.ui.screen.SearchScreen
import com.rank.football.ui.screen.SettingsScreen
import com.rank.football.ui.screen.WatchScreen
import com.rank.football.ui.screen.StandingsScreen
import com.rank.football.ui.settings.LanguageSettingsScreen
import com.rank.football.ui.debug.AnalyticsDashboardScreen
import com.rank.football.ui.settings.RevenueStatsScreen
import com.rank.football.ui.tv.TvWatchScreen
import com.rank.football.BuildConfig
import com.rank.football.monetization.RewardedInterstitialManager
import com.rank.football.ui.theme.GoalStreamTheme
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.update.InAppUpdateManager
import com.rank.football.util.NetworkMonitor
import com.rank.football.util.ReminderHelper
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var interstitialAdManager: InterstitialAdManager
    private lateinit var rewardedAdManager: RewardedAdManager
    private lateinit var appOpenAdManager: AppOpenAdManager
    private var isOnWatchScreen = false
    private var isPlaying = false
    private var isAdPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        ReminderHelper.createNotificationChannel(this)
        interstitialAdManager = InterstitialAdManager(this)
        rewardedAdManager = RewardedAdManager(this)
        appOpenAdManager = AppOpenAdManager(this)
        interstitialAdManager.loadAd()
        rewardedAdManager.loadAd()

        enableEdgeToEdge()
        setContent {
            GoalStreamTheme {
                GoalStreamRoot(
                    interstitialAdManager = interstitialAdManager,
                    rewardedAdManager = rewardedAdManager,
                    appOpenAdManager = appOpenAdManager,
                    onWatchScreenChanged = { onWatch ->
                        isOnWatchScreen = onWatch
                        appOpenAdManager.setOnWatchScreen(onWatch)
                        if (!onWatch) interstitialAdManager.markLeftWatch()
                    },
                    onPlayingChanged = { playing -> isPlaying = playing },
                    onAdPlayingChanged = { isAdPlaying = it },
                    deepLinkFixtureId = parseFixtureId(intent)
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        parseFixtureId(intent)?.let { id ->
            // Navigation handled via recomposition when intent extra updates
        }
    }

    /** Parses fixture ID from intent extras or goalstream://watch/{id} deep link. */
    private fun parseFixtureId(intent: Intent?): Int? {
        intent ?: return null
        intent.getIntExtra("fixtureId", -1).takeIf { it > 0 }?.let { return it }
        val uri: Uri? = intent.data
        if (uri?.scheme == "goalstream" && uri.host == "watch") {
            return uri.lastPathSegment?.toIntOrNull()
        }
        return null
    }

    override fun onStart() {
        super.onStart()
        if (!isOnWatchScreen) {
            appOpenAdManager.showAdIfAvailable(this)
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isOnWatchScreen && isPlaying && !isAdPlaying && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                params.setActions(PipHelper.buildPipActions(this, isPlaying))
            }
            enterPictureInPictureMode(params.build())
        }
    }

    fun setImmersiveMode(enabled: Boolean) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (enabled) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            controller.show(WindowInsetsCompat.Type.systemBars())
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
    }
}

@Composable
private fun GoalStreamRoot(
    interstitialAdManager: InterstitialAdManager,
    rewardedAdManager: RewardedAdManager,
    appOpenAdManager: AppOpenAdManager,
    onWatchScreenChanged: (Boolean) -> Unit,
    onPlayingChanged: (Boolean) -> Unit,
    onAdPlayingChanged: (Boolean) -> Unit,
    deepLinkFixtureId: Int?
) {
    val context = LocalContext.current
    var forceShowMain by remember { mutableStateOf(false) }
    val consentGiven by AppPreferences.consentGiven(context).collectAsState(initial = false)
    val onboardingComplete by OnboardingPreference.isOnboardingComplete(context)
        .collectAsState(initial = false)

    when {
        !consentGiven && !forceShowMain -> ConsentScreen(onAccepted = { forceShowMain = true })
        !onboardingComplete && !forceShowMain -> OnboardingScreen(onComplete = { forceShowMain = true })
        else -> GoalStreamNav(
            interstitialAdManager = interstitialAdManager,
            rewardedAdManager = rewardedAdManager,
            onWatchScreenChanged = onWatchScreenChanged,
            onPlayingChanged = onPlayingChanged,
            onAdPlayingChanged = onAdPlayingChanged,
            deepLinkFixtureId = deepLinkFixtureId
        )
    }
}

private sealed class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Home : BottomNavItem("home", R.string.nav_home, Icons.Default.Home)
    data object Live : BottomNavItem("live", R.string.nav_live, Icons.Default.Sensors)
    data object Fixtures : BottomNavItem("fixtures", R.string.nav_fixtures, Icons.Default.CalendarMonth)
    data object Leagues : BottomNavItem("leagues", R.string.nav_leagues, Icons.Default.EmojiEvents)
}

@Composable
private fun GoalStreamNav(
    interstitialAdManager: InterstitialAdManager,
    rewardedAdManager: RewardedAdManager,
    onWatchScreenChanged: (Boolean) -> Unit,
    onPlayingChanged: (Boolean) -> Unit,
    onAdPlayingChanged: (Boolean) -> Unit,
    deepLinkFixtureId: Int?
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != null &&
        !currentRoute.startsWith("watch") &&
        !currentRoute.startsWith("standings/") &&
        currentRoute !in setOf(
            "search", "settings", "language", "revenue", "analytics-dashboard"
        )
    var liveMatchCount by remember { mutableIntStateOf(0) }
    var pipFixture by remember { mutableStateOf<FixtureItem?>(null) }
    var latestLiveMatches by remember { mutableStateOf<List<FixtureItem>>(emptyList()) }
    val activity = LocalContext.current as MainActivity
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val isTablet = config.screenWidthDp >= 600
    val isTv = config.uiMode and android.content.res.Configuration.UI_MODE_TYPE_MASK ==
        android.content.res.Configuration.UI_MODE_TYPE_TELEVISION
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isOnline by NetworkMonitor.observeNetwork(context)
        .map { it != com.rank.football.util.NetworkType.NONE }
        .collectAsState(initial = true)

    val bottomItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Live,
        BottomNavItem.Fixtures,
        BottomNavItem.Leagues
    )

    val rewardedInterstitial = remember { RewardedInterstitialManager(context) }
    LaunchedEffect(Unit) { rewardedInterstitial.loadAd() }

    LiveGoalMonitor(enabled = true)
    val busMatches by LiveMatchBus.matches.collectAsState(initial = emptyList())
    LaunchedEffect(busMatches) {
        if (busMatches.isNotEmpty() || liveMatchCount == 0) {
            liveMatchCount = busMatches.size
            latestLiveMatches = busMatches
        }
    }

    val navigateToWatch: (Int) -> Unit = { fixtureId ->
        // Keep another live match as PiP when opening watch
        val other = latestLiveMatches.firstOrNull { it.fixture.id != fixtureId }
        if (other != null && liveMatchCount >= 2) {
            pipFixture = other
        }
        interstitialAdManager.showBeforeNavigation(activity) {
            navController.navigate("watch/$fixtureId")
        }
    }

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo("home") { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    LaunchedEffect(deepLinkFixtureId) {
        deepLinkFixtureId?.let { navigateToWatch(it) }
    }

    LaunchedEffect(currentRoute) {
        if (currentRoute == "home") {
            InAppReviewManager.maybeRequestReview(activity)
            InAppUpdateManager.checkForUpdate(activity) {
                scope.launch {
                    snackbarHostState.showSnackbar("Update ready — Restart to apply")
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = StadiumBlack,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    StreamBottomBar(
                        items = bottomItems.map { item ->
                            StreamNavItem(
                                route = item.route,
                                label = stringResource(item.labelRes),
                                icon = item.icon,
                                liveCount = if (item == BottomNavItem.Live) liveMatchCount else -1
                            )
                        },
                        selectedRoute = currentRoute,
                        onSelect = { route ->
                            navController.navigate(route) {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(if (showBottomBar) padding else PaddingValues(0.dp))
            ) {
                composable("home") {
                    onWatchScreenChanged(false)
                    HomeScreen(
                        onMatchClick = navigateToWatch,
                        onSearchClick = { navController.navigate("search") },
                        onSettingsClick = { navController.navigate("settings") },
                        onBrowseFixtures = { navigateToTab("fixtures") },
                        onBrowseLeagues = { navigateToTab("leagues") }
                    )
                }
                composable("search") {
                    onWatchScreenChanged(false)
                    SearchScreen(
                        onMatchClick = navigateToWatch,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("live") {
                    onWatchScreenChanged(false)
                    LiveScreen(
                        onMatchClick = navigateToWatch,
                        onLiveCountChanged = { liveMatchCount = it },
                        onLiveMatchesChanged = { latestLiveMatches = it },
                        onBrowseFixtures = { navigateToTab("fixtures") },
                        onBrowseLeagues = { navigateToTab("leagues") }
                    )
                }
                composable("fixtures") {
                    onWatchScreenChanged(false)
                    FixturesScreen(
                        onMatchClick = navigateToWatch,
                        onBrowseLeagues = { navigateToTab("leagues") }
                    )
                }
                composable("leagues") {
                    onWatchScreenChanged(false)
                    LeaguesScreen(
                        onMatchClick = navigateToWatch,
                        onStandingsClick = { navController.navigate("standings/$it") },
                        onBrowseFixtures = { navigateToTab("fixtures") }
                    )
                }
                composable("settings") {
                    onWatchScreenChanged(false)
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onLanguageClick = { navController.navigate("language") },
                        onDebugClick = { navController.navigate("analytics-dashboard") },
                        onRevenueClick = { navController.navigate("revenue") }
                    )
                }
                composable(
                    route = "standings/{leagueId}",
                    arguments = listOf(navArgument("leagueId") { type = NavType.IntType })
                ) {
                    val leagueId = it.arguments?.getInt("leagueId") ?: 0
                    StandingsScreen(leagueId = leagueId, onBack = { navController.popBackStack() })
                }
                composable("language") {
                    LanguageSettingsScreen(onBack = { navController.popBackStack() })
                }
                if (BuildConfig.DEBUG) {
                    composable("revenue") {
                        RevenueStatsScreen(onBack = { navController.popBackStack() })
                    }
                    composable("analytics-dashboard") {
                        AnalyticsDashboardScreen()
                    }
                }
                composable(
                    route = "watch/{fixtureId}",
                    arguments = listOf(navArgument("fixtureId") { type = NavType.IntType })
                ) {
                    onWatchScreenChanged(true)
                    onPlayingChanged(true)
                    val fixtureId = it.arguments?.getInt("fixtureId") ?: 0
                    if (isTv) {
                        TvWatchScreen(
                            fixtureId = fixtureId,
                            onBack = {
                                onPlayingChanged(false)
                                activity.setImmersiveMode(false)
                                navController.popBackStack()
                            },
                            onEnterFullscreen = { activity.setImmersiveMode(true) },
                            onExitFullscreen = { activity.setImmersiveMode(false) },
                            rewardedAdManager = rewardedAdManager
                        )
                    } else {
                        WatchScreen(
                            fixtureId = fixtureId,
                            onBack = {
                                onPlayingChanged(false)
                                activity.setImmersiveMode(false)
                                navController.popBackStack()
                            },
                            onEnterFullscreen = { activity.setImmersiveMode(true) },
                            onExitFullscreen = { activity.setImmersiveMode(false) },
                            rewardedAdManager = rewardedAdManager,
                            onAdPlayingChanged = onAdPlayingChanged,
                            isTablet = isTablet
                        )
                    }
                }
            }
        }
            GoalToastHost(
                onOpenMatch = navigateToWatch,
                hostModifier = Modifier.align(Alignment.TopCenter)
            )
            pipFixture?.let { pip ->
                if (currentRoute?.startsWith("watch") != true) {
                    MiniPipPlayer(
                        fixture = pip,
                        onExpand = {
                            val id = pip.fixture.id
                            pipFixture = null
                            navigateToWatch(id)
                        },
                        onDismiss = { pipFixture = null },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = if (showBottomBar) 8.dp else 24.dp)
                    )
                }
            }
        }
        NetworkBanner(visible = !isOnline)
    }
}


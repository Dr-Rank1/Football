package com.rank.football.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.sin
import com.rank.football.data.model.FixtureItem
import com.rank.football.data.model.isLive
import com.rank.football.data.model.kickOffTime
import com.rank.football.ui.theme.BarlowCondensed
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.CompetitionColors
import com.rank.football.ui.theme.DmSans
import com.rank.football.ui.theme.GoalYellow
import com.rank.football.ui.theme.LiveRed
import com.rank.football.ui.theme.NeonGreen
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.SurfaceDark
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val CODE_OVERRIDES = mapOf(
    "manchester city" to "MCI", "man city" to "MCI", "arsenal" to "ARS",
    "real madrid" to "RMA", "barcelona" to "BAR", "bayern" to "BAY",
    "bayern munich" to "BAY", "borussia dortmund" to "BVB", "psg" to "PSG",
    "paris saint germain" to "PSG", "marseille" to "MAR", "inter milan" to "INT",
    "inter" to "INT", "ac milan" to "ACM", "milan" to "ACM", "atletico madrid" to "ATM",
    "atlético madrid" to "ATM", "sevilla" to "SEV", "liverpool" to "LIV",
    "chelsea" to "CHE", "ajax" to "AJX", "psv" to "PSV", "manchester united" to "MUN",
    "man united" to "MUN", "newcastle" to "NEW", "tottenham" to "TOT",
    "aston villa" to "AVL", "burnley" to "BUR", "luton town" to "LUT", "luton" to "LUT"
)

private val CODE_COLORS: Map<String, Pair<Color, Color>> = mapOf(
    "MCI" to (Color(0xFF6CABDD) to Color(0xFF1C2C5B)),
    "ARS" to (Color(0xFFEF0107) to Color(0xFF9C824A)),
    "RMA" to (Color(0xFFFEBE10) to Color(0xFF00529F)),
    "BAR" to (Color(0xFFA50044) to Color(0xFF004D98)),
    "BAY" to (Color(0xFFDC052D) to Color(0xFF0066B2)),
    "BVB" to (Color(0xFFFDE100) to Color(0xFF1C1C1B)),
    "PSG" to (Color(0xFF004170) to Color(0xFFDA291C)),
    "MAR" to (Color(0xFF009AC7) to Color(0xFFFFFFFF)),
    "INT" to (Color(0xFF0068A8) to Color(0xFF010E80)),
    "ACM" to (Color(0xFFFB090B) to Color(0xFF000000)),
    "ATM" to (Color(0xFFCB3524) to Color(0xFFFFFFFF)),
    "SEV" to (Color(0xFFD4AF37) to Color(0xFFFFFFFF)),
    "LIV" to (Color(0xFFC8102E) to Color(0xFFF6EB61)),
    "CHE" to (Color(0xFF034694) to Color(0xFFFFFFFF)),
    "AJX" to (Color(0xFFD2122E) to Color(0xFFFFFFFF)),
    "PSV" to (Color(0xFFED1C24) to Color(0xFFFFFFFF)),
    "MUN" to (Color(0xFFDA291C) to Color(0xFFFBE122)),
    "NEW" to (Color(0xFF241F20) to Color(0xFF41B3A3)),
    "TOT" to (Color(0xFF132257) to Color(0xFFFFFFFF)),
    "AVL" to (Color(0xFF95BFE5) to Color(0xFF7B003C)),
    "BUR" to (Color(0xFF6C1D45) to Color(0xFF99D6EA)),
    "LUT" to (Color(0xFFF78F1E) to Color(0xFF231F20))
)

fun teamCodeOf(name: String): String {
    val key = name.trim().lowercase()
    CODE_OVERRIDES[key]?.let { return it }
    val parts = name.uppercase()
        .replace(Regex("[^A-Z0-9 ]"), "")
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "TBD"
        parts.size >= 3 -> parts.take(3).map { it.first() }.joinToString("")
        parts.size == 2 -> parts[0].first() + parts[1].take(2)
        else -> parts[0].take(3).padEnd(3, 'X')
    }
}

fun teamColorsOf(name: String): Pair<Color, Color> {
    val code = teamCodeOf(name)
    CODE_COLORS[code]?.let { return it }
    return Color(0xFF2A2A3A) to TextWhite.copy(alpha = 0.2f)
}

/** Initials/crest circle: real logo when available, else gradient code tile (UI reference). */
@Composable
fun TeamCrest(
    name: String,
    logo: String?,
    size: Dp,
    modifier: Modifier = Modifier
) {
    if (!logo.isNullOrBlank()) {
        AsyncImage(
            model = logo,
            contentDescription = name,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(TextGrey.copy(alpha = 0.2f)),
            contentScale = ContentScale.Fit
        )
    } else {
        val colors = remember(name) { teamColorsOf(name) }
        val code = remember(name) { teamCodeOf(name) }
        val tileShape = RoundedCornerShape(size.value * 0.22f)
        Box(
            modifier = modifier
                .size(size)
                .shadow(
                    elevation = 8.dp,
                    shape = tileShape,
                    ambientColor = colors.first.copy(alpha = 0.35f),
                    spotColor = colors.first.copy(alpha = 0.35f)
                )
                .clip(tileShape)
                .background(
                    Brush.linearGradient(
                        colorStops = arrayOf(0.55f to colors.first, 1f to colors.second.copy(alpha = 0.2f))
                    )
                )
                .border(1.dp, colors.first.copy(alpha = 0.53f), tileShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = code,
                color = TextWhite,
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Black,
                fontSize = (size.value * 0.28f).sp,
                letterSpacing = (-0.5).sp,
                maxLines = 1
            )
        }
    }
}

// ─── STATUS CHIP ──────────────────────────────────────────────────────────────

/** Small round status chip used on match cards and detail headers (UI reference). */
@Composable
fun StatusChip(
    status: String,
    elapsed: Int?,
    modifier: Modifier = Modifier,
    kickoff: String? = null
) {
    val shape = RoundedCornerShape(6.dp)
    val live = status in setOf("1H", "2H", "ET", "LIVE", "PEN")
    val pulse = rememberInfiniteTransition(label = "chip_dot")
    val alpha by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "chip_dot_alpha"
    )

    val chipBg = when (status) {
        "HT" -> GoalYellow
        "PEN", "P" -> Color(0xFFAA00FF)
        "ET" -> Color(0xFFFF6D00)
        "1H", "2H", "LIVE", "BR", "INT", "BT" -> LiveRed
        else -> Color.Transparent
    }
    val chipBorder = when (status) {
        "NS" -> PitchGreen.copy(alpha = 0.33f)
        else -> Color(0xFF2A2A3A)
    }

    Row(
        modifier = modifier
            .clip(shape)
            .background(chipBg)
            .border(1.dp, chipBorder, shape)
            .padding(horizontal = 7.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (live) {
            Box(
                modifier = Modifier
                    .padding(end = 5.dp)
                    .size(5.dp)
                    .graphicsLayer { this.alpha = alpha }
                    .background(TextWhite, CircleShape)
            )
        }
        val label = when (status) {
            "HT" -> "HT"
            "PEN", "P" -> "PEN"
            "LIVE" -> "LIVE"
            "ET", "1H", "2H" -> "${elapsed ?: 0}'"
            "FT", "FF", "WO", "AWD" -> "FT"
            "NS" -> "NS"
            else -> status
        }
        Text(
            text = label,
            color = when {
                status == "HT" -> StadiumBlack
                status == "NS" -> PitchGreen
                status == "LIVE" -> TextWhite
                live -> TextWhite
                else -> TextGrey
            },
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            maxLines = 1
        )
    }
}

// ─── SECTION HEADER ───────────────────────────────────────────────────────────

/** Accent-tick section header with red count pill + optional trailing action (UI reference). */
@Composable
fun SectionHeader(
    label: String,
    count: Int? = null,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
    accent: Color = PitchGreen,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label.uppercase(),
            color = TextWhite,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            letterSpacing = 1.5.sp,
            maxLines = 1,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (count != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$count",
                color = TextWhite,
                fontFamily = DmSans,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(LiveRed)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        if (action != null) {
            Spacer(modifier = Modifier.width(12.dp))
            Row(
                modifier = Modifier
                    .then(if (onAction != null) Modifier.clickable(onClick = onAction) else Modifier)
                    .clip(RoundedCornerShape(6.dp))
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = action,
                    color = PitchGreen,
                    fontFamily = DmSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = PitchGreen,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// ─── HOME TOP BAR ─────────────────────────────────────────────────────────────

/** Brand wordmark (UI reference home top bar). */
@Composable
fun ProtoTopBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "RANK",
            color = TextWhite,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "FOOTBALL",
            color = PitchGreen,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            letterSpacing = 2.sp
        )
    }
}

// ─── SEARCH BAR ───────────────────────────────────────────────────────────────

/** Rounded search pill. Pass a value+callback for editing, or onClick for navigation. */
@Composable
fun ProtoSearchBar(
    hint: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    onValueChange: ((String) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null,
    onSearchSubmit: (() -> Unit)? = null,
    autoFocus: Boolean = false
) {
    val shape = RoundedCornerShape(50)
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            kotlinx.coroutines.delay(200)
            focusRequester.requestFocus()
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(shape)
            .background(SurfaceDark)
            .border(1.dp, TextWhite.copy(alpha = 0.07f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = TextGrey,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (value != null && onValueChange != null) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                singleLine = true,
                cursorBrush = SolidColor(PitchGreen),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = TextWhite,
                    fontFamily = DmSans,
                    fontSize = 14.sp
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchSubmit?.invoke() }),
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty()) {
                            Text(text = hint, color = TextGrey, fontFamily = DmSans, fontSize = 14.sp)
                        }
                        inner()
                    }
                }
            )
        } else {
            Text(
                text = hint,
                color = TextGrey,
                fontFamily = DmSans,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        if (onClear != null && value != null && value.isNotEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = TextGrey,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ─── HERO CARD ────────────────────────────────────────────────────────────────

/** Match-of-the-day banner with live score or kickoff countdown (UI reference). */
@Composable
fun ProtoHeroCard(
    fixture: FixtureItem,
    onWatchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = CompetitionColors.accent(fixture.league.name, fixture.league.id)
    val live = fixture.isLive()
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(accent.copy(alpha = 0.4f), StadiumBlack),
                    start = androidx.compose.ui.geometry.Offset.Zero,
                    end = androidx.compose.ui.geometry.Offset(1000f, 500f)
                )
            )
            .border(1.dp, TextWhite.copy(alpha = 0.1f), shape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(PitchGreen.copy(alpha = 0.08f), Color.Transparent),
                        radius = 800f
                    )
                )
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 3.dp, height = 12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(PitchGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Match of the Day",
                    color = TextGrey,
                    fontFamily = DmSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = fixture.league.name.uppercase(),
                    color = PitchGreen,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeroTeam(
                    name = fixture.teams.home.name,
                    logo = fixture.teams.home.logo,
                    modifier = Modifier.weight(1f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (live) {
                        Text(
                            text = "${fixture.goals.home ?: 0} – ${fixture.goals.away ?: 0}",
                            color = TextWhite,
                            fontFamily = BarlowCondensed,
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = "${fixture.fixture.status.elapsed ?: 0}'",
                            color = LiveRed,
                            fontFamily = DmSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    } else {
                        Text(
                            text = "VS",
                            color = TextGrey,
                            fontFamily = BarlowCondensed,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        CountdownTicker(kickoff = fixture.fixture.date)
                        Text(
                            text = "KO ${fixture.kickOffTime()}",
                            color = TextGrey,
                            fontFamily = DmSans,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                HeroTeam(
                    name = fixture.teams.away.name,
                    logo = fixture.teams.away.logo,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(PitchGreen, NeonGreen)))
                    .clickable(onClick = onWatchClick)
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = StadiumBlack,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "WATCH",
                    color = StadiumBlack,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

@Composable
private fun HeroTeam(name: String, logo: String?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TeamCrest(name = name, logo = logo, size = 48.dp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name.uppercase(),
            color = TextWhite,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CountdownTicker(kickoff: String) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(kickoff) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val text = remember(kickoff, now) {
        try {
            val kickOff = LocalDateTime.parse(kickoff, DateTimeFormatter.ISO_DATE_TIME)
                .atZone(ZoneId.systemDefault())
            val remaining = Duration.between(
                java.time.Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()),
                kickOff
            )
            if (remaining.isNegative) {
                "--:--:--"
            } else {
                val h = remaining.toHours()
                val m = remaining.toMinutesPart()
                val s = remaining.toSecondsPart()
                "%02d:%02d:%02d".format(h, m, s)
            }
        } catch (_: Exception) {
            "--:--:--"
        }
    }
    Text(
        text = text,
        color = GoalYellow,
        fontFamily = BarlowCondensed,
        fontWeight = FontWeight.Black,
        fontSize = 20.sp,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}

// ─── FILTER PILLS ─────────────────────────────────────────────────────────────

/** Solid green pill when selected (UI reference filter chips). */
@Composable
fun ProtoPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) PitchGreen else SurfaceDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) StadiumBlack else TextGrey,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 0.5.sp
        )
    }
}

// ─── LIVE BANNER + EMPTY STATE ────────────────────────────────────────────────

@Composable
fun LiveDot(modifier: Modifier = Modifier) {
    val pulse = rememberInfiniteTransition(label = "live_dot_pulse")
    val alpha by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "live_dot_alpha"
    )
    Box(
        modifier = modifier
            .size(8.dp)
            .graphicsLayer { this.alpha = alpha }
            .clip(CircleShape)
            .background(LiveRed)
    )
}

@Composable
fun LiveCountBanner(count: Int, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(shape)
            .background(LiveRed.copy(alpha = 0.1f))
            .border(1.dp, LiveRed.copy(alpha = 0.3f), shape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LiveDot()
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$count match${if (count == 1) "" else "es"} live right now",
            color = TextWhite,
            fontFamily = DmSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** A tappable pill shown on empty states that jumps to a specific destination. */
data class EmptyQuickJump(val label: String, val onClick: () -> Unit)

@Composable
fun EmptyStadium(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    cta: String? = null,
    onCta: (() -> Unit)? = null,
    secondaryCta: String? = null,
    onSecondaryCta: (() -> Unit)? = null,
    quickJumps: List<EmptyQuickJump> = emptyList(),
    quickJumpsHint: String? = null
) {
    val glowTransition = rememberInfiniteTransition(label = "empty_glow")
    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    val bobTransition = rememberInfiniteTransition(label = "empty_bob")
    val bobPhase by bobTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Restart
        ),
        label = "bob_phase"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .size(width = 148.dp, height = 104.dp)
                .graphicsLayer { alpha = 0.85f }
        ) {
            val s = size.width / 148f
            fun p(x: Float, y: Float) = Offset(x * s, y * s)
            val pitch = Color(0xFF00C853)

            // Floodlight wash from above
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00C853).copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = size.height * 0.5f
                )
            )
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF00C853).copy(alpha = 0.05f), Color.Transparent)
                ),
                start = Offset(0f, 0f),
                end = Offset(size.width * 0.42f, size.height * 0.5f),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color(0xFF00C853).copy(alpha = 0.05f))
                ),
                start = Offset(size.width, 0f),
                end = Offset(size.width * 0.58f, size.height * 0.5f),
                strokeWidth = 2.dp.toPx()
            )

            // Glow under the pitch
            drawOval(
                color = pitch.copy(alpha = glowAlpha),
                topLeft = p(38f, 84f),
                size = Size(72f * s, 8f * s)
            )

            // Stands
            drawRoundRect(
                color = Color(0xFF1A1A28),
                topLeft = p(0f, 14f),
                size = Size(148f * s, 12f * s),
                cornerRadius = CornerRadius(3.dp.toPx()),
                style = Stroke(1.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFF1A1A28),
                topLeft = p(0f, 80f),
                size = Size(148f * s, 12f * s),
                cornerRadius = CornerRadius(3.dp.toPx()),
                style = Stroke(1.dp.toPx())
            )
            // Stand fill texture
            drawRoundRect(
                color = Color(0xFF14141F),
                topLeft = p(2f, 16f),
                size = Size(144f * s, 8f * s),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFF14141F),
                topLeft = p(2f, 82f),
                size = Size(144f * s, 8f * s),
                cornerRadius = CornerRadius(2.dp.toPx())
            )

            // Pitch outline
            drawRoundRect(
                color = pitch,
                topLeft = p(15f, 30f),
                size = Size(118f * s, 50f * s),
                cornerRadius = CornerRadius(4.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = pitch,
                radius = 14f * s,
                center = p(74f, 55f),
                style = Stroke(width = 1.2.dp.toPx())
            )
            drawCircle(color = pitch, radius = 2f * s, center = p(74f, 55f))
            drawLine(pitch, p(74f, 30f), p(74f, 80f), strokeWidth = 1.2.dp.toPx())
            drawRoundRect(pitch, p(15f, 40f), Size(22f * s, 30f * s), style = Stroke(1.2.dp.toPx()))
            drawRoundRect(pitch, p(15f, 46f), Size(10f * s, 18f * s), style = Stroke(1.dp.toPx()))
            drawRoundRect(pitch, p(111f, 40f), Size(22f * s, 30f * s), style = Stroke(1.2.dp.toPx()))
            drawRoundRect(pitch, p(133f, 46f), Size(10f * s, 18f * s), style = Stroke(1.dp.toPx()))
            val cornerArc = Stroke(width = 1.dp.toPx())
            drawArc(pitch, startAngle = 180f, sweepAngle = 90f, useCenter = false, topLeft = p(15f, 30f), size = Size(4f * s, 4f * s), style = cornerArc)
            drawArc(pitch, startAngle = 270f, sweepAngle = 90f, useCenter = false, topLeft = p(129f, 30f), size = Size(4f * s, 4f * s), style = cornerArc)
            drawArc(pitch, startAngle = 90f, sweepAngle = 90f, useCenter = false, topLeft = p(15f, 76f), size = Size(4f * s, 4f * s), style = cornerArc)
            drawArc(pitch, startAngle = 0f, sweepAngle = 90f, useCenter = false, topLeft = p(129f, 76f), size = Size(4f * s, 4f * s), style = cornerArc)

            // Bouncing ball above the centre spot
            val phase = sin(bobPhase * 6.2831853f)
            val ballY = 55f - 4.5f * s * phase
            val shadowScale = 1f - 0.35f * ((phase + 1f) / 2f)
            drawOval(
                color = Color.Black.copy(alpha = 0.35f),
                topLeft = p(74f - 5f * shadowScale, 57f),
                size = Size(10f * shadowScale * s, 3f * s)
            )
            drawCircle(
                color = Color.White,
                radius = 4.5f * s,
                center = p(74f, ballY)
            )
            drawCircle(
                color = Color(0xFF2E2E3E),
                radius = 2f * s,
                center = p(73.2f, ballY - 1.2f * s)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = 0.9f * s,
                center = p(72.6f, ballY - 1.8f * s)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = title.uppercase(),
            color = TextWhite,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            color = TextGrey,
            fontFamily = DmSans,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 260.dp)
        )
        if (quickJumps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(18.dp))
            if (quickJumpsHint != null) {
                Text(
                    text = quickJumpsHint.uppercase(),
                    color = TextGrey,
                    fontFamily = DmSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                quickJumps.take(3).forEach { jump ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(PitchGreen.copy(alpha = 0.1f))
                            .border(1.dp, PitchGreen.copy(alpha = 0.45f), RoundedCornerShape(50))
                            .clickable(onClick = jump.onClick)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = jump.label,
                            color = PitchGreen,
                            fontFamily = BarlowCondensed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
        if (cta != null && onCta != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(PitchGreen.copy(alpha = 0.13f))
                    .border(1.dp, PitchGreen, RoundedCornerShape(50))
                    .clickable(onClick = onCta)
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cta,
                    color = PitchGreen,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }
        }
        if (secondaryCta != null && onSecondaryCta != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = secondaryCta,
                color = TextGrey,
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, TextWhite.copy(alpha = 0.12f), RoundedCornerShape(50))
                    .clickable(onClick = onSecondaryCta)
                    .padding(horizontal = 20.dp, vertical = 9.dp)
            )
        }
    }
}

@Composable
fun EmptySearch(modifier: Modifier = Modifier, onSuggestion: (String) -> Unit = {}) {
    val suggestions = listOf(
        "El Clásico this weekend",
        "Champions League fixtures",
        "Premier League top scorer",
        "Today's derbies"
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = Modifier.size(56.dp)) {
            val c = center
            drawCircle(
                color = TextGrey.copy(alpha = 0.45f),
                radius = 20.dp.toPx(),
                center = c,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = PitchGreen.copy(alpha = 0.8f),
                radius = 9.dp.toPx(),
                center = c,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawLine(
                color = TextGrey.copy(alpha = 0.45f),
                start = Offset(c.x + 14.dp.toPx(), c.y + 14.dp.toPx()),
                end = Offset(c.x + 22.dp.toPx(), c.y + 22.dp.toPx()),
                strokeWidth = 3.dp.toPx()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Search football",
            color = TextWhite,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Matches, live scores, fixtures",
            color = TextGrey,
            fontFamily = DmSans,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "AI SUGGESTIONS",
            color = TextGrey,
            fontFamily = DmSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        suggestions.forEach { s ->
            Box(
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.linearGradient(listOf(PitchGreen, NeonGreen)))
                    .clickable { onSuggestion(s) }
                    .padding(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(CardDark)
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = s,
                        color = TextWhite,
                        fontFamily = DmSans,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ─── SETTINGS UPGRADE ─────────────────────────────────────────────────────────

/** Go Ad-Free upsell card with gradient border (UI reference). */
@Composable
fun SettingsUpgradeCard(
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(NeonGreen, PitchGreen, PitchGreen, StadiumBlack)))
    ) {
        Row(
            modifier = Modifier
                .padding(1.5.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CardDark)
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "GO AD-FREE",
                    color = NeonGreen,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Remove all ads • Unlock HD streams",
                    color = TextGrey,
                    fontFamily = DmSans,
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.linearGradient(listOf(PitchGreen, NeonGreen)))
                    .clickable(onClick = onUpgrade)
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "UPGRADE",
                    color = StadiumBlack,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ─── SHARED CHROME ATOMS ───────────────────────────────────────────────────────

/** Circular back chip used across sub-screens (UI reference "‹ Leagues"). */
@Composable
fun BackChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(SurfaceDark)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "‹",
            color = PitchGreen,
            fontFamily = DmSans,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
    }
}

/** Uppercase condensed screen title used under back chips (UI reference). */
@Composable
fun ProtoScreenTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        color = TextWhite,
        fontFamily = BarlowCondensed,
        fontWeight = FontWeight.Black,
        fontSize = 20.sp,
        letterSpacing = 2.sp,
        modifier = modifier
    )
}

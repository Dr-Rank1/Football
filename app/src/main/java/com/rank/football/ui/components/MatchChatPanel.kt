package com.rank.football.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rank.football.R
import com.rank.football.data.firebase.ChatMessage
import com.rank.football.data.firebase.ChatRepository
import com.rank.football.ui.theme.CardDark
import com.rank.football.ui.theme.PitchGreen
import com.rank.football.ui.theme.StadiumBlack
import com.rank.football.ui.theme.SurfaceDark
import com.rank.football.ui.theme.TextGrey
import com.rank.football.ui.theme.TextWhite
import kotlinx.coroutines.launch

/** Live match chat panel backed by Firebase Realtime Database. */
@Composable
fun MatchChatPanel(
    fixtureId: Int,
    panelModifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repo = remember { ChatRepository(context) }
    val messages by repo.observeMessages(fixtureId).collectAsState(initial = emptyList())
    val watchers by repo.observeWatcherCount(fixtureId).collectAsState(initial = 1)
    var draft by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    DisposableEffect(fixtureId) {
        scope.launch { runCatching { repo.joinWatchers(fixtureId) } }
        onDispose {
            scope.launch { runCatching { repo.leaveWatchers(fixtureId) } }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        panelModifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .heightIn(min = 220.dp, max = 360.dp)
    ) {
        Text(
            text = stringResource(R.string.watching_count, watchers),
            color = TextGrey,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(CardDark, RoundedCornerShape(12.dp))
                .padding(8.dp),
            contentPadding = PaddingValues(4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.chat_empty),
                        color = TextGrey,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            items(messages, key = { it.id }) { msg ->
                ChatBubble(msg)
            }
        }
        error?.let {
            Text(it, color = PitchGreen, modifier = Modifier.padding(top = 4.dp))
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it.take(200) },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.chat_hint), color = TextGrey) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = PitchGreen,
                    unfocusedBorderColor = SurfaceDark,
                    cursorColor = PitchGreen
                ),
                shape = RoundedCornerShape(12.dp)
            )
            IconButton(
                onClick = {
                    val text = draft.trim()
                    if (text.isEmpty()) return@IconButton
                    scope.launch {
                        val result = repo.sendMessage(fixtureId, text)
                        if (result.isSuccess) {
                            draft = ""
                            error = null
                        } else {
                            error = result.exceptionOrNull()?.message
                                ?: context.getString(R.string.chat_send_failed)
                        }
                    }
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = PitchGreen)
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(StadiumBlack.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = msg.displayName.ifBlank { "Fan" },
            color = PitchGreen,
            fontWeight = FontWeight.SemiBold,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall
        )
        Spacer(Modifier.height(2.dp))
        Text(msg.text, color = TextWhite, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
    }
}

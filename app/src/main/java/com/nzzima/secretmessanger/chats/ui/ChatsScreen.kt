package com.nzzima.secretmessanger.chats.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nzzima.secretmessanger.chats.domain.models.Conversation
import com.nzzima.secretmessanger.ui.theme.Ink
import com.nzzima.secretmessanger.ui.theme.InkDim
import com.nzzima.secretmessanger.utils.constants.Constants
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import org.koin.androidx.compose.koinViewModel

/**
 * Экран списка диалогов.
 *
 * Строка не нажимается: открывать пока нечего — экрана переписки нет.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatsViewModel = koinViewModel(),
) {
    val state by viewModel.observeChatsScreenState().collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(Constants.CHATS_TITLE) },
                actions = { TextButton(onClick = viewModel::signOut) { Text(Constants.SIGN_OUT) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { insets ->
        Box(modifier = Modifier.fillMaxSize().padding(insets), contentAlignment = Alignment.Center) {
            when (val current = state) {
                ChatsUiState.Loading -> CircularProgressIndicator()

                ChatsUiState.Empty -> Notice(Constants.CHATS_EMPTY)

                is ChatsUiState.Content -> ConversationList(current.conversations)

                is ChatsUiState.Failed -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Notice(current.message)
                    TextButton(onClick = viewModel::retry) { Text(Constants.RETRY) }
                }
            }
        }
    }
}

@Composable
private fun ConversationList(conversations: List<Conversation>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(conversations, key = { it.chat.id }) { conversation ->
            ConversationRow(conversation)
            HorizontalDivider(color = MaterialTheme.colorScheme.surface)
        }
    }
}

/** Строка списка: название диалога, превью последней реплики и её время. */
@Composable
private fun ConversationRow(conversation: Conversation) {
    Row(
        modifier = Modifier.padding(horizontal = SIDE_PADDING, vertical = ROW_PADDING),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = GAP),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = conversation.chat.title,
                color = Ink,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = conversation.preview,
                color = InkDim,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = formatTime(conversation.date),
            color = InkDim,
            // Табличные цифры: без них время пляшет по горизонтали от строки к строке.
            style = TextStyle(fontSize = 12.sp, fontFeatureSettings = "tnum"),
        )
    }
}

@Composable
private fun Notice(text: String) {
    Text(
        text = text,
        color = InkDim,
        fontSize = 15.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = SIDE_PADDING),
    )
}

/** Время последней реплики в коротком формате системной локали, как на iOS. */
private fun formatTime(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(TIME_FORMAT)

private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
private val SIDE_PADDING = 16.dp
private val ROW_PADDING = 12.dp
private val GAP = 10.dp

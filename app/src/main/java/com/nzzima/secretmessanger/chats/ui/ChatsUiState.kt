package com.nzzima.secretmessanger.chats.ui

import com.nzzima.secretmessanger.chats.domain.models.Conversation

/** Состояние экрана списка диалогов. */
sealed interface ChatsUiState {

    /** Первый снимок ещё не пришёл. */
    data object Loading : ChatsUiState

    /** Диалогов нет ни одного. */
    data object Empty : ChatsUiState

    /** [conversations] — свежие сверху, пустым список здесь не бывает. */
    data class Content(val conversations: List<Conversation>) : ChatsUiState

    /** Подписка отказала. [message] показывается на экране, подписаться можно заново. */
    data class Failed(val message: String) : ChatsUiState
}

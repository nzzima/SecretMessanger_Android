package com.nzzima.secretmessanger.chats.domain.api

import com.nzzima.secretmessanger.chats.domain.models.Conversation
import kotlinx.coroutines.flow.Flow

/** Список диалогов для экрана «Чаты». */
interface ChatsInteractor {

    /**
     * Диалоги аккаунта [selfId] с открытыми превью, свежими сверху.
     *
     * Диалоги без единого сообщения в список не попадают: шапка заводится при открытии
     * чата, чтобы правила могли проверить состав участников, — но показывать там нечего,
     * пока никто ничего не написал.
     *
     * Условия завершения потока и отказа — те же, что у [ConversationRepository.observeHeaders].
     */
    fun observeConversations(selfId: String): Flow<Result<List<Conversation>>>
}

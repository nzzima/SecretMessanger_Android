package com.nzzima.secretmessanger.chats.domain.api

import com.nzzima.secretmessanger.chats.domain.models.ConversationHeader
import kotlinx.coroutines.flow.Flow

/** Шапки диалогов из коллекции `conversation`. */
interface ConversationRepository {

    /**
     * Диалоги, где состоит [selfId], — целым снимком на каждое изменение любого из них.
     *
     * Порядок произвольный: сортирует вызывающий. Пара `arrayContains` + `orderBy` требует
     * составного индекса, которого в базе нет.
     *
     * Снимок отдаётся целиком, а не изменениями: шапка меняется на каждой отправке, и
     * список всё равно пересобирается заново.
     *
     * Отказ приходит последним значением, после чего поток закрывается: слушатель Firestore
     * после ошибки снимается сам, и продолжать слушать уже нечего — за повторной попыткой
     * нужна новая подписка.
     */
    fun observeHeaders(selfId: String): Flow<Result<List<ConversationHeader>>>
}

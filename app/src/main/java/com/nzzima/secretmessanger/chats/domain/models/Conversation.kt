package com.nzzima.secretmessanger.chats.domain.models

/**
 * Строка списка «Чаты»: диалог, открытое превью последней реплики и её время.
 *
 * @property preview расшифрованный текст либо
 *   [com.nzzima.secretmessanger.utils.constants.Constants.PREVIEW_UNREADABLE], если ключа
 *   диалога у нас нет.
 * @property date время последней реплики в миллисекундах эпохи.
 */
data class Conversation(
    val chat: Chat,
    val preview: String,
    val date: Long,
)

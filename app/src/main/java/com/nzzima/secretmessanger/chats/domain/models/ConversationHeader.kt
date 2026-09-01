package com.nzzima.secretmessanger.chats.domain.models

/**
 * Шапка диалога так, как она лежит в базе.
 *
 * Последняя реплика хранится тем же шифротекстом, что и само сообщение: если бы шапка
 * держала её открытой, последняя строка каждого диалога лежала бы в базе читаемой.
 *
 * @property lastMessage шифротекст либо открытый текст, смотря по [encrypted]. Пустая
 *   строка — диалог, в котором ещё никто ничего не написал.
 * @property encrypted поле `lastEnc` шапки.
 * @property version версия ключа, которой закрыта [lastMessage]; у диалогов без поля
 *   `lastV` равна [Chat.keyVersion].
 * @property date время последней реплики в миллисекундах эпохи.
 */
data class ConversationHeader(
    val chat: Chat,
    val lastMessage: String,
    val encrypted: Boolean,
    val version: Int,
    val date: Long,
)

package com.nzzima.secretmessanger.crypto.domain.impl

import com.nzzima.secretmessanger.crypto.domain.CryptoBox
import com.nzzima.secretmessanger.crypto.domain.api.ConversationKeys
import com.nzzima.secretmessanger.crypto.domain.api.IdentityKeyStore
import java.util.concurrent.ConcurrentHashMap

/**
 * [ConversationKeys] поверх постоянного ключа аккаунта из [IdentityKeyStore].
 *
 * Открытые ключи кэшируются по диалогу, версии и аккаунту: распечатывание асимметричное,
 * а один ключ обслуживает и превью в списке, и все сообщения диалога.
 *
 * Кэш живёт, пока жив экземпляр, и растёт вместе с числом открытых диалогов; записи из
 * него не вытесняются. Стёртый диалог кэш пережил бы — забывать его будет нечем, пока
 * удаления в приложении нет.
 */
class ConversationKeysImpl(private val identityKeys: IdentityKeyStore) : ConversationKeys {

    private val cache = ConcurrentHashMap<String, ByteArray>()

    override fun open(
        convoId: String,
        uid: String,
        version: Int,
        entries: Map<String, String>,
    ): ByteArray? {
        val cacheKey = "$convoId/$version/$uid"
        cache[cacheKey]?.let { return it }

        val payload = entries[entryKey(uid, version)] ?: return null
        val identityPrivate = identityKeys.existing(uid) ?: return null

        return runCatching { CryptoBox.openKey(payload, identityPrivate, context(convoId, version)) }
            .getOrNull()
            ?.also { cache[cacheKey] = it }
    }

    /** Ключ записи в карте `convoKeys`: чей ключ и какой версии. */
    private fun entryKey(uid: String, version: Int) = "${uid}_$version"

    /**
     * Контекст HKDF: привязывает запечатанный ключ к диалогу и версии, поэтому запись,
     * переставленная в другой диалог, не открывается.
     */
    private fun context(convoId: String, version: Int) = "$convoId/v$version"
}

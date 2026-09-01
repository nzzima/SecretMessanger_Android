package com.nzzima.secretmessanger.crypto.domain

import com.nzzima.secretmessanger.crypto.data.impl.IdentityKeyStoreImpl
import com.nzzima.secretmessanger.crypto.domain.impl.ConversationKeysImpl
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Ключ диалога: открывается своей записью и ничем больше.
 *
 * Записи собираются тем же [CryptoBox], которым их собирает отправитель, — совместимость
 * формата с CryptoKit проверяет `IosInteropTest` на фикстурах.
 */
class ConversationKeysTest {

    private val identityKeys = IdentityKeyStoreImpl(FakeSharedPreferences(), FakeMasterKeyProvider())
    private val conversationKeys = ConversationKeysImpl(identityKeys)

    private val convoId = "uid-1_uid-2"

    /** Запись `convoKeys` для [uid] версии [version], запечатанная его же открытой половиной. */
    private fun entry(
        key: ByteArray,
        uid: String,
        version: Int,
        identityPrivate: ByteArray,
        convoId: String = this.convoId,
    ) = "${uid}_$version" to
        CryptoBox.sealKey(key, CryptoBox.publicKey(identityPrivate), "$convoId/v$version")

    @Test
    fun `своя запись открывается`() {
        val identityPrivate = identityKeys.createNew("uid-1")
        val key = CryptoBox.newConversationKey()

        val opened = conversationKeys.open(convoId, "uid-1", 1, mapOf(entry(key, "uid-1", 1, identityPrivate)))

        assertArrayEquals(key, opened)
    }

    @Test
    fun `записи для нас нет — ключа нет`() {
        val stranger = CryptoBox.newIdentityPrivateKey()
        identityKeys.createNew("uid-1")
        val entries = mapOf(entry(CryptoBox.newConversationKey(), "uid-2", 1, stranger))

        assertNull(conversationKeys.open(convoId, "uid-1", 1, entries))
    }

    @Test
    fun `постоянного ключа на устройстве нет — ключа диалога нет`() {
        val stranger = CryptoBox.newIdentityPrivateKey()
        val entries = mapOf(entry(CryptoBox.newConversationKey(), "uid-1", 1, stranger))

        assertNull(conversationKeys.open(convoId, "uid-1", 1, entries))
    }

    @Test
    fun `запись запечатана для другой пары ключей — ключа нет, исключение наружу не идёт`() {
        identityKeys.createNew("uid-1")
        val otherDevice = CryptoBox.newIdentityPrivateKey()
        val entries = mapOf(entry(CryptoBox.newConversationKey(), "uid-1", 1, otherDevice))

        assertNull(conversationKeys.open(convoId, "uid-1", 1, entries))
    }

    @Test
    fun `запись из другого диалога не открывается`() {
        val identityPrivate = identityKeys.createNew("uid-1")
        val entries = mapOf(
            entry(CryptoBox.newConversationKey(), "uid-1", 1, identityPrivate, convoId = "uid-1_uid-3"),
        )

        assertNull(conversationKeys.open(convoId, "uid-1", 1, entries))
    }

    @Test
    fun `версии не путаются между собой`() {
        val identityPrivate = identityKeys.createNew("uid-1")
        val first = CryptoBox.newConversationKey()
        val second = CryptoBox.newConversationKey()
        val entries = mapOf(
            entry(first, "uid-1", 1, identityPrivate),
            entry(second, "uid-1", 2, identityPrivate),
        )

        assertArrayEquals(first, conversationKeys.open(convoId, "uid-1", 1, entries))
        assertArrayEquals(second, conversationKeys.open(convoId, "uid-1", 2, entries))
    }

    @Test
    fun `открытый ключ берётся из кэша, а не распечатывается заново`() {
        val identityPrivate = identityKeys.createNew("uid-1")
        val key = CryptoBox.newConversationKey()
        val entries = mapOf(entry(key, "uid-1", 1, identityPrivate))

        val first = conversationKeys.open(convoId, "uid-1", 1, entries)

        // Записи больше нет, а ключ обязан остаться тем же экземпляром из кэша.
        assertSame(first, conversationKeys.open(convoId, "uid-1", 1, emptyMap()))
    }

    @Test
    fun `кэш не путает аккаунты на одном устройстве`() {
        val firstIdentity = identityKeys.createNew("uid-1")
        val secondIdentity = identityKeys.createNew("uid-2")
        val forFirst = CryptoBox.newConversationKey()
        val forSecond = CryptoBox.newConversationKey()
        val entries = mapOf(
            entry(forFirst, "uid-1", 1, firstIdentity),
            entry(forSecond, "uid-2", 1, secondIdentity),
        )

        assertArrayEquals(forFirst, conversationKeys.open(convoId, "uid-1", 1, entries))
        assertArrayEquals(forSecond, conversationKeys.open(convoId, "uid-2", 1, entries))
    }
}

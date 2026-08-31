package com.nzzima.secretmessanger.crypto.domain

import com.nzzima.secretmessanger.crypto.data.impl.IdentityKeyStoreImpl
import com.nzzima.secretmessanger.crypto.domain.models.CryptoFailure
import com.nzzima.secretmessanger.utils.constants.Constants
import java.util.Base64
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Хранилище постоянного ключа. Половина проверок утверждает, что ключ **не** заводится.
 */
class IdentityKeyStoreTest {

    private val preferences = FakeSharedPreferences()
    private val store = IdentityKeyStoreImpl(preferences, FakeMasterKeyProvider())

    @Test
    fun `на чистом устройстве ключа нет и он не создаётся`() {
        assertNull(store.existing("uid-1"))
        assertNull("повторное чтение тоже не создаёт", store.existing("uid-1"))
        assertTrue("в хранилище не должно появиться записей", preferences.all.isEmpty())
    }

    @Test
    fun `заведённый ключ читается обратно теми же байтами`() {
        val created = store.createNew("uid-1")

        assertArrayEquals(created, store.existing("uid-1"))
        assertEquals(32, created.size)
    }

    @Test
    fun `у разных аккаунтов ключи разные`() {
        val first = store.createNew("uid-1")
        val second = store.createNew("uid-2")

        assertFalse(first.contentEquals(second))
        assertArrayEquals(first, store.existing("uid-1"))
        assertArrayEquals(second, store.existing("uid-2"))
    }

    @Test
    fun `повторное заведение затирает прежний ключ`() {
        val first = store.createNew("uid-1")
        val second = store.createNew("uid-1")

        assertFalse("новая пара обязана отличаться", first.contentEquals(second))
        assertArrayEquals(second, store.existing("uid-1"))
    }

    @Test
    fun `забытый ключ не возвращается и соседний не задет`() {
        store.createNew("uid-1")
        val untouched = store.createNew("uid-2")

        store.forget("uid-1")

        assertNull(store.existing("uid-1"))
        assertArrayEquals(untouched, store.existing("uid-2"))
    }

    @Test
    fun `приватная половина не лежит в открытом виде`() {
        val created = store.createNew("uid-1")
        val stored = Base64.getDecoder()
            .decode(preferences.getString(Constants.IDENTITY_ENTRY_PREFIX + "uid-1", null))

        assertFalse(
            "сырые байты ключа не должны встречаться в записи",
            stored.asList().windowed(created.size).any { it.toByteArray().contentEquals(created) },
        )
        assertNotEquals(created.size, stored.size)
    }

    @Test
    fun `запись, закрытая другим мастер-ключом, не открывается`() {
        store.createNew("uid-1")
        val foreign = IdentityKeyStoreImpl(preferences, FakeMasterKeyProvider())

        assertThrows(CryptoFailure.WrongKey::class.java) { foreign.existing("uid-1") }
    }

    @Test
    fun `испорченная запись отбивается, а не подменяется новым ключом`() {
        store.createNew("uid-1")
        val entry = Constants.IDENTITY_ENTRY_PREFIX + "uid-1"
        val broken = Base64.getDecoder().decode(preferences.getString(entry, null))
            .also { it[it.lastIndex] = (it[it.lastIndex] + 1).toByte() }
        preferences.edit().putString(entry, Base64.getEncoder().encodeToString(broken)).apply()

        assertThrows(CryptoFailure.WrongKey::class.java) { store.existing("uid-1") }
    }

    @Test
    fun `обрезанная запись разбору не поддаётся`() {
        preferences.edit()
            .putString(Constants.IDENTITY_ENTRY_PREFIX + "uid-1", Base64.getEncoder().encodeToString(ByteArray(8)))
            .apply()

        assertThrows(CryptoFailure.MalformedPayload::class.java) { store.existing("uid-1") }
    }

    @Test
    fun `ключ из хранилища открывает переписку, запечатанную на него`() {
        val identityPrivate = store.createNew("uid-1")
        val conversationKey = CryptoBox.newConversationKey()
        val sealed = CryptoBox.sealKey(
            key = conversationKey,
            recipientPublic = CryptoBox.publicKey(identityPrivate),
            context = "uid-1_uid-2#1",
        )

        val opened = CryptoBox.openKey(sealed, requireNotNull(store.existing("uid-1")), "uid-1_uid-2#1")

        assertArrayEquals(conversationKey, opened)
    }

    private fun List<Byte>.toByteArray() = ByteArray(size) { this[it] }
}

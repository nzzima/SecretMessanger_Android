package com.nzzima.secretmessanger.crypto.domain

import com.nzzima.secretmessanger.crypto.domain.models.CryptoFailure
import java.security.MessageDigest
import java.util.Base64
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * iOS → Android: всё, что запечатал CryptoKit, открывается здесь.
 *
 * Проверка идёт на настоящих байтах из [IosFixture], а не на данных, зашифрованных этим же
 * кодом: совпадение с самим собой совместимости не доказывает.
 */
class IosInteropTest {

    @Test
    fun `открытая половина выводится из приватной так же, как на iOS`() {
        assertArrayEquals(IosFixture.identityPublic, CryptoBox.publicKey(IosFixture.identityPrivate))
    }

    @Test
    fun `ключ диалога распечатывается из записи, запечатанной CryptoKit`() {
        val key = CryptoBox.openKey(IosFixture.sealedKey, IosFixture.identityPrivate, IosFixture.context)

        assertArrayEquals(IosFixture.convoKeyExpected, key)
    }

    @Test
    fun `текст, зашифрованный на iOS, читается дословно`() {
        val key = CryptoBox.openKey(IosFixture.sealedKey, IosFixture.identityPrivate, IosFixture.context)

        assertEquals(IosFixture.expectedPlaintext, CryptoBox.open(IosFixture.sealedMessage, key))
    }

    @Test
    fun `вложение открывается и сходится по SHA-256`() {
        val key = CryptoBox.openKey(IosFixture.sealedKey, IosFixture.identityPrivate, IosFixture.context)
        val blob = CryptoBox.open(Base64.getDecoder().decode(IosFixture.sealedBlob), key)

        assertArrayEquals(IosFixture.expectedBlobSha256, MessageDigest.getInstance("SHA-256").digest(blob))
    }

    @Test
    fun `чужой контекст ключ диалога не открывает`() {
        assertThrows(CryptoFailure.WrongKey::class.java) {
            CryptoBox.openKey(IosFixture.sealedKey, IosFixture.identityPrivate, "другой_диалог#1")
        }
    }

    @Test
    fun `чужой постоянный ключ запись не открывает`() {
        val stranger = CryptoBox.newIdentityPrivateKey()

        assertThrows(CryptoFailure.WrongKey::class.java) {
            CryptoBox.openKey(IosFixture.sealedKey, stranger, IosFixture.context)
        }
    }

    @Test
    fun `испорченный тег отбивается как чужой ключ`() {
        val key = CryptoBox.openKey(IosFixture.sealedKey, IosFixture.identityPrivate, IosFixture.context)
        val broken = Base64.getDecoder().decode(IosFixture.sealedMessage)
            .also { it[it.lastIndex] = (it[it.lastIndex] + 1).toByte() }

        assertThrows(CryptoFailure.WrongKey::class.java) {
            CryptoBox.open(Base64.getEncoder().encodeToString(broken), key)
        }
    }

    @Test
    fun `запись без разделителя разбору не поддаётся`() {
        assertThrows(CryptoFailure.MalformedPayload::class.java) {
            CryptoBox.openKey("нетточки", IosFixture.identityPrivate, IosFixture.context)
        }
    }

    @Test
    fun `нонс не повторяется между запечатываниями`() {
        val key = CryptoBox.newConversationKey()

        assertNotEquals(CryptoBox.seal("одно и то же", key), CryptoBox.seal("одно и то же", key))
    }
}

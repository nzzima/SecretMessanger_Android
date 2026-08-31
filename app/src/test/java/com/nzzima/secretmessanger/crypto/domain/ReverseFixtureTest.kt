package com.nzzima.secretmessanger.crypto.domain

import java.io.File
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Android → iOS: складывает запечатанное здесь в `build/interop/reverse.json`.
 *
 * Файл открывает `tools/crypto-interop/reverse.swift` настоящим CryptoKit — это вторая
 * половина проверки совместимости, без которой [IosInteropTest] доказывал бы только
 * чтение, но не запись.
 *
 * Сам тест проверяет замкнутость цикла в Kotlin; вердикт о совместимости выносит Swift.
 */
class ReverseFixtureTest {

    private companion object {
        const val CONTEXT = "uidC_uidD#7"
        const val REPLY = "Ответ с Android — если это читается на iPhone, порт замкнулся."
        const val OUTPUT = "build/interop/reverse.json"
    }

    @Test
    fun `запечатанное здесь открывается здесь же и ложится в файл для CryptoKit`() {
        val identityPrivate = CryptoBox.newIdentityPrivateKey()
        val conversationKey = CryptoBox.newConversationKey()

        val sealedKey = CryptoBox.sealKey(
            key = conversationKey,
            recipientPublic = CryptoBox.publicKey(identityPrivate),
            context = CONTEXT,
        )
        val sealedReply = CryptoBox.seal(REPLY, conversationKey)

        assertEquals(REPLY, CryptoBox.open(sealedReply, conversationKey))
        assertTrue(
            CryptoBox.openKey(sealedKey, identityPrivate, CONTEXT).contentEquals(conversationKey),
        )

        val encoder = Base64.getEncoder()
        val fields = linkedMapOf(
            "identityPrivate" to encoder.encodeToString(identityPrivate),
            "convoKeyExpected" to encoder.encodeToString(conversationKey),
            "context" to CONTEXT,
            "sealedKey" to sealedKey,
            "sealedReply" to sealedReply,
            "expectedReply" to REPLY,
        )

        File(OUTPUT).apply { parentFile?.mkdirs() }.writeText(
            fields.entries.joinToString(",\n", "{\n", "\n}\n") { (key, value) ->
                """  "$key": "$value""""
            },
        )
    }
}

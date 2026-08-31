package com.nzzima.secretmessanger.crypto.domain

import java.util.Base64

/**
 * Фикстура, собранная CryptoKit на iOS — `tools/crypto-interop/fixture.swift` в вики.
 *
 * Читается из `app/src/test/resources/ios-fixture.json`. Файл плоский, значения — строки,
 * поэтому разбирается без библиотеки JSON: пары `"ключ" : "значение"` и единственное
 * экранирование, которое ставит `JSONSerialization`, — `\/` вместо `/`.
 *
 * Поля переноса `SMK1` в копию не вошли: формат отменён владельцем 28.08.2026.
 */
object IosFixture {

    private val values: Map<String, String> by lazy {
        val json = requireNotNull(javaClass.getResourceAsStream("/ios-fixture.json")) {
            "нет app/src/test/resources/ios-fixture.json"
        }.bufferedReader().use { it.readText() }

        Regex("\"([^\"]+)\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
            .findAll(json)
            .associate { it.groupValues[1] to it.groupValues[2].replace("\\/", "/") }
    }

    private operator fun get(key: String): String =
        requireNotNull(values[key]) { "в фикстуре нет поля $key" }

    val identityPrivate: ByteArray get() = decode("identityPrivate")
    val identityPublic: ByteArray get() = decode("identityPublic")
    val convoKeyExpected: ByteArray get() = decode("convoKeyExpected")
    val expectedBlobSha256: ByteArray get() = decode("expectedBlobSHA256")

    val context: String get() = get("context")
    val sealedKey: String get() = get("sealedKey")
    val sealedMessage: String get() = get("sealedMessage")
    val sealedBlob: String get() = get("sealedBlob")
    val expectedPlaintext: String get() = get("expectedPlaintext")

    private fun decode(key: String): ByteArray = Base64.getDecoder().decode(get(key))
}

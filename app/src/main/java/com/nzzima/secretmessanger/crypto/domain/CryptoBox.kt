package com.nzzima.secretmessanger.crypto.domain

import com.nzzima.secretmessanger.crypto.domain.models.CryptoFailure
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.bouncycastle.crypto.agreement.X25519Agreement
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters

/**
 * Шифрование: два примитива, побайтово совместимых с `CryptoBox.swift` на iOS.
 *
 * Симметричный — AES-256-GCM ключом диалога, им закрываются сообщения и вложения.
 * Асимметричный — X25519 с эфемерной парой, им ключ диалога запечатывается каждому
 * участнику отдельно.
 *
 * **Формат, который менять нельзя** — это договор с iOS:
 * - запечатанные данные: `nonce(12) ‖ шифротекст ‖ тег(16)`, как `combined` у CryptoKit;
 * - запечатанный ключ диалога: `<base64 эфемерного открытого ключа>.<base64 записи>`;
 * - обёртка ключа диалога выводится HKDF-SHA256 из общего секрета X25519: соль пустая,
 *   `info` = `эфемерный открытый(32) ‖ открытый получателя(32) ‖ UTF-8 контекста`,
 *   длина вывода 32 байта;
 * - текст кладётся в UTF-8, готовая запись — в base64; вложения остаются байтами.
 *
 * Все ключи — сырые 32 байта. Проверку длин выполняет вызывающий: сюда они приходят
 * из хранилища и из профиля, где формат уже задан.
 */
object CryptoBox {

    private const val NONCE_LENGTH = 12
    private const val TAG_BITS = 128
    private const val KEY_LENGTH = 32
    private const val SEPARATOR = '.'
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val HMAC = "HmacSHA256"

    private val random = SecureRandom()

    /** Новый случайный ключ диалога — 32 байта. */
    fun newConversationKey(): ByteArray = ByteArray(KEY_LENGTH).also(random::nextBytes)

    /** Новая постоянная пара X25519. Возвращает приватную половину — 32 байта. */
    fun newIdentityPrivateKey(): ByteArray = X25519PrivateKeyParameters(random).encoded

    /** Открытая половина пары [identityPrivate] — 32 байта. */
    fun publicKey(identityPrivate: ByteArray): ByteArray =
        X25519PrivateKeyParameters(identityPrivate, 0).generatePublicKey().encoded

    /**
     * Шифрует [text] ключом диалога [key].
     *
     * @return base64 записи — в таком виде она ложится в поле документа.
     */
    fun seal(text: String, key: ByteArray): String =
        Base64.getEncoder().encodeToString(seal(text.toByteArray(Charsets.UTF_8), key))

    /**
     * Расшифровывает запись, собранную [seal].
     *
     * @throws CryptoFailure.MalformedPayload если строка не base64 или короче нонса.
     * @throws CryptoFailure.WrongKey если тег не сошёлся.
     */
    fun open(payload: String, key: ByteArray): String =
        String(open(payload.decodeBase64(), key), Charsets.UTF_8)

    /**
     * Шифрует вложение [data] тем же ключом диалога.
     *
     * @return байты, а не base64: запись кладётся в поле типа `bytes`.
     */
    fun seal(data: ByteArray, key: ByteArray): ByteArray {
        val nonce = ByteArray(NONCE_LENGTH).also(random::nextBytes)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, nonce))
        return nonce + cipher.doFinal(data)
    }

    /**
     * Расшифровывает вложение.
     *
     * @throws CryptoFailure.MalformedPayload если байт меньше, чем нонс и тег.
     * @throws CryptoFailure.WrongKey если тег не сошёлся.
     */
    fun open(data: ByteArray, key: ByteArray): ByteArray {
        if (data.size <= NONCE_LENGTH) throw CryptoFailure.MalformedPayload
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(key, "AES"),
            GCMParameterSpec(TAG_BITS, data.copyOf(NONCE_LENGTH)),
        )
        return try {
            cipher.doFinal(data, NONCE_LENGTH, data.size - NONCE_LENGTH)
        } catch (error: AEADBadTagException) {
            throw CryptoFailure.WrongKey
        }
    }

    /**
     * Запечатывает ключ диалога [key] для участника с открытой половиной [recipientPublic].
     *
     * [context] — идентификатор диалога и версия ключа. Подмешивается в вывод HKDF, поэтому
     * запись, запечатанная для одного диалога, не открывается в другом.
     *
     * @return `<base64 эфемерного открытого ключа>.<base64 записи>`.
     */
    fun sealKey(key: ByteArray, recipientPublic: ByteArray, context: String): String {
        val ephemeral = X25519PrivateKeyParameters(random)
        val ephemeralPublic = ephemeral.generatePublicKey().encoded
        val wrap = wrapKey(
            shared = agree(ephemeral.encoded, recipientPublic),
            ephemeralPublic = ephemeralPublic,
            recipientPublic = recipientPublic,
            context = context,
        )
        return Base64.getEncoder().encodeToString(ephemeralPublic) +
            SEPARATOR +
            Base64.getEncoder().encodeToString(seal(key, wrap))
    }

    /**
     * Достаёт ключ диалога из записи, запечатанной для нас.
     *
     * [context] обязан совпадать с тем, что был при запечатывании: иначе HKDF выведет
     * другую обёртку и тег не сойдётся.
     *
     * @throws CryptoFailure.MalformedPayload если в записи не две части или они не base64.
     * @throws CryptoFailure.WrongKey если запись запечатана не для нас или контекст другой.
     */
    fun openKey(payload: String, identityPrivate: ByteArray, context: String): ByteArray {
        val parts = payload.split(SEPARATOR, limit = 2)
        if (parts.size != 2) throw CryptoFailure.MalformedPayload

        val ephemeralPublic = parts[0].decodeBase64()
        val wrap = wrapKey(
            shared = agree(identityPrivate, ephemeralPublic),
            ephemeralPublic = ephemeralPublic,
            recipientPublic = publicKey(identityPrivate),
            context = context,
        )
        return open(parts[1].decodeBase64(), wrap)
    }

    /** Общий секрет X25519 — 32 байта. Лёгкий API BouncyCastle, список провайдеров не трогается. */
    private fun agree(privateKey: ByteArray, publicKey: ByteArray): ByteArray {
        val agreement = X25519Agreement()
        agreement.init(X25519PrivateKeyParameters(privateKey, 0))
        return ByteArray(agreement.agreementSize).also {
            agreement.calculateAgreement(X25519PublicKeyParameters(publicKey, 0), it, 0)
        }
    }

    /** Обёртка ключа диалога: HKDF-SHA256 с пустой солью и `info` из обеих половин и контекста. */
    private fun wrapKey(
        shared: ByteArray,
        ephemeralPublic: ByteArray,
        recipientPublic: ByteArray,
        context: String,
    ): ByteArray = hkdf(
        ikm = shared,
        info = ephemeralPublic + recipientPublic + context.toByteArray(Charsets.UTF_8),
        length = KEY_LENGTH,
    )

    /**
     * HKDF-SHA256 с пустой солью.
     *
     * Соль подставляется как 32 нулевых байта: HMAC дополняет ключ нулями до размера блока,
     * поэтому пустая соль и нулевая дают один и тот же PRK, а `SecretKeySpec` пустой массив
     * не принимает.
     */
    private fun hkdf(ikm: ByteArray, info: ByteArray, length: Int): ByteArray {
        val mac = Mac.getInstance(HMAC)

        mac.init(SecretKeySpec(ByteArray(KEY_LENGTH), HMAC))
        val pseudoRandomKey = mac.doFinal(ikm)

        mac.init(SecretKeySpec(pseudoRandomKey, HMAC))
        val output = ByteArrayOutputStream()
        var block = ByteArray(0)
        var counter = 1
        while (output.size() < length) {
            mac.update(block)
            mac.update(info)
            mac.update(counter.toByte())
            block = mac.doFinal()
            output.write(block)
            counter++
        }
        return output.toByteArray().copyOf(length)
    }

    private fun String.decodeBase64(): ByteArray = try {
        Base64.getDecoder().decode(this)
    } catch (error: IllegalArgumentException) {
        throw CryptoFailure.MalformedPayload
    }
}

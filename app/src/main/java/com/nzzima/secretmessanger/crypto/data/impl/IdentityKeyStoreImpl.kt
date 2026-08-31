package com.nzzima.secretmessanger.crypto.data.impl

import android.content.SharedPreferences
import com.nzzima.secretmessanger.crypto.domain.CryptoBox
import com.nzzima.secretmessanger.crypto.domain.api.IdentityKeyStore
import com.nzzima.secretmessanger.crypto.domain.api.MasterKeyProvider
import com.nzzima.secretmessanger.crypto.domain.models.CryptoFailure
import com.nzzima.secretmessanger.utils.constants.Constants
import java.security.GeneralSecurityException
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

/**
 * [IdentityKeyStore] в две ступени: мастер-ключ из [MasterKeyProvider] закрывает приватную
 * половину, закрытая половина лежит в [SharedPreferences].
 *
 * Двух ступеней требует платформа: аппаратное хранилище Android держит только свои
 * алгоритмы и ключ X25519 в него не положить.
 *
 * Запись: `nonce(12) ‖ шифротекст ‖ тег(16)` в base64, ключ записи — `identity.<uid>`.
 */
class IdentityKeyStoreImpl(
    private val preferences: SharedPreferences,
    private val masterKeys: MasterKeyProvider,
) : IdentityKeyStore {

    private companion object {
        const val NONCE_LENGTH = 12
        const val TAG_BITS = 128
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }

    override fun existing(uid: String): ByteArray? {
        val stored = preferences.getString(entry(uid), null) ?: return null
        return decrypt(decode(stored))
    }

    override fun createNew(uid: String): ByteArray {
        val identityPrivate = CryptoBox.newIdentityPrivateKey()
        preferences.edit()
            .putString(entry(uid), Base64.getEncoder().encodeToString(encrypt(identityPrivate)))
            .apply()
        return identityPrivate
    }

    override fun forget(uid: String) {
        preferences.edit().remove(entry(uid)).apply()
    }

    private fun entry(uid: String) = Constants.IDENTITY_ENTRY_PREFIX + uid

    private fun encrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, masterKeys.masterKey())
        return cipher.iv + cipher.doFinal(data)
    }

    private fun decrypt(record: ByteArray): ByteArray {
        if (record.size <= NONCE_LENGTH) throw CryptoFailure.MalformedPayload
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                masterKeys.masterKey(),
                GCMParameterSpec(TAG_BITS, record, 0, NONCE_LENGTH),
            )
            cipher.doFinal(record, NONCE_LENGTH, record.size - NONCE_LENGTH)
        } catch (error: GeneralSecurityException) {
            throw CryptoFailure.WrongKey
        }
    }

    private fun decode(stored: String): ByteArray = try {
        Base64.getDecoder().decode(stored)
    } catch (error: IllegalArgumentException) {
        throw CryptoFailure.MalformedPayload
    }
}

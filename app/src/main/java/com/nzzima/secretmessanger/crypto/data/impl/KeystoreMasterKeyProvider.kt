package com.nzzima.secretmessanger.crypto.data.impl

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.nzzima.secretmessanger.crypto.domain.api.MasterKeyProvider
import com.nzzima.secretmessanger.utils.constants.Constants
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * [MasterKeyProvider] поверх аппаратного хранилища Android.
 *
 * Ключ создаётся под псевдонимом [Constants.MASTER_KEY_ALIAS] при первом обращении и
 * дальше только читается. Наружу он не выходит: шифрование идёт вызовами хранилища.
 *
 * Подтверждения личности ключ не требует — сообщения читаются и при запертом экране.
 *
 * Ключ привязан к устройству и не переносится: `Auto Backup` его не восстанавливает,
 * поэтому переустановка приложения делает сохранённые половины нечитаемыми.
 */
class KeystoreMasterKeyProvider : MasterKeyProvider {

    private companion object {
        const val PROVIDER = "AndroidKeyStore"
        const val KEY_SIZE = 256
    }

    override fun masterKey(): SecretKey {
        val keystore = KeyStore.getInstance(PROVIDER).apply { load(null) }
        val stored = keystore.getEntry(Constants.MASTER_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return stored?.secretKey ?: generate()
    }

    private fun generate(): SecretKey {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
        generator.init(
            KeyGenParameterSpec.Builder(
                Constants.MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE)
                .setUserAuthenticationRequired(false)
                .build(),
        )
        return generator.generateKey()
    }
}

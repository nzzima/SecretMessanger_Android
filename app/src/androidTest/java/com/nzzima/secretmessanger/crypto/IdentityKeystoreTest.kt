package com.nzzima.secretmessanger.crypto

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nzzima.secretmessanger.crypto.data.impl.IdentityKeyStoreImpl
import com.nzzima.secretmessanger.crypto.data.impl.KeystoreMasterKeyProvider
import com.nzzima.secretmessanger.crypto.domain.CryptoBox
import java.security.KeyStore
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Аппаратное хранилище на живом устройстве.
 *
 * Проверяет ровно то, чего не достаёт JVM-тестам: что мастер-ключ действительно заводится
 * в `AndroidKeyStore`, переживает создание нового экземпляра и открывает записи, закрытые
 * прежним. Логику самого хранилища закрывает `IdentityKeyStoreTest`.
 *
 * Псевдоним ключа и файл настроек берутся рабочие, поэтому тест чистит за собой в [tearDown].
 */
@RunWith(AndroidJUnit4::class)
class IdentityKeystoreTest {

    private companion object {
        const val PREFERENCES = "com.nzzima.secretmessanger.identity.androidTest"
        const val UID = "uid-instrumented"
    }

    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun preferences() = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    private fun store() = IdentityKeyStoreImpl(preferences(), KeystoreMasterKeyProvider())

    @Before
    fun setUp() = preferences().edit().clear().apply()

    @After
    fun tearDown() = preferences().edit().clear().apply()

    @Test
    fun masterKeyLandsInAndroidKeystore() {
        KeystoreMasterKeyProvider().masterKey()

        val keystore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

        assertTrue(keystore.containsAlias("com.nzzima.secretmessanger.master"))
        assertNotNull(keystore.getEntry("com.nzzima.secretmessanger.master", null))
    }

    @Test
    fun masterKeyIsTheSameAcrossProviderInstances() {
        val created = IdentityKeyStoreImpl(preferences(), KeystoreMasterKeyProvider()).createNew(UID)
        val readBack = IdentityKeyStoreImpl(preferences(), KeystoreMasterKeyProvider()).existing(UID)

        assertArrayEquals(created, readBack)
    }

    @Test
    fun identityKeySurvivesNewStoreInstance() {
        val created = store().createNew(UID)

        assertArrayEquals(created, store().existing(UID))
        assertEquals(32, created.size)
    }

    @Test
    fun forgottenKeyDoesNotComeBack() {
        store().createNew(UID)
        store().forget(UID)

        assertNull(store().existing(UID))
    }

    @Test
    fun keyFromDeviceStoreOpensSealedConversationKey() {
        val identityPrivate = store().createNew(UID)
        val conversationKey = CryptoBox.newConversationKey()
        val sealed = CryptoBox.sealKey(
            key = conversationKey,
            recipientPublic = CryptoBox.publicKey(identityPrivate),
            context = "uid-1_uid-2#1",
        )

        val opened = CryptoBox.openKey(sealed, requireNotNull(store().existing(UID)), "uid-1_uid-2#1")

        assertArrayEquals(conversationKey, opened)
    }
}

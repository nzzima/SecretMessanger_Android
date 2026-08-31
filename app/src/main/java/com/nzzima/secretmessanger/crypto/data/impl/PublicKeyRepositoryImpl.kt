package com.nzzima.secretmessanger.crypto.data.impl

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nzzima.secretmessanger.crypto.domain.api.PublicKeyRepository
import com.nzzima.secretmessanger.utils.constants.Constants
import kotlinx.coroutines.tasks.await

/** [PublicKeyRepository] поверх Firestore. */
class PublicKeyRepositoryImpl(private val firestore: FirebaseFirestore) : PublicKeyRepository {

    override suspend fun published(uid: String): Result<String?> = runCatching {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(uid)
            .get()
            .await()
            .getString(Constants.PUBLIC_KEY_FIELD)
            ?.takeIf { it.isNotEmpty() }
    }

    override suspend fun publish(uid: String, publicKey: String): Result<Unit> = runCatching {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(uid)
            .set(mapOf(Constants.PUBLIC_KEY_FIELD to publicKey), SetOptions.merge())
            .await()
    }
}

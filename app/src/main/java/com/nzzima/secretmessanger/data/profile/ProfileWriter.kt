package com.nzzima.secretmessanger.data.profile

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/** Запись профиля `users/{uid}`. */
interface ProfileWriter {

    /**
     * Создаёт профиль аккаунта [uid].
     *
     * Запись проходит только тогда, когда [login] уже занят этим же аккаунтом в реестре
     * `logins`: правило Firestore на `users/{uid}` сверяет их между собой.
     */
    suspend fun createProfile(uid: String, login: String, name: String): Result<Unit>
}

/** [ProfileWriter] поверх Firestore. */
class FirestoreProfileRepository(private val firestore: FirebaseFirestore) : ProfileWriter {

    override suspend fun createProfile(uid: String, login: String, name: String): Result<Unit> = runCatching {
        firestore.collection(COLLECTION)
            .document(uid)
            .set(mapOf("login" to login, "name" to name, "someInfo" to ""), SetOptions.merge())
            .await()
    }

    private companion object {
        const val COLLECTION = "users"
    }
}

package com.nzzima.secretmessanger.data.account

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

/** Состояние логина в реестре. */
enum class LoginAvailability {
    /** Логин свободен. */
    FREE,

    /** Логин занят запрашивающим аккаунтом. */
    MINE,

    /** Логин занят другим аккаунтом. */
    TAKEN,
}

/**
 * Реестр занятых логинов: коллекция `logins`, документ на логин.
 *
 * Уникальность обеспечивает правило Firestore, а не этот интерфейс: `create` по
 * существующему документу запрещён, `update` запрещён всем. [check] описывает состояние на
 * момент вызова и не резервирует имя; занимает его только [claim].
 */
interface LoginRegistry {

    /** Состояние логина [login] с точки зрения аккаунта [uid]. */
    suspend fun check(login: String, uid: String?): Result<LoginAvailability>

    /**
     * Занимает [login] за аккаунтом [uid].
     *
     * Проваливается с [Taken], если логин уже занят, и с исходной ошибкой при любом другом
     * отказе.
     */
    suspend fun claim(login: String, uid: String): Result<Unit>

    /** Логин уже занят другим аккаунтом. */
    class Taken : Exception("Логин уже занят — выберите другой")

    companion object {
        /** Идентификатор документа реестра: логин в нижнем регистре. */
        fun key(login: String): String = login.lowercase()
    }
}

/** [LoginRegistry] поверх Firestore. */
class FirestoreLoginRegistry(private val firestore: FirebaseFirestore) : LoginRegistry {

    override suspend fun check(login: String, uid: String?): Result<LoginAvailability> = runCatching {
        val snapshot = firestore.collection(COLLECTION).document(LoginRegistry.key(login)).get().await()
        when {
            !snapshot.exists() -> LoginAvailability.FREE
            snapshot.getString("uid") == uid -> LoginAvailability.MINE
            else -> LoginAvailability.TAKEN
        }
    }

    override suspend fun claim(login: String, uid: String): Result<Unit> = runCatching {
        try {
            firestore.collection(COLLECTION)
                .document(LoginRegistry.key(login))
                .set(mapOf("uid" to uid, "login" to login))
                .await()
        } catch (error: FirebaseFirestoreException) {
            if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) throw LoginRegistry.Taken()
            throw error
        }
    }

    private companion object {
        const val COLLECTION = "logins"
    }
}

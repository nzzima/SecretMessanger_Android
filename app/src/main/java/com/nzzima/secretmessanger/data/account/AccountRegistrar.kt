package com.nzzima.secretmessanger.data.account

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/** Создание и удаление аккаунта. */
interface AccountRegistrar {

    /** Создаёт аккаунт и открывает сессию. Возвращает идентификатор нового аккаунта. */
    suspend fun register(email: String, password: String): Result<String>

    /** Удаляет аккаунт текущей сессии. */
    suspend fun deleteCurrent(): Result<Unit>
}

/** [AccountRegistrar] поверх Firebase Authentication. Почта записывается без окружающих пробелов. */
class FirebaseAccountRepository(private val auth: FirebaseAuth) : AccountRegistrar {

    override suspend fun register(email: String, password: String): Result<String> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        requireNotNull(result.user).uid
    }

    override suspend fun deleteCurrent(): Result<Unit> = runCatching {
        auth.currentUser?.delete()?.await()
        Unit
    }
}

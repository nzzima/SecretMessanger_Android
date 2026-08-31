package com.nzzima.secretmessanger.auth.data.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.nzzima.secretmessanger.auth.domain.api.RegistrationRepository
import com.nzzima.secretmessanger.auth.domain.models.AccountFailure
import kotlinx.coroutines.tasks.await

/**
 * [RegistrationRepository] поверх Firebase Authentication.
 *
 * Почта записывается без окружающих пробелов. Ответы сервиса переводятся в
 * [AccountFailure]; ошибки сети и прочие отказы возвращаются как есть.
 */
class RegistrationRepositoryImpl(private val auth: FirebaseAuth) : RegistrationRepository {

    override suspend fun register(email: String, password: String): Result<String> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        requireNotNull(result.user).uid
    }.recoverCatching { error ->
        throw when (error) {
            is FirebaseAuthUserCollisionException -> AccountFailure.EmailTaken
            is FirebaseAuthWeakPasswordException -> AccountFailure.WeakPassword
            else -> error
        }
    }

    override suspend fun deleteCurrent(): Result<Unit> = runCatching {
        auth.currentUser?.delete()?.await()
    }.map { }
}

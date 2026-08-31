package com.nzzima.secretmessanger.auth.data.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.nzzima.secretmessanger.auth.domain.api.AuthenticationRepository
import com.nzzima.secretmessanger.auth.domain.models.AccountFailure
import kotlinx.coroutines.tasks.await

/**
 * [AuthenticationRepository] поверх Firebase Authentication.
 *
 * Почта сверяется без окружающих пробелов. Отказ по несуществующему аккаунту и отказ по
 * паролю переводятся в один [AccountFailure.WrongCredentials]; ошибки сети возвращаются
 * как есть.
 */
class AuthenticationRepositoryImpl(private val auth: FirebaseAuth) : AuthenticationRepository {

    override suspend fun signIn(email: String, password: String): Result<String> = runCatching {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        requireNotNull(result.user).uid
    }.recoverCatching { error ->
        throw when (error) {
            is FirebaseAuthInvalidUserException,
            is FirebaseAuthInvalidCredentialsException,
            -> AccountFailure.WrongCredentials
            else -> error
        }
    }
}

package com.nzzima.secretmessanger.auth.domain.api

import com.nzzima.secretmessanger.auth.domain.models.AccountFailure

/** Вход в существующий аккаунт. */
interface AuthenticationRepository {

    /**
     * Открывает сессию. Возвращает идентификатор аккаунта.
     *
     * Проваливается с [AccountFailure.WrongCredentials].
     */
    suspend fun signIn(email: String, password: String): Result<String>
}

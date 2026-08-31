package com.nzzima.secretmessanger.auth.domain.api

import com.nzzima.secretmessanger.auth.domain.models.AccountFailure

/** Создание и удаление аккаунта. */
interface RegistrationRepository {

    /**
     * Создаёт аккаунт и открывает сессию. Возвращает идентификатор нового аккаунта.
     *
     * Проваливается с [AccountFailure.EmailTaken] или [AccountFailure.WeakPassword].
     */
    suspend fun register(email: String, password: String): Result<String>

    /** Удаляет аккаунт текущей сессии. */
    suspend fun deleteCurrent(): Result<Unit>
}

package com.nzzima.secretmessanger.auth.domain.api

import com.nzzima.secretmessanger.auth.domain.models.RegistrationFailure

/** Регистрация нового аккаунта для слоя представления. */
interface RegistrationInteractor {

    /**
     * Заводит аккаунт, занимает логин и создаёт профиль. Возвращает идентификатор аккаунта.
     *
     * Проваливается с [RegistrationFailure] либо с отказом, пришедшим из слоя данных.
     */
    suspend fun register(email: String, password: String, login: String): Result<String>
}

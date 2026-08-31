package com.nzzima.secretmessanger.auth.domain.models

import com.nzzima.secretmessanger.utils.constants.Constants

/** Отказы сценария регистрации. Текст показывается пользователю. */
sealed class RegistrationFailure(message: String) : Exception(message) {

    /** Логин не соответствует [com.nzzima.secretmessanger.auth.domain.FieldRules]. */
    data object InvalidLogin : RegistrationFailure(Constants.INVALID_LOGIN)

    /** Логин занят другим аккаунтом. */
    data object LoginTaken : RegistrationFailure(Constants.LOGIN_TAKEN)
}

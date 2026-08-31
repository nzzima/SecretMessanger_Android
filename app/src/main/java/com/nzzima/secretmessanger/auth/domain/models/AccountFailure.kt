package com.nzzima.secretmessanger.auth.domain.models

import com.nzzima.secretmessanger.utils.constants.Constants

/** Отказы, различаемые по ответу сервиса авторизации. Текст показывается пользователю. */
sealed class AccountFailure(message: String) : Exception(message) {

    /** Почта уже занята другим аккаунтом. */
    data object EmailTaken : AccountFailure(Constants.EMAIL_TAKEN)

    /** Пароль отклонён сервисом как слишком простой. */
    data object WeakPassword : AccountFailure(Constants.WEAK_PASSWORD)

    /**
     * Почта или пароль не подошли.
     *
     * Несуществующий аккаунт и неверный пароль сведены в один отказ: раздельные сообщения
     * позволяют проверять, зарегистрирована ли почта.
     */
    data object WrongCredentials : AccountFailure(Constants.WRONG_CREDENTIALS)
}

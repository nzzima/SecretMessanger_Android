package com.nzzima.secretmessanger.auth.domain.models

import com.nzzima.secretmessanger.utils.constants.Constants

/**
 * Логин уже занят другим аккаунтом.
 *
 * Отличает проигранную гонку за имя от отказа связи: на неё сценарий регистрации
 * откатывает созданный аккаунт, на сетевую ошибку — нет.
 */
class LoginTaken : Exception(Constants.LOGIN_TAKEN)

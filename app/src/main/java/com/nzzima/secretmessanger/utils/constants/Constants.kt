package com.nzzima.secretmessanger.utils.constants

/**
 * Значения, общие для нескольких слоёв.
 *
 * Имена коллекций и полей Firestore входят в схему, общую с iOS-приложением: их изменение
 * разрывает совместимость платформ. Тексты отказов и подписи экранов совпадают дословно с
 * `FieldValidator` и `RegistrationViewPresenter` на iOS.
 */
object Constants {

    const val LOGINS_COLLECTION = "logins"
    const val USERS_COLLECTION = "users"

    const val UID_FIELD = "uid"
    const val LOGIN_FIELD = "login"
    const val NAME_FIELD = "name"
    const val SOME_INFO_FIELD = "someInfo"

    const val AUTH_ROUTE = "auth"
    const val CHATS_ROUTE = "chats"

    const val LOGIN_MIN_LENGTH = 3
    const val LOGIN_MAX_LENGTH = 20
    const val PASSWORD_MIN_LENGTH = 6

    const val SUBMIT_TIMEOUT_MS = 20_000L

    const val EMAIL_TAKEN = "Эта почта уже занята"
    const val WEAK_PASSWORD = "Пароль слишком простой"
    const val WRONG_CREDENTIALS = "Неверная почта или пароль"
    const val LOGIN_TAKEN = "Логин уже занят — выберите другой"
    const val INVALID_EMAIL = "Проверьте адрес почты"
    const val INVALID_LOGIN = "Логин — от 3 до 20 символов: латиница, цифры, подчёркивание"
    const val SHORT_PASSWORD = "Пароль должен быть не короче 6 символов"
    const val PASSWORDS_MISMATCH = "Пароли не совпадают"
    const val SERVER_SILENT = "Сервер не ответил. Проверьте связь и попробуйте снова"

    const val AUTH_TITLE = "Авторизация"
    const val REGISTER_TITLE = "Регистрация"
    const val SIGN_IN_SUBMIT = "Войти"
    const val REGISTER_SUBMIT = "Зарегистрироваться"
    const val SWITCH_TO_REGISTER = "Нет аккаунта? Зарегистрироваться"
    const val SWITCH_TO_SIGN_IN = "Уже есть аккаунт? Войти"

    const val EMAIL_PLACEHOLDER = "Email"
    const val LOGIN_PLACEHOLDER = "Логин"
    const val PASSWORD_PLACEHOLDER = "Пароль"
    const val PASSWORD_REPEAT_PLACEHOLDER = "Повторите пароль"

    const val CHATS_TITLE = "Чаты"
    const val CHATS_EMPTY = "Диалогов пока нет"
    const val SIGN_OUT = "Выйти"
}

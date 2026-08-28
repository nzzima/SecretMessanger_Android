package com.nzzima.secretmessanger.ui.auth

/**
 * Состояние экрана авторизации.
 *
 * [passwordRepeat] участвует только в режиме [Mode.Register].
 */
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val passwordRepeat: String = "",
    val login: String = "",
    val mode: Mode = Mode.SignIn,
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {

    /** Режим экрана: вход в существующий аккаунт или создание нового. */
    enum class Mode {
        SignIn,
        Register,
    }

    /** Заголовок экрана для текущего [mode]. */
    val title: String
        get() = when (mode) {
            Mode.SignIn -> "Авторизация"
            Mode.Register -> "Регистрация"
        }

    /** Надпись на кнопке отправки для текущего [mode]. */
    val submitTitle: String
        get() = when (mode) {
            Mode.SignIn -> "Войти"
            Mode.Register -> "Зарегистрироваться"
        }

    /** Надпись на кнопке переключения режима. */
    val switchTitle: String
        get() = when (mode) {
            Mode.SignIn -> "Нет аккаунта? Зарегистрироваться"
            Mode.Register -> "Уже есть аккаунт? Войти"
        }
}

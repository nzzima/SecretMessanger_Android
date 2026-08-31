package com.nzzima.secretmessanger.auth.ui

import com.nzzima.secretmessanger.utils.constants.Constants

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
            Mode.SignIn -> Constants.AUTH_TITLE
            Mode.Register -> Constants.REGISTER_TITLE
        }

    /** Надпись на кнопке отправки для текущего [mode]. */
    val submitTitle: String
        get() = when (mode) {
            Mode.SignIn -> Constants.SIGN_IN_SUBMIT
            Mode.Register -> Constants.REGISTER_SUBMIT
        }

    /** Надпись на кнопке переключения режима. */
    val switchTitle: String
        get() = when (mode) {
            Mode.SignIn -> Constants.SWITCH_TO_REGISTER
            Mode.Register -> Constants.SWITCH_TO_SIGN_IN
        }
}

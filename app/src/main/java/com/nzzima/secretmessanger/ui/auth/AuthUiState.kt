package com.nzzima.secretmessanger.ui.auth

/**
 * Состояние экрана входа.
 *
 * [canSubmit] истинно, когда отправка не идёт, почта не пуста, а пароль не короче шести
 * символов; в режиме [Mode.Register] дополнительно требуется непустой логин.
 */
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val login: String = "",
    val mode: Mode = Mode.SignIn,
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {

    /** Режим экрана: вход в существующий аккаунт или создание нового. */
    enum class Mode { SignIn, Register }

    val canSubmit: Boolean
        get() = !isSubmitting &&
            email.isNotBlank() &&
            password.length >= MIN_PASSWORD_LENGTH &&
            (mode == Mode.SignIn || login.isNotBlank())

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}

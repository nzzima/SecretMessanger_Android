package com.nzzima.secretmessanger.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nzzima.secretmessanger.auth.domain.FieldRules
import com.nzzima.secretmessanger.auth.domain.api.AuthenticationInteractor
import com.nzzima.secretmessanger.auth.domain.api.RegistrationInteractor
import com.nzzima.secretmessanger.utils.constants.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Состояние экрана авторизации и обработка ввода.
 *
 * Успешный вход или регистрация открывают сессию; переход на следующий экран выполняет
 * навигация по изменению
 * [com.nzzima.secretmessanger.session.domain.api.SessionInteractor.observeSession].
 */
class AuthViewModel(
    private val registrationInteractor: RegistrationInteractor,
    private val authenticationInteractor: AuthenticationInteractor,
) : ViewModel() {

    private val authScreenState = MutableStateFlow(AuthUiState())

    /** Текущее состояние экрана. */
    fun observeAuthScreenState(): StateFlow<AuthUiState> = authScreenState.asStateFlow()

    /** Записывает почту и снимает показанную ошибку. */
    fun onEmailChange(value: String) = authScreenState.update { it.copy(email = value, error = null) }

    /** Записывает логин без окружающих пробелов и снимает показанную ошибку. */
    fun onLoginChange(value: String) = authScreenState.update { it.copy(login = value.trim(), error = null) }

    /** Записывает пароль и снимает показанную ошибку. */
    fun onPasswordChange(value: String) = authScreenState.update { it.copy(password = value, error = null) }

    /** Записывает повтор пароля и снимает показанную ошибку. */
    fun onPasswordRepeatChange(value: String) =
        authScreenState.update { it.copy(passwordRepeat = value, error = null) }

    /** Переключает [AuthUiState.mode] и очищает пароли. */
    fun onModeToggle() = authScreenState.update {
        val next = when (it.mode) {
            AuthUiState.Mode.SignIn -> AuthUiState.Mode.Register
            AuthUiState.Mode.Register -> AuthUiState.Mode.SignIn
        }
        it.copy(mode = next, password = "", passwordRepeat = "", error = null)
    }

    /**
     * Проверяет поля и запускает вход или регистрацию по текущему [AuthUiState.mode].
     *
     * Непройденная проверка и отказ сервиса попадают в [AuthUiState.error]; порядок проверок
     * совпадает с `RegistrationViewPresenter` на iOS.
     *
     * Отправка ограничена [Constants.SUBMIT_TIMEOUT_MS]. По истечении срока вызов
     * отменяется, [AuthUiState.isSubmitting] снимается, а в [AuthUiState.error] ложится
     * [Constants.SERVER_SILENT]. Отменённая на полпути регистрация оставляет созданный
     * аккаунт без логина и профиля.
     */
    fun onSubmit() {
        val state = authScreenState.value
        if (state.isSubmitting) return

        validationError(state)?.let { message ->
            authScreenState.update { it.copy(error = message) }
            return
        }

        authScreenState.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val result = withTimeoutOrNull(Constants.SUBMIT_TIMEOUT_MS) {
                when (state.mode) {
                    AuthUiState.Mode.SignIn ->
                        authenticationInteractor.signIn(state.email, state.password)

                    AuthUiState.Mode.Register ->
                        registrationInteractor.register(state.email, state.password, state.login)
                }
            }
            authScreenState.update { current ->
                current.copy(
                    isSubmitting = false,
                    error = when (result) {
                        null -> Constants.SERVER_SILENT
                        else -> result.exceptionOrNull()?.message
                    },
                )
            }
        }
    }

    private fun validationError(state: AuthUiState): String? = when {
        !FieldRules.isValidEmail(state.email) -> Constants.INVALID_EMAIL

        state.mode == AuthUiState.Mode.Register && !FieldRules.isValidLogin(state.login) ->
            Constants.INVALID_LOGIN

        !FieldRules.isValidPassword(state.password) -> Constants.SHORT_PASSWORD

        state.mode == AuthUiState.Mode.Register && state.password != state.passwordRepeat ->
            Constants.PASSWORDS_MISMATCH

        else -> null
    }
}

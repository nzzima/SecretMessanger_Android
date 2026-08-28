package com.nzzima.secretmessanger.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nzzima.secretmessanger.data.account.AccountAuthenticator
import com.nzzima.secretmessanger.domain.FieldRules
import com.nzzima.secretmessanger.domain.RegisterAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Состояние экрана авторизации и обработка ввода.
 *
 * Успешный вход или регистрация открывают сессию; переход на следующий экран выполняет
 * навигация по изменению
 * [com.nzzima.secretmessanger.data.session.SessionReader.session].
 */
class AuthViewModel(
    private val registerAccount: RegisterAccount,
    private val authenticator: AccountAuthenticator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** Записывает почту и снимает показанную ошибку. */
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }

    /** Записывает логин без окружающих пробелов и снимает показанную ошибку. */
    fun onLoginChange(value: String) = _uiState.update { it.copy(login = value.trim(), error = null) }

    /** Записывает пароль и снимает показанную ошибку. */
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }

    /** Записывает повтор пароля и снимает показанную ошибку. */
    fun onPasswordRepeatChange(value: String) =
        _uiState.update { it.copy(passwordRepeat = value, error = null) }

    /** Переключает [AuthUiState.mode] и очищает пароли. */
    fun onModeToggle() = _uiState.update {
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
     */
    fun onSubmit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        validationError(state)?.let { message ->
            _uiState.update { it.copy(error = message) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val result = when (state.mode) {
                AuthUiState.Mode.SignIn -> authenticator.signIn(state.email, state.password)
                AuthUiState.Mode.Register -> registerAccount(state.email, state.password, state.login)
            }
            _uiState.update { current ->
                current.copy(isSubmitting = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    private fun validationError(state: AuthUiState): String? = when {
        !FieldRules.isValidEmail(state.email) -> "Проверьте адрес почты"

        state.mode == AuthUiState.Mode.Register && !FieldRules.isValidLogin(state.login) ->
            "Логин — от 3 до 20 символов: латиница, цифры, подчёркивание"

        !FieldRules.isValidPassword(state.password) -> "Пароль должен быть не короче 6 символов"

        state.mode == AuthUiState.Mode.Register && state.password != state.passwordRepeat ->
            "Пароли не совпадают"

        else -> null
    }
}

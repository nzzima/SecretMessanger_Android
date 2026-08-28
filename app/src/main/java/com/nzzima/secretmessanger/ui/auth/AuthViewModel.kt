package com.nzzima.secretmessanger.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nzzima.secretmessanger.domain.RegisterAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Состояние экрана входа и обработка ввода.
 *
 * Успешная регистрация открывает сессию; переход на следующий экран выполняет навигация по
 * изменению [com.nzzima.secretmessanger.data.session.SessionReader.session].
 *
 * Режим [AuthUiState.Mode.SignIn] отправку не выполняет.
 */
class AuthViewModel(
    private val registerAccount: RegisterAccount,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** Записывает почту и снимает показанную ошибку. */
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }

    /** Записывает пароль и снимает показанную ошибку. */
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }

    /** Записывает логин без окружающих пробелов и снимает показанную ошибку. */
    fun onLoginChange(value: String) = _uiState.update { it.copy(login = value.trim(), error = null) }

    /** Переключает [AuthUiState.mode] между входом и регистрацией. */
    fun onModeToggle() = _uiState.update {
        val next = when (it.mode) {
            AuthUiState.Mode.SignIn -> AuthUiState.Mode.Register
            AuthUiState.Mode.Register -> AuthUiState.Mode.SignIn
        }
        it.copy(mode = next, error = null)
    }

    /**
     * Запускает регистрацию, если [AuthUiState.canSubmit] истинно и режим —
     * [AuthUiState.Mode.Register]. Отказ попадает в [AuthUiState.error].
     */
    fun onSubmit() {
        val state = _uiState.value
        if (!state.canSubmit || state.mode != AuthUiState.Mode.Register) return

        _uiState.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val result = registerAccount(state.email, state.password, state.login)
            _uiState.update { current ->
                current.copy(
                    isSubmitting = false,
                    error = result.exceptionOrNull()?.message,
                )
            }
        }
    }
}

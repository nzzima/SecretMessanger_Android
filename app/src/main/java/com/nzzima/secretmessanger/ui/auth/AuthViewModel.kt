package com.nzzima.secretmessanger.ui.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Состояние экрана входа и обработка ввода.
 *
 * Отправка формы не реализована: [AuthUiState.isSubmitting] всегда `false`. Сценарии входа
 * и регистрации подключаются отдельно.
 */
class AuthViewModel : ViewModel() {

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
}

package com.nzzima.secretmessanger.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/** Экран входа и регистрации. */
@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AuthScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginChange = viewModel::onLoginChange,
        onModeToggle = viewModel::onModeToggle,
        modifier = modifier,
    )
}

/** Разметка экрана входа. Не зависит от [AuthViewModel] и пригодна для превью. */
@Composable
private fun AuthScreen(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginChange: (String) -> Unit,
    onModeToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text("SecretMessanger", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = { Text("Почта") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )

        if (uiState.mode == AuthUiState.Mode.Register) {
            OutlinedTextField(
                value = uiState.login,
                onValueChange = onLoginChange,
                label = { Text("Логин") },
                supportingText = { Text("3–20 символов: латиница, цифры и _") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        uiState.error?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(onClick = {}, enabled = uiState.canSubmit, modifier = Modifier.fillMaxWidth()) {
            Text(if (uiState.mode == AuthUiState.Mode.SignIn) "Войти" else "Зарегистрироваться")
        }

        TextButton(onClick = onModeToggle, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (uiState.mode == AuthUiState.Mode.SignIn) {
                    "Нет аккаунта — завести новый"
                } else {
                    "Уже есть аккаунт — войти"
                },
            )
        }
    }
}

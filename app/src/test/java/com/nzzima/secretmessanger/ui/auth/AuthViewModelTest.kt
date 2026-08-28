package com.nzzima.secretmessanger.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthViewModelTest {

    @Test
    fun `отправка недоступна при коротком пароле`() {
        val model = AuthViewModel()

        model.onEmailChange("a@b.c")
        model.onPasswordChange("12345")

        assertFalse(model.uiState.value.canSubmit)
    }

    @Test
    fun `отправка доступна при заполненных почте и пароле`() {
        val model = AuthViewModel()

        model.onEmailChange("a@b.c")
        model.onPasswordChange("123456")

        assertTrue(model.uiState.value.canSubmit)
    }

    @Test
    fun `регистрация дополнительно требует логин`() {
        val model = AuthViewModel()

        model.onModeToggle()
        model.onEmailChange("a@b.c")
        model.onPasswordChange("123456")
        assertFalse(model.uiState.value.canSubmit)

        model.onLoginChange("nzzima")
        assertTrue(model.uiState.value.canSubmit)
    }

    @Test
    fun `логин записывается без окружающих пробелов`() {
        val model = AuthViewModel()

        model.onLoginChange("  nzzima  ")

        assertEquals("nzzima", model.uiState.value.login)
    }
}

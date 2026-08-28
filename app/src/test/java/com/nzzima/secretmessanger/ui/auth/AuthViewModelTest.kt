package com.nzzima.secretmessanger.ui.auth

import com.nzzima.secretmessanger.domain.FakeAccountRegistrar
import com.nzzima.secretmessanger.domain.FakeLoginRegistry
import com.nzzima.secretmessanger.domain.FakeProfileWriter
import com.nzzima.secretmessanger.domain.RegisterAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        registrar: FakeAccountRegistrar = FakeAccountRegistrar(),
        registry: FakeLoginRegistry = FakeLoginRegistry(),
        profiles: FakeProfileWriter = FakeProfileWriter(),
    ) = AuthViewModel(RegisterAccount(registrar, registry, profiles), registrar)

    @Test
    fun `отправка недоступна при коротком пароле`() {
        val model = viewModel()

        model.onEmailChange("a@b.c")
        model.onPasswordChange("12345")

        assertFalse(model.uiState.value.canSubmit)
    }

    @Test
    fun `регистрация дополнительно требует логин`() {
        val model = viewModel()

        model.onModeToggle()
        model.onEmailChange("a@b.c")
        model.onPasswordChange("123456")
        assertFalse(model.uiState.value.canSubmit)

        model.onLoginChange("nzzima")
        assertTrue(model.uiState.value.canSubmit)
    }

    @Test
    fun `логин записывается без окружающих пробелов`() {
        val model = viewModel()

        model.onLoginChange("  nzzima  ")

        assertEquals("nzzima", model.uiState.value.login)
    }

    @Test
    fun `удачная регистрация проходит до профиля и не оставляет ошибки`() = runTest(dispatcher) {
        val registry = FakeLoginRegistry()
        val profiles = FakeProfileWriter()
        val model = viewModel(registry = registry, profiles = profiles)

        model.onModeToggle()
        model.onEmailChange("a@b.c")
        model.onPasswordChange("123456")
        model.onLoginChange("nzzima")
        model.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("nzzima"), registry.claimed)
        assertEquals(Triple("uid-1", "nzzima", "nzzima"), profiles.created)
        assertFalse(model.uiState.value.isSubmitting)
        assertNull(model.uiState.value.error)
    }

    @Test
    fun `занятый логин показывается ошибкой на экране`() = runTest(dispatcher) {
        val registry = FakeLoginRegistry(mutableMapOf("nzzima" to "uid-другой"))
        val model = viewModel(registry = registry)

        model.onModeToggle()
        model.onEmailChange("a@b.c")
        model.onPasswordChange("123456")
        model.onLoginChange("nzzima")
        model.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Логин уже занят — выберите другой", model.uiState.value.error)
        assertFalse(model.uiState.value.isSubmitting)
    }

    @Test
    fun `вход с верной парой проходит и не оставляет ошибки`() = runTest(dispatcher) {
        val profiles = FakeProfileWriter()
        val model = viewModel(
            registrar = FakeAccountRegistrar(knownCredentials = "a@b.c" to "123456"),
            profiles = profiles,
        )

        model.onEmailChange("a@b.c")
        model.onPasswordChange("123456")
        model.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        assertNull(model.uiState.value.error)
        assertFalse(model.uiState.value.isSubmitting)
        assertNull("вход профиль не пишет", profiles.created)
    }

    @Test
    fun `неверная пара показывается одним отказом`() = runTest(dispatcher) {
        val model = viewModel(registrar = FakeAccountRegistrar(knownCredentials = "a@b.c" to "123456"))

        model.onEmailChange("a@b.c")
        model.onPasswordChange("неверный")
        model.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Неверная почта или пароль", model.uiState.value.error)
    }

    @Test
    fun `в режиме входа логин не требуется`() {
        val model = viewModel()

        model.onEmailChange("a@b.c")
        model.onPasswordChange("123456")

        assertTrue(model.uiState.value.canSubmit)
    }
}

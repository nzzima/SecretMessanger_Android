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
    fun `короткий пароль отбивается сообщением`() = runTest(dispatcher) {
        val model = viewModel()

        model.onEmailChange("a@b.co")
        model.onPasswordChange("12345")
        model.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Пароль должен быть не короче 6 символов", model.uiState.value.error)
    }

    @Test
    fun `битая почта отбивается первой`() = runTest(dispatcher) {
        val model = viewModel()

        model.onEmailChange("не-почта")
        model.onPasswordChange("12345")
        model.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Проверьте адрес почты", model.uiState.value.error)
    }

    @Test
    fun `несовпадающие пароли отбиваются при регистрации`() = runTest(dispatcher) {
        val profiles = FakeProfileWriter()
        val model = viewModel(profiles = profiles)

        model.onModeToggle()
        model.onEmailChange("a@b.co")
        model.onLoginChange("nzzima")
        model.onPasswordChange("123456")
        model.onPasswordRepeatChange("123457")
        model.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Пароли не совпадают", model.uiState.value.error)
        assertNull(profiles.created)
    }

    @Test
    fun `регистрация требует логин по правилам`() = runTest(dispatcher) {
        val model = viewModel()

        model.onModeToggle()
        model.onEmailChange("a@b.co")
        model.onPasswordChange("123456")
        model.onPasswordRepeatChange("123456")
        model.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Логин — от 3 до 20 символов: латиница, цифры, подчёркивание", model.uiState.value.error)
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
        model.onEmailChange("a@b.co")
        model.onPasswordChange("123456")
        model.onPasswordRepeatChange("123456")
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
        model.onEmailChange("a@b.co")
        model.onPasswordChange("123456")
        model.onPasswordRepeatChange("123456")
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
            registrar = FakeAccountRegistrar(knownCredentials = "a@b.co" to "123456"),
            profiles = profiles,
        )

        model.onEmailChange("a@b.co")
        model.onPasswordChange("123456")
        model.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        assertNull(model.uiState.value.error)
        assertFalse(model.uiState.value.isSubmitting)
        assertNull("вход профиль не пишет", profiles.created)
    }

    @Test
    fun `неверная пара показывается одним отказом`() = runTest(dispatcher) {
        val model = viewModel(registrar = FakeAccountRegistrar(knownCredentials = "a@b.co" to "123456"))

        model.onEmailChange("a@b.co")
        model.onPasswordChange("неверный")
        model.onSubmit()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Неверная почта или пароль", model.uiState.value.error)
    }

    @Test
    fun `переключение режима очищает пароли`() {
        val model = viewModel()

        model.onPasswordChange("123456")
        model.onModeToggle()

        assertEquals("", model.uiState.value.password)
        assertEquals("", model.uiState.value.passwordRepeat)
        assertEquals("Регистрация", model.uiState.value.title)
    }
}

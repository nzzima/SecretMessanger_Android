package com.nzzima.secretmessanger.main.ui

import com.nzzima.secretmessanger.crypto.domain.api.IdentityInteractor
import com.nzzima.secretmessanger.crypto.domain.models.IdentityState
import com.nzzima.secretmessanger.session.domain.FakeSessionRepository
import com.nzzima.secretmessanger.session.domain.impl.SessionInteractorImpl
import com.nzzima.secretmessanger.session.domain.models.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

/**
 * Оболочка приложения. Главная проверка — **в чаты не попасть мимо развилки**.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RootViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val sessions = FakeSessionRepository()
    private val identity = FakeIdentityInteractor()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = RootViewModel(SessionInteractorImpl(sessions, sessions), identity)

    private fun RootViewModel.state() = observeRootState().value

    @Test
    fun `без сессии оболочка анонимна и ключ не проверяется`() = runTest(dispatcher) {
        val model = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        assertSame(RootState.Anonymous, model.state())
        assertEquals("развилка не должна вызываться без сессии", 0, identity.prepares)
    }

    @Test
    fun `сессия с готовым ключом открывает чаты`() = runTest(dispatcher) {
        sessions.signIn("uid-1")
        val model = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        assertSame(RootState.Ready, model.state())
        assertEquals(1, identity.prepares)
    }

    @Test
    fun `чужая половина в профиле держит на развилке, а не пускает в чаты`() = runTest(dispatcher) {
        identity.state = IdentityState.NeedsConfirmation
        sessions.signIn("uid-1")
        val model = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        assertSame(RootState.NeedsConfirmation, model.state())
        assertNotEquals(RootState.Ready, model.state())
    }

    @Test
    fun `отказ проверки не пускает в чаты и показывает причину`() = runTest(dispatcher) {
        identity.prepareFails = IllegalStateException("client is offline")
        sessions.signIn("uid-1")
        val model = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(RootState.Failed("client is offline"), model.state())
    }

    @Test
    fun `подтверждение публикует поверх и открывает чаты`() = runTest(dispatcher) {
        identity.state = IdentityState.NeedsConfirmation
        sessions.signIn("uid-1")
        val model = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        model.confirmOverwrite()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("uid-1"), identity.overwritten)
        assertSame(RootState.Ready, model.state())
    }

    @Test
    fun `неудачная публикация оставляет на развилке`() = runTest(dispatcher) {
        identity.state = IdentityState.NeedsConfirmation
        identity.overwriteFails = IllegalStateException("PERMISSION_DENIED")
        sessions.signIn("uid-1")
        val model = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        model.confirmOverwrite()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(RootState.Failed("PERMISSION_DENIED"), model.state())
    }

    @Test
    fun `повтор после отказа связи доводит до чатов`() = runTest(dispatcher) {
        identity.prepareFails = IllegalStateException("client is offline")
        sessions.signIn("uid-1")
        val model = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        identity.prepareFails = null
        model.retry()
        dispatcher.scheduler.advanceUntilIdle()

        assertSame(RootState.Ready, model.state())
    }

    @Test
    fun `выход возвращает в анонимное состояние`() = runTest(dispatcher) {
        sessions.signIn("uid-1")
        val model = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        model.signOut()
        dispatcher.scheduler.advanceUntilIdle()

        assertSame(RootState.Anonymous, model.state())
        assertEquals(Session.Anonymous, sessions.session.value)
    }
}

/** [IdentityInteractor] в памяти. Считает вызовы, чтобы ловить лишние проверки. */
private class FakeIdentityInteractor : IdentityInteractor {

    var state: IdentityState = IdentityState.Ready
    var prepareFails: Throwable? = null
    var overwriteFails: Throwable? = null
    var prepares = 0
    val overwritten = mutableListOf<String>()

    override suspend fun prepare(uid: String): Result<IdentityState> {
        prepares++
        return prepareFails?.let { Result.failure(it) } ?: Result.success(state)
    }

    override suspend fun publishOverwriting(uid: String): Result<Unit> {
        overwriteFails?.let { return Result.failure(it) }
        overwritten += uid
        return Result.success(Unit)
    }
}

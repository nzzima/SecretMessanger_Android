package com.nzzima.secretmessanger.auth.domain

import com.nzzima.secretmessanger.auth.domain.impl.ProfileRepairInteractorImpl
import com.nzzima.secretmessanger.auth.domain.models.RegistrationFailure
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Достройка регистрации, оборванной на полпути.
 *
 * Два вида обрыва: логин не занят вовсе и логин занят, а профиля нет. Оба должны
 * достраиваться, и ни один не должен затирать чужое имя.
 */
class ProfileRepairInteractorTest {

    private fun interactor(
        profiles: FakeProfileRepository = FakeProfileRepository(),
        logins: FakeLoginRepository = FakeLoginRepository(),
    ) = Triple(profiles, logins, ProfileRepairInteractorImpl(profiles, logins))

    @Test
    fun `аккаунт без профиля считается недостроенным`() = runTest {
        val (_, _, repair) = interactor()

        assertFalse(repair.isComplete("uid-1").getOrThrow())
    }

    @Test
    fun `аккаунт с профилем достраивать не нужно`() = runTest {
        val (_, _, repair) = interactor(profiles = FakeProfileRepository(mutableSetOf("uid-1")))

        assertTrue(repair.isComplete("uid-1").getOrThrow())
    }

    @Test
    fun `отказ чтения профиля возвращается отказом, а не «профиля нет»`() = runTest {
        val error = IllegalStateException("нет связи")
        val (_, _, repair) = interactor(profiles = FakeProfileRepository(existsFails = error))

        assertSame(error, repair.isComplete("uid-1").exceptionOrNull())
    }

    @Test
    fun `свободный логин занимается и профиль пишется`() = runTest {
        val (profiles, logins, repair) = interactor()

        repair.complete("uid-1", "nzzima").getOrThrow()

        assertEquals(listOf("nzzima"), logins.claimed)
        assertEquals(Triple("uid-1", "nzzima", "nzzima"), profiles.created)
    }

    @Test
    fun `логин уже наш — повторно не занимается, профиль дописывается`() = runTest {
        val logins = FakeLoginRepository(taken = mutableMapOf("nzzima" to "uid-1"))
        val (profiles, _, repair) = interactor(logins = logins)

        repair.complete("uid-1", "nzzima").getOrThrow()

        assertTrue("занимать своё же незачем", logins.claimed.isEmpty())
        assertEquals(Triple("uid-1", "nzzima", "nzzima"), profiles.created)
    }

    @Test
    fun `чужой логин не занимается и профиль не пишется`() = runTest {
        val logins = FakeLoginRepository(taken = mutableMapOf("nzzima" to "uid-2"))
        val (profiles, _, repair) = interactor(logins = logins)

        val error = repair.complete("uid-1", "nzzima").exceptionOrNull()

        assertSame(RegistrationFailure.LoginTaken, error)
        assertTrue(logins.claimed.isEmpty())
        assertNull("чужое имя не должно попасть в профиль", profiles.created)
    }

    @Test
    fun `негодный логин отбивается до похода в базу`() = runTest {
        val (profiles, logins, repair) = interactor()

        val error = repair.complete("uid-1", "ab").exceptionOrNull()

        assertSame(RegistrationFailure.InvalidLogin, error)
        assertTrue(logins.claimed.isEmpty())
        assertNull(profiles.created)
    }

    @Test
    fun `проигранная гонка за логин возвращает «логин занят»`() = runTest {
        val logins = FakeLoginRepository(claimFails = com.nzzima.secretmessanger.auth.domain.models.LoginTaken())
        val (profiles, _, repair) = interactor(logins = logins)

        val error = repair.complete("uid-1", "nzzima").exceptionOrNull()

        assertSame(RegistrationFailure.LoginTaken, error)
        assertNull(profiles.created)
    }
}

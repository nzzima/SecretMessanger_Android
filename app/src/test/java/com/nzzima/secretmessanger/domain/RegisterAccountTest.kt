package com.nzzima.secretmessanger.domain

import com.nzzima.secretmessanger.data.account.LoginRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterAccountTest {

    private fun register(
        registrar: FakeAccountRegistrar = FakeAccountRegistrar(),
        registry: FakeLoginRegistry = FakeLoginRegistry(),
        profiles: FakeProfileWriter = FakeProfileWriter(),
    ) = RegisterAccount(registrar, registry, profiles)

    @Test
    fun `логин не по правилам отбивается до обращения к сети`() = runTest {
        val registrar = FakeAccountRegistrar()
        val profiles = FakeProfileWriter()

        for (bad in listOf("ab", "с-кириллицей", "слишком_длинный_логин_совсем", "с пробелом", "точка.точка")) {
            val result = register(registrar = registrar, profiles = profiles)("a@b.c", "123456", bad)

            assertTrue(bad, result.exceptionOrNull() is RegisterAccount.Failure.InvalidLogin)
        }
        assertNull("профиль не должен создаваться", profiles.created)
        assertFalse("аккаунт не должен удаляться", registrar.deleted)
    }

    @Test
    fun `занятый логин отбивается до создания аккаунта`() = runTest {
        val registrar = FakeAccountRegistrar()
        val registry = FakeLoginRegistry(mutableMapOf("nzzima" to "uid-другой"))

        val result = register(registrar = registrar, registry = registry)("a@b.c", "123456", "nzzima")

        assertTrue(result.exceptionOrNull() is RegisterAccount.Failure.LoginTaken)
        assertFalse(registrar.deleted)
    }

    @Test
    fun `удачная регистрация занимает логин и пишет профиль`() = runTest {
        val registry = FakeLoginRegistry()
        val profiles = FakeProfileWriter()

        val result = register(registry = registry, profiles = profiles)("a@b.c", "123456", "nzzima")

        assertEquals("uid-1", result.getOrNull())
        assertEquals(listOf("nzzima"), registry.claimed)
        assertEquals(Triple("uid-1", "nzzima", "nzzima"), profiles.created)
    }

    @Test
    fun `проигранная гонка за логин откатывает аккаунт`() = runTest {
        val registrar = FakeAccountRegistrar()
        val registry = FakeLoginRegistry(claimFails = LoginRegistry.Taken())
        val profiles = FakeProfileWriter()

        val result = register(registrar, registry, profiles)("a@b.c", "123456", "nzzima")

        assertTrue(result.exceptionOrNull() is RegisterAccount.Failure.LoginTaken)
        assertTrue("аккаунт обязан откатиться", registrar.deleted)
        assertNull("профиль писать нечему", profiles.created)
    }

    @Test
    fun `сетевой отказ при занятии логина аккаунт не удаляет`() = runTest {
        val registrar = FakeAccountRegistrar()
        val registry = FakeLoginRegistry(claimFails = IllegalStateException("client is offline"))

        val result = register(registrar = registrar, registry = registry)("a@b.c", "123456", "nzzima")

        assertTrue(result.isFailure)
        assertFalse("на сетевой ошибке аккаунт остаётся", registrar.deleted)
    }

    @Test
    fun `отказ создания аккаунта возвращается как есть`() = runTest {
        val registrar = FakeAccountRegistrar(registerFails = IllegalStateException("email занят"))
        val profiles = FakeProfileWriter()

        val result = register(registrar = registrar, profiles = profiles)("a@b.c", "123456", "nzzima")

        assertEquals("email занят", result.exceptionOrNull()?.message)
        assertNull(profiles.created)
    }
}

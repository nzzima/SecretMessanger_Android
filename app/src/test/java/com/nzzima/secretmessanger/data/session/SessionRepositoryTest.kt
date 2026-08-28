package com.nzzima.secretmessanger.data.session

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionRepositoryTest {

    @Test
    fun `новый репозиторий отдаёт анонимную сессию`() = runTest {
        val repository = InMemorySessionRepository()

        assertEquals(Session.Anonymous, repository.session.value)
        assertNull(repository.session.value.uidOrNull)
    }

    @Test
    fun `вход публикует uid в состоянии сессии`() = runTest {
        val repository = InMemorySessionRepository()

        repository.signIn("uid-1")

        assertEquals(Session.Authenticated("uid-1"), repository.session.value)
        assertEquals("uid-1", repository.session.value.uidOrNull)
    }

    @Test
    fun `выход возвращает состояние к анонимному`() = runTest {
        val repository = InMemorySessionRepository(Session.Authenticated("uid-1"))

        repository.signOut()

        assertEquals(Session.Anonymous, repository.session.value)
    }
}

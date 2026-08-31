package com.nzzima.secretmessanger.session.domain

import com.nzzima.secretmessanger.session.domain.impl.SessionInteractorImpl
import com.nzzima.secretmessanger.session.domain.models.Session
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionInteractorTest {

    @Test
    fun `состояние сессии видно наблюдателю сразу и после входа`() {
        val repository = FakeSessionRepository()
        val interactor = SessionInteractorImpl(repository, repository)

        assertEquals(Session.Anonymous, interactor.observeSession().value)

        repository.signIn("uid-1")

        assertEquals(Session.Authenticated("uid-1"), interactor.observeSession().value)
    }

    @Test
    fun `выход переводит сессию в анонимную`() {
        val repository = FakeSessionRepository(Session.Authenticated("uid-1"))
        val interactor = SessionInteractorImpl(repository, repository)

        interactor.signOut()

        assertEquals(Session.Anonymous, interactor.observeSession().value)
    }
}

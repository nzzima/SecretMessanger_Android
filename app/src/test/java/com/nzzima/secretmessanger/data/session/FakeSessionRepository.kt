package com.nzzima.secretmessanger.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Сессия в памяти. Состояние задаётся тестом и не переживает создание нового экземпляра. */
class FakeSessionRepository(initial: Session = Session.Anonymous) : SessionReader, SessionCloser {

    private val state = MutableStateFlow(initial)

    override val session: StateFlow<Session> = state.asStateFlow()

    override fun signOut() {
        state.value = Session.Anonymous
    }

    /** Устанавливает [Session.Authenticated] с идентификатором [uid]. */
    fun signIn(uid: String) {
        state.value = Session.Authenticated(uid)
    }
}

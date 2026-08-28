package com.nzzima.secretmessanger.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Чтение текущей сессии.
 *
 * [session] отдаёт актуальное состояние сразу при подписке и обновляется при каждом входе
 * и выходе.
 */
interface SessionReader {
    val session: StateFlow<Session>
}

/** Завершение сессии. */
interface SessionCloser {
    /** Переводит [SessionReader.session] в [Session.Anonymous]. */
    fun signOut()
}

/**
 * Реализация в памяти.
 *
 * Сессия задаётся при создании и не переживает перезапуск процесса. Замещается
 * реализацией поверх Firebase Authentication.
 */
class InMemorySessionRepository(initial: Session = Session.Anonymous) : SessionReader, SessionCloser {

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

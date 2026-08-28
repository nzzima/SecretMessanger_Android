package com.nzzima.secretmessanger.data.session

import kotlinx.coroutines.flow.StateFlow

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

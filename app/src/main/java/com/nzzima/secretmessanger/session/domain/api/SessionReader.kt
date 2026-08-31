package com.nzzima.secretmessanger.session.domain.api

import com.nzzima.secretmessanger.session.domain.models.Session
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

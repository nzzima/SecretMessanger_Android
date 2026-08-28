package com.nzzima.secretmessanger.data.session

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Сессия поверх Firebase Authentication.
 *
 * Начальное значение [session] берётся из [FirebaseAuth.getCurrentUser] при создании,
 * дальнейшие — из слушателя состояния авторизации. Слушатель снимается только вместе с
 * процессом.
 *
 * Сессия Firebase сохраняется на устройстве и переживает перезапуск приложения.
 */
class FirebaseSessionRepository(private val auth: FirebaseAuth) : SessionReader, SessionCloser {

    private val state = MutableStateFlow(auth.currentUser.toSession())

    override val session: StateFlow<Session> = state.asStateFlow()

    init {
        auth.addAuthStateListener { updated -> state.value = updated.currentUser.toSession() }
    }

    override fun signOut() = auth.signOut()
}

private fun FirebaseUser?.toSession(): Session =
    if (this == null) Session.Anonymous else Session.Authenticated(uid)

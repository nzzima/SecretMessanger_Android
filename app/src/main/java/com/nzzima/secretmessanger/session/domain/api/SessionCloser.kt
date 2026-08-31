package com.nzzima.secretmessanger.session.domain.api

/** Завершение сессии. */
interface SessionCloser {

    /** Переводит [SessionReader.session] в [com.nzzima.secretmessanger.session.domain.models.Session.Anonymous]. */
    fun signOut()
}

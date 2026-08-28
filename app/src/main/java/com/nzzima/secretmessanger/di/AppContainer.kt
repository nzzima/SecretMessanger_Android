package com.nzzima.secretmessanger.di

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.nzzima.secretmessanger.data.session.FirebaseSessionRepository
import com.nzzima.secretmessanger.data.session.SessionCloser
import com.nzzima.secretmessanger.data.session.SessionReader

/**
 * Граф зависимостей приложения. Создаётся один раз в
 * [com.nzzima.secretmessanger.SecretMessangerApp] и живёт столько же, сколько процесс.
 *
 * Наружу отдаются только интерфейсы: конкретные реализации остаются приватными.
 */
class AppContainer {

    private val sessionRepository by lazy { FirebaseSessionRepository(Firebase.auth) }

    val sessionReader: SessionReader get() = sessionRepository

    val sessionCloser: SessionCloser get() = sessionRepository
}

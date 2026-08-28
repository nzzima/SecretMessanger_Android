package com.nzzima.secretmessanger.di

import com.nzzima.secretmessanger.data.session.InMemorySessionRepository
import com.nzzima.secretmessanger.data.session.SessionCloser
import com.nzzima.secretmessanger.data.session.SessionReader

/**
 * Граф зависимостей приложения. Создаётся один раз в
 * [com.nzzima.secretmessanger.SecretMessangerApp] и живёт столько же, сколько процесс.
 *
 * Наружу отдаются только интерфейсы: конкретные реализации остаются приватными.
 */
class AppContainer {

    private val sessionRepository = InMemorySessionRepository()

    val sessionReader: SessionReader = sessionRepository

    val sessionCloser: SessionCloser = sessionRepository
}

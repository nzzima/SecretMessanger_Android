package com.nzzima.secretmessanger.di

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.nzzima.secretmessanger.data.account.FirebaseAccountRepository
import com.nzzima.secretmessanger.data.account.FirestoreLoginRegistry
import com.nzzima.secretmessanger.data.profile.FirestoreProfileRepository
import com.nzzima.secretmessanger.data.session.FirebaseSessionRepository
import com.nzzima.secretmessanger.data.session.SessionCloser
import com.nzzima.secretmessanger.data.session.SessionReader
import com.nzzima.secretmessanger.domain.RegisterAccount

/**
 * Граф зависимостей приложения. Создаётся один раз в
 * [com.nzzima.secretmessanger.SecretMessangerApp] и живёт столько же, сколько процесс.
 *
 * Наружу отдаются сценарии и интерфейсы: конкретные реализации остаются приватными.
 */
class AppContainer {

    private val auth by lazy { Firebase.auth }
    private val firestore by lazy { Firebase.firestore }

    private val sessionRepository by lazy { FirebaseSessionRepository(auth) }
    private val accountRepository by lazy { FirebaseAccountRepository(auth) }
    private val loginRegistry by lazy { FirestoreLoginRegistry(firestore) }
    private val profileRepository by lazy { FirestoreProfileRepository(firestore) }

    val sessionReader: SessionReader get() = sessionRepository

    val sessionCloser: SessionCloser get() = sessionRepository

    val registerAccount by lazy { RegisterAccount(accountRepository, loginRegistry, profileRepository) }
}

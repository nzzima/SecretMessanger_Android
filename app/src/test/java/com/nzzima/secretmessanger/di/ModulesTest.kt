package com.nzzima.secretmessanger.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nzzima.secretmessanger.auth.domain.api.AuthenticationInteractor
import com.nzzima.secretmessanger.auth.domain.api.AuthenticationRepository
import com.nzzima.secretmessanger.auth.domain.api.LoginRepository
import com.nzzima.secretmessanger.auth.domain.api.ProfileRepository
import com.nzzima.secretmessanger.auth.domain.api.RegistrationInteractor
import com.nzzima.secretmessanger.auth.domain.api.RegistrationRepository
import com.nzzima.secretmessanger.session.domain.api.SessionCloser
import com.nzzima.secretmessanger.session.domain.api.SessionInteractor
import com.nzzima.secretmessanger.session.domain.api.SessionReader
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

/**
 * Проверка полноты графа зависимостей.
 *
 * Koin разрешает зависимости во время выполнения: пропущенное определение падает не при
 * сборке, а при открытии экрана. Проверка сверяет параметры конструкторов всех определений
 * с объявленными типами и держит эту ошибку на уровне тестов.
 *
 * `extraTypes` перечисляет типы, объявленные в соседних модулях: каждый модуль проверяется
 * отдельно и о чужих определениях не знает.
 *
 * `dataModule` не проверяется: он отдаёт клиентов Firebase, созданных фабриками SDK, а
 * проверка читает конструкторы объявленных типов и требует их аргументы как зависимости.
 */
@OptIn(KoinExperimentalAPI::class)
class ModulesTest {

    @Test
    fun `граф зависимостей полон`() {
        repositoryModule.verify(
            extraTypes = listOf(FirebaseAuth::class, FirebaseFirestore::class),
        )

        interactorModule.verify(
            extraTypes = listOf(
                RegistrationRepository::class,
                AuthenticationRepository::class,
                LoginRepository::class,
                ProfileRepository::class,
                SessionReader::class,
                SessionCloser::class,
            ),
        )

        viewModelModule.verify(
            extraTypes = listOf(
                RegistrationInteractor::class,
                AuthenticationInteractor::class,
                SessionInteractor::class,
            ),
        )
    }
}

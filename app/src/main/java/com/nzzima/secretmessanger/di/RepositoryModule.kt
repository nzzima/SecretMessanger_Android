package com.nzzima.secretmessanger.di

import com.nzzima.secretmessanger.auth.data.impl.AuthenticationRepositoryImpl
import com.nzzima.secretmessanger.auth.data.impl.LoginRepositoryImpl
import com.nzzima.secretmessanger.auth.data.impl.ProfileRepositoryImpl
import com.nzzima.secretmessanger.auth.data.impl.RegistrationRepositoryImpl
import com.nzzima.secretmessanger.auth.domain.api.AuthenticationRepository
import com.nzzima.secretmessanger.auth.domain.api.LoginRepository
import com.nzzima.secretmessanger.auth.domain.api.ProfileRepository
import com.nzzima.secretmessanger.auth.domain.api.RegistrationRepository
import com.nzzima.secretmessanger.session.data.impl.SessionRepositoryImpl
import com.nzzima.secretmessanger.session.domain.api.SessionCloser
import com.nzzima.secretmessanger.session.domain.api.SessionReader
import org.koin.dsl.binds
import org.koin.dsl.module

/**
 * Реализации репозиториев.
 *
 * [SessionRepositoryImpl] объявлен одним определением на два интерфейса: он держит
 * состояние сессии, и второй экземпляр слушал бы авторизацию отдельно.
 */
val repositoryModule = module {

    single<RegistrationRepository> {
        RegistrationRepositoryImpl(get())
    }

    single<AuthenticationRepository> {
        AuthenticationRepositoryImpl(get())
    }

    single<LoginRepository> {
        LoginRepositoryImpl(get())
    }

    single<ProfileRepository> {
        ProfileRepositoryImpl(get())
    }

    single {
        SessionRepositoryImpl(get())
    } binds arrayOf(SessionReader::class, SessionCloser::class)
}

package com.nzzima.secretmessanger.di

import com.nzzima.secretmessanger.auth.domain.api.AuthenticationInteractor
import com.nzzima.secretmessanger.auth.domain.api.RegistrationInteractor
import com.nzzima.secretmessanger.auth.domain.impl.AuthenticationInteractorImpl
import com.nzzima.secretmessanger.auth.domain.impl.RegistrationInteractorImpl
import com.nzzima.secretmessanger.session.domain.api.SessionInteractor
import com.nzzima.secretmessanger.session.domain.impl.SessionInteractorImpl
import org.koin.dsl.module

/** Сценарии, которыми пользуется слой представления. */
val interactorModule = module {

    single<RegistrationInteractor> {
        RegistrationInteractorImpl(get(), get(), get())
    }

    single<AuthenticationInteractor> {
        AuthenticationInteractorImpl(get())
    }

    single<SessionInteractor> {
        SessionInteractorImpl(get(), get())
    }
}

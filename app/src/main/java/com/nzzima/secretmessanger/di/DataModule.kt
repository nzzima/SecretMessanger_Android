package com.nzzima.secretmessanger.di

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import org.koin.dsl.module

/** Клиенты внешних сервисов. Создаются один раз на процесс. */
val dataModule = module {

    single {
        Firebase.auth
    }

    single {
        Firebase.firestore
    }
}

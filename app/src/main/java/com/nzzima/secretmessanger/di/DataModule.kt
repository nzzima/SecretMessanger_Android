package com.nzzima.secretmessanger.di

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.nzzima.secretmessanger.crypto.data.impl.KeystoreMasterKeyProvider
import com.nzzima.secretmessanger.crypto.domain.api.MasterKeyProvider
import com.nzzima.secretmessanger.utils.constants.Constants
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Клиенты внешних сервисов. Создаются один раз на процесс. */
val dataModule = module {

    single {
        Firebase.auth
    }

    single {
        Firebase.firestore
    }

    single {
        androidContext().getSharedPreferences(Constants.IDENTITY_PREFERENCES, Context.MODE_PRIVATE)
    }

    single<MasterKeyProvider> {
        KeystoreMasterKeyProvider()
    }
}

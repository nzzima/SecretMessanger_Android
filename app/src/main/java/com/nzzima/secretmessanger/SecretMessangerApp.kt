package com.nzzima.secretmessanger

import android.app.Application
import com.nzzima.secretmessanger.di.AppContainer

/** Точка создания [AppContainer]. */
class SecretMessangerApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}

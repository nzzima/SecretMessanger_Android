package com.nzzima.secretmessanger

import android.app.Application
import com.nzzima.secretmessanger.di.dataModule
import com.nzzima.secretmessanger.di.interactorModule
import com.nzzima.secretmessanger.di.repositoryModule
import com.nzzima.secretmessanger.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/** Точка запуска графа зависимостей. Граф живёт столько же, сколько процесс. */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(dataModule, repositoryModule, interactorModule, viewModelModule)
        }
    }
}

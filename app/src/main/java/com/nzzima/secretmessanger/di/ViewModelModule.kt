package com.nzzima.secretmessanger.di

import com.nzzima.secretmessanger.auth.ui.AuthViewModel
import com.nzzima.secretmessanger.chats.ui.ChatsViewModel
import com.nzzima.secretmessanger.main.ui.RootViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Модели представления экранов. */
val viewModelModule = module {

    viewModel {
        AuthViewModel(get(), get())
    }

    viewModel {
        ChatsViewModel(get(), get())
    }

    viewModel {
        RootViewModel(get(), get())
    }
}

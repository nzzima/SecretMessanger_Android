package com.nzzima.secretmessanger.main.ui

import com.nzzima.secretmessanger.utils.constants.Constants

/** Назначения навигации. [route] — идентификатор в графе [AppNavHost]. */
enum class Destination(val route: String) {
    Loading(Constants.LOADING_ROUTE),
    Auth(Constants.AUTH_ROUTE),
    Identity(Constants.IDENTITY_ROUTE),
    Chats(Constants.CHATS_ROUTE),
}

package com.nzzima.secretmessanger.main.ui

import com.nzzima.secretmessanger.utils.constants.Constants

/** Назначения навигации. [route] — идентификатор в графе [AppNavHost]. */
enum class Destination(val route: String) {
    Auth(Constants.AUTH_ROUTE),
    Chats(Constants.CHATS_ROUTE),
}

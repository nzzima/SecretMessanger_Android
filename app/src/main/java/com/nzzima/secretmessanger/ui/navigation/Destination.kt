package com.nzzima.secretmessanger.ui.navigation

/** Назначения навигации. [route] — идентификатор в графе [AppNavHost]. */
enum class Destination(val route: String) {
    Auth("auth"),
    Chats("chats"),
}

package com.nzzima.secretmessanger.session.domain.models

/** Состояние сессии пользователя. */
sealed interface Session {

    /** Сессии нет: пользователь не вошёл. */
    data object Anonymous : Session

    /** Пользователь вошёл. [uid] — идентификатор аккаунта. */
    data class Authenticated(val uid: String) : Session

    /** Идентификатор аккаунта активной сессии или `null` для [Anonymous]. */
    val uidOrNull: String? get() = (this as? Authenticated)?.uid
}

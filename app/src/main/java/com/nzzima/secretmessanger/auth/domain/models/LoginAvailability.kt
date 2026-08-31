package com.nzzima.secretmessanger.auth.domain.models

/** Состояние логина в реестре. */
enum class LoginAvailability {
    /** Логин свободен. */
    FREE,

    /** Логин занят запрашивающим аккаунтом. */
    MINE,

    /** Логин занят другим аккаунтом. */
    TAKEN,
}

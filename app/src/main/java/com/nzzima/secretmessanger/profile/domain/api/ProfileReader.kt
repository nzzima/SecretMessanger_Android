package com.nzzima.secretmessanger.profile.domain.api

import com.nzzima.secretmessanger.profile.domain.models.Profile
import kotlinx.coroutines.flow.Flow

/**
 * Чтение профиля из `users/{uid}`.
 *
 * Отдельно от [com.nzzima.secretmessanger.auth.domain.api.ProfileRepository], который
 * профиль создаёт: создание — шаг регистрации, чтение — работа вкладки «Профиль».
 */
interface ProfileReader {

    /**
     * Профиль аккаунта [uid] — свежим значением на каждое изменение документа.
     *
     * Документа может не быть: регистрация, оборванная таймаутом между созданием аккаунта
     * и записью профиля, оставляет аккаунт без него. Такой случай приходит отказом.
     *
     * Отказ приходит последним значением, после чего поток закрывается.
     */
    fun observe(uid: String): Flow<Result<Profile>>
}

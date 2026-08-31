package com.nzzima.secretmessanger.auth.domain.api

/** Запись профиля `users/{uid}`. */
interface ProfileRepository {

    /**
     * Создаёт профиль аккаунта [uid].
     *
     * Запись проходит только тогда, когда [login] уже занят этим же аккаунтом в реестре
     * `logins`: правило Firestore на `users/{uid}` сверяет их между собой.
     */
    suspend fun createProfile(uid: String, login: String, name: String): Result<Unit>
}

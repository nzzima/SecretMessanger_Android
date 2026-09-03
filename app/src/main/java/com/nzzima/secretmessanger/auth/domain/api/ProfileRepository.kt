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

    /**
     * Есть ли профиль у аккаунта [uid].
     *
     * Отсутствие означает оборванную регистрацию: аккаунт в Firebase Auth создан, а логин и
     * профиль дописаны не были.
     */
    suspend fun exists(uid: String): Result<Boolean>
}

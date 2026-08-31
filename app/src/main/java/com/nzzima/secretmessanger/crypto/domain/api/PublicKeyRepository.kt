package com.nzzima.secretmessanger.crypto.domain.api

/**
 * Открытая половина постоянного ключа в профиле — `users/{uid}.publicKey`.
 *
 * Значение — base64 сырых 32 байт. Формат общий с iOS, менять его нельзя.
 */
interface PublicKeyRepository {

    /**
     * Открытая половина, опубликованная аккаунтом [uid], либо `null`, если её там нет.
     *
     * Пустая строка считается отсутствием: так же её читает iOS.
     */
    suspend fun published(uid: String): Result<String?>

    /**
     * Кладёт [publicKey] в профиль аккаунта [uid], затирая прежнее значение.
     *
     * Требует существующего профиля с занятым логином: правило Firestore на `users/{uid}`
     * сверяет логин записи с реестром `logins`, а слияние подставляет в проверку логин
     * уже лежащего документа.
     */
    suspend fun publish(uid: String, publicKey: String): Result<Unit>
}

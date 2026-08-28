package com.nzzima.secretmessanger.data.account

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await

/** Создание и удаление аккаунта. */
interface AccountRegistrar {

    /**
     * Создаёт аккаунт и открывает сессию. Возвращает идентификатор нового аккаунта.
     *
     * Проваливается с [AccountFailure.EmailTaken] или [AccountFailure.WeakPassword].
     */
    suspend fun register(email: String, password: String): Result<String>

    /** Удаляет аккаунт текущей сессии. */
    suspend fun deleteCurrent(): Result<Unit>
}

/** Вход в существующий аккаунт. */
interface AccountAuthenticator {

    /**
     * Открывает сессию. Возвращает идентификатор аккаунта.
     *
     * Проваливается с [AccountFailure.WrongCredentials].
     */
    suspend fun signIn(email: String, password: String): Result<String>
}

/** Отказы, различаемые по ответу сервиса авторизации. Текст показывается пользователю. */
sealed class AccountFailure(message: String) : Exception(message) {

    /** Почта уже занята другим аккаунтом. */
    data object EmailTaken : AccountFailure("Эта почта уже занята")

    /** Пароль отклонён сервисом как слишком простой. */
    data object WeakPassword : AccountFailure("Пароль слишком простой")

    /**
     * Почта или пароль не подошли.
     *
     * Несуществующий аккаунт и неверный пароль сведены в один отказ: раздельные сообщения
     * позволяют проверять, зарегистрирована ли почта.
     */
    data object WrongCredentials : AccountFailure("Неверная почта или пароль")
}

/**
 * [AccountRegistrar] и [AccountAuthenticator] поверх Firebase Authentication.
 *
 * Почта записывается без окружающих пробелов. Ответы сервиса переводятся в
 * [AccountFailure]; ошибки сети и прочие отказы возвращаются как есть.
 */
class FirebaseAccountRepository(private val auth: FirebaseAuth) : AccountRegistrar, AccountAuthenticator {

    override suspend fun register(email: String, password: String): Result<String> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        requireNotNull(result.user).uid
    }.recoverCatching { error ->
        throw when (error) {
            is FirebaseAuthUserCollisionException -> AccountFailure.EmailTaken
            is FirebaseAuthWeakPasswordException -> AccountFailure.WeakPassword
            else -> error
        }
    }

    override suspend fun signIn(email: String, password: String): Result<String> = runCatching {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        requireNotNull(result.user).uid
    }.recoverCatching { error ->
        throw when (error) {
            is FirebaseAuthInvalidUserException,
            is FirebaseAuthInvalidCredentialsException,
            -> AccountFailure.WrongCredentials
            else -> error
        }
    }

    override suspend fun deleteCurrent(): Result<Unit> = runCatching {
        auth.currentUser?.delete()?.await()
        Unit
    }
}

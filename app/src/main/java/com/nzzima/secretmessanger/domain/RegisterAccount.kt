package com.nzzima.secretmessanger.domain

import com.nzzima.secretmessanger.data.account.AccountRegistrar
import com.nzzima.secretmessanger.data.account.LoginAvailability
import com.nzzima.secretmessanger.data.account.LoginRegistry
import com.nzzima.secretmessanger.data.profile.ProfileWriter

/**
 * Регистрация: аккаунт, логин в реестре, профиль.
 *
 * Шаги идут строго в этом порядке. Логин занимается после создания аккаунта, потому что
 * запись в реестр разрешена только вошедшему, и до записи профиля, потому что правило на
 * `users/{uid}` сверяет логин профиля с реестром.
 *
 * Возвращает идентификатор нового аккаунта либо [Failure] при отказе.
 */
class RegisterAccount(
    private val registrar: AccountRegistrar,
    private val registry: LoginRegistry,
    private val profiles: ProfileWriter,
) {

    /** Причины отказа, показываемые пользователю. */
    sealed class Failure(message: String) : Exception(message) {

        /** Логин не соответствует [LoginRules]. */
        data object InvalidLogin : Failure("Логин: 3–20 символов, латиница, цифры и _")

        /** Логин занят другим аккаунтом. */
        data object LoginTaken : Failure("Логин уже занят — выберите другой")
    }

    suspend operator fun invoke(email: String, password: String, login: String): Result<String> {
        if (!LoginRules.isValid(login)) return Result.failure(Failure.InvalidLogin)

        registry.check(login, uid = null).onSuccess { availability ->
            if (availability == LoginAvailability.TAKEN) return Result.failure(Failure.LoginTaken)
        }

        val uid = registrar.register(email, password).getOrElse { return Result.failure(it) }

        registry.claim(login, uid).onFailure { error ->
            if (error is LoginRegistry.Taken) {
                registrar.deleteCurrent()
                return Result.failure(Failure.LoginTaken)
            }
            return Result.failure(error)
        }

        return profiles.createProfile(uid, login = login, name = login).map { uid }
    }
}

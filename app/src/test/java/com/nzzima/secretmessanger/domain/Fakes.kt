package com.nzzima.secretmessanger.domain

import com.nzzima.secretmessanger.data.account.AccountAuthenticator
import com.nzzima.secretmessanger.data.account.AccountFailure
import com.nzzima.secretmessanger.data.account.AccountRegistrar
import com.nzzima.secretmessanger.data.account.LoginAvailability
import com.nzzima.secretmessanger.data.account.LoginRegistry
import com.nzzima.secretmessanger.data.profile.ProfileWriter

/**
 * [AccountRegistrar] и [AccountAuthenticator] в памяти.
 *
 * [registerFails] задаёт отказ создания аккаунта, [knownCredentials] — пару «почта к
 * паролю», которую примет [signIn]; любая другая пара даёт
 * [AccountFailure.WrongCredentials].
 */
class FakeAccountRegistrar(
    private val uid: String = "uid-1",
    private val registerFails: Throwable? = null,
    private val knownCredentials: Pair<String, String>? = null,
) : AccountRegistrar, AccountAuthenticator {

    var deleted = false
        private set

    override suspend fun register(email: String, password: String): Result<String> =
        registerFails?.let { Result.failure(it) } ?: Result.success(uid)

    override suspend fun signIn(email: String, password: String): Result<String> =
        if (knownCredentials == Pair(email, password)) {
            Result.success(uid)
        } else {
            Result.failure(AccountFailure.WrongCredentials)
        }

    override suspend fun deleteCurrent(): Result<Unit> {
        deleted = true
        return Result.success(Unit)
    }
}

/**
 * [LoginRegistry] в памяти. [taken] задаёт занятые логины как «ключ реестра → uid»,
 * [claimFails] — отказ при попытке занять.
 */
class FakeLoginRegistry(
    private val taken: MutableMap<String, String> = mutableMapOf(),
    private val claimFails: Throwable? = null,
) : LoginRegistry {

    var claimed = mutableListOf<String>()
        private set

    override suspend fun check(login: String, uid: String?): Result<LoginAvailability> {
        val owner = taken[LoginRegistry.key(login)]
        return Result.success(
            when {
                owner == null -> LoginAvailability.FREE
                owner == uid -> LoginAvailability.MINE
                else -> LoginAvailability.TAKEN
            },
        )
    }

    override suspend fun claim(login: String, uid: String): Result<Unit> {
        claimFails?.let { return Result.failure(it) }
        taken[LoginRegistry.key(login)] = uid
        claimed += login
        return Result.success(Unit)
    }
}

/** [ProfileWriter] в памяти. Хранит последний записанный профиль. */
class FakeProfileWriter : ProfileWriter {

    var created: Triple<String, String, String>? = null
        private set

    override suspend fun createProfile(uid: String, login: String, name: String): Result<Unit> {
        created = Triple(uid, login, name)
        return Result.success(Unit)
    }
}

package com.nzzima.secretmessanger.auth.domain.impl

import com.nzzima.secretmessanger.auth.domain.FieldRules
import com.nzzima.secretmessanger.auth.domain.api.LoginRepository
import com.nzzima.secretmessanger.auth.domain.api.ProfileRepository
import com.nzzima.secretmessanger.auth.domain.api.RegistrationInteractor
import com.nzzima.secretmessanger.auth.domain.api.RegistrationRepository
import com.nzzima.secretmessanger.auth.domain.models.LoginAvailability
import com.nzzima.secretmessanger.auth.domain.models.LoginTaken
import com.nzzima.secretmessanger.auth.domain.models.RegistrationFailure

/**
 * Регистрация: аккаунт, логин в реестре, профиль.
 *
 * Шаги идут строго в этом порядке. Логин занимается после создания аккаунта, потому что
 * запись в реестр разрешена только вошедшему, и до записи профиля, потому что правило на
 * `users/{uid}` сверяет логин профиля с реестром.
 *
 * Проигранная гонка за логин откатывает созданный аккаунт; отказ связи его оставляет.
 */
class RegistrationInteractorImpl(
    private val registrationRepository: RegistrationRepository,
    private val loginRepository: LoginRepository,
    private val profileRepository: ProfileRepository,
) : RegistrationInteractor {

    override suspend fun register(email: String, password: String, login: String): Result<String> {
        if (!FieldRules.isValidLogin(login)) return Result.failure(RegistrationFailure.InvalidLogin)

        loginRepository.check(login, uid = null).onSuccess { availability ->
            if (availability == LoginAvailability.TAKEN) return Result.failure(RegistrationFailure.LoginTaken)
        }

        val uid = registrationRepository.register(email, password).getOrElse { return Result.failure(it) }

        loginRepository.claim(login, uid).onFailure { error ->
            if (error is LoginTaken) {
                registrationRepository.deleteCurrent()
                return Result.failure(RegistrationFailure.LoginTaken)
            }
            return Result.failure(error)
        }

        return profileRepository.createProfile(uid, login = login, name = login).map { uid }
    }
}

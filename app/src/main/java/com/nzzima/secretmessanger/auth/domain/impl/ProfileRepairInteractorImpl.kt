package com.nzzima.secretmessanger.auth.domain.impl

import com.nzzima.secretmessanger.auth.domain.FieldRules
import com.nzzima.secretmessanger.auth.domain.api.LoginRepository
import com.nzzima.secretmessanger.auth.domain.api.ProfileRepairInteractor
import com.nzzima.secretmessanger.auth.domain.api.ProfileRepository
import com.nzzima.secretmessanger.auth.domain.models.LoginAvailability
import com.nzzima.secretmessanger.auth.domain.models.LoginTaken
import com.nzzima.secretmessanger.auth.domain.models.RegistrationFailure

/**
 * Достройка регистрации: занять логин, если он ещё не наш, и записать профиль.
 *
 * Порядок тот же, что в [RegistrationInteractorImpl], и по той же причине: правило на
 * `users/{uid}` сверяет логин профиля с реестром.
 */
class ProfileRepairInteractorImpl(
    private val profileRepository: ProfileRepository,
    private val loginRepository: LoginRepository,
) : ProfileRepairInteractor {

    override suspend fun isComplete(uid: String): Result<Boolean> = profileRepository.exists(uid)

    override suspend fun complete(uid: String, login: String): Result<Unit> {
        if (!FieldRules.isValidLogin(login)) return Result.failure(RegistrationFailure.InvalidLogin)

        val availability = loginRepository.check(login, uid).getOrElse { return Result.failure(it) }

        when (availability) {
            LoginAvailability.TAKEN -> return Result.failure(RegistrationFailure.LoginTaken)

            LoginAvailability.FREE -> loginRepository.claim(login, uid).onFailure { error ->
                return Result.failure(if (error is LoginTaken) RegistrationFailure.LoginTaken else error)
            }

            // Логин уже наш: обрыв случился между записью в реестр и записью профиля.
            LoginAvailability.MINE -> Unit
        }

        return profileRepository.createProfile(uid, login = login, name = login)
    }
}

package com.nzzima.secretmessanger.profile.domain.impl

import com.nzzima.secretmessanger.profile.domain.api.ProfileInteractor
import com.nzzima.secretmessanger.profile.domain.api.ProfileReader
import com.nzzima.secretmessanger.profile.domain.models.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Подстановка имени вместо пустого логина.
 *
 * Правило показа, а не свойство документа: в базе логин и имя — разные поля, и пустой
 * логин там законен.
 */
class ProfileInteractorImpl(private val profiles: ProfileReader) : ProfileInteractor {

    override fun observeProfile(uid: String): Flow<Result<Profile>> =
        profiles.observe(uid).map { snapshot ->
            snapshot.map { profile ->
                if (profile.login.isEmpty()) profile.copy(login = profile.name) else profile
            }
        }
}

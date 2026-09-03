package com.nzzima.secretmessanger.profile.domain.api

import com.nzzima.secretmessanger.profile.domain.models.Profile
import kotlinx.coroutines.flow.Flow

/** Свой профиль для одноимённой вкладки. */
interface ProfileInteractor {

    /** Профиль аккаунта [uid]; условия отказа — как у [ProfileReader.observe]. */
    fun observeProfile(uid: String): Flow<Result<Profile>>
}

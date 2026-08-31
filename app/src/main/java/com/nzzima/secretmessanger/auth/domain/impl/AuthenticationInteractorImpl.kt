package com.nzzima.secretmessanger.auth.domain.impl

import com.nzzima.secretmessanger.auth.domain.api.AuthenticationInteractor
import com.nzzima.secretmessanger.auth.domain.api.AuthenticationRepository

/** [AuthenticationInteractor] поверх [AuthenticationRepository]. */
class AuthenticationInteractorImpl(
    private val authenticationRepository: AuthenticationRepository,
) : AuthenticationInteractor {

    override suspend fun signIn(email: String, password: String): Result<String> =
        authenticationRepository.signIn(email, password)
}

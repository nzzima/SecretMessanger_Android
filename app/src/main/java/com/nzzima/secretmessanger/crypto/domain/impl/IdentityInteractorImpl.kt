package com.nzzima.secretmessanger.crypto.domain.impl

import com.nzzima.secretmessanger.crypto.domain.CryptoBox
import com.nzzima.secretmessanger.crypto.domain.api.IdentityInteractor
import com.nzzima.secretmessanger.crypto.domain.api.IdentityKeyStore
import com.nzzima.secretmessanger.crypto.domain.api.PublicKeyRepository
import com.nzzima.secretmessanger.crypto.domain.models.IdentityState
import java.util.Base64

/**
 * Развилка ключа при входе, пять исходов сведены к двум.
 *
 * | Ключ на устройстве | В профиле | Что делает |
 * |---|---|---|
 * | есть | пусто | публикует молча |
 * | есть | наш же | ничего |
 * | есть | чужая половина | спрашивает |
 * | нет | пусто | заводит и публикует молча |
 * | нет | чужая половина | спрашивает |
 *
 * Половина работы — ничего не делать: [prepare] не заводит и не публикует ключ ни в одном
 * случае, когда в профиле лежит чужая половина.
 */
class IdentityInteractorImpl(
    private val keys: IdentityKeyStore,
    private val publicKeys: PublicKeyRepository,
) : IdentityInteractor {

    override suspend fun prepare(uid: String): Result<IdentityState> {
        val published = publicKeys.published(uid).getOrElse { return Result.failure(it) }
        val local = keys.existing(uid)

        if (published != null && published != local?.let(::encodePublicKey)) {
            return Result.success(IdentityState.NeedsConfirmation)
        }

        if (published != null) return Result.success(IdentityState.Ready)

        val identityPrivate = local ?: keys.createNew(uid)
        return publicKeys.publish(uid, encodePublicKey(identityPrivate))
            .map { IdentityState.Ready }
    }

    override suspend fun publishOverwriting(uid: String): Result<Unit> {
        val identityPrivate = keys.existing(uid) ?: keys.createNew(uid)
        return publicKeys.publish(uid, encodePublicKey(identityPrivate))
    }

    private fun encodePublicKey(identityPrivate: ByteArray): String =
        Base64.getEncoder().encodeToString(CryptoBox.publicKey(identityPrivate))
}

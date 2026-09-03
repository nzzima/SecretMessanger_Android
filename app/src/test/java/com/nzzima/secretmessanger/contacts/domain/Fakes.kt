package com.nzzima.secretmessanger.contacts.domain

import com.nzzima.secretmessanger.contacts.domain.api.ContactsRepository
import com.nzzima.secretmessanger.contacts.domain.models.Contact
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart

/**
 * Профили в памяти.
 *
 * Начального значения нет: до первого [send] подписчик не получает ничего — так же ведёт
 * себя Firestore, пока не пришёл первый снимок.
 */
class FakeContactsRepository : ContactsRepository {

    private val snapshots = MutableSharedFlow<Result<List<Contact>>>(
        replay = 1,
        extraBufferCapacity = BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** Сколько раз на поток подписывались. */
    var subscriptions = 0
        private set

    override fun observeAll(): Flow<Result<List<Contact>>> = snapshots.onStart { subscriptions++ }

    /** Отдаёт подписчикам очередной снимок. */
    fun send(contacts: List<Contact>) = snapshots.tryEmit(Result.success(contacts))

    /** Отдаёт подписчикам отказ. */
    fun fail(error: Throwable) = snapshots.tryEmit(Result.failure(error))

    private companion object {
        const val BUFFER = 8
    }
}

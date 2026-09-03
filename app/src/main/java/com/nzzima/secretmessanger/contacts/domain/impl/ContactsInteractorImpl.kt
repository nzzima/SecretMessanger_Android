package com.nzzima.secretmessanger.contacts.domain.impl

import com.nzzima.secretmessanger.contacts.domain.api.ContactsInteractor
import com.nzzima.secretmessanger.contacts.domain.api.ContactsRepository
import com.nzzima.secretmessanger.contacts.domain.models.Contact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Отсев себя и безымянных, сортировка по алфавиту. */
class ContactsInteractorImpl(private val contacts: ContactsRepository) : ContactsInteractor {

    override fun observeContacts(selfId: String): Flow<Result<List<Contact>>> =
        contacts.observeAll().map { snapshot ->
            snapshot.map { all ->
                all.filter { it.id != selfId && it.login.isNotEmpty() }
                    .sortedBy { it.login.lowercase() }
            }
        }
}

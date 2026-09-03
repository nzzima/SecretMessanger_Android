package com.nzzima.secretmessanger.contacts.domain

import com.nzzima.secretmessanger.contacts.domain.impl.ContactsInteractorImpl
import com.nzzima.secretmessanger.contacts.domain.models.Contact
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/** Кто попадает в список контактов и в каком порядке. */
class ContactsInteractorTest {

    private val contacts = FakeContactsRepository()
    private val interactor = ContactsInteractorImpl(contacts)

    private suspend fun logins(): List<String> =
        interactor.observeContacts("uid-1").first().getOrThrow().map { it.login }

    @Test
    fun `себя в списке нет`() = runTest {
        contacts.send(
            listOf(Contact("uid-1", "сам"), Contact("uid-2", "другой")),
        )

        assertEquals(listOf("другой"), logins())
    }

    @Test
    fun `профиль без логина и имени отбрасывается`() = runTest {
        contacts.send(
            listOf(Contact("uid-2", ""), Contact("uid-3", "живой")),
        )

        assertEquals(listOf("живой"), logins())
    }

    @Test
    fun `по алфавиту, регистр не влияет`() = runTest {
        contacts.send(
            listOf(Contact("uid-2", "Яна"), Contact("uid-3", "борис"), Contact("uid-4", "Анна")),
        )

        assertEquals(listOf("Анна", "борис", "Яна"), logins())
    }

    @Test
    fun `отказ подписки доходит до вызывающего`() = runTest {
        val error = IllegalStateException("нет доступа")
        contacts.fail(error)

        assertSame(error, interactor.observeContacts("uid-1").first().exceptionOrNull())
    }
}

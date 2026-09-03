package com.nzzima.secretmessanger.contacts.domain.api

import com.nzzima.secretmessanger.contacts.domain.models.Contact
import kotlinx.coroutines.flow.Flow

/** Список контактов для экрана «Контакты». */
interface ContactsInteractor {

    /**
     * Все зарегистрированные, кроме [selfId], по алфавиту.
     *
     * Себя в списке нет: писать самому себе приложение не умеет, и строка вела бы в никуда.
     * Профиль без логина и имени отбрасывается — показать его нечем.
     */
    fun observeContacts(selfId: String): Flow<Result<List<Contact>>>
}

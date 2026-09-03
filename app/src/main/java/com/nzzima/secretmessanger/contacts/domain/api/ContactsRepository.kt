package com.nzzima.secretmessanger.contacts.domain.api

import com.nzzima.secretmessanger.contacts.domain.models.Contact
import kotlinx.coroutines.flow.Flow

/** Профили из коллекции `users`. */
interface ContactsRepository {

    /**
     * Все зарегистрированные — целым снимком на каждое изменение любого профиля.
     *
     * Слушатель стоит на всей коллекции: на десятках пользователей это незаметно, на
     * сотнях понадобится постраничная загрузка. Себя из списка не убирает — это дело
     * вызывающего.
     *
     * Отказ приходит последним значением, после чего поток закрывается: слушатель
     * Firestore после ошибки снимается сам.
     */
    fun observeAll(): Flow<Result<List<Contact>>>
}

package com.nzzima.secretmessanger.contacts.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nzzima.secretmessanger.contacts.domain.api.ContactsInteractor
import com.nzzima.secretmessanger.session.domain.api.SessionInteractor
import com.nzzima.secretmessanger.utils.constants.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Состояние экрана контактов.
 *
 * Идентификатор аккаунта берётся из сессии в момент подписки: вкладка достижима только при
 * живой сессии, а выход уводит с неё целиком.
 */
class ContactsViewModel(
    private val sessionInteractor: SessionInteractor,
    private val contactsInteractor: ContactsInteractor,
) : ViewModel() {

    private val contactsScreenState = MutableStateFlow<ContactsUiState>(ContactsUiState.Loading)
    private var subscription: Job? = null

    /** Текущее состояние экрана. */
    fun observeContactsScreenState(): StateFlow<ContactsUiState> = contactsScreenState.asStateFlow()

    init {
        subscribe()
    }

    /** Подписывается на список заново — нужна после отказа. */
    fun retry() = subscribe()

    private fun subscribe() {
        val uid = sessionInteractor.observeSession().value.uidOrNull ?: return

        subscription?.cancel()
        contactsScreenState.value = ContactsUiState.Loading

        subscription = viewModelScope.launch {
            contactsInteractor.observeContacts(uid).collect { snapshot ->
                contactsScreenState.value = snapshot.fold(
                    onSuccess = { if (it.isEmpty()) ContactsUiState.Empty else ContactsUiState.Content(it) },
                    onFailure = { ContactsUiState.Failed(it.message ?: Constants.SERVER_SILENT) },
                )
            }
        }
    }
}

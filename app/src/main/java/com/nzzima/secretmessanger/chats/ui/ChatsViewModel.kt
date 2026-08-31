package com.nzzima.secretmessanger.chats.ui

import androidx.lifecycle.ViewModel
import com.nzzima.secretmessanger.session.domain.api.SessionInteractor

/**
 * Состояние экрана списка диалогов.
 *
 * Источник диалогов не подключён: список пуст всегда.
 */
class ChatsViewModel(private val sessionInteractor: SessionInteractor) : ViewModel() {

    /** Завершает сессию. Локальные данные аккаунта не затрагивает. */
    fun signOut() = sessionInteractor.signOut()
}

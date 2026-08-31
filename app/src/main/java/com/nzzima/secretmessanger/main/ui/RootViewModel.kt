package com.nzzima.secretmessanger.main.ui

import androidx.lifecycle.ViewModel
import com.nzzima.secretmessanger.session.domain.api.SessionInteractor
import com.nzzima.secretmessanger.session.domain.models.Session
import kotlinx.coroutines.flow.StateFlow

/** Состояние оболочки приложения: сессия, по которой навигация выбирает назначение. */
class RootViewModel(sessionInteractor: SessionInteractor) : ViewModel() {

    private val session = sessionInteractor.observeSession()

    /** Текущее состояние сессии; обновляется при каждом входе и выходе. */
    fun observeSession(): StateFlow<Session> = session
}

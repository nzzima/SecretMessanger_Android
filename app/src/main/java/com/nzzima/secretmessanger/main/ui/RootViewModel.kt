package com.nzzima.secretmessanger.main.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nzzima.secretmessanger.crypto.domain.api.IdentityInteractor
import com.nzzima.secretmessanger.crypto.domain.models.IdentityState
import com.nzzima.secretmessanger.session.domain.api.SessionInteractor
import com.nzzima.secretmessanger.session.domain.models.Session
import com.nzzima.secretmessanger.utils.constants.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Состояние оболочки приложения.
 *
 * Развилка ключа проверяется здесь, а не в обработчике входа: сессия Firebase живёт
 * месяцами, и человек с давней сессией проверку бы не проходил вовсе.
 *
 * [RootState.Ready] выставляется только после успешной проверки, поэтому мимо развилки в
 * список диалогов не попасть.
 */
class RootViewModel(
    private val sessionInteractor: SessionInteractor,
    private val identityInteractor: IdentityInteractor,
) : ViewModel() {

    private val rootState = MutableStateFlow<RootState>(RootState.Checking)

    /** Текущее состояние оболочки. */
    fun observeRootState(): StateFlow<RootState> = rootState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionInteractor.observeSession().collect { session ->
                when (session) {
                    is Session.Anonymous -> rootState.value = RootState.Anonymous
                    is Session.Authenticated -> prepare(session.uid)
                }
            }
        }
    }

    /** Повторяет проверку после отказа связи. */
    fun retry() {
        val uid = uidOrNull() ?: return
        viewModelScope.launch { prepare(uid) }
    }

    /**
     * Публикует свою открытую половину поверх чужой.
     *
     * Вызывается только с экрана развилки, то есть после осознанного подтверждения.
     */
    fun confirmOverwrite() {
        val uid = uidOrNull() ?: return
        rootState.value = RootState.Checking
        viewModelScope.launch {
            identityInteractor.publishOverwriting(uid)
                .onSuccess { rootState.value = RootState.Ready }
                .onFailure { rootState.value = RootState.Failed(it.message ?: Constants.SERVER_SILENT) }
        }
    }

    /** Завершает сессию. Ключ на устройстве не стирается. */
    fun signOut() = sessionInteractor.signOut()

    private suspend fun prepare(uid: String) {
        rootState.value = RootState.Checking
        identityInteractor.prepare(uid)
            .onSuccess {
                rootState.value = when (it) {
                    IdentityState.Ready -> RootState.Ready
                    IdentityState.NeedsConfirmation -> RootState.NeedsConfirmation
                }
            }
            .onFailure { rootState.value = RootState.Failed(it.message ?: Constants.SERVER_SILENT) }
    }

    private fun uidOrNull() = (sessionInteractor.observeSession().value as? Session.Authenticated)?.uid
}

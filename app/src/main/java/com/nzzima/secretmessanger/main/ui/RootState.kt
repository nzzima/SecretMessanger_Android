package com.nzzima.secretmessanger.main.ui

/** Состояние оболочки: по нему навигация выбирает назначение. */
sealed interface RootState {

    /** Сессии нет. */
    data object Anonymous : RootState

    /** Сессия есть, ключ проверяется. Назначение не меняется, пока проверка идёт. */
    data object Checking : RootState

    /** В профиле чужая открытая половина: нужен осознанный выбор. */
    data object NeedsConfirmation : RootState

    /** Профиль не прочитан или публикация не прошла. [message] показывается на экране. */
    data class Failed(val message: String) : RootState

    /** Ключ на месте и опубликован. */
    data object Ready : RootState
}

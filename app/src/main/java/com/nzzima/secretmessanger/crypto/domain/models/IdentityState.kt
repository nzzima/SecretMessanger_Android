package com.nzzima.secretmessanger.crypto.domain.models

/** Итог проверки постоянного ключа при входе. */
sealed interface IdentityState {

    /** Ключ на месте и опубликован. Путь дальше открыт. */
    data object Ready : IdentityState

    /**
     * В профиле лежит чужая открытая половина.
     *
     * Публиковать свою поверх можно только по осознанному подтверждению: прежняя
     * переписка станет нечитаемой.
     */
    data object NeedsConfirmation : IdentityState
}

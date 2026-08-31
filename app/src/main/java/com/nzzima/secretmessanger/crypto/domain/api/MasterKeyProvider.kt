package com.nzzima.secretmessanger.crypto.domain.api

import javax.crypto.SecretKey

/**
 * Мастер-ключ AES-256, которым закрыта приватная половина постоянного ключа.
 *
 * Реализация обязана возвращать один и тот же ключ на протяжении жизни установки:
 * потеря мастер-ключа делает сохранённые половины нечитаемыми навсегда.
 */
interface MasterKeyProvider {

    /** Мастер-ключ. Создаётся при первом обращении. */
    fun masterKey(): SecretKey
}

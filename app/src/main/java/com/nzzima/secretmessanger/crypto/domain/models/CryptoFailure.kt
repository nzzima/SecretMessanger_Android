package com.nzzima.secretmessanger.crypto.domain.models

import com.nzzima.secretmessanger.utils.constants.Constants

/** Отказы крипто-слоя. Текст показывается пользователю. */
sealed class CryptoFailure(message: String) : Exception(message) {

    /** Запись не разбирается: не тот формат, не base64, обрезанные байты. */
    data object MalformedPayload : CryptoFailure(Constants.MALFORMED_PAYLOAD)

    /** Ключ не подошёл: тег AES-GCM не сошёлся либо контекст HKDF другой. */
    data object WrongKey : CryptoFailure(Constants.WRONG_KEY)
}

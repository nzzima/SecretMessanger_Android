package com.nzzima.secretmessanger.domain

/** Требования к логину. */
object LoginRules {

    private val ALLOWED = Regex("^[A-Za-z0-9_]{3,20}$")

    /** Истинно для строки из 3–20 символов латиницы, цифр и подчёркивания. */
    fun isValid(login: String): Boolean = ALLOWED.matches(login)
}

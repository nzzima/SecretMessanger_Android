package com.nzzima.secretmessanger.auth.domain

import com.nzzima.secretmessanger.utils.constants.Constants

/** Требования к полям формы. Выражения совпадают с `FieldValidator` на iOS. */
object FieldRules {

    private val EMAIL = Regex("[a-z0-9A-Z._%+-]+@[A-Z0-9a-z.-]+\\.[A-Za-z]{2,64}")
    private val LOGIN = Regex("^[A-Za-z0-9_]{${Constants.LOGIN_MIN_LENGTH},${Constants.LOGIN_MAX_LENGTH}}$")

    /** Истинно для строки вида `имя@домен.зона`. */
    fun isValidEmail(email: String): Boolean = EMAIL.matches(email.trim())

    /** Истинно для строки из 3–20 символов латиницы, цифр и подчёркивания. */
    fun isValidLogin(login: String): Boolean = LOGIN.matches(login)

    /** Истинно для строки не короче шести символов. */
    fun isValidPassword(password: String): Boolean = password.length >= Constants.PASSWORD_MIN_LENGTH
}

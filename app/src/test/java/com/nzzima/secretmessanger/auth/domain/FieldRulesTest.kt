package com.nzzima.secretmessanger.auth.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldRulesTest {

    @Test
    fun `допустимы латиница цифры и подчёркивание длиной 3-20`() {
        for (good in listOf("abc", "nzzima", "a_1", "A".repeat(20))) {
            assertTrue(good, FieldRules.isValidLogin(good))
        }
    }

    @Test
    fun `отклоняются короткие длинные и с посторонними символами`() {
        for (bad in listOf("ab", "a".repeat(21), "с-кириллицей", "с пробелом", "точка.точка", "")) {
            assertFalse(bad, FieldRules.isValidLogin(bad))
        }
    }

    @Test
    fun `почта принимается в обычном виде и отклоняется без домена`() {
        for (good in listOf("a@b.co", "nikita.krylov+2012@gmail.com", "A_1@sub.domain.org")) {
            assertTrue(good, FieldRules.isValidEmail(good))
        }
        for (bad in listOf("", "не-почта", "a@b", "a@b.c", "@b.co", "a b@c.co")) {
            assertFalse(bad, FieldRules.isValidEmail(bad))
        }
    }

    @Test
    fun `пароль от шести символов`() {
        assertTrue(FieldRules.isValidPassword("123456"))
        assertFalse(FieldRules.isValidPassword("12345"))
    }
}

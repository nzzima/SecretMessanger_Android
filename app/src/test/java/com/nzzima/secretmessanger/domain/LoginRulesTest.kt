package com.nzzima.secretmessanger.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginRulesTest {

    @Test
    fun `допустимы латиница цифры и подчёркивание длиной 3-20`() {
        for (good in listOf("abc", "nzzima", "a_1", "A".repeat(20))) {
            assertTrue(good, LoginRules.isValid(good))
        }
    }

    @Test
    fun `отклоняются короткие длинные и с посторонними символами`() {
        for (bad in listOf("ab", "a".repeat(21), "с-кириллицей", "с пробелом", "точка.точка", "")) {
            assertFalse(bad, LoginRules.isValid(bad))
        }
    }
}

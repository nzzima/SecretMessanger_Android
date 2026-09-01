package com.nzzima.secretmessanger.chats.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Название, признак группы и собеседник выводятся из состава, а не из полей базы. */
class ChatTest {

    @Test
    fun `название диалога на двоих — логин собеседника`() {
        assertEquals("companion", chat().title)
    }

    @Test
    fun `название группы — логины всех, кроме себя`() {
        val group = chat(
            members = listOf("uid-1", "uid-2", "uid-3"),
            logins = mapOf("uid-1" to "self", "uid-2" to "first", "uid-3" to "second"),
        )

        assertEquals("first, second", group.title)
        assertTrue(group.isGroup)
    }

    @Test
    fun `участник без логина в кэше выпадает из названия`() {
        val group = chat(
            members = listOf("uid-1", "uid-2", "uid-3"),
            logins = mapOf("uid-1" to "self", "uid-2" to "first"),
        )

        assertEquals("first", group.title)
    }

    @Test
    fun `у диалога на двоих есть собеседник, у группы — нет`() {
        assertEquals("uid-2", chat().companionId)
        assertFalse(chat().isGroup)

        assertNull(chat(members = listOf("uid-1", "uid-2", "uid-3")).companionId)
    }
}

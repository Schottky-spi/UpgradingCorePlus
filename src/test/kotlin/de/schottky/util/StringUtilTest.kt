package de.schottky.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringUtilTest {

    @Test fun `An empty string stays the same when capitalized`() {
        assertEquals("", StringUtil.capitalize(""))
        assertEquals("  ", StringUtil.capitalize("  "))
    }

    @Test fun `A single character gets capitalized correctly`() {
        assertEquals("A", StringUtil.capitalize("a"))
        assertEquals("Z", StringUtil.capitalize("z"))
    }

    @Test fun `The first character of a single word is correctly capitalized`() {
        assertEquals("Hello", StringUtil.capitalize("hello"))
    }

    @Test fun `The capitalize function retains whitespaces in multiple words`() {
        assertEquals("  Hello, World  ", StringUtil.capitalize("  hello, world  "))
        assertEquals("Hello   World", StringUtil.capitalize("hello   world"))
    }

    @Test fun `The capitalize function retains special characters in a string`() {
        assertEquals("Hello, World", StringUtil.capitalize("hello, world"))
        assertEquals("Hello_world", StringUtil.capitalize("hello_world"))
    }

    @Test fun `Two words in caps correctly get capitalized`() {
        assertEquals("Hello, World", StringUtil.capitalize("HELLO, WORLD"))
    }
}
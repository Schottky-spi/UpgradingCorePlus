package de.schottky.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest {

    @Test
    void test_capitalization() {
        assertEquals("Hello, World", StringUtil.capitalize("hello, world"));
        assertEquals("  Hello, World  ", StringUtil.capitalize("  hello, world  "));
        assertEquals("", StringUtil.capitalize(""));
        assertEquals("  ", StringUtil.capitalize("  "));
        assertEquals("Hello", StringUtil.capitalize("hello"));
        assertEquals("Hello_world", StringUtil.capitalize("hello_world"));
        assertEquals("Hello   World", StringUtil.capitalize("hello   world"));
        assertEquals("A", StringUtil.capitalize("a"));
        assertEquals("Hello, World", StringUtil.capitalize("HELLO, WORLD"));
    }
}
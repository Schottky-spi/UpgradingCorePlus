package de.schottky.util

import assertEmpty
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test

class MapUtilTests {

    @Test
    fun `A sorted empty map stays empty`() {
        val map = MapUtil.sortedValues(emptyMap<Any, Double>(), Double::compareTo)
        assertEmpty(map)
    }

    @Test
    fun `A list with integer values can be sorted in ascending order`() {
        val map = mapOf("A" to 0, "B" to 10, "C" to -3, "D" to 4, "E" to 5)
        val ascending = MapUtil.sortedValues(map) { i1, i2 -> Integer.compare(i1, i2) }
        val descending = MapUtil.sortedValues(map) { i1, i2 -> -Integer.compare(i1, i2) }
        assertIterableEquals(map.values.sorted(), ascending.values)
        assertIterableEquals(map.values.sortedDescending(), descending.values)
    }
}
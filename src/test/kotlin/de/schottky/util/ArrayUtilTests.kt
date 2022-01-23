package de.schottky.util

import org.junit.jupiter.api.Test
import java.util.function.Function
import kotlin.test.assertEquals

class ArrayUtilTests {

    @Test fun `A list stays the same when the mapper function is the identity function`() {
        val testList = listOf("A", "B", "C")
        val retList = ArrayUtil.map(testList, Function.identity())
        assertEquals(testList, retList)
    }

    @Test fun `A list can be mapped from string to int`() {
        val testList = listOf("1", "2", "3")
        val retList = ArrayUtil.map(testList) { it.toInt() }
        assertEquals(listOf(1, 2, 3), retList)
    }

    @Test fun `A set stays the same when the mapper function is the identity function`() {
        val testSet = setOf("A", "B", "C")
        val retSet = ArrayUtil.map(testSet, Function.identity())
        assertEquals(testSet, retSet)
    }

    @Test fun `A set can be mapped from string to int`() {
        val testSet = setOf("1", "2", "3")
        val retSet = ArrayUtil.map(testSet) { it.toInt() }
        assertEquals(setOf(1, 2, 3), retSet)
    }


}
package de.schottky.expression

import de.schottky.expression.Modifier.Calculated
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ModifierTests {

    private fun testModifier(modifier: Modifier, expectedPerLevel: DoubleArray, startPrevious: Double) {
        var previous = startPrevious
        for (level in expectedPerLevel.indices) {
            val next = modifier.next(previous, level)
            Assertions.assertEquals(expectedPerLevel[level], next, 1e-8)
            previous = next
        }
    }

    @ParameterizedTest
    @MethodSource("simpleModifiers")
    fun `A simple modifier increases linearly with the level`(rawModifer: String, perLevel: DoubleArray) {
        val modifier = Modifier.parse(rawModifer)
        Assertions.assertEquals(Modifier.Simple::class.java, modifier.javaClass)
        testModifier(modifier, perLevel, 0.0)
    }

    @ParameterizedTest
    @MethodSource("enumerationModifiers")
    fun `An enumeration modifier retains it's value when parsed`(rawModifier: String, perLevel: DoubleArray) {
        val modifier = Modifier.parse(rawModifier)
        Assertions.assertEquals(Modifier.Enumeration::class.java, modifier.javaClass)
        testModifier(modifier, perLevel, 0.0)
    }

    @ParameterizedTest
    @MethodSource("calculatedModifiers")
    fun `A calculated modifier correctly produces the pre-computed value`(
        rawModifier: String,
        perLevel: DoubleArray,
        startPrevious: Double
    ) {
        val modifier = Modifier.parse(rawModifier)
        Assertions.assertEquals(Calculated::class.java, modifier.javaClass)
        testModifier(modifier, perLevel, startPrevious)
    }

    companion object {

        @JvmStatic
        fun simpleModifiers(): Stream<out Arguments> = Stream.of(
            Arguments.of("0", doubleArrayOf(0.0, 0.0, 0.0)),
            Arguments.of("1", doubleArrayOf(1.0, 2.0, 3.0)),
            Arguments.of("3", doubleArrayOf(3.0, 6.0, 9.0)),
            Arguments.of("1 + 1", doubleArrayOf(2.0, 4.0, 6.0)),  // can be folded into a single argument
            Arguments.of("(2 + 0) * 1 -3+2", doubleArrayOf(1.0, 2.0, 3.0))
        )

        @JvmStatic
        fun enumerationModifiers(): Stream<out Arguments> = Stream.of(
            Arguments.of("[0 0 0]", doubleArrayOf(0.0, 0.0, 0.0)),
            Arguments.of("[1 2 3]", doubleArrayOf(1.0, 2.0, 3.0)),
            Arguments.of("[1.2 1.8 2.0", doubleArrayOf(1.2, 1.8, 2.0))
        )

        @JvmStatic
        fun calculatedModifiers(): Stream<out Arguments> = Stream.of(
            Arguments.of("2*lvl", doubleArrayOf(0.0, 2.0, 4.0), 0),
            Arguments.of("prev + 1", doubleArrayOf(1.0, 2.0, 3.0), 0),
            Arguments.of("prev + lvl", doubleArrayOf(0.0, 1.0, 3.0), 0),
            Arguments.of("lvl + 1", doubleArrayOf(1.0, 2.0, 3.0), 0),
            Arguments.of("(lvl + 1) * 2", doubleArrayOf(2.0, 4.0, 6.0), 0),
            Arguments.of("(lvl + 1) * 1.5", doubleArrayOf(1.5, 3.0, 4.5), 0),
            Arguments.of("prev * 1.1", doubleArrayOf(1.1, 1.21, 1.331), 1)
        )
    }
}
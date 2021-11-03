package de.schottky.expression

import assertThrows
import de.schottky.expression.Expression.Node
import de.schottky.expression.Expression.Node.*
import de.schottky.expression.Expression.Node.Operation.Type
import de.schottky.expression.Expression.Node.Variable.Kind
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*
import java.util.stream.Stream

class ExpressionParserTests {

    @ParameterizedTest
    @ValueSource(strings=["", "  ", "\t", " \n\t"])
    fun `The expression parser throws an error when empty`(str: String) {
        assertThrows<ExpressionParseException> {
            ExpressionParser(str).parse()
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [ "[]", "[  ]", "[ \n]", "[", "[  " ])
    fun `The parser correctly parses an empty enumeration`(toParse: String) {
        val parser = ExpressionParser(toParse)
        val modifier = parser.parse()
        assertEquals(modifier::class.java, Modifier.Enumeration::class.java)
        assertArrayEquals((modifier as Modifier.Enumeration).enumeration(), DoubleArray(0))
    }

    @ParameterizedTest
    @MethodSource("arrayWithContentsArgument")
    fun `The parser correctly parses an array with contents`(toParse: String, contents: DoubleArray) {
        val parser = ExpressionParser(toParse)
        val modifier = parser.parse()
        assertEquals(modifier.javaClass, Modifier.Enumeration::class.java)
        assertArrayEquals((modifier as Modifier.Enumeration).enumeration(), contents)
    }

    @ParameterizedTest
    @ValueSource(strings = ["[[1]", "[1, 2]", "[xy]", "[a+b]", "[f" ])
    fun `The parser throws an error on any illegal array combination`(stringToParse: String) {
        val parser = ExpressionParser(stringToParse)
        assertThrows<ExpressionParseException> { parser.parse() }
    }

    @ParameterizedTest
    @ValueSource(strings = [ "[1.1. ]", "[ 1.ee ]", "[ 1,5 ]" ])
    fun `The parser throws when there is an illegal number in an enumeration`(stringToParse: String) {
        val parser = ExpressionParser(stringToParse)
        assertThrows<ExpressionParseException> { parser.parse() }
    }

    @ParameterizedTest
    @MethodSource(value=["factorArguments", "termArguments", "simpleExpressionArguments"])
    fun `The parser correctly parses several expressions`(stringToParse: String, expected: Node) {
        val modifier = ExpressionParser(stringToParse).parse()
        if (expected is Literal) {
            assertEquals(Modifier.Simple::class.java, modifier.javaClass)
            assertEquals(expected.value, (modifier as Modifier.Simple).amountPerLevel)
        } else {
            assertEquals(Modifier.Calculated::class.java, modifier.javaClass)
            val root = (modifier as Modifier.Calculated).expression().root()
            assertEquals(expected, root)
        }
    }

    @ParameterizedTest
    @MethodSource("complexExpressionArguments")
    fun `The expression parser computes the correct result for complex expressions`(
        stringToParse: String,
        previous: Double,
        level: Int,
        expected: Double)
    {
        val expression = ExpressionParser(stringToParse).parse()
        val result = expression.next(previous, level)
        assertEquals(expected, result)
    }

    companion object {

        @JvmStatic
        fun arrayWithContentsArgument(): Stream<out Arguments> = Stream.of(
            Arguments.of("[1]", doubleArrayOf(1.0)),
            Arguments.of("[1 2]", doubleArrayOf(1.0, 2.0 )),
            Arguments.of("[  1.5]", doubleArrayOf(1.5 )),
            Arguments.of("[  1 5", doubleArrayOf(1.0, 5.0)),
            Arguments.of("[1  5]", doubleArrayOf(1.0, 5.0 )),
            Arguments.of("[1.0 .1]", doubleArrayOf(1.0, 0.1))
        )

        @JvmStatic
        fun factorArguments(): Stream<out Arguments> = Stream.of(
            Arguments.of("1", Literal(1.0)),
            Arguments.of("1.2", Literal(1.2)),
            Arguments.of("lvl", Variable(Kind.LEVEL)),
            Arguments.of("lEvEl", Variable(Kind.LEVEL)),
            Arguments.of("prev", Variable(Kind.PREVIOUS)),
            Arguments.of("pReViOUs", Variable(Kind.PREVIOUS))
        )

        @JvmStatic
        fun termArguments(): Stream<out Arguments> = Stream.of(
            Arguments.of("2 * lvl", Operation(
                Type.MULTIPLICATION,
                Literal(2.0),
                Variable(Kind.LEVEL)
            )),
            Arguments.of("prev%lvl", Operation(
                Type.MODULUS,
                Variable(Kind.PREVIOUS),
                Variable(Kind.LEVEL)
            ))
        )

        @JvmStatic
        fun simpleExpressionArguments(): Stream<out Arguments> = Stream.of(
            Arguments.of("2 + lvl", Operation(
                Type.ADDITION,
                Literal(2.0),
                Variable(Kind.LEVEL)
            )),
            Arguments.of("2 + lvl * 3", Operation(
                Type.ADDITION,
                Literal(2.0),
                Operation(
                    Type.MULTIPLICATION,
                    Variable(Kind.LEVEL),
                    Literal(3.0)
                )
            )),
            Arguments.of("-2", Operation(
                Type.MULTIPLICATION,
                Literal(-1.0),
                Literal(2.0)
            )),
            Arguments.of("2 * lvl + 3", Operation(
                Type.ADDITION,
                Operation(
                    Type.MULTIPLICATION,
                    Literal(2.0),
                    Variable(Kind.LEVEL)
                ),
                Literal(3.0)
            )),
            Arguments.of("2*3-3/2", Operation(
                Type.SUBTRACTION,
                operation(Type.MULTIPLICATION, 2.0, 3.0),
                operation(Type.DIVISION, 3.0, 2.0)
            ))
        )

        @JvmStatic
        fun complexExpressionArguments(): Stream<out Arguments> = Stream.of(
            Arguments.of("(2 * 2 * prev + 4) * 5", 3, -1, 80),
            Arguments.of("(2 * 2 * prev + 4) * 5", 2.5, -1, 70),
            Arguments.of("(2 * 2 * prev + 4) * 5", 7, -1, 160),
            Arguments.of("5 * lvl + 2 * (3 + lvl) + prev", 2, 2, 22)
        )
    }
}

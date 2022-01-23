package de.schottky.expression

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream

class LexerTests {

    @ParameterizedTest
    @ValueSource(strings = ["", "  ", "\t", "\n", "\n  ", "\n\t  "])
    fun `The lexer contains no tokens when the string is empty`(source: String) {
        val lexer = ExpressionLexer(source)
        assertFalse(lexer.hasNext())
    }

    @ParameterizedTest
    @MethodSource("singleTokens")
    fun `The lexer correctly parses a single token`(source: String, expected: ExpressionLexer.Token) {
        val lexer = ExpressionLexer(source)
        assertTrue(lexer.hasNext())
        val next = lexer.next()
        assertEquals(next, expected)
    }

    @Test
    fun `The lexer correctly parses a string to a single token when it is surrounded with whitespaces`() {
        val lexer = ExpressionLexer("  Hello  \n")
        assertTrue(lexer.hasNext())
        assertEquals(lexer.next(), ExpressionLexer.Symbol.VARIABLE.toToken(2, 5))
    }

    @ParameterizedTest
    @MethodSource("multipleTokens")
    fun `The lexer correctly parses multiple tokens`(toParse: String, expected: Iterable<ExpressionLexer.Token>) {
        val tokens = ExpressionLexer.iterateTokensIn(toParse)
        assertIterableEquals(expected, tokens)
    }

    companion object {

        @JvmStatic
        fun singleTokens(): Stream<out Arguments> = Stream.of(
            Arguments.of("+", ExpressionLexer.Symbol.PLUS.toToken(0, 1)),
            Arguments.of("-", ExpressionLexer.Symbol.MINUS.toToken(0, 1)),
            Arguments.of("bla", ExpressionLexer.Symbol.VARIABLE.toToken(0, 3)),
            Arguments.of("12", ExpressionLexer.Symbol.LITERAL.toToken(0, 2)),
            Arguments.of("1.2", ExpressionLexer.Symbol.LITERAL.toToken(0, 3)),
            Arguments.of("##", ExpressionLexer.Symbol.UNKNOWN.toToken(0, 2))
        )

        @JvmStatic
        fun multipleTokens(): Stream<out Arguments> = Stream.of(
            Arguments.of(
                "1 2", listOf(
                    ExpressionLexer.Symbol.LITERAL.toToken(0, 1),
                    ExpressionLexer.Symbol.LITERAL.toToken(2, 1)
                )
            ),
            Arguments.of(
                " 1 + 1", listOf(
                    ExpressionLexer.Symbol.LITERAL.toToken(1, 1),
                    ExpressionLexer.Symbol.PLUS.toToken(3, 1),
                    ExpressionLexer.Symbol.LITERAL.toToken(5, 1)
                )
            ),
            Arguments.of(
                "2*level", listOf(
                    ExpressionLexer.Symbol.LITERAL.toToken(0, 1),
                    ExpressionLexer.Symbol.MULTIPLY.toToken(1, 1),
                    ExpressionLexer.Symbol.VARIABLE.toToken(2, 5)
                )
            )
        )
    }


}
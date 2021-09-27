package de.schottky.expression;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

import de.schottky.expression.ExpressionLexer.Token;
import de.schottky.expression.ExpressionLexer.Symbol;

public class LexerTests {

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "\t", "\n", "\n  ", "\n\t  "})
    void the_lexer_has_no_token_when_the_string_is_empty(String source) {
        var lexer = new ExpressionLexer(source);
        assertFalse(lexer.hasNext());
    }

    @ParameterizedTest
    @ArgumentsSource(SingleTokenArgumentsProvider.class)
    void the_lexer_parses_a_single_token(String source, Token expected) {
        var lexer = new ExpressionLexer(source);
        assertTrue(lexer.hasNext());
        var next = lexer.next();
        assertEquals(next, expected);
    }

    static class SingleTokenArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("+", Symbol.PLUS.toToken(0, 1)),
                    Arguments.of("-", Symbol.MINUS.toToken(0, 1)),
                    Arguments.of("bla", Symbol.VARIABLE.toToken(0, 3)),
                    Arguments.of("12", Symbol.LITERAL.toToken(0, 2)),
                    Arguments.of("1.2", Symbol.LITERAL.toToken(0, 3)),
                    Arguments.of("##", Symbol.UNKNOWN.toToken(0, 2))
            );
        }
    }

    @Test
    void the_lexer_parses_a_single_token_with_surrounding_whitespaces() {
        var lexer = new ExpressionLexer("  Hello  \n");
        assertTrue(lexer.hasNext());
        assertEquals(lexer.next(), Symbol.VARIABLE.toToken(2, 5));
    }

    @ParameterizedTest
    @ArgumentsSource(MultipleTokensArgumentsProvider.class)
    void the_lexer_parses_multiple_tokens(String toParse, Iterable<Token> expected) {
        var tokens = ExpressionLexer.iterateTokensIn(toParse);
        assertIterableEquals(expected, tokens);
    }

    static class MultipleTokensArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("1 2", List.of(
                            Symbol.LITERAL.toToken(0, 1),
                            Symbol.LITERAL.toToken(2, 1)
                    )),
                    Arguments.of(" 1 + 1", List.of(
                            Symbol.LITERAL.toToken(1, 1),
                            Symbol.PLUS.toToken(3, 1),
                            Symbol.LITERAL.toToken(5, 1)
                    )),
                    Arguments.of("2*level", List.of(
                            Symbol.LITERAL.toToken(0, 1),
                            Symbol.MULTIPLY.toToken(1, 1),
                            Symbol.VARIABLE.toToken(2, 5)
                    ))
            );
        }
    }
}

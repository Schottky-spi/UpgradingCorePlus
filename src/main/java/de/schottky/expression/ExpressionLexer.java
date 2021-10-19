package de.schottky.expression;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Predicate;

public class ExpressionLexer implements Iterator<ExpressionLexer.Token> {

    private final char[] chars;
    private int index = 0;

    public ExpressionLexer(@NotNull final String stringToParse) {
        this.chars = stringToParse.toCharArray();
        this.skipWhitespaces();
    }

    public String substringForToken(@NotNull Token token) {
        return String.valueOf(chars, token.startIndex(), token.length());
    }

    @Contract(pure = true)
    static @NotNull Iterable<Token> iterateTokensIn(@NotNull final String stringToParse) {
        return () -> new ExpressionLexer(stringToParse);
    }

    @Override
    public boolean hasNext() {
        skipWhitespaces();
        return index < chars.length;
    }

    @Override
    public Token next() {
        skipWhitespaces();
        var ch = Character.toLowerCase(currentChar());
        var startIndex = index;
        Symbol symbol;
        if (Character.isAlphabetic(ch)) {
            symbol = readVariable();
        } else if (Character.isDigit(ch) || ch == '.') {
            symbol = readLiteral();
        } else {
            symbol = switch (ch) {
                case '+' -> Symbol.PLUS;
                case '-' -> Symbol.MINUS;
                case '*' -> Symbol.MULTIPLY;
                case '/' -> Symbol.DIVIDE;
                case '%' -> Symbol.MOD;
                case '(' -> Symbol.OPEN_PAREN;
                case ')' -> Symbol.CLOSED_PAREN;
                case '[' -> Symbol.OPEN_BRACKET;
                case ']' -> Symbol.CLOSED_BRACKET;
                case ',' -> Symbol.COMMA;
                default -> readUnknown();
            };
            if (symbol != Symbol.UNKNOWN) {
                index += 1;
            }
        }
        return new Token(symbol, startIndex, index - startIndex);
    }

    private void skipWhitespaces() {
        readWhile(Character::isWhitespace);
    }

    private @NotNull Symbol readVariable() {
        readWhile(Character::isAlphabetic);
        return Symbol.VARIABLE;
    }

    private @NotNull Symbol readLiteral() {
        readWhile((ch) ->
                Character.isDigit(ch)
                        || ch == '.'
        );
        return Symbol.LITERAL;
    }

    private @NotNull Symbol readUnknown() {
        readWhile(Predicate.not(Character::isWhitespace));
        return Symbol.UNKNOWN;
    }

    private void readWhile(@NotNull Predicate<Character> predicate) {
        while (predicate.test(currentChar()) && currentChar() != Character.MIN_VALUE) {
            index += 1;
        }
    }

    private char currentChar() {
        if (index < chars.length) {
            return chars[index];
        } else {
            return Character.MIN_VALUE;
        }
    }

    static record Token(Symbol symbol, int startIndex, int length) {}

    enum Symbol {
        PLUS,
        MINUS,
        MULTIPLY,
        DIVIDE,
        MOD,
        OPEN_PAREN,
        CLOSED_PAREN,
        OPEN_BRACKET,
        CLOSED_BRACKET,
        VARIABLE,
        LITERAL,
        COMMA,
        UNKNOWN;

        @Contract("_, _ -> new")
        ExpressionLexer.@NotNull Token toToken(int start, int length) {
            return new ExpressionLexer.Token(this, start, length);
        }

        public boolean isSign() {
            return this == PLUS || this == MINUS;
        }

        public boolean isMultiplicative() {
            return this == MULTIPLY || this == DIVIDE || this == MOD;
        }

        public boolean isAddingOperator() {
            return this == PLUS || this == MINUS;
        }
    }
}

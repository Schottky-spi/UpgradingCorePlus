package de.schottky.expression;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

import de.schottky.expression.Expression.Node;
import de.schottky.expression.Expression.Node.Variable.Kind;
import de.schottky.expression.Expression.Node.Literal;
import de.schottky.expression.Expression.Node.Variable;
import de.schottky.expression.Expression.Node.Operation;
import de.schottky.expression.Expression.Node.Operation.Type;

public class ExpressionParserTests {

    @ParameterizedTest
    @ValueSource(strings = { "", "  ", "\t", " \n\t" })
    void the_expression_parser_throws_when_empty(String toParse) {
        assertThrows(ExpressionParseException.class, () -> new ExpressionParser(toParse).parse());
    }

    @ParameterizedTest
    @ValueSource(strings = { "[]", "[  ]", "[ \n]", "[", "[  " })
    void it_parses_an_empty_enumeration(String toParse) throws Exception {
        var parser = new ExpressionParser(toParse);
        var modifier = parser.parse();
        assertEquals(modifier.getClass(), Modifier.Enumeration.class);
        assertArrayEquals(((Modifier.Enumeration) modifier).enumeration(), new double[0]);
    }

    @ParameterizedTest
    @ArgumentsSource(FullArrayArgumentsSource.class)
    void it_parses_an_array_with_contents(String toParse, double[] contents) throws Exception {
        var parser = new ExpressionParser(toParse);
        var modifier = parser.parse();
        assertEquals(modifier.getClass(), Modifier.Enumeration.class);
        assertArrayEquals(((Modifier.Enumeration) modifier).enumeration(), contents);
    }

    static class FullArrayArgumentsSource implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("[1]", new double[] { 1 }),
                    Arguments.of("[1 2]", new double[] { 1, 2 }),
                    Arguments.of("[  1.5]", new double[] { 1.5 }),
                    Arguments.of("[  1 5", new double[] { 1, 5 }),
                    Arguments.of("[1  5]", new double[] { 1, 5 }),
                    Arguments.of("[1.0 .1]", new double[] { 1.0, 0.1 })
            );
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "[[1]", "[1, 2]", "[xy]", "[a+b]", "[f" })
    void it_throws_on_illegal_combinations(String stringToParse) {
        var parser = new ExpressionParser(stringToParse);
        assertThrows(ExpressionParseException.class, parser::parse);
    }

    @ParameterizedTest
    @ValueSource(strings = { "[1.1. ]", "[ 1.ee ]", "[ 1,5 ]" })
    void it_throws_when_there_is_an_illegal_number(String stringToParse) {
        var parser = new ExpressionParser(stringToParse);
        assertThrows(ExpressionParseException.class, parser::parse);
    }

    @ParameterizedTest
    @ArgumentsSource(value = TermArgumentsProvider.class)
    @ArgumentsSource(SimpleExpressionArgumentsProvider.class)
    @ArgumentsSource(FactorArgumentsProvider.class)
    void it_parses_expressions(String stringToParse, Expression.Node expected) throws Exception {
        var modifier = new ExpressionParser(stringToParse).parse();
        if (expected instanceof Literal literal) {
            assertEquals(Modifier.Simple.class, modifier.getClass());
            assertEquals(literal.value(), ((Modifier.Simple) modifier).amountPerLevel());
        } else {
            assertEquals(Modifier.Calculated.class, modifier.getClass());
            var root = ((Modifier.Calculated) modifier).expression().root();
            assertEquals(expected, root);
        }
    }

    static class FactorArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("1", new Literal(1)),
                    Arguments.of("1.2", new Literal(1.2)),
                    Arguments.of("lvl", new Variable(Kind.LEVEL)),
                    Arguments.of("lEvEl", new Variable(Kind.LEVEL)),
                    Arguments.of("prev", new Variable(Kind.PREVIOUS)),
                    Arguments.of("pReViOUs", new Variable(Kind.PREVIOUS))
            );
        }
    }

    static class TermArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("2 * lvl", new Operation(
                            Type.MULTIPLICATION,
                            new Literal(2),
                            new Variable(Kind.LEVEL)
                    )),
                    Arguments.of("prev%lvl", new Operation(
                            Type.MODULUS,
                            new Variable(Kind.PREVIOUS),
                            new Variable(Kind.LEVEL)
                    ))
            );
        }
    }

    static class SimpleExpressionArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("2 + lvl", new Operation(
                            Type.ADDITION,
                            new Literal(2),
                            new Variable(Kind.LEVEL)
                    )),
                    Arguments.of("2 + lvl * 3", new Operation(
                            Type.ADDITION,
                            new Literal(2),
                            new Operation(
                                    Type.MULTIPLICATION,
                                    new Variable(Kind.LEVEL),
                                    new Literal(3)
                            )
                    )),
                    Arguments.of("-2", new Operation(
                            Type.MULTIPLICATION,
                            new Literal(-1),
                            new Literal(2)
                    )),
                    Arguments.of("2 * lvl + 3", new Operation(
                            Type.ADDITION,
                            new Operation(
                                    Type.MULTIPLICATION,
                                    new Literal(2),
                                    new Variable(Kind.LEVEL)
                            ),
                            new Literal(3)
                    )),
                    Arguments.of("2*3-3/2", new Operation(
                            Type.SUBTRACTION,
                            Node.operation(Type.MULTIPLICATION, 2, 3),
                            Node.operation(Type.DIVISION, 3, 2)
                    ))
            );
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ComplexExpressionsArgumentsProvider.class)
    void it_computes_the_correct_result_for_complex_expressions(String stringToParse, double previous, int level, double expected) throws Exception {
        var expression = new ExpressionParser(stringToParse).parse();
        var result = expression.next(previous, level);
        assertEquals(expected, result);
    }

    static class ComplexExpressionsArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("(2 * 2 * prev + 4) * 5", 3, -1, 80),
                    Arguments.of("(2 * 2 * prev + 4) * 5", 2.5, -1, 70),
                    Arguments.of("(2 * 2 * prev + 4) * 5", 7, -1, 160),
                    Arguments.of("5 * lvl + 2 * (3 + lvl) + prev", 2, 2, 22)
            );
        }
    }
}

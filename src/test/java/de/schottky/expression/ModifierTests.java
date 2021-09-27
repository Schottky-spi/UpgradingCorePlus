package de.schottky.expression;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ModifierTests {

    private void testModifier(Modifier modifier, double @NotNull [] expectedPerLevel, double startPrevious) {
        var previous = startPrevious;
        for (int level = 0; level < expectedPerLevel.length; ++level) {
            var next = modifier.next(previous, level);
            assertEquals(expectedPerLevel[level], next, 1e-8);
            previous = next;
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SimpleModifierArgumentsSource.class)
    void a_simple_modifier_increases_linearly_with_the_level(String rawModifer, double @NotNull [] perLevel) throws Exception {
        var modifier = Modifier.parse(rawModifer);
        assertEquals(Modifier.Simple.class, modifier.getClass());
        testModifier(modifier, perLevel, 0);
    }

    static class SimpleModifierArgumentsSource implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("0", new double[] { 0, 0, 0 }),
                    Arguments.of("1", new double[] { 1, 2, 3 }),
                    Arguments.of("3", new double[] { 3, 6, 9 }),
                    Arguments.of("1 + 1", new double[] { 2, 4, 6 }),
                    // can be folded into a single argument
                    Arguments.of("(2 + 0) * 1 -3+2", new double[] { 1, 2, 3 })
            );
        }
    }

    @ParameterizedTest
    @ArgumentsSource(EnumerationArgumentsSource.class)
    void an_enumeration_retains_the_value_when_parsed(String rawModifier, double @NotNull [] perLevel) throws Exception {
        var modifier = Modifier.parse(rawModifier);
        assertEquals(Modifier.Enumeration.class, modifier.getClass());
        testModifier(modifier, perLevel, 0);
    }

    static class EnumerationArgumentsSource implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("[0 0 0]", new double[] { 0, 0, 0 }),
                    Arguments.of("[1 2 3]", new double[] { 1, 2, 3 }),
                    Arguments.of("[1.2 1.8 2.0", new double[] { 1.2, 1.8, 2.0 })
            );
        }
    }

    @ParameterizedTest
    @ArgumentsSource(CalculatedArgumentsSource.class)
    void a_calculated_expression_computes_the_correct_value_per_level(
            String rawModifier,
            double @NotNull [] perLevel,
            double startPrevious
    ) throws Exception {
        var modifier = Modifier.parse(rawModifier);
        assertEquals(Modifier.Calculated.class, modifier.getClass());
        testModifier(modifier, perLevel, startPrevious);
    }

    static class CalculatedArgumentsSource implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("2*lvl", new double[] { 0, 2, 4 }, 0),
                    Arguments.of("prev + 1", new double[] { 1, 2, 3 }, 0),
                    Arguments.of("prev + lvl", new double[] { 0, 1, 3 }, 0),
                    Arguments.of("lvl + 1", new double[] { 1, 2, 3 }, 0),
                    Arguments.of("(lvl + 1) * 2", new double[] { 2, 4, 6 }, 0),
                    Arguments.of("(lvl + 1) * 1.5", new double[] { 1.5, 3, 4.5 }, 0),
                    Arguments.of("prev * 1.1", new double[] { 1.1, 1.21, 1.331 }, 1)
            );
        }
    }
}

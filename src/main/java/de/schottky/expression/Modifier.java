package de.schottky.expression;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface Modifier {

    static Modifier parse(String source) throws ExpressionParseException {
        return new ExpressionParser(source).parse().simplify();
    }

    @Contract(" -> new")
    static @NotNull Modifier noop() {
        return new Simple(0);
    }

    double next(double previous, int level);

    default Modifier simplify() {
        return this;
    }

    record Enumeration(double[] enumeration) implements Modifier {

        @Override
        public double next(double previous, int level) {
            // TODO: what happens if the enumeration contains less elements?
            // idea: interpolation
            return enumeration[level];
        }
    }

    record Simple(double amountPerLevel) implements Modifier {

        @Override
        public double next(double previous, int level) {
            return previous + amountPerLevel;
        }
    }

    record Calculated(Expression expression) implements Modifier {

        @Override
        public double next(double previous, int level) {
            return expression.evaluate(previous, level);
        }

        public Optional<Modifier> simplifyToSimpleModifier() {
            if (expression.root() instanceof Expression.Node.Literal literal) {
                var amount = literal.value();
                return Optional.of(new Simple(amount));
            } else {
                return Optional.empty();
            }
        }

        @Contract(" -> new")
        @Override
        public @NotNull Modifier simplify() {
            var simpleExpression = expression.simplify();
            if (simpleExpression.root() instanceof Expression.Node.Literal literal) {
                var amount = literal.value();
                return new Simple(amount);
            } else {
                return new Calculated(simpleExpression);
            }
        }
    }
}

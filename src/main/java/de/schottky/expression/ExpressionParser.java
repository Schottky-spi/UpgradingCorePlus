package de.schottky.expression;

import com.google.common.primitives.Doubles;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import de.schottky.expression.ExpressionLexer.Token;
import de.schottky.expression.ExpressionLexer.Symbol;

public class ExpressionParser {

    private final ExpressionLexer tokens;
    private Token current;

    @Contract(pure = true)
    public ExpressionParser(@NotNull String stringToParse) {
        this.tokens = new ExpressionLexer(stringToParse);
    }

    public Modifier parse() throws ExpressionParseException {
        if (!tokens.hasNext()) {
            throw new ExpressionParseException("empty expression");
        }
        current = tokens.next();
        return current.symbol() == Symbol.OPEN_BRACKET ?
                parseEnumeration() :
                parseExpression();
    }

    public Modifier parseEnumeration() throws ExpressionParseException {
        var literals = new ArrayList<Double>();
        while (tokens.hasNext()) {
            var next = tokens.next();
            if (next.symbol() == Symbol.LITERAL) {
                literals.add(getLiteral(next));
            } else if (next.symbol() == Symbol.CLOSED_BRACKET) {
                break;
            } else {
                throw new ExpressionParseException("There can only be numbers inside the enumerations");
            }
        }
        var doubles = Doubles.toArray(literals);
        return new Modifier.Enumeration(doubles);
    }

    private @NotNull Modifier parseExpression() throws ExpressionParseException {
        var root = simpleExpression();
        var expression = new Expression(root);
        var modifier = new Modifier.Calculated(expression);
        return modifier.simplifyToSimpleModifier().orElse(modifier);
    }

    private Expression.Node simpleExpression() throws ExpressionParseException {
        Symbol sign = null;
        if (current.symbol().isSign()) {
            sign = current.symbol();
            nextSymbol(true);
        }
        var term = term();
        if (sign != null) {
            var helper = sign == Symbol.MINUS ?
                    new Expression.Node.Literal(-1) :
                    new Expression.Node.Literal(+1);
            term = helper.formOperationLeft(Expression.Node.Operation.Type.MULTIPLICATION, term);
        }
        while (current != null && current.symbol().isAddingOperator()) {
            var operator = switch (current.symbol()) {
                case PLUS -> Expression.Node.Operation.Type.ADDITION;
                case MINUS -> Expression.Node.Operation.Type.SUBTRACTION;
                default -> throw new ExpressionParseException(
                        "Expected an adding operator but found " + current.symbol().name().toLowerCase()
                );
            };
            nextSymbol(true);
            var right = term();
            term = term.formOperationLeft(operator, right);
        }
        return term;
    }

    private Expression.@NotNull Node term() throws ExpressionParseException {
        var factor = factor();
        while (current != null && current.symbol().isMultiplicative()) {
            var operator = switch (current.symbol()) {
                case MULTIPLY -> Expression.Node.Operation.Type.MULTIPLICATION;
                case DIVIDE -> Expression.Node.Operation.Type.DIVISION;
                case MOD -> Expression.Node.Operation.Type.MODULUS;
                default -> throw new ExpressionParseException(
                        "Expected a multiplying operator but found " + current.symbol().name().toLowerCase()
                );
            };
            nextSymbol(true);
            var right = factor();
            factor = factor.formOperationLeft(operator, right);
        }
        return factor;
    }

    @Contract(" -> new")
    private Expression.@NotNull Node factor() throws ExpressionParseException {
        return primary();
    }

    private Expression.Node primary() throws ExpressionParseException {
        if (current.symbol() == Symbol.LITERAL) {
            var literal = getLiteral(current);
            nextSymbol(false);
            return new Expression.Node.Literal(literal);
        } else if (current.symbol() == Symbol.VARIABLE) {
            var variable = tokens.substringForToken(current);
            var kind = switch (variable.toLowerCase()) {
                case "lvl", "level" ->
                        Expression.Node.Variable.Kind.LEVEL;
                case "prev", "previous" ->
                        Expression.Node.Variable.Kind.PREVIOUS;
                default -> throw new ExpressionParseException("Unrecognized variable name: " + variable);
            };
            nextSymbol(false);
            return new Expression.Node.Variable(kind);
        } else if (current.symbol() == Symbol.OPEN_PAREN) {
            nextSymbol(true);
            var expression = simpleExpression();
            expectSymbol(Symbol.CLOSED_PAREN);
            return expression;
        } else {
            throw new ExpressionParseException(
                    "Expected to find a number or variable, but found " + current.symbol().name().toLowerCase()
            );
        }
    }

    private void nextSymbol(boolean force) throws ExpressionParseException {
        if (tokens.hasNext()) {
            current = tokens.next();
        } else {
            if (force) {
                throw new ExpressionParseException(
                        "Expected to find an expression after " + current.symbol().name().toLowerCase()
                );
            } else {
                current = null;
            }
        }
    }

    private void expectSymbol(Symbol symbol) throws ExpressionParseException {
        if (current.symbol() == symbol) {
            nextSymbol(false);
        } else {
            throw new ExpressionParseException("Expected to find symbol " + symbol.name().toLowerCase());
        }
    }

    public double getLiteral(Token token) throws ExpressionParseException {
        var literalAsString = tokens.substringForToken(token);
        try {
            return Double.parseDouble(literalAsString);
        } catch (NumberFormatException e) {
            throw new ExpressionParseException(e);
        }
    }

}

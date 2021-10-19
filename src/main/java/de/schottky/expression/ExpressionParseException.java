package de.schottky.expression;

public class ExpressionParseException extends Exception {

    public ExpressionParseException(String message) {
        super(message);
    }

    public ExpressionParseException(NumberFormatException e) {
        super(e);
    }
}

package de.schottky.exception;

import de.schottky.expression.ExpressionParseException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class InvalidConfiguration extends Exception {

    @Contract("_ -> new")
    public static @NotNull InvalidConfiguration required(String requiredValue) {
        return new InvalidConfiguration("Configuration must contain " + requiredValue + " but it's not present!");
    }

    @Contract("_, _ -> new")
    public static @NotNull InvalidConfiguration parsingFailed(@NotNull ExpressionParseException e, String name) {
        return new InvalidConfiguration("Cannot parse " + name + ": " + e.getMessage());
    }

    public InvalidConfiguration(String message) {
        super(message);
    }
}

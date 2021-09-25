package de.schottky.exception;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class InvalidConfiguration extends Exception {

    @Contract("_ -> new")
    public static @NotNull InvalidConfiguration required(String requiredValue) {
        return new InvalidConfiguration("Configuration must contain " + requiredValue + " but it's not present!");
    }

    public InvalidConfiguration(String message) {
        super(message);
    }
}

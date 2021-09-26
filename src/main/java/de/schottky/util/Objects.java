package de.schottky.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class Objects {

    /**
     * performs a certain action for two different elements. This can be used to simplify a statement of type
     * <pre>{@code
     * foo(first);
     * bar(first);
     * foo(second);
     * bar(second);
     * }</pre>
     * to
     * <pre>{@code
     * Objects.doTwice(first, second, t -> {
     *     foo(t);
     *     bar(t);
     * });
     * }</pre>
     * @param first The first object to perform an action for
     * @param second The second object to perform an action for
     * @param action The action to apply to both objects
     * @param <T> The type of the object
     */
    public static <T> void doTwiceTwiceFor(T first, T second, @NotNull Consumer<T> action) {
        action.accept(first);
        action.accept(second);
    }
}

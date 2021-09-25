package de.schottky.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class Objects {

    /**
     * casts the input-type to a class and performs a given action, if the cast was successful
     * This can be used to simplify the expression
     * <pre> {@code
     * final V v = getV();
     * if (v instanceof T) {
     *     T t = (T) v;
     *     performAction(t);
     * }
     * }</pre>
     * to
     * <pre>{@code
     * Objects.cast(getV(), T.class, t -> performAction(t));
     * }</pre>
     * @param in The object to cast to a specific type
     * @param castTo The class to cast this to
     * @param onSuccess The action to perform if the cast was successful
     * @param <T> The type of the object to cast to
     * @param <V> The type of the object to cast from
     */
    public static <T,V> void cast(@NotNull V in, @NotNull Class<T> castTo, Consumer<T> onSuccess) {
        if (castTo.isAssignableFrom(in.getClass())) {
            onSuccess.accept(castTo.cast(in));
        }
    }

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

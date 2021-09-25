package de.schottky.util;

import de.schottky.Shared;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public final class ArrayUtil {

    @NotNull
    public static <E,T> List<E> map(@NotNull List<T> list, Function<T,E> mapper) {
        List<E> result = new ArrayList<>();
        for (T t: list) {
            result.add(mapper.apply(t));
        }
        return result;
    }

    @NotNull
    public static <E,T> Set<E> map(@NotNull Set<T> set, Function<T,E> mapper) {
        Set<E> result = new HashSet<>();
        for (T t: set) {
            result.add(mapper.apply(t));
        }
        return result;
    }

    public static <Element> Optional<Element> randomElement(@NotNull List<Element> list) {
        return randomElement(list, Shared.random);
    }

    /**
     * Returns a random element from a list of elements
     * @param list The element to get the element from
     * @param random The random to use
     * @param <Element> The element-type of the list
     * @return A random element, if the list is not empty, an empty optional else
     */
    public static <Element> Optional<Element> randomElement(@NotNull List<Element> list, Random random) {
        return list.isEmpty() ?
                Optional.empty() :
                Optional.of(list.get(random.nextInt(list.size())));
    }
}

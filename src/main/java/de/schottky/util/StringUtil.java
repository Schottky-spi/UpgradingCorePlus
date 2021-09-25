package de.schottky.util;

import org.jetbrains.annotations.NotNull;

public class StringUtil {

    /**
     * Capitalizes a certain String to the form
     * <pre>
     *     "hello, world" -> "Hello, World"
     * </pre>
     * This will lowercase all input-characters except for the first of each word.
     * Other than that, the string will remain the same
     * @param in The string to capitalize
     * @return The capitalized string
     */
    public static @NotNull String capitalize(@NotNull String in) {
        StringBuilder builder = new StringBuilder();
        boolean lastWhitespace = true;
        for (char c: in.toCharArray()) {
            if (lastWhitespace && !Character.isWhitespace(c)) {
                lastWhitespace = false;
                builder.append(Character.toUpperCase(c));
            } else if (Character.isWhitespace(c)) {
                lastWhitespace = true;
                builder.append(c);
            } else {
                builder.append(Character.toLowerCase(c));
            }
        }
        return builder.toString();
    }
}

package de.schottky.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MapUtil {

    @NotNull
    @Contract(pure = true)
    public static <K,V> LinkedHashMap<K,V> sortedValues(@NotNull Map<K,V> map, Comparator<? super V> comparator) {
        List<Map.Entry<K,V>> sortedList = new ArrayList<>(map.entrySet());
        sortedList.sort(Map.Entry.comparingByValue(comparator));

        LinkedHashMap<K,V> newMap = new LinkedHashMap<>();
        for (Map.Entry<K,V> entry: sortedList) {
            newMap.put(entry.getKey(), entry.getValue());
        }
        return newMap;
    }
}

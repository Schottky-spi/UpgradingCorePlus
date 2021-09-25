package de.schottky.util;

import com.github.schottky.zener.messaging.Console;
import de.schottky.exception.InvalidConfiguration;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ConfigUtil {

    public static double getRequiredDouble(
            @NotNull ConfigurationSection section,
            String name) throws InvalidConfiguration
    {
        if (section.contains(name) && section.isDouble(name)) {
            return section.getDouble(name);
        } else {
            throw InvalidConfiguration.required(name);
        }
    }

    public static String getRequiredString(
            @NotNull ConfigurationSection section,
            String name)throws InvalidConfiguration
    {
        if (section.isString(name)) {
            return Objects.requireNonNull(section.getString(name));
        } else {
            throw InvalidConfiguration.required(name);
        }
    }

    public static @NotNull Material getRequiredMaterial(
            @NotNull ConfigurationSection section,
            String name) throws InvalidConfiguration
    {
        return getRequiredEnumValue(section, name, Material.class);
    }

    @NotNull
    public static <T extends Enum<T>> Set<T> getRequiredEnumSet(
            @NotNull ConfigurationSection section,
            String name,
            Class<T> clazz) throws InvalidConfiguration
    {
        if (section.isList(name)) {
            List<String> stringList = section.getStringList(name);
            Set<T> enumSet = EnumSet.noneOf(clazz);
            for (String value: stringList) {
                try {
                    T t = Enum.valueOf(clazz, value);
                    enumSet.add(t);
                } catch (IllegalArgumentException e) {
                    throw new InvalidConfiguration("Value " + value + " is not convertible to " + clazz.getName());
                }
            }
            return enumSet;
        } else {
            throw InvalidConfiguration.required(name);
        }
    }

    @NotNull
    public static <T extends Enum<T>> T getRequiredEnumValue(
            @NotNull ConfigurationSection section,
            String name,
            Class<T> clazz) throws InvalidConfiguration
    {
        if (section.isString(name)) {
            String value = section.getString(name);
            try {
                return Enum.valueOf(clazz, value);
            } catch (IllegalArgumentException e) {
                Console.error(e);
                throw new InvalidConfiguration("Value " + value + " is not convertible to " + clazz.getSimpleName());
            }
        } else {
            throw InvalidConfiguration.required(name);
        }
    }
}

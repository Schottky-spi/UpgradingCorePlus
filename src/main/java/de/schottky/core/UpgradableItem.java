package de.schottky.core;


import com.github.schottky.zener.messaging.Console;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.schottky.UpgradingCorePlugin;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

public abstract class UpgradableItem {

    public static boolean isUpgradable(Material material) {
        return UpgradableArmor.ALL_ARMAMENTS.containsKey(material) ||
                UpgradableMeleeWeapon.ALL_WEAPONS.containsKey(material) ||
                UpgradableRangedWeapon.ALL_WEAPONS.containsKey(material);
    }

    public static Optional<Tool> getTool(Material material) {
        UpgradableItem item = UpgradableArmor.ALL_ARMAMENTS.get(material);
        if (item == null) {
            item = UpgradableMeleeWeapon.ALL_WEAPONS.get(material);
        }
        if (item == null) {
            item = UpgradableRangedWeapon.ALL_WEAPONS.get(material);
        }
        return item == null ? Optional.empty() : Optional.of(item.tool);
    }

    public static boolean isMeleeWeapon(Material material) {
        return UpgradableMeleeWeapon.ALL_WEAPONS.containsKey(material);
    }

    public static boolean isRangedWeapon(Material material) {
        return UpgradableRangedWeapon.ALL_WEAPONS.containsKey(material);
    }

    public static boolean isArmor(Material material) {
        return UpgradableArmor.ALL_ARMAMENTS.containsKey(material);

    }

    public static void loadItems() {
        final InputStream is = JavaPlugin.getPlugin(UpgradingCorePlugin.class).getResource("materials.json");
        Objects.requireNonNull(is);
        final InputStreamReader reader = new InputStreamReader(is);
        fromJson(new JsonParser().parse(reader).getAsJsonObject());
        try {
            reader.close();
        } catch (IOException e) {
            Console.error(e);
        }
    }

    public final Tool tool;
    public final Material material;

    public UpgradableItem(Tool tool, Material material) {
        this.tool = tool;
        this.material = material;
    }

    private static void fromJson(@NotNull JsonObject object) {
        final var meleeWeapons = object.getAsJsonObject("MELEE");
        for (Entry<String, JsonElement> entry: meleeWeapons.entrySet()) {
            materialForName(entry.getKey()).ifPresent(mat ->
                    UpgradableMeleeWeapon.fromJson(entry.getValue().getAsJsonObject(), mat));
        }
        final var armaments = object.getAsJsonObject("ARMOR");
        for (Entry<String, JsonElement> entry: armaments.entrySet()) {
            final EquipmentSlot slot = EquipmentSlot.valueOf(entry.getKey());
            parseAllArmaments(entry.getValue().getAsJsonObject(), slot);
        }
        final var rangedWeapons = object.getAsJsonObject("RANGED");
        for (Entry<String, JsonElement> entry: rangedWeapons.entrySet()) {
            materialForName(entry.getKey()).ifPresent(mat ->
                    UpgradableRangedWeapon.fromJson(entry.getValue().getAsJsonObject(), mat));
        }
    }

    private static void parseAllArmaments(@NotNull JsonObject object, EquipmentSlot slot) {
        for (Entry<String,JsonElement> entry: object.entrySet()) {
            materialForName(entry.getKey()).ifPresent(mat ->
                    UpgradableArmor.fromJson(entry.getValue().getAsJsonObject(), slot, mat));
        }
    }

    private static Optional<Material> materialForName(@NotNull String materialName) {
        try {
            final Material material = Material.valueOf(materialName.toUpperCase());
            return Optional.of(material);
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

}

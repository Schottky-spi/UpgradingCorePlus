package de.schottky.core;

import com.github.schottky.zener.util.item.ItemStorage;
import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class UpgradableRangedWeapon extends UpgradableItem {
    public static final Map<Material, UpgradableRangedWeapon> ALL_WEAPONS = new EnumMap<>(Material.class);

    public final double arrowDamageModifier;

    public UpgradableRangedWeapon(
            Tool tool,
            Material material,
            double arrowDamageModifier
    ) {
        super(tool, material);
        this.arrowDamageModifier = arrowDamageModifier;
        ALL_WEAPONS.put(material, this);
    }

    public static void fromJson(@NotNull JsonObject object, Material material) {
        new UpgradableRangedWeapon(
                Tool.valueOf(object.get("tool").getAsString()),
                material,
                object.get("arrowDamage").getAsDouble()
        );
    }

    public static final String DAMAGE_KEY = "arrowDamage";

    public static void increaseAttributes(
            @NotNull ItemStack itemStack,
            double damage
    ) {
        final var meta = itemStack.getItemMeta();
        if (meta == null) return;
        final var previousValue = ItemStorage.getDouble(meta, DAMAGE_KEY, 0);
        final var newValue = previousValue + damage;
        ItemStorage.set(meta, newValue, DAMAGE_KEY);
        itemStack.setItemMeta(meta);
    }

    @Override
    public String toString() {
        return "UpgradableRangedWeapon{" +
                "arrowDamageMultiplier=" + arrowDamageModifier +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpgradableRangedWeapon that = (UpgradableRangedWeapon) o;
        return Double.compare(that.arrowDamageModifier, arrowDamageModifier) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(arrowDamageModifier);
    }
}

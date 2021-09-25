package de.schottky.core;

import com.google.gson.JsonObject;
import de.schottky.util.AttributeUtil;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class UpgradableArmor extends UpgradableItem {

    public static final Map<Material,UpgradableArmor> ALL_ARMAMENTS = new EnumMap<>(Material.class);

    public static Optional<UpgradableArmor> armorFor(Material material) {
        return Optional.ofNullable(ALL_ARMAMENTS.get(material));
    }

    public final double defaultArmor;
    public final double defaultArmorToughness;
    public final EquipmentSlot slot;

    public UpgradableArmor(
            Tool tool,
            Material material,
            double defaultArmor,
            double defaultArmorToughness,
            EquipmentSlot slot)
    {
        super(tool, material);
        this.defaultArmor = defaultArmor;
        this.defaultArmorToughness = defaultArmorToughness;
        this.slot = slot;
        ALL_ARMAMENTS.put(material, this);
    }

    public static void fromJson(@NotNull JsonObject object, EquipmentSlot slot, Material material) {
        new UpgradableArmor(
                Tool.valueOf(object.get("tool").getAsString()),
                material,
                object.get("armor").getAsDouble(),
                object.get("armorToughness").getAsDouble(),
                slot);
    }

    public static OptionalDouble defaultArmor(Material material) {
        final UpgradableArmor armor = ALL_ARMAMENTS.get(material);
        return armor == null ? OptionalDouble.empty() : OptionalDouble.of(armor.defaultArmor);
    }

    public static OptionalDouble defaultArmorToughness(Material material) {
        final UpgradableArmor armor = ALL_ARMAMENTS.get(material);
        return armor == null ? OptionalDouble.empty() : OptionalDouble.of(armor.defaultArmorToughness);
    }

    public static void increaseAttributesOf(@NotNull ItemStack stack, double armorValue, double armorToughness) {
        final UpgradableArmor armor = ALL_ARMAMENTS.get(stack.getType());
        if (armor == null) return;
        armor.increaseAttributes(stack, armorValue, armorToughness);
    }

    public void increaseAttributes(@NotNull ItemStack stack, double armor, double armorToughness) {
        AttributeUtil.increaseAttribute(stack, Attribute.GENERIC_ARMOR, armor, new AttributeModifier(
                UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF"),
                "Armor modifier",
                this.defaultArmor + armor,
                AttributeModifier.Operation.ADD_NUMBER,
                this.slot));

        AttributeUtil.increaseAttribute(stack, Attribute.GENERIC_ARMOR_TOUGHNESS, armorToughness, new AttributeModifier(
                UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3"),
                "Armor toughness",
                this.defaultArmorToughness + armorToughness,
                AttributeModifier.Operation.ADD_NUMBER,
                this.slot));
    }

    @Override
    public String toString() {
        return "UpgradableArmor{" +
                "armor=" + defaultArmor +
                ", armorToughness=" + defaultArmorToughness +
                ", slot=" + slot +
                ", tool=" + tool +
                ", material=" + material +
                '}';
    }
}

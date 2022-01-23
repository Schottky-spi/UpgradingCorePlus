package de.schottky.core;

import com.google.gson.JsonObject;
import de.schottky.util.AttributeUtil;
import de.schottky.util.Items;
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
        UUID armorUUID;
        UUID armorToughnessUUID;
        if (Items.isBoot(stack)) {
            armorUUID = UUID_BOOTS_ARMOR;
            armorToughnessUUID = UUID_BOOTS_TOUGHNESS;
        }
        else if (Items.isHelmet(stack)) {
            armorUUID = UUID_HELMET_ARMOR;
            armorToughnessUUID = UUID_HELMET_TOUGHNESS;
        }
        else if (Items.isChestplate(stack)) {
            armorUUID = UUID_CHESTPLATE_ARMOR;
            armorToughnessUUID = UUID_CHESTPLATE_TOUGHNESS;
        }
        else if (Items.isLeggings(stack)) {
            armorUUID = UUID_LEGGINGS_ARMOR;
            armorToughnessUUID = UUID_LEGGINGS_TOUGHNESS;
        } else {
            armorUUID = UUID_FALLBACK_ARMOR;
            armorToughnessUUID = UUID_FALLBACK_TOUGHNESS;
        }
        AttributeUtil.increaseAttribute(stack, Attribute.GENERIC_ARMOR, armor, new AttributeModifier(
                armorUUID,
                "Armor modifier",
                this.defaultArmor + armor,
                AttributeModifier.Operation.ADD_NUMBER,
                this.slot));

        AttributeUtil.increaseAttribute(stack, Attribute.GENERIC_ARMOR_TOUGHNESS, armorToughness, new AttributeModifier(
                armorToughnessUUID,
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

    static final UUID UUID_BOOTS_ARMOR = UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B");
    static final UUID UUID_BOOTS_TOUGHNESS = UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B");
    static final UUID UUID_HELMET_ARMOR = UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150");
    static final UUID UUID_HELMET_TOUGHNESS = UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150");
    static final UUID UUID_CHESTPLATE_ARMOR = UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E");
    static final UUID UUID_CHESTPLATE_TOUGHNESS = UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E");
    static final UUID UUID_LEGGINGS_ARMOR = UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D");
    static final UUID UUID_LEGGINGS_TOUGHNESS = UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D");
    static final UUID UUID_FALLBACK_ARMOR = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    static final UUID UUID_FALLBACK_TOUGHNESS = UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D");
}

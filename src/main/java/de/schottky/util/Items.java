package de.schottky.util;

import de.schottky.core.UpgradableArmor;
import de.schottky.core.UpgradableWeapon;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Function;

public class Items {

    public static double computeAttackDamage(@NotNull ItemStack stack) {
        double damage = computeAttribute(stack, Attribute.GENERIC_ATTACK_DAMAGE, 1, UpgradableWeapon::defaultDamage);
        if (damage == -1) return -1;
        final ItemMeta meta = Objects.requireNonNull(stack.getItemMeta());
        final Integer level = meta.getEnchants().get(Enchantment.DAMAGE_ALL);
        if (level != null) {
            damage += 0.5 * (level - 1 ) + 1;
        }
        return damage;
    }

    public static double computeAttackSpeed(@NotNull ItemStack stack) {
        return computeAttribute(stack, Attribute.GENERIC_ATTACK_SPEED, 4, UpgradableWeapon::defaultAttackSpeed);
    }

    public static double computeArmor(@NotNull ItemStack stack) {
        return computeAttribute(stack, Attribute.GENERIC_ARMOR, 0, UpgradableArmor::defaultArmor);
    }

    public static double computeArmorToughness(@NotNull ItemStack stack) {
        return computeAttribute(stack, Attribute.GENERIC_ARMOR_TOUGHNESS, 0, UpgradableArmor::defaultArmorToughness);
    }

    public static boolean isBoot(@NotNull ItemStack stack){
        return (stack.getType()==Material.CHAINMAIL_BOOTS||stack.getType()==Material.DIAMOND_BOOTS||
                stack.getType()==Material.GOLDEN_BOOTS||stack.getType()==Material.IRON_BOOTS||
                stack.getType()==Material.LEATHER_BOOTS||stack.getType()==Material.NETHERITE_BOOTS);
    }

    public static boolean isChestplate(@NotNull ItemStack stack){
        return (stack.getType()==Material.CHAINMAIL_CHESTPLATE||stack.getType()==Material.DIAMOND_CHESTPLATE||
                stack.getType()==Material.GOLDEN_CHESTPLATE||stack.getType()==Material.IRON_CHESTPLATE||
                stack.getType()==Material.LEATHER_CHESTPLATE||stack.getType()==Material.NETHERITE_CHESTPLATE);
    }

    public static boolean isLeggings(@NotNull ItemStack stack){
        return (stack.getType()==Material.CHAINMAIL_LEGGINGS||stack.getType()==Material.DIAMOND_LEGGINGS||
                stack.getType()==Material.GOLDEN_LEGGINGS||stack.getType()==Material.IRON_LEGGINGS||
                stack.getType()==Material.LEATHER_LEGGINGS||stack.getType()==Material.NETHERITE_LEGGINGS);
    }

    public static boolean isHelmet(@NotNull ItemStack stack){
        return (stack.getType()==Material.CHAINMAIL_HELMET||stack.getType()==Material.DIAMOND_HELMET||
                stack.getType()==Material.GOLDEN_HELMET||stack.getType()==Material.IRON_HELMET||
                stack.getType()==Material.LEATHER_HELMET||stack.getType()==Material.NETHERITE_HELMET||
                stack.getType()==Material.TURTLE_HELMET);
    }

    private static double computeAttribute(
            @NotNull ItemStack stack,
            Attribute attribute,
            double initialValue,
            Function<Material, OptionalDouble> defaultSupplier)
    {
        double attributeValue = initialValue;
        final ItemMeta meta = stack.getItemMeta();
        if (meta == null) return -1;
        Optional<Double> attributeValueOptional = AttributeUtil.getAttribute(meta, attribute);
        if (attributeValueOptional.isPresent()) {
            attributeValue += attributeValueOptional.get();
        } else {
            OptionalDouble defaultValue = defaultSupplier.apply(stack.getType());
            if (defaultValue.isPresent()) {
                attributeValue += defaultValue.getAsDouble();
            } else {
                return -1;
            }
        }
        return attributeValue;
    }
}

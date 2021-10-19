package de.schottky.core;

import com.google.gson.JsonObject;
import de.schottky.expression.Modifier;
import de.schottky.util.AttributeUtil;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class UpgradableMeleeWeapon extends UpgradableItem {

    public final double attackDamage;
    public final double attackSpeed;

    public static final Map<Material, UpgradableMeleeWeapon> ALL_WEAPONS = new EnumMap<>(Material.class);

    public UpgradableMeleeWeapon(
            Tool tool,
            Material material,
            double attackDamage,
            double attackSpeed)
    {
        super(tool, material);
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        ALL_WEAPONS.put(material, this);
    }

    public static void fromJson(@NotNull JsonObject object, Material material) {
        new UpgradableMeleeWeapon(
                Tool.valueOf(object.get("tool").getAsString()),
                material,
                object.get("attackDamage").getAsDouble(),
                object.get("attackSpeed").getAsDouble());
    }

    public static void increaseAttributesOf(@NotNull ItemStack stack, Modifier damage, Modifier attackSpeed, int level) {
        final UpgradableMeleeWeapon weapon = ALL_WEAPONS.get(stack.getType());
        if (weapon == null) return;
        weapon.increaseAttributes(stack, damage, attackSpeed, level);
    }

    public static OptionalDouble defaultDamage(Material material) {
        final UpgradableMeleeWeapon weapon = ALL_WEAPONS.get(material);
        return weapon == null ? OptionalDouble.empty() : OptionalDouble.of(weapon.attackDamage);
    }

    public static OptionalDouble defaultAttackSpeed(Material material) {
        final UpgradableMeleeWeapon weapon = ALL_WEAPONS.get(material);
        return weapon == null ? OptionalDouble.empty() : OptionalDouble.of(weapon.attackSpeed);
    }

    private void increaseAttributes(@NotNull ItemStack stack, Modifier damage, Modifier attackSpeed, int level) {
        AttributeUtil.increaseAttribute(stack, Attribute.GENERIC_ATTACK_DAMAGE, damage, level, new AttributeModifier(
                UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF"),
                "Weapon modifier",
                damage.next(this.attackDamage, level),
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND));

        AttributeUtil.increaseAttribute(stack, Attribute.GENERIC_ATTACK_SPEED, attackSpeed, level, new AttributeModifier(
                UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3"),
                "Weapon modifier",
                attackSpeed.next(this.attackSpeed, level),
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND));
    }

    @Override
    public String toString() {
        return "UpgradableWeapon{" +
                "attackDamage=" + attackDamage +
                ", attackSpeed=" + attackSpeed +
                ", tool=" + tool +
                ", material=" + material +
                '}';
    }
}

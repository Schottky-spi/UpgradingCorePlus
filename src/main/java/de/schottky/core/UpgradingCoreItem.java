package de.schottky.core;

import com.github.schottky.zener.localization.Language;
import com.github.schottky.zener.util.item.ItemStorage;
import com.github.schottky.zener.util.item.Lore;
import de.schottky.Options;
import de.schottky.exception.InvalidConfiguration;
import de.schottky.util.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.md_5.bungee.api.ChatColor.GREEN;
import static net.md_5.bungee.api.ChatColor.YELLOW;

public class UpgradingCoreItem extends CoreItem {

    private final double armorModifier;
    private final double armorToughnessModifier;
    private final double damageModifier;
    private final double attackSpeedModifier;
    private final double arrowDamageModifier;

    public UpgradingCoreItem(final String name, final ConfigurationSection section) throws InvalidConfiguration {
        super(name, section);
        this.armorModifier = ConfigUtil.getRequiredDouble(section, "armorModifier");
        this.armorToughnessModifier = section.getDouble("armorToughnessModifier", 0);
        this.damageModifier = ConfigUtil.getRequiredDouble(section, "damageModifier");
        this.attackSpeedModifier = section.getDouble("attackSpeedModifier", 0);
        this.arrowDamageModifier = section.getDouble("arrowDamageModifier", 0.0);
    }

    @Override
    public @NotNull Type type() {
        return Type.UPGRADING;
    }

    public ForgingResult forge(@Nullable ItemStack stack, @NotNull Permissible permissible) {
        for (ForgingResult result: ForgingResult.ALL_VALUES) {
            if (result.shouldFail(stack, permissible, this)) return result;
        }
        assert stack != null;
        final ItemMeta meta = stack.getItemMeta();
        assert meta != null;
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        ItemStorage.set(meta, itemLevelFor(meta) + 1, "level");
        stack.setItemMeta(meta);
        upgradeAttributes(stack);
        return ForgingResult.SUCCESS;
    }

    private void upgradeAttributes(@NotNull ItemStack stack) {
        if (UpgradableItem.isRangedWeapon(stack.getType())) {
            UpgradableRangedWeapon.increaseAttributes(stack, arrowDamageModifier);
        } else if (UpgradableItem.isMeleeWeapon(stack.getType())) {
            UpgradableMeleeWeapon.increaseAttributesOf(stack, damageModifier, attackSpeedModifier);
        } else if (UpgradableItem.isArmor(stack.getType())) {
            UpgradableArmor.increaseAttributesOf(stack, armorModifier, armorToughnessModifier);
        }
        updateVisualsOf(stack);
    }

    public static void updateVisualsIfTagged(ItemStack stack) {
        if (stack == null) return;
        if (ItemStorage.hasInt(stack.getItemMeta(), "level"))
            updateVisualsOf(stack);
    }

    private final static String LORE_IDENT = "lore";

    public static void updateVisualsOf(@NotNull ItemStack stack) {
        final ItemMeta meta = stack.getItemMeta();
        if (meta == null)
            return;

        final var lore = Lore.of(meta);
        final int startingLine = (ItemStorage.getInt(meta, LORE_IDENT, lore.size()) & 0xFFFF0000) >> 16;
        final int length = (ItemStorage.getInt(meta, LORE_IDENT, 0) & 0x0000FFFF);

        lore.removeRange(startingLine, startingLine + length);

        final List<String> newEntries = loreEntries(stack, meta);

        ItemStorage.set(meta, (startingLine << 16) & 0xFFFF0000 | (newEntries.size() & 0x0000FFFF), LORE_IDENT);

        lore.addAll(startingLine, newEntries);
        meta.setLore(lore);
        stack.setItemMeta(meta);
    }

    private static @NotNull List<String> loreEntries(final @NotNull ItemStack stack, final @NotNull ItemMeta meta) {
        List<String> newEntries = new ArrayList<>();
        setItemTitle(meta, stack.getType());
        appendLevelDesc(itemLevelFor(meta), newEntries);
        appendMeleeDescription(stack, newEntries);
        appendRangedDescription(stack, newEntries);
        appendArmamentsDescription(stack, newEntries);
        return newEntries;
    }

    private static void setItemTitle(final @NotNull ItemMeta meta, @NotNull final Material material) {
        if (Options.displayLevelInTitle)
            meta.setDisplayName(displayNameFor(itemLevelFor(meta), material));
    }

    @Contract(mutates = "param2")
    private static void appendLevelDesc(final int level, final @NotNull List<String> appendTo) {
        String levelDescription = Language.current().translateWithExtra("uc.level_desc",
                "level", level,
                "maxLevel", Options.maxLevel);
        if (!levelDescription.isEmpty())
            appendTo.add(ChatColor.GREEN + levelDescription);
    }

    @Contract(mutates = "param2")
    private static void appendMeleeDescription(final @NotNull ItemStack stack, final @NotNull List<String> appendTo) {
        if (UpgradableItem.isMeleeWeapon(stack.getType())) {
            double damage = Items.computeAttackDamage(stack);
            double attackSpeed = Items.computeAttackSpeed(stack);
            if (damage > 0)
                appendTo.add(ChatColor.GREEN + "+" + damage + " " + Language.current().translate("ident.damage"));
            if (attackSpeed > 0)
                appendTo.add(ChatColor.GREEN + "+" + attackSpeed + " " + Language.current().translate("ident.attack_speed"));
        }
    }

    @Contract(mutates = "param2")
    private static void appendRangedDescription(
            final @NotNull ItemStack stack,
            final @NotNull List<String> newEntries)
    {
        if (UpgradableItem.isRangedWeapon(stack.getType())) {
            final var damage = ItemStorage.getDouble(
                    stack.getItemMeta(),
                    UpgradableRangedWeapon.DAMAGE_KEY,
                    0
            );
            if (damage > 0) {
                newEntries.add(GREEN + "+" + damage + Language.current().translate("ident.arrow_damage"));
            }
        }
    }

    @Contract(mutates = "param2")
    private static void appendArmamentsDescription(final @NotNull ItemStack stack, final @NotNull List<String> appendTo) {
        if (UpgradableItem.isArmor(stack.getType())) {
            double armor = Items.computeArmor(stack);
            double toughness = Items.computeArmorToughness(stack);
            if (armor > 0)
                appendTo.add(GREEN + "+" + armor + " " + Language.current().translate("ident.armor"));
            if (toughness > 0)
                appendTo.add(GREEN + "+" + toughness + " " + Language.current().translate("ident.armor_toughness"));
        }
    }

    private static @NotNull String displayNameFor(int level, final @NotNull Material type) {
        return YELLOW + StringUtil.capitalize(type.name().replace("_", " ")) + " +" + level;
    }

    public static void updateItemsInInventory(final @NotNull PlayerInventory inventory) {
        for (ItemStack stack: inventory.getContents()) updateVisualsIfTagged(stack);
    }

    static int itemLevelFor(final @NotNull ItemMeta meta) {
        return ItemStorage.getInt(meta,"level", 0);
    }

    public String upgradePermission() {
        return "uc.upgrade." + name().toLowerCase();
    }

    @Override
    public String toString() {
        return "UpgradingCoreItem{" +
                "armorModifier=" + armorModifier +
                ", armorToughnessModifier=" + armorToughnessModifier +
                ", damageModifier=" + damageModifier +
                ", attackSpeedModifier=" + attackSpeedModifier +
                ", arrowDamageModifier=" + arrowDamageModifier +
                ", name='" + name + '\'' +
                ", material=" + material +
                ", chance=" + chance +
                ", color=" + color +
                ", entityTypes=" + entityTypes +
                ", materials=" + materials +
                ", lore=" + lore +
                ", failChance=" + failChance +
                '}';
    }
}

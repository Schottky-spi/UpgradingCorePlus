package de.schottky.menu;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private final ItemStack stack;
    private final ItemMeta meta;

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ItemBuilder forType(Material material) {
        return ItemBuilder.of(new ItemStack(material));
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ItemBuilder of(ItemStack stack) {
        return new ItemBuilder(stack, stack.getItemMeta());
    }

    public ItemBuilder(@NotNull ItemStack stack, ItemMeta meta) {
        this.stack = stack;
        this.meta = meta;
    }

    public ItemBuilder setTitle(String title) {
        if (meta != null) meta.setDisplayName(title);
        return this;
    }

    public ItemBuilder addLine(String... lines) {
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();
            lore.addAll(Arrays.asList(lines));
            meta.setLore(lore);
        }
        return this;
    }

    public ItemStack toStack() {
        if (meta == null) return stack;
        stack.setItemMeta(meta);
        return stack;
    }

}

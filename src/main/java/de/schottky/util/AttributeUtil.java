package de.schottky.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.schottky.expression.Modifier;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;

public class AttributeUtil {

    public static Optional<Double> getAttribute(@NotNull ItemMeta meta, Attribute attribute) {
        return Optional.ofNullable(meta.getAttributeModifiers())
                .map(attributeModifiers -> new ArrayList<>(attributeModifiers.get(attribute)))
                .map(modifiers -> modifiers.size() > 0 ? modifiers.get(0).getAmount() : null);
    }
    
    public static void increaseAttribute(
            @NotNull ItemStack stack,
            Attribute attribute,
            Modifier byValue,
            int level,
            AttributeModifier ifAbsent
    ) {
        final ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;

        Multimap<Attribute,AttributeModifier> attributeModifiers = meta.getAttributeModifiers() != null ?
                HashMultimap.create(meta.getAttributeModifiers()) :
                HashMultimap.create();

        if (attributeModifiers.containsKey(attribute)) {
            for (AttributeModifier modifier : attributeModifiers.removeAll(attribute)) {
                final AttributeModifier newModifier = new AttributeModifier(
                        modifier.getUniqueId(),
                        modifier.getName(),
                        byValue.next(modifier.getAmount(), level),
                        modifier.getOperation(),
                        modifier.getSlot()
                );
                attributeModifiers.put(attribute, newModifier);
            }
        } else {
            attributeModifiers.put(attribute, ifAbsent);
        }

        meta.setAttributeModifiers(attributeModifiers);
        stack.setItemMeta(meta);
    }
}

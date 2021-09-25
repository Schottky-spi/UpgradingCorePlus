package de.schottky.event;

import de.schottky.core.UpgradingCoreItem;
import de.schottky.util.Timers;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class VisualChangeListener implements Listener {

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        UpgradingCoreItem.updateItemsInInventory(event.getPlayer().getInventory());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemEnchant(@NotNull EnchantItemEvent event) {
        update(event.getItem());
    }

    private void update(ItemStack stack) {
        Timers.runLater(1, () -> UpgradingCoreItem.updateVisualsIfTagged(stack));
    }
}
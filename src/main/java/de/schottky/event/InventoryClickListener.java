package de.schottky.event;

import de.schottky.core.CoreItem;
import de.schottky.menu.RefactorMenu;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InventoryClickListener implements Listener {

    private final Map<UUID,RefactorMenu> activeMenus = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInventoryClick(@NotNull InventoryClickEvent event) {
        menuForPlayer(event::getWhoClicked, menu -> menu.onInventoryClick(event));
    }

    @EventHandler
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        menuForPlayer(event::getWhoClicked, menu -> menu.onInventoryDrag(event));
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        menuForPlayer(event::getPlayer, menu -> {
            activeMenus.remove(event.getPlayer().getUniqueId());
            menu.onInventoryClose(event.getInventory(), event.getPlayer());
        });
    }

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            CoreItem.fromItemStack(event.getItem()).ifPresent(item -> {
                RefactorMenu menu = new RefactorMenu();
                activeMenus.put(event.getPlayer().getUniqueId(), menu);
                menu.open(event.getPlayer());
            });
        }
    }

    private void menuForPlayer(
            @NotNull Supplier<HumanEntity> humanEntity,
            @NotNull Consumer<RefactorMenu> ifPresent)
    {
        Optional.ofNullable(activeMenus.get(humanEntity.get().getUniqueId())).ifPresent(ifPresent);
    }

    public Map<UUID, RefactorMenu> getActiveMenus() {
        return activeMenus;
    }
}

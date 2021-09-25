package de.schottky.event;

import de.schottky.core.CoreItem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

public class MobDeathListener implements Listener {

    @EventHandler
    public void onMobDeath(@NotNull EntityDeathEvent event) {
        final LivingEntity entity = event.getEntity();
        CoreItem.randomItem(entity.getType())
                .filter(item -> entity.getKiller() != null && item.shouldDropOnEntityDeath(entity.getKiller()))
                .ifPresent(item -> event.getDrops().add(item.generateItemStack()));
    }
}

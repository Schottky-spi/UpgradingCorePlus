package de.schottky.event;

import com.github.schottky.zener.api.Zener;
import com.github.schottky.zener.util.item.ItemStorage;
import de.schottky.core.UpgradableRangedWeapon;
import de.schottky.util.Timers;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.jetbrains.annotations.NotNull;

import static org.bukkit.persistence.PersistentDataType.DOUBLE;

public class ArrowShootAndHitListener implements Listener {

    private static final String HAS_DAMAGE_ATTRIBUTE_KEY = "additionalDamage";

    @EventHandler
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event){
        if (
                event.getDamager() instanceof Projectile projectile &&
                event.getEntity() instanceof Damageable entity &&
                !entity.isInvulnerable()
        ) {
            final var container = projectile.getPersistentDataContainer();
            final var damageModifier = container.get(Zener.key(HAS_DAMAGE_ATTRIBUTE_KEY), DOUBLE);
            if (damageModifier != null) {
                event.setDamage(event.getDamage() + damageModifier);
            }
        }
        if (
                event.getDamager() instanceof Projectile &&
                event.getEntity() instanceof Damageable damageable
        ) {
            Timers.runLater(1, () -> System.out.println(damageable.getHealth()));
        }
    }

    @EventHandler
    public void onArrowShoot(@NotNull EntityShootBowEvent event) {
        final var stack = event.getBow();
        final var projectile = event.getProjectile();
        if (stack == null) return;
        final var meta = stack.getItemMeta();
        ItemStorage
                .getDouble(meta, UpgradableRangedWeapon.DAMAGE_KEY)
                .ifPresent(damageModifier -> {
                    final var container = projectile.getPersistentDataContainer();
                    container.set(Zener.key(HAS_DAMAGE_ATTRIBUTE_KEY), DOUBLE, damageModifier);
                });
    }
}

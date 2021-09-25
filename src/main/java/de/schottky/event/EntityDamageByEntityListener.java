package de.schottky.event;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageByEntityListener implements Listener {
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof AbstractArrow && ((AbstractArrow)e.getDamager()).getShooter() instanceof Player
                && e.getEntity() instanceof Damageable && !((Damageable)e.getEntity()).isInvulnerable()){
            Player p= (Player) ((AbstractArrow)e.getDamager()).getShooter();
            AbstractArrow arrow = ((AbstractArrow)e.getDamager());
            if(p.getInventory().getItemInMainHand().getType() == Material.BOW
                    || p.getInventory().getItemInMainHand().getType() == Material.CROSSBOW){
                double attack_val = p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
                e.setDamage(attack_val);
                /*
                Tries to recover attack heart particle but in vain

                ((Damageable)e.getEntity()).damage(attack_val);
                EntityDamageEvent ev = new EntityDamageEvent(e.getEntity(), EntityDamageEvent.DamageCause.PROJECTILE,attack_val);
                e.getEntity().setLastDamageCause(ev);
                Bukkit.getServer().getPluginManager().callEvent(ev);
                 */
            }
        }
    }
}

package de.schottky.event;

import de.schottky.UpgradingCorePlugin;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class ArrowListener implements Listener {

    /*
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if(e.getEntity().getType() == EntityType.ARROW && e.getEntity().getShooter() instanceof Player){
            Player p= (Player) e.getEntity().getShooter();
            if(p.getInventory().getItemInMainHand().getType() == Material.BOW
              || p.getInventory().getItemInMainHand().getType() == Material.CROSSBOW){
                Arrow arrow = (Arrow) e.getEntity();
                double attack_val = p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
                arrow.setDamage(attack_val); //set damage for arrow
            }
        }
    }
    */

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e){
        if(e.getEntity().getType() == EntityType.ARROW && e.getEntity().getShooter() instanceof Player
          && e.getHitEntity()!=null && e.getHitEntity() instanceof LivingEntity && !((LivingEntity)e.getHitEntity()).isInvulnerable()
          && !e.getEntity().doesBounce()){
            Player p= (Player) e.getEntity().getShooter();
            if(p.getInventory().getItemInMainHand().getType() == Material.BOW
                    || p.getInventory().getItemInMainHand().getType() == Material.CROSSBOW){
                double attack_val = p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
                ((LivingEntity)e.getHitEntity()).setLastDamage(attack_val);
            }
        }
    }
}

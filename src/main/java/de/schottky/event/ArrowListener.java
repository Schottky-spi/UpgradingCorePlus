package de.schottky.event;

import de.schottky.UpgradingCorePlugin;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ArrowListener implements Listener {

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
}

package eu.dragoncore.itemcooldown;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by marc on 05.01.17.
 */
public class Main extends JavaPlugin implements Listener {
    Map<String, Integer> cooldowns = new HashMap<>();

    public void onEnable() {
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (String key : cooldowns.keySet()) {
                    Integer time = cooldowns.get(key);
                    if (time == 0) {
                        cooldowns.remove(key);
                    } else {
                        cooldowns.put(key, time - 1);
                    }
                }
            }
        }.runTaskTimerAsynchronously(this, 0L, 20L);
    }


    public boolean canUse(Player p, Integer itemid) {
        if (p.hasPermission(this.getConfig().getString("items." + itemid + ".bypasspermissions")))
            return true;
        String key = p.getName() + itemid;
        if (cooldowns.containsKey(key)) {
            Integer time = cooldowns.get(key);
            //Workaround for 0 Seconds left.
            if (time == 0)
                time = 1;
            p.sendMessage(colorMessage(this.getConfig().getString("items." + itemid + ".message")).replaceAll("#SECONDS#", time + ""));
            return false;
        } else {
            cooldowns.put(key, getCooldown(itemid));
            return true;
        }
    }

    public boolean hasCooldown(Integer itemid) {
        return this.getConfig().isSet("items." + itemid);
    }


    public String colorMessage(String message) {
        for (ChatColor color : ChatColor.values()) {
            message = message.replaceAll("(?i)<" + color.name() + ">", "" + color);
        }
        return message;
    }

    public Integer getCooldown(Integer itemid) {
        return this.getConfig().getInt("items." + itemid + ".cooldown");
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        Integer itemid = event.getPlayer().getInventory().getItemInMainHand().getTypeId();
        if (hasCooldown(itemid)) {
            if (!canUse(event.getPlayer(), itemid))
                event.setCancelled(true);
        } else {
            itemid = event.getPlayer().getInventory().getItemInOffHand().getTypeId();
            if (hasCooldown(itemid)) {
                if (!canUse(event.getPlayer(), itemid))
                    event.setCancelled(true);
            }
        }
    }
}

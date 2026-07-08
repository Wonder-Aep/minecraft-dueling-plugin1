package com.dueling.plugin.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import com.dueling.plugin.DuelingPlugin;
import com.dueling.plugin.commands.DuelCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * Listens for player interactions with the dueling wand
 */
public class PlayerInteractionListener implements Listener {

    private final DuelingPlugin plugin;
    private final DuelCommand duelCommand;

    public PlayerInteractionListener(DuelingPlugin plugin, DuelCommand duelCommand) {
        this.plugin = plugin;
        this.duelCommand = duelCommand;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.IRON_HOE) {
            return;
        }

        if (item.getItemMeta() == null || !item.getItemMeta().getDisplayName().contains("Dueling Wand")) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        event.setCancelled(true);

        if (event.getAction().toString().contains("LEFT")) {
            Location pos1 = event.getClickedBlock().getLocation();
            duelCommand.storePos1(player, pos1);
            player.sendMessage("§a§lPos1 set at: " + pos1.getBlockX() + ", " 
                + pos1.getBlockY() + ", " 
                + pos1.getBlockZ());
        } else if (event.getAction().toString().contains("RIGHT")) {
            Location pos2 = event.getClickedBlock().getLocation();
            duelCommand.storePos2(player, pos2);
            player.sendMessage("§a§lPos2 set at: " + pos2.getBlockX() + ", " 
                + pos2.getBlockY() + ", " 
                + pos2.getBlockZ());
        }
    }
}
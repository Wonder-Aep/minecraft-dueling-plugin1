package com.dueling.plugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.dueling.plugin.DuelingPlugin;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIManager implements Listener {

    private static GUIManager instance;
    private final DuelingPlugin plugin;
    private final Map<String, String> pendingArenaSelections = new HashMap<>();
    private final Map<String, String> pendingTimeLimitSelections = new HashMap<>();

    public GUIManager(DuelingPlugin plugin) {
        this.plugin = plugin;
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void openArenaSelectionGUI(Player player1, Player player2) {
        Inventory inv = Bukkit.createInventory(null, 27, "Select an Arena");
        int slot = 0;
        for (ArenaManager.Arena arena : instance.plugin.getArenaManager().getAllArenas().values()) {
            inv.setItem(slot++, createItemStack(Material.GRASS_BLOCK, arena.getName(), ""));
        }
        instance.pendingArenaSelections.put(player1.getUniqueId() + ":" + player2.getUniqueId(), "");
        player1.openInventory(inv);
    }

    public static void openTimeLimitGUI(Player player1, Player player2, String arenaName) {
        Inventory inv = Bukkit.createInventory(null, 9, "Select Time Limit");
        inv.setItem(2, createItemStack(Material.CLOCK, "3 Minutes", "180 seconds"));
        inv.setItem(4, createItemStack(Material.CLOCK, "5 Minutes", "300 seconds"));
        inv.setItem(6, createItemStack(Material.CLOCK, "10 Minutes", "600 seconds"));
        instance.pendingTimeLimitSelections.put(player1.getUniqueId() + ":" + player2.getUniqueId(), arenaName);
        player1.openInventory(inv);
    }

    public static void openConfirmationGUI(Player player1, Player player2, String arenaName, int timeLimit) {
        Inventory inv = Bukkit.createInventory(null, 27, "Confirm Duel");
        inv.setItem(13, createItemStack(Material.PAPER, "Duel Info", "Arena: " + arenaName + " | Time: " + timeLimit + "s"));
        inv.setItem(11, createItemStack(Material.GREEN_STAINED_GLASS_PANE, "Confirm", "Start Duel"));
        inv.setItem(15, createItemStack(Material.RED_STAINED_GLASS_PANE, "Cancel", "Cancel Duel"));
        player1.openInventory(inv);
    }

    private static ItemStack createItemStack(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Collections.singletonList(lore));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals("Select an Arena")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            String arenaName = event.getCurrentItem().getItemMeta().getDisplayName();
            for (String key : pendingArenaSelections.keySet()) {
                if (key.startsWith(player.getUniqueId().toString())) {
                    String[] parts = key.split(":");
                    Player p2 = Bukkit.getPlayer(UUID.fromString(parts[1]));
                    if (p2 != null) openTimeLimitGUI(player, p2, arenaName);
                    pendingArenaSelections.remove(key);
                }
            }
        } else if (title.equals("Select Time Limit")) {
            event.setCancelled(true);
            int time = (event.getRawSlot() == 2) ? 180 : (event.getRawSlot() == 4) ? 300 : (event.getRawSlot() == 6) ? 600 : 0;
            if (time > 0) {
                for (String key : pendingTimeLimitSelections.keySet()) {
                    if (key.startsWith(player.getUniqueId().toString())) {
                        String[] parts = key.split(":");
                        Player p2 = Bukkit.getPlayer(UUID.fromString(parts[1]));
                        if (p2 != null) openConfirmationGUI(player, p2, pendingTimeLimitSelections.get(key), time);
                        pendingTimeLimitSelections.remove(key);
                    }
                }
            }
        }
    }
}

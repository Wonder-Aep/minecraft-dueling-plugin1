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

/**
 * Manages all GUI interactions for the dueling system
 */
public class GUIManager implements Listener {

    private static final String ARENA_SELECT_PREFIX = "arena_select_";
    private static final String TIME_LIMIT_PREFIX = "time_limit_";
    private static final String CONFIRM_PREFIX = "confirm_";
    
    private static GUIManager instance;
    private DuelingPlugin plugin;
    private Map<String, String> pendingArenaSelections = new HashMap<>();
    private Map<String, String> pendingTimeLimitSelections = new HashMap<>();

    public GUIManager(DuelingPlugin plugin) {
        this.plugin = plugin;
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Opens arena selection GUI
     */
    public static void openArenaSelectionGUI(DuelingPlugin plugin, Player player1, Player player2) {
        Inventory inv = Bukkit.createInventory(null, 27, "§eSelect an Arena");

        int slot = 0;
        for (ArenaManager.Arena arena : plugin.getArenaManager().getAllArenas().values()) {
            ItemStack item = createItemStack(Material.GRASS_BLOCK, arena.getName(), "");
            inv.setItem(slot++, item);
        }

        String key = player1.getUniqueId() + ":" + player2.getUniqueId();
        instance.pendingArenaSelections.put(key, "");
        player1.openInventory(inv);
    }

    /**
     * Opens time limit selection GUI
     */
    public static void openTimeLimitGUI(DuelingPlugin plugin, Player player1, Player player2, String arenaName) {
        Inventory inv = Bukkit.createInventory(null, 9, "§eSelect Time Limit");

        ItemStack item3min = createItemStack(Material.CLOCK, "3 Minutes", "§7180 seconds");
        ItemStack item5min = createItemStack(Material.CLOCK, "5 Minutes", "§7300 seconds");
        ItemStack item10min = createItemStack(Material.CLOCK, "10 Minutes", "§7600 seconds");

        inv.setItem(2, item3min);
        inv.setItem(4, item5min);
        inv.setItem(6, item10min);

        String key = player1.getUniqueId() + ":" + player2.getUniqueId();
        instance.pendingTimeLimitSelections.put(key, arenaName);
        player1.openInventory(inv);
    }

    /**
     * Opens confirmation GUI
     */
    public static void openConfirmationGUI(DuelingPlugin plugin, Player player1, Player player2, String arenaName, int timeLimit) {
        Inventory inv = Bukkit.createInventory(null, 27, "§eConfirm Duel");

        // Display info
        ItemStack infoItem = createItemStack(Material.PAPER, "§eDuel Information", 
            "§7Player 1: " + player1.getName() + 
            "\n§7Player 2: " + player2.getName() +
            "\n§7Time Limit: " + timeLimit + "s" +
            "\n§7Ping P1: " + player1.getPing() + "ms" +
            "\n§7Ping P2: " + player2.getPing() + "ms" +
            "\n§7Arena: " + arenaName);
        
        inv.setItem(13, infoItem);

        // Confirm button (Green Glass Pane)
        ItemStack confirmItem = createItemStack(Material.GREEN_STAINED_GLASS_PANE, "§aConfirm", "§7Click to start the duel");
        inv.setItem(11, confirmItem);

        // Cancel button (Red Glass Pane)
        ItemStack cancelItem = createItemStack(Material.RED_STAINED_GLASS_PANE, "§cCancel", "§7Click to cancel");
        inv.setItem(15, cancelItem);

        player1.openInventory(inv);
    }

    /**
     * Creates an ItemStack with name and lore
     */
    private static ItemStack createItemStack(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (!lore.isEmpty()) {
                meta.setLore(Collections.singletonList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Single event handler for all GUI interactions
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (title.contains("Select an Arena")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            String arenaName = clicked.getItemMeta().getDisplayName();
            
            // Find player2 from pending selections
            for (String key : pendingArenaSelections.keySet()) {
                String[] parts = key.split(":");
                if (parts[0].equals(player.getUniqueId().toString())) {
                    Player player2 = Bukkit.getPlayer(java.util.UUID.fromString(parts[1]));
                    if (player2 != null) {
                        openTimeLimitGUI(plugin, player, player2, arenaName);
                        pendingArenaSelections.remove(key);
                    }
                    return;
                }
            }
        } 
        else if (title.contains("Select Time Limit")) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            int timeLimit = 0;

            if (slot == 2) timeLimit = 180;      // 3 minutes
            else if (slot == 4) timeLimit = 300; // 5 minutes
            else if (slot == 6) timeLimit = 600; // 10 minutes

            if (timeLimit > 0) {
                // Find player2 and arenaName from pending selections
                for (String key : pendingTimeLimitSelections.keySet()) {
                    String[] parts = key.split(":");
                    if (parts[0].equals(player.getUniqueId().toString())) {
                        Player player2 = Bukkit.getPlayer(java.util.UUID.fromString(parts[1]));
                        String arenaName = pendingTimeLimitSelections.get(key);
                        if (player2 != null) {
                            openConfirmationGUI(plugin, player, player2, arenaName, timeLimit);
                            pendingTimeLimitSelections.remove(key);
                        }
                        return;
                    }
                }
            }
        }
        else if (title.contains("Confirm Duel")) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            
            if (slot == 11 || slot == 15) {
                // Get both players from the inventory - this is tricky, so we'll need to track it
                // For now, just close and cancel
                player.closeInventory();
            }
        }
    }
}

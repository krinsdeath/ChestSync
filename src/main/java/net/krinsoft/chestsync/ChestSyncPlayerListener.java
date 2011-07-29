package net.krinsoft.chestsync;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkitcontrib.block.ContribChest;
import org.bukkitcontrib.player.ContribPlayer;

/**
 * @author dumptruckman
 */
public class ChestSyncPlayerListener extends PlayerListener {

    private final ChestSync plugin;

	ChestSyncPlayerListener(ChestSync aThis) {
		plugin = aThis;
	}

    public void onPlayerInteract(PlayerInteractEvent event) {
        // Throw out all the useless events
        if (event.isCancelled()) return;
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType() != Material.CHEST) return;

        Location loc = event.getClickedBlock().getLocation();
        if (loc == null) return;

        // Now get down to business
        SyncedChest chest = SyncedChest.getSyncedChest(loc);
        if (chest == null) {
            try {
                if (((ContribChest)event.getClickedBlock().getState()).isDoubleChest()) {
                    chest = SyncedChest.getSyncedChest(((ContribChest)event.getClickedBlock().getState()).getOtherSide().getBlock().getLocation());
                }
            } catch (NullPointerException e) {}
        }
        if (chest != null) {
            Player player = event.getPlayer();
            if (!checkPermission(player, "access", chest.getName())) {
                player.sendMessage(ChatColor.RED + "You do not have permission to access this Synced Chest");
                ((ContribPlayer)player).closeActiveWindow();
                event.setCancelled(true);
                return;
            }
            event.getPlayer().sendMessage(ChatColor.RED + "This chest is currently being used.");
            event.setCancelled(true);
            ((ContribPlayer)player).openInventoryWindow(chest.getNetworkInventory());
        }
    }

    private boolean checkPermission(Player player, String field, String name) {
		boolean perm = false;
		if (player.hasPermission("chestsync." + field)) {
			if (player.getName().equalsIgnoreCase(name) && player.hasPermission("chestsync." + field + ".self")) {
				perm = true;
			} else if (player.hasPermission("chestsync." + field + "." + name)) {
				perm = true;
			} else if(player.hasPermission("chestsync." + field + ".*")) {
				perm = true;
			} else {
				perm = false;
			}
		}
		return perm;
	}
}

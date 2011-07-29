package net.krinsoft.chestsync;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkitcontrib.block.ContribChest;
import org.bukkitcontrib.event.inventory.InventoryCloseEvent;
import org.bukkitcontrib.event.inventory.InventoryListener;
import org.bukkitcontrib.event.inventory.InventoryOpenEvent;
import org.bukkitcontrib.player.ContribPlayer;

public class ChestSyncInventoryListener extends InventoryListener {
	private final ChestSync plugin;

	ChestSyncInventoryListener(ChestSync aThis) {
		plugin = aThis;
	}

	@Override
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (event.getLocation() != null) {
			SyncedChest chest = SyncedChest.getSyncedChest(event.getLocation());
			if (chest == null) {
				try {
					if (((ContribChest)event.getLocation().getBlock().getState()).isDoubleChest()) {
						chest = SyncedChest.getSyncedChest(((ContribChest)event.getLocation().getBlock().getState()).getOtherSide().getBlock().getLocation());
					}
				} catch (NullPointerException e) {}
			}
			if (chest != null) {
				if (!chest.isInUse()) {
					Player player = event.getPlayer();
					if (!checkPermission(player, "access", chest.getName())) {
						player.sendMessage(ChatColor.RED + "You do not have permission to access this Synced Chest");
						((ContribPlayer)player).closeActiveWindow();
						event.setCancelled(true);
						return;
					}
					chest.setInUse(true);
				} else {
					event.getPlayer().sendMessage(ChatColor.RED + "This chest is currently being used.");
					event.setCancelled(true);
				}
			}
		}
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (event.getLocation() != null) {
			SyncedChest chest = SyncedChest.getSyncedChest(event.getLocation());
			if (chest == null) {
				try {
					if (((ContribChest)event.getLocation().getBlock().getState()).isDoubleChest()) {
						chest = SyncedChest.getSyncedChest(((ContribChest)event.getLocation().getBlock().getState()).getOtherSide().getBlock().getLocation());
					}
				} catch (NullPointerException e) {}
			}
			if (chest != null) {
				chest.setInUse(false);
				chest.updateInventories();
			}
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

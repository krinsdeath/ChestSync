package net.krinsoft.chestsync;

import org.bukkit.ChatColor;
import org.bukkitcontrib.event.inventory.InventoryCloseEvent;
import org.bukkitcontrib.event.inventory.InventoryListener;
import org.bukkitcontrib.event.inventory.InventoryOpenEvent;

public class ChestSyncInventoryListener extends InventoryListener{
	private final ChestSync plugin;

	ChestSyncInventoryListener(ChestSync aThis) {
		plugin = aThis;
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (event.getLocation() != null) {
			SyncedChest chest = SyncedChest.getSyncedChest(event.getLocation());
			if (chest != null) {
				chest.setInUse(false);
				chest.update();
			}
		}
	}

	@Override
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (event.getLocation() != null) {
			SyncedChest chest = SyncedChest.getSyncedChest(event.getLocation());
			if (chest != null) {
				if (!chest.syncedChestsInUse()) {
					boolean perm = false;
					if (event.getPlayer().hasPermission("chestsync.access")) {
						if (chest.getName().equalsIgnoreCase(event.getPlayer().getName()) && event.getPlayer().hasPermission("chestsync.access.self")) {
							perm = true;
						} else if (event.getPlayer().hasPermission("chestsync.access." + chest.getName())) {
							perm = true;
						} else if (event.getPlayer().hasPermission("chestsync.access.*")) {
							perm = true;
						} else {
							perm = false;
						}
					}
					if (!perm) {
						event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to access this Synced Chest");
						event.setCancelled(true);
						return;
					}
					chest.setInUse(true);
				} else {
					event.setCancelled(true);
					event.getPlayer().sendMessage(ChatColor.RED + "This chest is currently being used");
				}
			}
		}
	}
}

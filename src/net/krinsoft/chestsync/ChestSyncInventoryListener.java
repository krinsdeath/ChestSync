package net.krinsoft.chestsync;

import org.bukkit.ChatColor;
import org.bukkitcontrib.event.inventory.InventoryCloseEvent;
import org.bukkitcontrib.event.inventory.InventoryListener;
import org.bukkitcontrib.event.inventory.InventoryOpenEvent;

public class ChestSyncInventoryListener extends InventoryListener{

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
					chest.setInUse(true);
				} else {
					event.setCancelled(true);
					event.getPlayer().sendMessage(ChatColor.RED + "This chest is currently being used");
				}
			}
		}
	}
}

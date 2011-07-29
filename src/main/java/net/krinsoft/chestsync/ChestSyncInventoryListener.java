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


	
}

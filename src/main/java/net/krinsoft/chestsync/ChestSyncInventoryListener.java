package net.krinsoft.chestsync;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
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
		ContribPlayer player = (ContribPlayer) event.getPlayer();
		player.openInventoryWindow(SyncedChest.getSyncedInventory(event.getLocation()));
	}
}

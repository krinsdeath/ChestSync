package net.krinsoft.chestsync;

import org.bukkit.Location;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.getspout.spoutapi.block.SpoutChest;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 *
 * @author krinsdeath
 */

class PlayerListener extends org.bukkit.event.player.PlayerListener {
	private ChestSync plugin;

	public PlayerListener(ChestSync aThis) {
		plugin = aThis;
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled()) { return; }
		if (event.getClickedBlock() == null) { return; }

		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) { return; }

		if (event.getClickedBlock().getState() instanceof SpoutChest) {
			Location l = event.getClickedBlock().getLocation();
			SyncedChest c = SyncedChest.getSyncedChest(l);
			if (c != null) {
				if (!Utility.checkPermission(event.getPlayer(), "access", c.getNetwork())) {
					Utility.error(event.getPlayer(), "permission");
					event.setCancelled(true);
					return;
				}
				((SpoutPlayer)event.getPlayer()).openInventoryWindow(SyncedChest.getInventory(c.getNetwork()), c.getLocation());
				event.setCancelled(true);
			}
		}
	}

}

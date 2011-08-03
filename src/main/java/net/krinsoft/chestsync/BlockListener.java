package net.krinsoft.chestsync;

import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

/**
 *
 * @author krinsdeath
 */

class BlockListener extends org.bukkit.event.block.BlockListener {
	private ChestSync plugin;

	public BlockListener(ChestSync aThis) {
		plugin = aThis;
	}

	@Override
	public void onSignChange(SignChangeEvent event) {
		if (event.isCancelled()) { return; }

		if (event.getLine(0).equalsIgnoreCase("synced chest")) {
			if (!event.getLine(1).isEmpty()) {
				final String network = event.getLine(1);
				if (!checkPermission(event.getPlayer(), "make", event.getLine(1))) {
					event.setLine(0, "[error make]");
					return;
				}
				Sign sign = (Sign) event.getBlock().getState();
				org.bukkit.material.Sign aSign = (org.bukkit.material.Sign) sign.getData();
				Location loc = event.getBlock().getRelative(aSign.getFacing().getOppositeFace()).getLocation();
				if (loc.getBlock().getState() instanceof Chest) {
					if (SyncedChest.addSyncedChest(event.getLine(1), loc, sign.getBlock().getLocation())) {
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							@Override
							public void run() {
								SyncedChest.synchronize(network);
							}
						}, 2L);
						return;
					} else {
						event.setLine(0, "[error exists]");
						return;
					}
				} else {
					event.setLine(0, "[no chest]");
					return;
				}
			}
		}
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event) {

	}


	public boolean checkPermission(Player p, String field, String key) {
		if (p.getName().equalsIgnoreCase(key) && p.hasPermission("chestsync." + field + ".self")) {
			return true;
		}
		if (p.hasPermission("chestsync." + field + "." + key)) {
			return true;
		}
		if (p.hasPermission("chestsync." + field + ".*")) {
			return true;
		}
		return false;
	}
}

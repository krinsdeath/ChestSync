package net.krinsoft.chestsync;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;
import org.getspout.spoutapi.block.SpoutBlock;

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
				if (!Utility.checkPermission(event.getPlayer(), "make", event.getLine(1))) {
					event.setLine(0, Utility.color("&C[error]"));
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
						}, 5);
						return;
					} else {
						event.setLine(0, Utility.color("&C[error]"));
						return;
					}
				} else {
					event.setLine(0, Utility.color("&C[no chest]"));
					return;
				}
			}
		}
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) { return; }
		SpoutBlock b = (SpoutBlock) event.getBlock();
		if (b.getState() instanceof Sign) {
			MaterialData m = b.getState().getData();
			BlockFace f = BlockFace.DOWN;
			if (m instanceof Attachable) {
				f = ((Attachable) m).getAttachedFace();
			}
			if (f == null) { System.out.println("wtf"); return; }
			boolean REMOVE = true;
			SpoutBlock behind = b.getRelative(f);
			SyncedChest c = SyncedChest.getSyncedChest(behind.getLocation());
			if (c != null) {
				final String net = c.getNetwork();
				if (!Utility.checkPermission(event.getPlayer(), "destroy", c.getNetwork())) {
					Utility.error(event.getPlayer(), "permission");
					event.setCancelled(true);
					REMOVE = false;
				}
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					@Override
					public void run() {
						SyncedChest.synchronize(net);
					}
				}, 5);
				if (REMOVE) {
					SyncedChest.removeSyncedChest(c.getNetwork(), c.getLocation());
				}
			}
		}
		if (b.getState() instanceof Chest) {
			boolean REMOVE = true;
			SyncedChest c = SyncedChest.getSyncedChest(b.getLocation());
			if (c != null) {
				final String net = c.getNetwork();
				if (!Utility.checkPermission(event.getPlayer(), "destroy", c.getNetwork())) {
					Utility.error(event.getPlayer(), "permission");
					event.setCancelled(true);
					REMOVE = false;
				}
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					@Override
					public void run() {
						SyncedChest.synchronize(net);
					}
				}, 5);
				if (REMOVE) {
					SyncedChest.removeSyncedChest(c.getNetwork(), c.getLocation());
				}
			}
		}
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) { return; }
		if (!event.canBuild()) { return; }

		if (event.getBlock().getState() instanceof Chest) {
			if (SyncedChest.getSyncedChest(event.getBlock().getLocation()) != null) {

				event.setCancelled(true);
			}
		}
	}

}

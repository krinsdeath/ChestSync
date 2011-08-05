package net.krinsoft.chestsync;

import org.bukkit.Location;
import org.bukkit.block.Block;
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
				Block behind = event.getBlock().getRelative(aSign.getFacing().getOppositeFace());
				if (behind.getState() instanceof Chest) {
					if (SyncedChest.addSyncedChest(event.getLine(1), behind.getLocation(), sign.getBlock().getLocation())) {
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							@Override
							public void run() {
								SyncedChest.synchronize(network);
							}
						}, 2);
						event.getPlayer().sendMessage(Utility.color("&ESynced Chest created."));
						return;
					} else {
						event.getPlayer().sendMessage(Utility.color("&CA Synced Chest may already exist at this location."));
						event.setLine(0, Utility.color("&C[error]"));
						return;
					}
				} else {
					event.getPlayer().sendMessage(Utility.color("&CThere's no chest to sync at this location."));
					event.setLine(0, Utility.color("&C[No chest.]"));
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
				}, 2);
				if (REMOVE) {
					SyncedChest.removeSyncedChest(c.getNetwork(), c.getLocation());
				}
			}
		}
		if (b.getState() instanceof Chest) {
			SyncedChest c = SyncedChest.getSyncedChest(b.getLocation());
			if (c != null) {
				event.setCancelled(true);
			}
		}
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		if (block.getState() instanceof Chest) {
			if (SyncedChest.getSyncedChest(block.getRelative(BlockFace.NORTH).getLocation()) != null) {
				event.getPlayer().sendMessage(Utility.color("&CThis network requires single chests."));
				event.setCancelled(true);
			}
			if (SyncedChest.getSyncedChest(block.getRelative(BlockFace.SOUTH).getLocation()) != null) {
				event.getPlayer().sendMessage(Utility.color("&CThis network requires single chests."));
				event.setCancelled(true);
			}
			if (SyncedChest.getSyncedChest(block.getRelative(BlockFace.EAST).getLocation()) != null) {
				event.getPlayer().sendMessage(Utility.color("&CThis network requires single chests."));
				event.setCancelled(true);
			}
			if (SyncedChest.getSyncedChest(block.getRelative(BlockFace.WEST).getLocation()) != null) {
				event.getPlayer().sendMessage(Utility.color("&CThis network requires single chests."));
				event.setCancelled(true);
			}
		}
	}

}

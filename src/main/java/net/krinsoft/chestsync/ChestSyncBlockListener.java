package net.krinsoft.chestsync;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkitcontrib.block.ContribChest;

public class ChestSyncBlockListener extends BlockListener{
	private final ChestSync plugin;

	ChestSyncBlockListener(ChestSync aThis) {
		plugin = aThis;
	}

	@Override
	public void onSignChange(SignChangeEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (event.getLine(0).toLowerCase().contains("synced chest")) {
			Sign sign = (Sign)event.getBlock().getState();
			org.bukkit.material.Sign data = (org.bukkit.material.Sign)sign.getData();
			Block behind = event.getBlock().getFace(data.getFacing().getOppositeFace());
			if (behind.getState() instanceof Chest) {
				Player player = event.getPlayer();
				String name = event.getLine(1);
				if (name.equalsIgnoreCase("")) {
					player.sendMessage(ChatColor.RED + "Synced Chests require a name on line 2");
					sign.setLine(0, "&C[error]".replaceAll("&([a-fA-F0-9])", "\u00A7$1"));
					sign.update();
					event.setCancelled(true);
					return;
				}
				if (!checkPermission(player, "make", name)) {
					player.sendMessage(ChatColor.RED + "You do not have permission to make this Synced Chest");
					sign.setLine(0, "&C[error]".replaceAll("&([a-fA-F0-9])", "\u00A7$1"));
					sign.update();
					event.setCancelled(true);
					return;
				}
				if (SyncedChest.hasSyncedChest(behind.getLocation())) {
					player.sendMessage(ChatColor.RED + "A Synced Chest already exists at this location");
					sign.setLine(0, "&C[error]".replaceAll("&([a-fA-F0-9])", "\u00A7$1"));
					sign.update();
					event.setCancelled(true);
					return;
				}
				try {
					if (SyncedChest.syncedChests.get(name).getFirst().isDouble()) {
						if (!((ContribChest)behind.getState()).isDoubleChest()) {
							player.sendMessage(ChatColor.RED + "Synced Chests on this network are double chests");
							sign.setLine(0, "&C[error]".replaceAll("&([a-fA-F0-9])", "\u00A7$1"));
							sign.update();
							event.setCancelled(true);
							return;
						}
						if (SyncedChest.chests.containsKey(((ContribChest)behind.getState()).getOtherSide().getBlock().getLocation())) {
							player.sendMessage(ChatColor.RED + "A Synced Chest already exists at this location");
							sign.setLine(0, "&C[error]".replaceAll("&([a-fA-F0-9])", "\u00A7$1"));
							sign.update();
							event.setCancelled(true);
							return;
						}
					}
				} catch (NullPointerException e) {}
				SyncedChest.createSyncedChest(event.getBlock().getLocation(), behind.getLocation(), name);
				player.sendMessage(ChatColor.YELLOW + "Synced Chest created.");
				String line0 = "", line2 = "";
				if (SyncedChest.syncedChests.get(name).size() == 1) {
					line0 = "&C[Synced]";
					line2 = "&C[no link]";
				} else {
					line0 = "&A[Synced]";
				}
				line0 = line0.replaceAll("&([a-fA-F0-9])", "\u00A7$1");
				line2 = line2.replaceAll("&([a-fA-F0-9])", "\u00A7$1");
				event.setLine(0, line0);
				event.setLine(2, line2);
				event.setLine(3, "[" + SyncedChest.syncedChests.get(name).size() + "]");
				return;
			} else {
				event.getPlayer().sendMessage(ChatColor.RED + "There's no chest there!");
				sign.setLine(0, "[no chest]");
				sign.update();
				event.setCancelled(true);
				return;
			}
		}
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()){
			return;
		}
		Block block = event.getBlock();
		if (block.getState() instanceof Sign) {
			final Sign sign = (Sign)block.getState();
			if (sign.getLine(0).toLowerCase().contains("[synced]")) {
				if (!checkPermission(event.getPlayer(), "destroy", sign.getLine(1))) {
					event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to destroy this chest.");
					event.setCancelled(true);
					event.getPlayer().getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
						@Override
						public void run() {
							sign.update();
						}
					}, 2L);
					return;
				}
				org.bukkit.material.Sign data = (org.bukkit.material.Sign)sign.getData();
				Block behind = block.getFace(data.getFacing().getOppositeFace());
				SyncedChest chest = SyncedChest.getSyncedChest(behind.getLocation());
				if (chest != null) {
					SyncedChest.removeSyncedChest(behind.getLocation());
					event.getPlayer().sendMessage(ChatColor.YELLOW + "Synced Chest removed");
				}
			}
		} else if (block.getState() instanceof Chest) {
			SyncedChest chest = SyncedChest.getSyncedChest(block.getLocation());
			if (chest == null) {
				try {
					if (((ContribChest)block.getState()).isDoubleChest()) {
						chest = SyncedChest.getSyncedChest(((ContribChest)block.getState()).getOtherSide().getBlock().getLocation());
					}
				} catch (NullPointerException e) {}
			}
			if (chest != null) {
				if (!checkPermission(event.getPlayer(), "destroy", chest.getName())) {
					event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to destroy this chest.");
					event.setCancelled(true);
					return;
				}
				SyncedChest.removeSyncedChest(chest.getLocation());
				event.getPlayer().sendMessage(ChatColor.YELLOW + "Synced Chest removed");
			}
		}
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		if (block.getState() instanceof Chest) {
			if (SyncedChest.chests.containsKey(block.getRelative(BlockFace.NORTH).getLocation())) {
				if (!SyncedChest.getSyncedChest(block.getRelative(BlockFace.NORTH).getLocation()).isDouble()) {
					event.getPlayer().sendMessage(ChatColor.RED + "Synced Chests on this network can only be single.");
					event.setCancelled(true);
				}
			}
			if (SyncedChest.chests.containsKey(block.getRelative(BlockFace.EAST).getLocation())) {
				if (!SyncedChest.getSyncedChest(block.getRelative(BlockFace.EAST).getLocation()).isDouble()) {
					event.getPlayer().sendMessage(ChatColor.RED + "Synced Chests on this network can only be single.");
					event.setCancelled(true);
				}
			}
			if (SyncedChest.chests.containsKey(block.getRelative(BlockFace.SOUTH).getLocation())) {
				if (!SyncedChest.getSyncedChest(block.getRelative(BlockFace.SOUTH).getLocation()).isDouble()) {
					event.getPlayer().sendMessage(ChatColor.RED + "Synced Chests on this network can only be single.");
					event.setCancelled(true);
				}
			}
			if (SyncedChest.chests.containsKey(block.getRelative(BlockFace.WEST).getLocation())) {
				if (!SyncedChest.getSyncedChest(block.getRelative(BlockFace.WEST).getLocation()).isDouble()) {
					event.getPlayer().sendMessage(ChatColor.RED + "Synced Chests on this network can only be single.");
					event.setCancelled(true);
				}
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

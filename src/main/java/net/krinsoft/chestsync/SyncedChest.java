package net.krinsoft.chestsync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkitcontrib.block.ContribChest;
import org.bukkitcontrib.inventory.ContribInventory;

public class SyncedChest implements Serializable {
	/*
	 *
	 * Class members
	 *
	 */

	/**
	 * Serializable Version ID
	 */
	private static final long serialVersionUID = 32L;
	/**
	 * A HashMap containing names -> Synced Chests
	 */
	protected static HashMap<String, LinkedList<SyncedChest>> syncedChests = new HashMap<String, LinkedList<SyncedChest>>();
	/**
	 * A HashMap containing locations mapped to Synced Chests
	 */
	protected static HashMap<Location, SyncedChest> chests = new HashMap<Location, SyncedChest>();
	/**
	 * A Static reference to the ChestSync plugin
	 */
	protected static ChestSync plugin;


	/**
	 * Attempts to create a Synced Chest with the given parameters
	 * @param sign
	 * The location of the sign that initiated the Sync
	 * @param chest
	 * The location of the chest indicated by the sign
	 * @param name
	 * The second line of the Sign's text
	 */
	public static void createSyncedChest(Location sign, Location chest, String name) {
		if (chest.getBlock().getState() instanceof Chest && name.length() > 0) {
			if (chests.get(chest) != null) {
				return;
			}
			LinkedList<SyncedChest> list = null;
			SyncedChest c = new SyncedChest(sign, chest, name);
			if (syncedChests.containsKey(name)) {
				list = syncedChests.get(name);
				list.add(c);
				list.getFirst().updateInventories();
				for (SyncedChest in : list) {
					Location loc = new Location(sign.getWorld(), in.signx, in.signy, in.signz);
					Block tmp = loc.getBlock();
					if (tmp.getState() instanceof Sign) {
						Sign the = (Sign) tmp.getState();
						String line0 = "&A[Synced]".replaceAll("&([a-fA-F0-9])", "\u00A7$1");
						the.setLine(0, line0);
						the.setLine(1, name);
						the.setLine(2, "");
						the.setLine(3, "[" + list.size() + "]");
						the.update();
					}
				}
			} else {
				list = new LinkedList<SyncedChest>();
				list.add(c);
				String line0 = "&C[Synced]".replaceAll("&([a-fA-F0-9])", "\u00A7$1");
				String line2 = "&C[no link]".replaceAll("&([a-fA-F0-9])", "\u00A7$1");
				Sign tmp = ((Sign)sign.getBlock().getState());
				tmp.setLine(0, line0);
				tmp.setLine(1, name);
				tmp.setLine(2, line2);
				tmp.setLine(3, "[" + list.size() + "]");
				tmp.update();
			}
			syncedChests.put(name, list);
			chests.put(chest, c);
		}
	}

	/**
	 * Removes the Synced Chest at the location provided
	 * @param chest
	 */
	public static void removeSyncedChest(Location chest) {
		SyncedChest c = chests.get(chest);
		LinkedList<SyncedChest> list = syncedChests.get(c.getName());
		list.remove(c);
		chests.remove(chest);
		c.error("no chest");
		if (list.size() >= 1) {
			if (c.isDouble()) {
				c.getLeftSideInventory().clear();
				c.getRightSideInventory().clear();
			} else {
				c.getInventory().clear();
			}
			syncedChests.put(c.getName(), list);
			c.updateSigns();
		} else {
			syncedChests.remove(c.getName());
		}
	}

	/**
	 * Gets whether a Synced Chest exists at this location
	 * @param chest
	 * The location of the chest to check
	 * @return
	 * true if one exists, otherwise false
	 */
	public static boolean hasSyncedChest(Location chest) {
		return chests.containsKey(chest);
	}

	/**
	 * Fetches the Synced Chest at the location provided
	 * @param chest
	 * The location of the chest to fetch
	 * @return
	 * The Synced Chest at the location provided
	 */
	public static SyncedChest getSyncedChest(Location chest) {
		return chests.get(chest);
	}

	/*
	 *
	 * Instance fields
	 *
	 */

	/*
	 * Synced Chest Locations (Including Double Chest)
	 */
	private double fx;		// First  X
	private double sx;		// Second X
	// ---------------------
	private double fy;		// First  Y
	private double sy;		// Second Y
	// ---------------------
	private double fz;		// First  Z
	private double sz;		// Second Z
	// ---------------------

	/*
	 * Sign location relative to this Synced Chest
	 */
	private double signx;	// Sign X
	private double signy;	// Sign Y
	private double signz;	// Sign Z
	// ---------------------
	/*
	 * Name of the Sync, and the World name
	 */
	private String name;
	private String world;
	/*
	 * Whether this chest is a double chest
	 */
	private boolean isDouble;
	/*
	 * Whether this chest is currently in use (to prevent duping)
	 */
	private boolean inUse;

	/**
	 * Instance constructor
	 * @param sign
	 * The location of the sign that created the link
	 * @param chest
	 * The location of the chest relative to the sign
	 * @param name
	 * The name on the second line of the sign
	 */
	protected SyncedChest(Location sign, Location chest, String name) {
		buildSignLocation(sign);
		buildChestLocation(chest);
		this.name = name;
		this.world = chest.getWorld().getName();
	}

	/**
	 * Builds the sign's location relative to this Synced Chest
	 * @param sign
	 * The sign's location
	 */
	private void buildSignLocation(Location sign) {
		this.signx = sign.getX();
		this.signy = sign.getY();
		this.signz = sign.getZ();
	}

	/**
	 * Builds the chest's location, including any double chest (if relevant)
	 * @param chest
	 * The chest's location
	 */
	private void buildChestLocation(Location chest) {
		ContribChest c = (ContribChest) chest.getBlock().getState();
		// build X locations
		this.fx = chest.getX();
		this.sx = (c.isDoubleChest()) ? c.getOtherSide().getX() : 0;
		// build Y locations
		this.fy = chest.getY();
		this.sy = (c.isDoubleChest()) ? c.getOtherSide().getY() : 0;
		// build Z locations
		this.fz = chest.getZ();
		this.sz = (c.isDoubleChest()) ? c.getOtherSide().getZ() : 0;
		this.isDouble = c.isDoubleChest();
	}

	/**
	 * Gets this chest's inventory
	 * @return
	 * the inventory
	 */
	private ContribInventory getInventory() {
		return ((ContribChest)getLocation().getBlock().getState()).getLargestInventory();
	}

	private ContribInventory getLeftSideInventory() {
		return ((ContribChest)getLeftLocation().getBlock().getState()).getInventory();
	}

	private ContribInventory getRightSideInventory() {
		return ((ContribChest)getRightLocation().getBlock().getState()).getInventory();
	}

	/**
	 * Updates the inventories of all chests on this network
	 */
	public void updateInventories() {
		LinkedList<SyncedChest> list = syncedChests.get(name);
		Iterator<SyncedChest> i = list.iterator();
		while (i.hasNext()) {
			SyncedChest chest = i.next();
			if (!chest.getLocation().equals(getLocation())) {
				if (!(chest.getLocation().getBlock().getState() instanceof Chest)) {
					i.remove();
				} else {
					try {
						if (chest.isDouble() && this.isDouble()) {
							chest.getLeftSideInventory().setContents(getLeftSideInventory().getContents());
							chest.getRightSideInventory().setContents(getRightSideInventory().getContents());
						} else {
							chest.getInventory().setContents(getInventory().getContents());
						}
					} catch (NullPointerException e) {
					}
				}
			}
		}
	}

	/**
	 * Displays an error message on the sign
	 */
	private void error(String field) {
		Sign sign = (Sign) getSign().getBlock().getState();
		String line0 = "", line1 = "", line2 = "", line3 = "";
		if (field.equalsIgnoreCase("no link")) {
			line0 = "&C[Synced]";
			line1 = name;
			line2 = "&C[no link]";
			line3 = "[" + syncedChests.get(name).size() + "]";
		} else if (field.equalsIgnoreCase("no chest")) {
			line0 = "&C[no chest]";
			line1 = "";
			line2 = "";
			line3 = "";
		} else if (field.equalsIgnoreCase("error")) {
			line0 = "&C[error]";
			line1 = "";
			line2 = "";
			line3 = "";
		}
		line0 = line0.replaceAll("&([a-fA-F0-9])", "\u00A7$1");
		line1 = line1.replaceAll("&([a-fA-F0-9])", "\u00A7$1");
		line2 = line2.replaceAll("&([a-fA-F0-9])", "\u00A7$1");
		line3 = line3.replaceAll("&([a-fA-F0-9])", "\u00A7$1");
		sign.setLine(0, line0);
		sign.setLine(1, line1);
		sign.setLine(2, line2);
		sign.setLine(3, line3);
		sign.update(true);
	}

	/**
	 * Attempts to update all signs on this Synced Chest's network
	 */
	private void updateSigns() {
		Location loc = null;
		Sign sign = null;
		LinkedList<SyncedChest> list = syncedChests.get(name);
		String line0 = "", line2 = "";
		if (list.size() > 1) {
			line0 = "&A[Synced]".replaceAll("&([a-fA-F0-9])", "\u00A7$1");
			line2 = "";
		} else {
			line0 = "&C[Synced]".replaceAll("&([a-fA-F0-9])", "\u00A7$1");
			line2 = "&C[no link]".replaceAll("&([a-fA-F0-9])", "\u00A7$1");
		}
		for (SyncedChest chest : list) {
			loc = new Location(getWorld(), chest.signx, chest.signy, chest.signz);
			sign = (Sign) loc.getBlock().getState();
			sign.setLine(0, line0);
			sign.setLine(1, name);
			sign.setLine(2, line2);
			sign.setLine(3, "[" + list.size() + "]");
			sign.update(true);
		}
	}

	/**
	 * Gets whether this chest is currently being accessed
	 */
	public boolean isInUse() {
		for (SyncedChest chest : syncedChests.get(name)) {
			if (chest.inUse) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets whether this chest is currently being accessed
	 */
	public void setInUse(boolean flag) {
		this.inUse = flag;
	}

	/**
	 * Returns the name of this Synced Chest
	 * @return
	 * the name of this Synced Chest
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Saves the Synced Chests to disk
	 */
	public static void save() {
		FileOutputStream file = null;
		ObjectOutputStream out = null;
		File tmp = new File("plugins/ChestSync/chests.dat");
		try {
			if (!tmp.exists()) {
				tmp.getParentFile().mkdirs();
				tmp.createNewFile();
			}
			if (tmp.length() > 0) {
				tmp.delete();
				tmp.createNewFile();
			}
			file = new FileOutputStream(tmp);
			out = new ObjectOutputStream(file);
			out.writeObject(syncedChests);
			out.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Loads the Synced Chests from disk
	 * @param plugin
	 * Sets up a static reference for the Synced Chests to use (for logging)
	 */
	public static void load(ChestSync instance) {
		plugin = instance;
		HashMap<String, LinkedList<SyncedChest>> temp = new HashMap<String, LinkedList<SyncedChest>>();
		FileInputStream file = null;
		ObjectInputStream in = null;
		File tmp = new File("plugins/ChestSync/chests.dat");
		try {
			if (!tmp.exists()) {
				tmp.getParentFile().mkdirs();
				tmp.createNewFile();
			}
			file = new FileInputStream(tmp);
			in = new ObjectInputStream(file);
			temp = (HashMap<String, LinkedList<SyncedChest>>) in.readObject();
			in.close();
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
		}
		// build the lists again
		for (String key : temp.keySet()) {
			for (SyncedChest chest : temp.get(key)) {
				Location loc = new Location(plugin.getServer().getWorld(chest.world), chest.fx, chest.fy, chest.fz);
				Location sign = new Location(plugin.getServer().getWorld(chest.world), chest.signx, chest.signy, chest.signz);
				createSyncedChest(sign, loc, chest.name);
			}
		}
	}

	/**
	 * Gets the world associated with this Synced Chest
	 * @return
	 * the world
	 */
	public World getWorld() {
		return plugin.getServer().getWorld(world);
	}

	/**
	 * Gets the location of this Synced Chest
	 * @return
	 * the location of this chest
	 */
	public Location getLocation() {
		return new Location(getWorld(), fx, fy, fz);
	}

	private Location getLeftLocation() {
		return new Location(getWorld(), fx, fy, fz);
	}

	private Location getRightLocation() {
		return new Location(getWorld(), sx, sy, sz);
	}

	/**
	 * Gets the location of the sign used to create this Synced Chest
	 * @return
	 * the sign's location
	 */
	public Location getSign() {
		return new Location(getWorld(), signx, signy, signz);
	}

	boolean isDouble() {
		return isDouble;
	}

}

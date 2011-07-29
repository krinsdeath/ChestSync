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
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkitcontrib.block.ContribChest;

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
		this.world = sign.getWorld().getName();
		populateMappings();
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
	 * Adds this instance of a Synced Chest to the Static HashMaps
	 */
	private void populateMappings() {
		Location loc = new Location(plugin.getServer().getWorld(world), fx, fy, fz);
		if (chests.containsKey(loc)) {
			LinkedList<SyncedChest> list = syncedChests.get(name);
			if (list.contains(chests.get(loc))) {
				return;
			} else {
				list.add(this);
				if (list.size() == 1) {
					error("no link");
				} else {
					updateSigns();
				}
			}
			chests.put(loc, this);
			syncedChests.put(name, list);
		}
	}

	/**
	 * Displays an error message on the sign
	 */
	private void error(String field) {
		if (field.equalsIgnoreCase("no link")) {

		} else if (field.equalsIgnoreCase("no chest")) {

		} else if (field.equalsIgnoreCase("error")) {

		}
	}

	/**
	 * Attempts to update all signs on this Synced Chest's network
	 */
	private void updateSigns() {
		Location loc = null;
		Sign sign = null;
		LinkedList<SyncedChest> list = syncedChests.get(name);
		for (SyncedChest chest : list) {
			loc = new Location(plugin.getServer().getWorld(world), chest.signx, chest.signy, chest.signz);
			sign = (Sign) loc.getBlock().getState();
			sign.setLine(0, "&A[Synced]".replaceAll("&([a-fA-F0-9])", "\u00A7$1"));
			sign.setLine(1, name);
			sign.setLine(2, "");
			sign.setLine(3, "[" + list.size() + "]");
			sign.update(true);
		}
	}

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

	}

	public static boolean hasSyncedChest(Location chest) {
		return chests.containsKey(chest);
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
}

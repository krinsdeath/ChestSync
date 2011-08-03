package net.krinsoft.chestsync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;
import org.getspout.spoutapi.block.SpoutChest;

/**
 *
 * @author krinsdeath
 */

public class SyncedChest implements Serializable {
	private static boolean debug = false;

	/**
	 * Serializable version ID
	 */
	private static final long serialVersionUID = 11931L;

	/**
	 * Chest Mappings, key = network name
	 */
	protected static HashMap<String, LinkedList<SyncedChest>> networks = new HashMap<String, LinkedList<SyncedChest>>();

	/**
	 * A List of all the Synced Chest locations
	 */
	protected static List<SyncedChest> chests = new ArrayList<SyncedChest>();

	/**
	 * Static plugin reference
	 */
	protected static ChestSync plugin;

	/**
	 * Serializes the 'chests' HashMap
	 */
	protected static void save() {
		File file = new File("plugins/ChestSync/chests.dat");
		FileOutputStream out = null;
		ObjectOutputStream obj = null;
		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			} else {
				file.delete();
				file.createNewFile();
			}
			out = new FileOutputStream(file);
			obj = new ObjectOutputStream(out);
			obj.writeObject(networks);
			obj.flush();
		} catch (IOException e) {
			System.out.println("Error with file " + file.getName() + "... " + e);
		} finally {
			try {
				obj.close();
				out.close();
			} catch (IOException e) {
				System.out.println("Error closing stream... " + e);
			}
		}
		networks = null;
		chests = null;
	}
	
	/**
	 * De-serializes the 'chests' HashMap
	 */
	protected static void load(ChestSync inst) {
		plugin = inst;
		File file = new File("plugins/ChestSync/chests.dat");
		if (file.exists()) {
			FileInputStream in = null;
			ObjectInputStream obj = null;
			HashMap<String, LinkedList<SyncedChest>> list = new HashMap<String, LinkedList<SyncedChest>>();
			try {
				in = new FileInputStream(file);
				obj = new ObjectInputStream(in);
				list = (HashMap<String, LinkedList<SyncedChest>>) obj.readObject();
			} catch (IOException e) {
				System.out.println("Error opening stream: " + e);
			} catch (ClassNotFoundException e) {
				System.out.println("Error reading object stream: " + e);
			} finally {
				try {
					obj.close();
					in.close();
				} catch (IOException e) {
					System.out.println("Error closing stream: " + e);
				}
			}
			LinkedList<SyncedChest> l = null;
			for (String entry : list.keySet()) {
				if (debug) { System.out.println("Syncing network... " + entry); }
				l = new LinkedList<SyncedChest>();
				for (SyncedChest chest : list.get(entry)) {
					l.add(chest);
					chests.add(chest);
				}
				networks.put(entry, l);
				synchronize(entry);
				if (debug) { System.out.println("... done. (" + networks.get(entry).size() + " chests)"); }
			}
		}
	}

	/**
	 * Attempts to add the specified chest to the network specified
	 * @param network
	 * The network name for this chest
	 * @param loc
	 * the location of the chest to add
	 * @return
	 * true if the chest is added, false if it already exists
	 */
	public static boolean addSyncedChest(String network, Location loc, Location sign) {
		if (getSyncedChest(loc) == null) {
			boolean ok = false;
			LinkedList<SyncedChest> list = networks.get(network);
			if (list == null) { list = new LinkedList<SyncedChest>(); }
			if (isDoubleNetwork(network)) {
				if (((SpoutChest)loc.getBlock().getState()).isDoubleChest()) {
					ok = true;
				}
			} else {
				if (!((SpoutChest)loc.getBlock().getState()).isDoubleChest()) {
					ok = true;
				}
			}
			if (ok) {
				SyncedChest c = new SyncedChest(network, loc, sign);
				list.add(c);
				chests.add(c);
				networks.put(network, list);
				return true;
			}
		}
		return false;
	}

	/**
	 * Attempts to remove the specified chest from the network given
	 * @param network
	 * The network name for this chest
	 * @param loc
	 * The location of the chest to remove
	 * @return
	 * true if the chest is removed, false if it wasn't found
	 */
	public static boolean removeSyncedChest(String network, Location loc) {
		if (getSyncedChest(loc) != null) {
			SyncedChest c = getSyncedChest(loc);
			LinkedList<SyncedChest> list = networks.get(network);
			if (list.size() == 1) {
				ItemStack[] stack = getInventory(network).getContents();
				for (ItemStack i : stack) {
					if (i == null) { continue; }
					plugin.getServer().getWorld(c.world).dropItemNaturally(loc, i);
				}
				getInventory(network).clear();
			}
			if (list.getFirst().equals(c) && list.size() > 1) {
				SpoutChest next = (SpoutChest) list.get(1).getLocation().getBlock().getState();
				ItemStack[] stack = getInventory(network).getContents();
				for (ItemStack i : stack) {
					if (i == null) { continue; }
					next.getInventory().addItem(i);
				}
				((SpoutChest) list.getFirst().getLocation().getBlock().getState()).getInventory().clear();
			}
			list.remove(getSyncedChest(loc));
			chests.remove(getSyncedChest(loc));
			if (list.isEmpty()) {
				networks.remove(network);
				return true;
			} else {
				networks.put(network, list);
				synchronize(network);
				return true;
			}
		}
		return false;
	}

	/**
	 * Attempts to find a chest at the given location
	 * @param loc
	 * The location of the chest
	 * @return
	 * A Synced Chest object
	 */
	public static SyncedChest getSyncedChest(Location loc) {
		for (SyncedChest c : chests) {
			if (c.isDouble()) {
				if (c.getLocation().equals(loc) || c.getOtherSide().equals(loc)) {
					return c;
				}
			} else {
				if (c.getLocation().equals(loc)) {
					return c;
				}
			}
		}
		return null;
	}

	public static boolean isDoubleNetwork(String network) {
		if (networks.get(network) != null) {
			return networks.get(network).getFirst().isDouble();
		} else {
			return false;
		}
	}

	/**
	 * Synchronizes the inventories of every chest on this network
	 * @param network
	 * The network of chests to update
	 * @return
	 * true on success, otherwise false
	 */
	public static boolean synchronize(String network) {
		if (networks.get(network) != null) {
			for (SyncedChest c : networks.get(network)) {
				c.updateSign();
			}
			return true;
		} else {
			return false;
		}
	}

	static Inventory getInventory(String network) {
		if (networks.get(network) != null) {
			SyncedChest c = networks.get(network).getFirst();
			if (c.isDouble()) {
				return ((SpoutChest)c.getLocation().getBlock().getState()).getLargestInventory();
			} else {
				return ((SpoutChest)c.getLocation().getBlock().getState()).getInventory();
			}
		}
		return null;
	}


	// ---------------- //
	// INSTANCE MEMBERS //
	// ---------------- //
	private String network;
	private String world;
	
	private double fx;
	private double fy;
	private double fz;
	private double sx;
	private double sy;
	private double sz;
	
	private double[] sign = new double[3];

	private boolean big;

	private final int UID;
	/**
	 * Instance constructor
	 */
	protected SyncedChest(String net, Location loc, Location sign) {
		this.network = net;
		this.world = loc.getWorld().getName();
		this.fx = loc.getX();
		this.fy = loc.getY();
		this.fz = loc.getZ();
		this.sign = new double[]{sign.getX(), sign.getY(), sign.getZ()};
		this.big = determineDouble(loc);
		this.UID = (this.network + "," + this.world + "," + this.fx + "," + this.fy + "," + this.fz).hashCode();
	}

	public String getNetwork() {
		return this.network;
	}

	public Location getLocation() {
		return new Location(plugin.getServer().getWorld(world), fx, fy, fz);
	}
	
	public Location getOtherSide() {
		if (isDouble()) {
			return new Location(plugin.getServer().getWorld(world), sx, sy, sz);
		} else {
			return null;
		}
	}

	public Sign getSign() {
		Sign s = (Sign) new Location(plugin.getServer().getWorld(world), sign[0], sign[1], sign[2]).getBlock().getState();
		return s;
	}

	public void updateSign() {
		String line0 = "", line1 = "", line2 = "", line3 = "";
		Sign s = getSign();
		if (s.getType() != Material.WALL_SIGN) {
			MaterialData m = s.getData();
			BlockFace f = ((Directional) m).getFacing();
			s.setType(Material.WALL_SIGN);
			m = s.getData();
			((Directional)m).setFacingDirection(f);
			s.setData(m);
			s.update(true);
		}
		s = getSign();
		int size = networks.get(this.network).size();
		line0 = ((size > 1) ? Utility.color("&A") : Utility.color("&C")) + "[Synced]";
		line1 = ((size > 1) ? Utility.color("&E") : Utility.color("&C")) + this.network;
		line3 = ((size > 1) ? Utility.color("&A") : Utility.color("&C")) + "Chests: " + size;
		s.setLine(0, line0);
		s.setLine(1, line1);
		s.setLine(2, line2);
		s.setLine(3, line3);
		s.update(true);
	}

	public boolean isDouble() {
		return big;
	}

	private boolean determineDouble(Location loc) {
		SpoutChest chest = (SpoutChest) loc.getBlock().getState();
		if (chest.isDoubleChest()) {
			Location l = chest.getOtherSide().getBlock().getLocation();
			this.sx = l.getX();
			this.sy = l.getY();
			this.sz = l.getZ();
			return true;
		}
		this.sx = 0;
		this.sy = 0;
		this.sz = 0;
		return false;
	}

	@Override
	public String toString() {
		return "SyncedChest{network=" + this.network + ",x=" + this.fx + ",y=" + this.fy + ",z=" + this.fz + "}@" + this.UID;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public boolean equals(Object aThat) {
		if (this == aThat) { return true; }
		if (!(aThat instanceof SyncedChest)) {
			return false;
		}
		SyncedChest that = (SyncedChest) aThat;
		if (that.toString().equals(this.toString())) {
			return true;
		} else if (that.hashCode() == this.hashCode()) {
			return true;
		} else {
			return false;
		}
	}
}

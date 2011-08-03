package net.krinsoft.chestsync;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;

/**
 *
 * @author krinsdeath
 */

public class SyncedChest implements Serializable {
	private Pattern color = Pattern.compile("&([a-fA-F0-9])");

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
		
	}
	
	/**
	 * De-serializes the 'chests' HashMap
	 */
	protected static void load(ChestSync inst) {
		plugin = inst;
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
			LinkedList<SyncedChest> list = networks.get(network);
			if (list == null) { list = new LinkedList<SyncedChest>(); }
			SyncedChest c = new SyncedChest(network, loc, sign);
			list.add(c);
			chests.add(c);
			networks.put(network, list);
			return true;
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
			LinkedList<SyncedChest> list = networks.get(network);
			list.remove(getSyncedChest(loc));
			chests.remove(getSyncedChest(loc));
			networks.put(network, list);
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

	/**
	 * Synchronizes the inventories of every chest on this network
	 * @param network
	 * The network of chests to update
	 * @return
	 * true on success, otherwise false
	 */
	public static boolean synchronize(String network) {
		if (networks.get(network) != null) {
			System.out.println("Network isn't null! Has " + networks.get(network).size() + " chests.");
			Inventory[] inv = networks.get(network).getFirst().getInventory();
			for (SyncedChest c : networks.get(network)) {
				c.setInventory(inv);
				c.updateSign();
			}
			return true;
		} else {
			return false;
		}
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
			System.out.println("test1");
			org.bukkit.material.Sign aSign = (org.bukkit.material.Sign) s.getData();
			s.setType(Material.WALL_SIGN);
			aSign.setFacingDirection(aSign.getFacing());
			s.setData(aSign);
			s.update(true);
		}
		s = getSign();
		int size = networks.get(this.network).size();
		line0 = color.matcher((size > 1) ? "&A[Synced]" : "&C[Synced]").replaceAll("\u00A7$1");
		line1 = this.network;
		line2 = color.matcher((size > 1) ? "" : "&C[no link]").replaceAll("\u00A7$1");
		line3 = "[" + size + "]";
		s.setLine(0, line0);
		s.setLine(1, line1);
		s.setLine(2, line2);
		s.setLine(3, line3);
		s.update(true);
		System.out.println("test2");
	}

	public Inventory[] getInventory() {
		Inventory[] inv = new Inventory[2];
		if (this.isDouble()) {
			inv[0] = ((Chest)new Location(plugin.getServer().getWorld(world), fx, fy, fz).getBlock().getState()).getInventory();
			inv[1] = ((Chest)new Location(plugin.getServer().getWorld(world), sx, sy, sz).getBlock().getState()).getInventory();
			return inv;
		} else {
			inv[0] = ((Chest)new Location(plugin.getServer().getWorld(world), fx, fy, fz).getBlock().getState()).getInventory();
			inv[1] = null;
			return inv;
		}
	}

	public void setInventory(Inventory[] inv) {
		if (this.isDouble()) {
			((Chest)this.getLocation().getBlock().getState()).getInventory().setContents(inv[0].getContents());
			((Chest)this.getOtherSide().getBlock().getState()).getInventory().setContents(inv[1].getContents());
		} else {
			((Chest)this.getLocation().getBlock().getState()).getInventory().setContents(inv[0].getContents());
		}
	}

	public boolean isDouble() {
		return big;
	}

	private boolean determineDouble(Location loc) {
		if (loc.getBlock().getRelative(BlockFace.NORTH).getState() instanceof Chest) {
			Location tmp = loc.getBlock().getRelative(BlockFace.NORTH).getLocation();
			this.sx = tmp.getX();
			this.sy = tmp.getY();
			this.sz = tmp.getZ();
			return true;
		}
		if (loc.getBlock().getRelative(BlockFace.SOUTH).getState() instanceof Chest) {
			Location tmp = loc.getBlock().getRelative(BlockFace.SOUTH).getLocation();
			this.sx = tmp.getX();
			this.sy = tmp.getY();
			this.sz = tmp.getZ();
			return true;
		}
		if (loc.getBlock().getRelative(BlockFace.WEST).getState() instanceof Chest) {
			Location tmp = loc.getBlock().getRelative(BlockFace.WEST).getLocation();
			this.sx = tmp.getX();
			this.sy = tmp.getY();
			this.sz = tmp.getZ();
			return true;
		}
		if (loc.getBlock().getRelative(BlockFace.EAST).getState() instanceof Chest) {
			Location tmp = loc.getBlock().getRelative(BlockFace.EAST).getLocation();
			this.sx = tmp.getX();
			this.sy = tmp.getY();
			this.sz = tmp.getZ();
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

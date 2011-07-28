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

public class SyncedChest implements Serializable {
	private static final long serialVersionUID = 31L;

	protected static HashMap<String, LinkedList<SyncedChest>> syncedChests = new HashMap<String, LinkedList<SyncedChest>>();
	protected static HashMap<Location, SyncedChest> chests = new HashMap<Location, SyncedChest>();
	protected transient Location chest;
	protected String world;
	protected String name;
	protected boolean inUse = false;
	protected final double x;
	protected final double y;
	protected final double z;
	protected final double signx;
	protected final double signy;
	protected final double signz;
	protected SyncedChest(Location sign, Location location, String name) {
		this.chest = location;
		this.x = location.getX();
		this.y = location.getY();
		this.z = location.getZ();
		this.signx = sign.getX();
		this.signy = sign.getY();
		this.signz = sign.getZ();
		this.world = location.getWorld().getName();
		this.name = name;
	}

	public Location getLocation() {
		return this.chest;
	}

	public Inventory getInventory(){
		return ((Chest)chest.getBlock().getState()).getInventory();
	}

	public String getName() {
		return this.name;
	}

	public void update() {
		LinkedList<SyncedChest> list = syncedChests.get(getName());
		Iterator<SyncedChest> i = list.iterator();
		while(i.hasNext()) {
			SyncedChest chest = i.next();
			if (!chest.getLocation().equals(getLocation())) {
				if (!(chest.getLocation().getBlock().getState() instanceof Chest)) {
					i.remove();
				}
				else {
					chest.getInventory().setContents(getInventory().getContents());
				}
			}
		}
	}

	public boolean syncedChestsInUse() {
		LinkedList<SyncedChest> list = syncedChests.get(getName());
		for (SyncedChest chest : list) {
			if (chest.inUse()) {
				return true;
			}
		}
		return false;
	}

	public boolean inUse() {
		return inUse;
	}

	public void setInUse(boolean use) {
		inUse = use;
	}

	public static SyncedChest createSyncedChest(Location sign, Location location, String name) {
		if (location.getBlock().getState() instanceof Chest && !name.isEmpty()) {
			if (chests.get(location) != null) {
				return null;
			}
			SyncedChest chest = new SyncedChest(sign, location, name);
			if (syncedChests.containsKey(name)) {
				LinkedList<SyncedChest> list = syncedChests.get(name);
				list.add(chest);
				list.getFirst().update();
				for (SyncedChest in : syncedChests.get(name)) {
					Location loc = new Location(sign.getWorld(), in.signx, in.signy, in.signz);
					Block tmp = loc.getBlock();
					if (tmp.getState() instanceof Sign) {
						Sign the = (Sign) tmp.getState();
						String line0 = "&A[Synced]".replaceAll("&([a-fA-F0-9])", "\u00A7$1");
						the.setLine(0, line0);
						the.setLine(1, name);
						the.setLine(2, "");
						the.setLine(3, "[" + syncedChests.get(name).size() + "]");
						the.update();
					}
				}
			} else {
				LinkedList<SyncedChest> list = new LinkedList<SyncedChest>();
				list.add(chest);
				String line0 = "&C[Synced]".replaceAll("&([a-fA-F0-9])", "\u00A7$1");
				String line2 = "&C[no link]".replaceAll("&([a-fA-F0-9])", "\u00A7$1");
				syncedChests.put(name, list);
				Sign tmp = ((Sign)sign.getBlock().getState());
				tmp.setLine(0, line0);
				tmp.setLine(1, name);
				tmp.setLine(2, line2);
				tmp.setLine(3, "[" + syncedChests.get(name).size() + "]");
				tmp.update();
			}
			chests.put(location, chest);
			return chest;
		}
		return null;
	}

	public static SyncedChest removeSyncedChest(Location location) {
		SyncedChest chest = chests.get(location);
		if (chest != null) {
			LinkedList<SyncedChest> list = syncedChests.get(chest.getName());
			for (SyncedChest in : list) {
				Location loc = new Location(chest.getLocation().getWorld(), in.signx, in.signy, in.signz);
				Block tmp = loc.getBlock();
				if (tmp.getState() instanceof Sign) {
					Sign the = (Sign) tmp.getState();
					String line0 = "", line2 = "";
					if (syncedChests.get(chest.getName()).size() == 2) {
						line0 = "&C[Synced]";
						line2 = "&C[no link]";
					} else {
						line0 = "&A[Synced]";
					}
					line0 = line0.replaceAll("&([a-fA-F0-9])", "\u00A7$1");
					line2 = line2.replaceAll("&([a-fA-F0-9])", "\u00A7$1");
					the.setLine(0, line0);
					the.setLine(1, chest.getName());
					the.setLine(2, line2);
					the.setLine(3, "[" + (syncedChests.get(chest.getName()).size()-1) + "]");
					the.update();
				}
			}
			if (list.size() == 1) {
				syncedChests.remove(chest.getName());
				for (ItemStack item : chest.getInventory().getContents()) {
					if (item == null) { continue; }
					location.getWorld().dropItemNaturally(location, item);
				}

				chest.getInventory().clear();
			} else {
				chest.getInventory().clear();
			}
			Location sign = new Location(chest.getLocation().getWorld(), chest.signx, chest.signy, chest.signz);
			Sign tmp = (Sign) sign.getBlock().getState();
			tmp.setLine(0, "[no chest]");
			tmp.setLine(1, "");
			tmp.setLine(2, "");
			tmp.setLine(3, "");
			list.remove(chest);
			chests.remove(chest.getLocation());
			return chest;
		}
		return null;
	}

	public static SyncedChest getSyncedChest(Location location) {
		return chests.get(location);
	}

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

	public static void load(ChestSync plugin) {
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
				Location loc = new Location(plugin.getServer().getWorld(chest.world), chest.x, chest.y, chest.z);
				Location sign = new Location(plugin.getServer().getWorld(chest.world), chest.signx, chest.signy, chest.signz);
				createSyncedChest(sign, loc, chest.getName());
			}
		}
	}
}

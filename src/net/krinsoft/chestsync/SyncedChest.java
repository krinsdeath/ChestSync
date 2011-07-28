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
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

public class SyncedChest implements Serializable {
    private static final long serialVersionUID = 31L;

	protected static HashMap<String, LinkedList<SyncedChest>> syncedChests = new HashMap<String, LinkedList<SyncedChest>>();
	protected static HashMap<Location, SyncedChest> chests = new HashMap<Location, SyncedChest>();
    protected double x;
    protected double y;
    protected double z;
	protected transient Location chest;
    protected String world;
	protected String name;
	protected boolean inUse = false;
	protected SyncedChest(Location location, String name) {
		this.chest = location;
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
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
	
	public static SyncedChest createSyncedChest(Location location, String name) {
		if (location.getBlock().getState() instanceof Chest && !name.isEmpty()) {
			SyncedChest chest = new SyncedChest(location, name);
			if (syncedChests.containsKey(name)) {
				LinkedList<SyncedChest> list = syncedChests.get(name);
				list.add(chest);
				list.getFirst().update();
				
			}
			else {
				LinkedList<SyncedChest> list = new LinkedList<SyncedChest>();
				list.add(chest);
				syncedChests.put(name, list);
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
			if (list.size() == 1) {
				syncedChests.remove(chest.getName());
			}
			else {
				list.remove(chest);
			}
            chests.remove(chest);
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
            file = new FileOutputStream(tmp);
            out = new ObjectOutputStream(file);
            out.writeObject(syncedChests);
            out.close();
        } catch (IOException e) {
            System.out.println("Error creating " + tmp.getName() + ": " + e);
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
            System.out.println("Error: " + e);
        } catch (ClassNotFoundException e) {
            System.out.println("Error: " + e);
        }
        
        for (String key : temp.keySet()) {
            for (SyncedChest chest : temp.get(key)) {
                Location loc = new Location(plugin.getServer().getWorld(chest.world), chest.x, chest.y, chest.z);
                createSyncedChest(loc, chest.getName());
            }
        }
    }
}

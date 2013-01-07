package net.krinsoft.chestsync;

import net.krinsoft.chestsync.util.ChestUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author krinsdeath
 */
public class SyncedChest implements Serializable {
    private static Map<String, SyncedChest> chests = new HashMap<String, SyncedChest>();
    private final static long serialVersionUID = 1L;
    private static ChestSync instance;

    public static void load(ChestSync plugin) {
        instance = plugin;
        File persisted = new File(plugin.getDataFolder(), "chests.dat");
        if (persisted.exists()) {
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(persisted));
                chests = (Map<String, SyncedChest>) in.readObject();
                in.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void save() {
        File persisted = new File(instance.getDataFolder(), "chests.dat");
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(persisted));
            out.writeObject(chests);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SyncedChest getChest(Block chest) {
        if (chest.getState() instanceof Chest && ((Chest) chest.getState()).getBlockInventory().getHolder() instanceof DoubleChest) {
            Location tmp = chest.getLocation();
            SyncedChest that = chests.get(tmp.toString());
            if (that == null) {
                that = chests.get(ChestUtils.getOtherSide(chest).getLocation().toString());
                if (that != null) {
                    return that;
                } else {
                    return null;
                }
            }
        }
        return chests.get(chest.getLocation().toString());
    }

    public static SyncedChest addChest(String owner, Block chest) {
        SyncedChest synced = getChest(chest);
        if (synced == null) {
            synced = new SyncedChest(owner, chest);
            chests.put(chest.getLocation().toString(), synced);
        }
        return synced;
    }

    private final String owner;
    private final Set<String> access = new HashSet<String>();
    private final double x;
    private final double y;
    private final double z;
    private final String world;
    private final boolean isdouble;

    public SyncedChest(String owner, Block chest) {
        this.owner = owner;
        Location loc = chest.getLocation();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.world = loc.getWorld().getName();
        this.isdouble = ((Chest) chest.getState()).getBlockInventory().getHolder() instanceof DoubleChest;
    }

    public Inventory getInventory() {
        if (isdouble) {
            return (((Chest) new Location(instance.getServer().getWorld(world), x, y, z).getBlock().getState()).getBlockInventory().getHolder()).getInventory();
        }
        return ((Chest) new Location(instance.getServer().getWorld(world), x, y, z).getBlock().getState()).getBlockInventory();
    }

    public boolean hasAccess(Player player) {
        return player.getName().equals(owner) || access.contains(player.getName()) || player.hasPermission("chestsync.override");
    }

    public boolean addAccess(String sender, String player) {
        return sender.equals(owner) && access.add(player);
    }

    public boolean removeAccess(String sender, String player) {
        return sender.equals(owner) && access.remove(player);
    }

}

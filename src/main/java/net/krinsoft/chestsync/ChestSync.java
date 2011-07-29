package net.krinsoft.chestsync;

import java.util.logging.Logger;

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestSync extends JavaPlugin {

	private final ChestSyncInventoryListener inventoryListener = new ChestSyncInventoryListener(this);
	private final ChestSyncBlockListener blockListener = new ChestSyncBlockListener(this);
    private final ChestSyncPlayerListener playerListener = new ChestSyncPlayerListener(this);

    @Override
	public void onDisable() {
		SyncedChest.save();
		SyncedChest.syncedChests = null;
		SyncedChest.chests = null;
		SyncedChest.plugin = null;
		String disabled = "[ChestSync] v" + getDescription().getVersion() + " disabled.";
		Logger.getLogger("Minecraft").info(disabled);
	}

    @Override
	public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Type.CUSTOM_EVENT, inventoryListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.SIGN_CHANGE, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
		SyncedChest.plugin = this;
		SyncedChest.load(this);
		String init = "[ChestSync] v" + getDescription().getVersion() + " enabled.";
		Logger.getLogger("Minecraft").info(init);
	}
}

package net.krinsoft.chestsync;

import java.util.logging.Logger;

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestSync extends JavaPlugin{
	private static final ChestSyncInventoryListener inventoryListener = new ChestSyncInventoryListener();
	private static final ChestSyncBlockListener blockListener = new ChestSyncBlockListener();

	@Override
	public void onDisable() {
        SyncedChest.save();
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvent(Type.CUSTOM_EVENT, inventoryListener, Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Type.SIGN_CHANGE, blockListener, Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        SyncedChest.load(this);
		Logger.getLogger("Minecraft").info("ChestSync " + this.getDescription().getVersion() + " has been initialized");
	}
}

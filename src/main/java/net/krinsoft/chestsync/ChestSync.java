package net.krinsoft.chestsync;

import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author krinsdeath
 */

public class ChestSync extends JavaPlugin {
	private PluginDescriptionFile pdf;
	private PluginManager pm;

	private final BlockListener bListener = new BlockListener(this);
	private final ChestListener iListener = new ChestListener(this);

	@Override
	public void onEnable() {
		pm = getServer().getPluginManager();
		pdf = getDescription();

		// events
		pm.registerEvent(Event.Type.CUSTOM_EVENT, iListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.SIGN_CHANGE, bListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, bListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, bListener, Event.Priority.Normal, this);
		SyncedChest.load(this);

	}

	@Override
	public void onDisable() {
		SyncedChest.save();
	}
}

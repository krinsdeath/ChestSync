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
	private final PlayerListener pListener = new PlayerListener(this);
	private final EntityListener eListener = new EntityListener(this);

	@Override
	public void onEnable() {
		System.out.println(getDescription().getFullName() + " initializing...");
		pm = getServer().getPluginManager();
		pdf = getDescription();

		// events
		pm.registerEvent(Event.Type.PLAYER_INTERACT, pListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_EXPLODE, eListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.SIGN_CHANGE, bListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, bListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, bListener, Event.Priority.Normal, this);

		SyncedChest.load(this);
		Utility.init(this);

		System.out.println(getDescription().getFullName() + " by " + getDescription().getAuthors() + " enabled.");
	}

	@Override
	public void onDisable() {
		SyncedChest.save();
		System.out.println(getDescription().getFullName() + " disabled.");
	}
}

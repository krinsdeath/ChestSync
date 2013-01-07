package net.krinsoft.chestsync;

import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author krinsdeath
 */

public class ChestSync extends JavaPlugin {

	@Override
	public void onEnable() {
        saveConfig();
        SyncedChest.load(this);
	}

	@Override
	public void onDisable() {
        SyncedChest.save();
	}

}
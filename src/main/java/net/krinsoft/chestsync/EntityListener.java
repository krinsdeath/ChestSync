package net.krinsoft.chestsync;

import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 *
 * @author krinsdeath
 */

class EntityListener extends org.bukkit.event.entity.EntityListener {
	private ChestSync plugin;

	public EntityListener(ChestSync aThis) {
		plugin = aThis;
	}

	@Override
	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.isCancelled()) { return; }
		List<Block> blocks = event.blockList();
		for (Block b : blocks) {
			if (SyncedChest.getSyncedChest(b.getLocation()) != null) {
				event.setCancelled(true);
				return;
			}
		}
	}

}

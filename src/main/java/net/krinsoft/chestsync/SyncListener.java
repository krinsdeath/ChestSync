package net.krinsoft.chestsync;

import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 *
 * @author krinsdeath
 */

public class SyncListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    void playerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof Chest) {
            SyncedChest chest = SyncedChest.getChest(event.getClickedBlock());
            if (chest != null && chest.hasAccess(event.getPlayer())) {
                event.getPlayer().openInventory(chest.getInventory());
            }
        }
    }

    @EventHandler
    void signChange(SignChangeEvent event) {

    }

}

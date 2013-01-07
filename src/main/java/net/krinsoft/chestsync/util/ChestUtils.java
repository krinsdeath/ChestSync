package net.krinsoft.chestsync.util;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;

/**
 * @author krinsdeath
 */
public class ChestUtils {

    /**
     * Attempts to find the other side of a double chest from the block.
     * @param block The block where the chest is located
     * @return The other side of the chest, or the same block if the chest is not a double chest
     * @throws IllegalArgumentException if the specified block is null or not a chest.
     */
    public static Block getOtherSide(Block block) throws IllegalArgumentException {
        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null.");
        }
        Block chest;
        if (block.getState() instanceof Chest) {
            if (((Chest) block.getState()).getInventory().getHolder() instanceof DoubleChest) {
                if ((chest = block.getRelative(BlockFace.NORTH)).getState() instanceof Chest) {
                } else if ((chest = block.getRelative(BlockFace.EAST)).getState() instanceof Chest) {
                } else if ((chest = block.getRelative(BlockFace.SOUTH)).getState() instanceof Chest) {
                } else if ((chest = block.getRelative(BlockFace.WEST)).getState() instanceof Chest) {
                } else {
                    chest = block;
                }
            } else {
                return block;
            }
        } else {
            throw new IllegalArgumentException("Block must be a chest.");
        }
        return chest;
    }

}

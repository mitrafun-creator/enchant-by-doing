package aiefu.ebd.workstation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class WorkstationHelper {

    public static boolean isWorkstationNearby(Level level, BlockPos centerPos, WorkstationType type, int radius) {
        if (level == null || centerPos == null) return false;
        int minX = centerPos.getX() - radius;
        int maxX = centerPos.getX() + radius;
        int minY = Math.max(level.getMinBuildHeight(), centerPos.getY() - 3);
        int maxY = Math.min(level.getMaxBuildHeight(), centerPos.getY() + 3);
        int minZ = centerPos.getZ() - radius;
        int maxZ = centerPos.getZ() + radius;

        for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            Block block = level.getBlockState(pos).getBlock();
            if (type.matchingBlocks.contains(block)) {
                return true;
            }
        }
        return false;
    }

    public static byte getNearbyWorkstationsMask(Level level, BlockPos centerPos, int radius) {
        byte mask = 0;
        if (level == null || centerPos == null) return mask;

        for (WorkstationType type : WorkstationType.values()) {
            if (isWorkstationNearby(level, centerPos, type, radius)) {
                mask |= (byte) (1 << type.bitIndex);
            }
        }
        return mask;
    }

    public static boolean isWorkstationPresent(byte mask, WorkstationType type) {
        return (mask & (1 << type.bitIndex)) != 0;
    }

    public static WorkstationType getRequiredWorkstation(ItemStack resultStack) {
        if (resultStack.isEmpty()) return null;

        // Exceptions: crafting the workstations themselves does NOT require having one nearby!
        Item item = resultStack.getItem();
        if (item == Items.ANVIL || item == Items.CHIPPED_ANVIL || item == Items.DAMAGED_ANVIL) return null;
        if (item == Items.ENCHANTING_TABLE) return null;
        if (item == Items.FLETCHING_TABLE) return null;
        if (item == Items.SMITHING_TABLE || item == Items.BLAST_FURNACE) return null;

        for (WorkstationType type : WorkstationType.values()) {
            if (resultStack.is(type.requiredItemTag)) {
                return type;
            }
        }
        return null;
    }
}
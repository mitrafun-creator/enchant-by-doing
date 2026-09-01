package aiefu.ebd.compat;

import aiefu.ebd.IServerPlayerAcc;
import aiefu.ebd.LBDConfig;
import aiefu.ebd.Utils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;

public class TreeChopCompat {

    @SubscribeEvent
    public void onFinishChop(ht.treechop.api.ChopEvent.FinishChopEvent event) {
        if (event.getLevel().isClientSide()) return;
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer sp)) return;

        // If the tree was felled, handle all experience in onAfterFell instead
        if (event.getFelled()) return;

        BlockState state = event.getChoppedBlockState();
        Block block = state.getBlock();
        ItemStack tool = sp.getMainHandItem();

        String blockKey = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString();
        boolean inConfig = LBDConfig.INSTANCE.customBlockXp.containsKey(blockKey);

        if (tool.getItem() instanceof net.minecraft.world.item.AxeItem) {
            double xp;
            if (inConfig) {
                xp = LBDConfig.INSTANCE.customBlockXp.get(blockKey);
            } else {
                float hardness = state.getDestroySpeed(sp.level(), event.getChoppedBlockPos());
                xp = Math.max(0.1, hardness * LBDConfig.INSTANCE.blockXpHardnessMultiplier);
            }

            long penaltyTime = ((IServerPlayerAcc) sp).ebd$getLumberjackPenaltyTime();
            if (System.currentTimeMillis() < penaltyTime) {
                xp = 0.0;
            }

            if (xp > 0) {
                ((IServerPlayerAcc) sp).ebd$addSkillXP("lumberjack", xp, sp);
            }
        }
    }

    @SubscribeEvent
    public void onBeforeFell(ht.treechop.api.ChopEvent.BeforeFellEvent event) {
        if (event.getLevel().isClientSide()) return;
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer sp)) return;

        BlockState state = event.getChoppedBlockState();
        Block block = state.getBlock();
        ItemStack tool = sp.getMainHandItem();

        String blockKey = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString();
        boolean inConfig = LBDConfig.INSTANCE.customBlockXp.containsKey(blockKey);

        if (tool.getItem() instanceof net.minecraft.world.item.AxeItem) {
            java.util.Set<net.minecraft.core.BlockPos> logBlocks = event.getFellData().getTree().getLogBlocksOrEmpty();
            int logsCount = logBlocks.size();
            if (logsCount > 0) {
                double xpPerLog;
                if (inConfig) {
                    xpPerLog = LBDConfig.INSTANCE.customBlockXp.get(blockKey);
                } else {
                    float hardness = state.getDestroySpeed(sp.level(), event.getChoppedBlockPos());
                    xpPerLog = Math.max(0.1, hardness * LBDConfig.INSTANCE.blockXpHardnessMultiplier);
                }

                double totalXp = xpPerLog * logsCount;
                long penaltyTime = ((IServerPlayerAcc) sp).ebd$getLumberjackPenaltyTime();
                if (System.currentTimeMillis() < penaltyTime) {
                    totalXp = 0.0;
                }

                if (totalXp > 0) {
                    ((IServerPlayerAcc) sp).ebd$addSkillXP("lumberjack", totalXp, sp);
                }
            }
        }
    }

    @SubscribeEvent
    public void onAfterFell(ht.treechop.api.ChopEvent.AfterFellEvent event) {
        if (event.getLevel().isClientSide()) return;
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer sp)) return;

        int lumberjackLevel = Utils.getSkillLevel(sp, "lumberjack");

        // x10 apple drops for lumberjack level 40+ on leaves felling
        if (lumberjackLevel >= 40) {
            net.minecraft.world.level.block.state.BlockState choppedState = event.getChoppedBlockState();
            boolean isOak = choppedState.is(net.minecraft.tags.BlockTags.OAK_LOGS) || choppedState.is(net.minecraft.tags.BlockTags.DARK_OAK_LOGS);
            if (isOak) {
                ItemStack tool = sp.getMainHandItem();
                int fortune = 0;
                if (!tool.isEmpty()) {
                    var registry = sp.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                    var fortuneHolder = registry.getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE);
                    fortune = tool.getEnchantments().getLevel(fortuneHolder);
                }
                double baseChance = 0.005;
                if (fortune == 1) baseChance = 0.00625;
                else if (fortune == 2) baseChance = 0.00833;
                else if (fortune >= 3) baseChance = 0.025;

                double targetChance = baseChance * 10.0;

                event.getFellData().getTree().forEachLeaves(pos -> {
                    if (sp.getRandom().nextDouble() < targetChance) {
                        net.minecraft.world.entity.item.ItemEntity appleDrop = new net.minecraft.world.entity.item.ItemEntity(
                                event.getLevel(),
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                new ItemStack(net.minecraft.world.item.Items.APPLE)
                        );
                        appleDrop.setDefaultPickUpDelay();
                        event.getLevel().addFreshEntity(appleDrop);
                    }
                });
            }
        }

        if (lumberjackLevel >= 20) {
            net.minecraft.core.BlockPos basePos = event.getChoppedBlockPos();
            java.util.Set<net.minecraft.core.BlockPos> logs = event.getFellData().getTree().getLogBlocksOrEmpty();
            net.minecraft.core.BlockPos lowestLog = null;
            for (net.minecraft.core.BlockPos logPos : logs) {
                if (lowestLog == null || logPos.getY() < lowestLog.getY()) {
                    lowestLog = logPos;
                }
            }
            net.minecraft.core.BlockPos plantPos = (lowestLog != null) ? lowestLog : basePos;

            // Run planting 1 tick later when log blocks are actually air
            sp.server.tell(new net.minecraft.server.TickTask(sp.server.getTickCount() + 1, () -> {
                if (!sp.isAlive()) return;
                ItemStack saplingStack = ItemStack.EMPTY;
                for (int i = 0; i < sp.getInventory().getContainerSize(); i++) {
                    ItemStack stack = sp.getInventory().getItem(i);
                    if (!stack.isEmpty() && stack.is(net.minecraft.tags.ItemTags.SAPLINGS) && stack.getItem() instanceof net.minecraft.world.item.BlockItem) {
                        saplingStack = stack;
                        break;
                    }
                }

                if (!saplingStack.isEmpty()) {
                    net.minecraft.server.level.ServerLevel serverLevel = sp.serverLevel();
                    if (serverLevel.isEmptyBlock(plantPos)) {
                        net.minecraft.world.level.block.state.BlockState saplingState = ((net.minecraft.world.item.BlockItem) saplingStack.getItem()).getBlock().defaultBlockState();
                        if (saplingState.canSurvive(serverLevel, plantPos)) {
                            serverLevel.setBlock(plantPos, saplingState, 3);
                            if (!sp.getAbilities().instabuild) {
                                saplingStack.shrink(1);
                            }
                        }
                    }
                }
            }));
        }
    }
}

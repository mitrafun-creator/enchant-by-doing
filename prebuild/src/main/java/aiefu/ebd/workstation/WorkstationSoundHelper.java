package aiefu.ebd.workstation;

import aiefu.ebd.LBDConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

public class WorkstationSoundHelper {

    private static final List<QueuedSound> PENDING_SOUNDS = new ArrayList<>();
    private static final Map<UUID, Long> LAST_CRAFT_TICK = new HashMap<>();

    private static class QueuedSound {
        final ServerLevel level;
        final Vec3 pos;
        final SoundEvent sound;
        final float volume;
        final float pitch;
        int delayTicks;

        QueuedSound(ServerLevel level, Vec3 pos, SoundEvent sound, float volume, float pitch, int delayTicks) {
            this.level = level;
            this.pos = pos;
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
            this.delayTicks = delayTicks;
        }
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (PENDING_SOUNDS.isEmpty()) return;

        Iterator<QueuedSound> iterator = PENDING_SOUNDS.iterator();
        while (iterator.hasNext()) {
            QueuedSound sound = iterator.next();
            sound.delayTicks--;
            if (sound.delayTicks <= 0) {
                if (sound.level != null && !sound.level.isClientSide()) {
                    sound.level.playSound(
                            null,
                            sound.pos.x, sound.pos.y, sound.pos.z,
                            sound.sound,
                            SoundSource.BLOCKS,
                            sound.volume,
                            sound.pitch
                    );
                }
                iterator.remove();
            }
        }
    }

    public static void playCraftingSounds(ServerPlayer player, ItemStack craftedStack) {
        if (!LBDConfig.INSTANCE.enableCraftingWorkstations) return;
        if (craftedStack == null || craftedStack.isEmpty()) return;

        byte reqMask = WorkstationHelper.getRequiredWorkstationsMask(craftedStack);
        if (reqMask == 0) return;

        long currentTick = player.server.getTickCount();
        Long lastTick = LAST_CRAFT_TICK.get(player.getUUID());
        if (lastTick != null && currentTick - lastTick < 6) {
            return;
        }
        LAST_CRAFT_TICK.put(player.getUUID(), currentTick);

        ServerLevel level = player.serverLevel();
        Vec3 pos = new Vec3(player.getX(), player.getY() + 0.5, player.getZ());
        Random random = new Random();

        // Natural artisan crafting progression order:
        // 1. Fletching Table (wood shaping / carving)
        // 2. Anvil (heavy forging / hammering)
        // 3. Armorer / Smithing Table (assembly & reinforcement)
        // 4. Grindstone (sharpening & edge finishing)
        // 5. Cartography Table (fine calibration & drafting)
        // 6. Enchanting Table (magical imbuing & XP resonance)
        List<WorkstationType> executionOrder = List.of(
                WorkstationType.FLETCHING_TABLE,
                WorkstationType.ANVIL,
                WorkstationType.ARMORER,
                WorkstationType.GRINDSTONE,
                WorkstationType.CARTOGRAPHY_TABLE,
                WorkstationType.ENCHANTING_TABLE
        );

        int currentDelay = 0;
        int delayStep = 6; // 6 ticks = 300 ms between sequential workstation sounds

        for (WorkstationType type : executionOrder) {
            if ((reqMask & (1 << type.bitIndex)) == 0) continue;

            switch (type) {
                case FLETCHING_TABLE -> {
                    float pitch = 0.95F + random.nextFloat() * 0.1F;
                    queueOrPlay(level, pos, SoundEvents.VILLAGER_WORK_FLETCHER, 0.75F, pitch, currentDelay);
                    currentDelay += delayStep;
                }
                case ANVIL -> {
                    float pitch = 0.95F + random.nextFloat() * 0.1F;
                    queueOrPlay(level, pos, SoundEvents.ANVIL_USE, 0.6F, pitch, currentDelay);
                    currentDelay += delayStep;
                }
                case ARMORER -> {
                    float pitch = 0.95F + random.nextFloat() * 0.1F;
                    queueOrPlay(level, pos, SoundEvents.SMITHING_TABLE_USE, 0.75F, pitch, currentDelay);
                    currentDelay += delayStep;
                }
                case GRINDSTONE -> {
                    float pitch = 1.0F + random.nextFloat() * 0.1F;
                    queueOrPlay(level, pos, SoundEvents.GRINDSTONE_USE, 0.65F, pitch, currentDelay);
                    currentDelay += delayStep;
                }
                case CARTOGRAPHY_TABLE -> {
                    float pitch = 0.95F + random.nextFloat() * 0.1F;
                    queueOrPlay(level, pos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 0.75F, pitch, currentDelay);
                    currentDelay += delayStep;
                }
                case ENCHANTING_TABLE -> {
                    // Mystical enchanting whoosh + sparkling experience orb pickup chime
                    queueOrPlay(level, pos, SoundEvents.ENCHANTMENT_TABLE_USE, 0.7F, 1.1F, currentDelay);
                    queueOrPlay(level, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.6F, 0.9F + random.nextFloat() * 0.15F, currentDelay + 2);
                    currentDelay += delayStep;
                }
            }
        }
    }

    private static void queueOrPlay(ServerLevel level, Vec3 pos, SoundEvent sound, float volume, float pitch, int delay) {
        if (delay <= 0) {
            level.playSound(null, pos.x, pos.y, pos.z, sound, SoundSource.BLOCKS, volume, pitch);
        } else {
            PENDING_SOUNDS.add(new QueuedSound(level, pos, sound, volume, pitch, delay));
        }
    }

    public static void clear() {
        PENDING_SOUNDS.clear();
        LAST_CRAFT_TICK.clear();
    }
}

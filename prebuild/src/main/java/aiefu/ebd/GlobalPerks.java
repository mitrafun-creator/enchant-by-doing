package aiefu.ebd;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public class GlobalPerks {

    public enum Perk {
        HEALTH_BOOST("health_boost", "perk.enchant_by_doing.health_boost", 20, 1, Items.GOLDEN_APPLE),
        FISHERMAN("perk_fisherman", "perk.enchant_by_doing.fisherman", 1, 1, Items.FISHING_ROD),
        MAGIC("perk_magic", "perk.enchant_by_doing.magic", 1, 3, Items.ENCHANTING_TABLE),
        ALCHEMY("perk_alchemy", "perk.enchant_by_doing.alchemy", 1, 2, Items.BREWING_STAND),
        ENGINEERING("perk_engineering", "perk.enchant_by_doing.engineering", 1, 4, Items.REDSTONE),
        ANCIENT_KNOWLEDGE("perk_ancient_knowledge", "perk.enchant_by_doing.ancient_knowledge", 1, 5, Items.NETHERITE_INGOT),
        CONSTRUCTION("perk_construction", "perk.enchant_by_doing.construction", 1, 1, Items.SCAFFOLDING);

        public final String id;
        public final String translationKey;
        public final int maxLevel;
        public final int costPerLevel;
        public final Item icon;

        Perk(String id, String translationKey, int maxLevel, int costPerLevel, Item icon) {
            this.id = id;
            this.translationKey = translationKey;
            this.maxLevel = maxLevel;
            this.costPerLevel = costPerLevel;
            this.icon = icon;
        }

        public Component getDisplayName() {
            return Component.translatable(translationKey + ".name");
        }

        public Component getDescription() {
            return Component.translatable(translationKey + ".desc");
        }
    }

    private static final Map<String, Perk> BY_ID = new HashMap<>();
    private static final Set<Item> REDSTONE_ITEMS = new HashSet<>();

    static {
        for (Perk p : Perk.values()) {
            BY_ID.put(p.id, p);
        }

        // Redstone engineering items
        REDSTONE_ITEMS.add(Items.PISTON);
        REDSTONE_ITEMS.add(Items.STICKY_PISTON);
        REDSTONE_ITEMS.add(Items.REDSTONE_TORCH);
        REDSTONE_ITEMS.add(Items.REPEATER);
        REDSTONE_ITEMS.add(Items.COMPARATOR);
        REDSTONE_ITEMS.add(Items.DISPENSER);
        REDSTONE_ITEMS.add(Items.DROPPER);
        REDSTONE_ITEMS.add(Items.OBSERVER);
        REDSTONE_ITEMS.add(Items.HOPPER);
        REDSTONE_ITEMS.add(Items.TARGET);
        REDSTONE_ITEMS.add(Items.DAYLIGHT_DETECTOR);
        REDSTONE_ITEMS.add(Items.REDSTONE_LAMP);
        REDSTONE_ITEMS.add(Items.CRAFTER);
        REDSTONE_ITEMS.add(Items.NOTE_BLOCK);
        REDSTONE_ITEMS.add(Items.REDSTONE_BLOCK);
        REDSTONE_ITEMS.add(Items.TNT);
        REDSTONE_ITEMS.add(Items.TRIPWIRE_HOOK);
        REDSTONE_ITEMS.add(Items.TRAPPED_CHEST);
        REDSTONE_ITEMS.add(Items.POWERED_RAIL);
        REDSTONE_ITEMS.add(Items.DETECTOR_RAIL);
        REDSTONE_ITEMS.add(Items.ACTIVATOR_RAIL);
        REDSTONE_ITEMS.add(Items.HOPPER_MINECART);
        REDSTONE_ITEMS.add(Items.CHEST_MINECART);
        REDSTONE_ITEMS.add(Items.FURNACE_MINECART);
        REDSTONE_ITEMS.add(Items.TNT_MINECART);
    }

    public static Perk getById(String id) {
        return BY_ID.get(id);
    }

    public static double getNeededXPForLevel(int level) {
        return (level + 3) * 25.0;
    }

    /**
     * Determines which perk is required to craft the given item stack, if any.
     */
    public static Perk getRequiredPerk(ItemStack stack) {
        if (stack.isEmpty()) return null;

        Item item = stack.getItem();
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        String path = key.getPath();

        // 1. Fisherman: boats and fishing rod
        if (stack.is(ItemTags.BOATS) || item == Items.FISHING_ROD || path.contains("boat") || path.contains("raft")) {
            return Perk.FISHERMAN;
        }

        // 2. Magic: enchanting table
        if (item == Items.ENCHANTING_TABLE) {
            return Perk.MAGIC;
        }

        // 3. Alchemy: brewing stand
        if (item == Items.BREWING_STAND) {
            return Perk.ALCHEMY;
        }

        // 4. Engineering: redstone machinery
        if (REDSTONE_ITEMS.contains(item)) {
            return Perk.ENGINEERING;
        }

        // 5. Ancient Knowledge: netherite ingot
        if (item == Items.NETHERITE_INGOT) {
            return Perk.ANCIENT_KNOWLEDGE;
        }

        // 6. Construction: scaffolding and concrete powder
        if (item == Items.SCAFFOLDING || path.endsWith("_concrete_powder")) {
            return Perk.CONSTRUCTION;
        }

        return null;
    }

    /**
     * Checks if the item craft is locked for the given player.
     */
    public static boolean isItemLockedForPlayer(Player player, ItemStack stack) {
        if (!LBDConfig.INSTANCE.enableGlobalLevelSystem) return false;
        Perk required = getRequiredPerk(stack);
        if (required == null) return false;

        if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
            if (sp instanceof IServerPlayerAcc acc) {
                return acc.ebd$getPerkLevel(required.id) < 1;
            }
        } else if (player.level().isClientSide()) {
            return aiefu.ebd.network.ClientsideNetworkManager.clientPerks.getOrDefault(required.id, 0) < 1;
        }
        return false;
    }
}

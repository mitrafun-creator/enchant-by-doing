package aiefu.ebd;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.*;

public class GlobalPerks {

    public enum Perk {
        HEALTH_BOOST("health_boost", "perk.enchant_by_doing.health_boost", 20, 1, Items.GOLDEN_APPLE),
        LIGHT_STEP("light_step", "perk.enchant_by_doing.light_step", 3, 1, Items.FEATHER),
        WELL_FED("well_fed", "perk.enchant_by_doing.well_fed", 3, 1, Items.COOKED_BEEF),
        SOUL_MAGNET("soul_magnet", "perk.enchant_by_doing.soul_magnet", 2, 2, Items.EXPERIENCE_BOTTLE),
        IRON_WILL("iron_will", "perk.enchant_by_doing.iron_will", 3, 1, Items.SHIELD),
        WAVE_RIDER("wave_rider", "perk.enchant_by_doing.wave_rider", 2, 1, Items.HEART_OF_THE_SEA),
        SILVER_TONGUE("silver_tongue", "perk.enchant_by_doing.silver_tongue", 2, 2, Items.EMERALD);

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

    static {
        for (Perk p : Perk.values()) {
            BY_ID.put(p.id, p);
        }
    }

    public static Perk getById(String id) {
        return BY_ID.get(id);
    }

    public static double getNeededXPForLevel(int level) {
        return (level + 3) * 25.0;
    }
}

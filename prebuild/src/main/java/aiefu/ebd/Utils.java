package aiefu.ebd;

import aiefu.ebd.data.materialoverrides.MaterialData;
import aiefu.ebd.data.materialoverrides.MaterialOverrides;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiPredicate;

public class Utils {
    public static int getTotalAvailableXPPoints(Player player){
        int i = player.experienceLevel;
        int totalXP = 0;
        for (int j = 0; j < i; j++) {
            totalXP += getXpNeededForLevel(j);
        }
        return (int) (totalXP + player.experienceProgress * player.getXpNeededForNextLevel());
    }

    public static float getXPCostInLevels(Player player, int points){
        return getXPCostInLevels(player, points, getTotalAvailableXPPoints(player));
    }

    public static float getXPCostInLevels(Player player, int points, int totalXP){
        int remainingPoints = points;
        int level = player.experienceLevel;
        float result = 0.0F;
        if(totalXP == points){
            return player.experienceLevel + player.experienceProgress;
        } else if(totalXP > points){
            while (remainingPoints > 0){
                int xp = getXpNeededForLevel(level);
                if(remainingPoints >= xp){
                    result += 1.0F;
                } else result += (float) remainingPoints / xp;
                remainingPoints -= xp;
                level--;
            }
        } else {
            level = 0;
            while (remainingPoints > 0){
                int xp = getXpNeededForLevel(level);
                if(remainingPoints >= xp){
                    result  += 1.0F;
                } else result += (float) remainingPoints / xp;
                remainingPoints -= xp;
                level++;
            }

        }
        return result;
    }

    public static int getXpNeededForLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else {
            return level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
        }
    }

    public static int getEnchantmentsLimit(int curses, MaterialData data){
        ConfigurationFile cfg = EBDCommon.config;
        return cfg.enableCursesAmplifier ? data.getMaxEnchantments() + Math.min(curses, data.getMaxCurses()) * data.getCurseMultiplier() : data.getMaxEnchantments();
    }

    public static int getEnchantmentsLimit(Player player, int curses, MaterialData data){
        int limit = getEnchantmentsLimit(curses, data);
        if (player != null) {
            int enchanterLevel = getSkillLevel(player, "enchanter");
            if (enchanterLevel >= 50) {
                limit += 1;
            }
            if (enchanterLevel >= 100) {
                limit += 1;
            }
        }
        return limit;
    }

    public static MaterialData getMatData(Item item){
        return EBDCommon.config.enableEnchantability ? EBDCommon.mat_config.getMaterialData(item) : MaterialOverrides.defaultMatData;
    }

    public static int getCurrentLimit(int appliedEnchantments, int curses){
        ConfigurationFile cfg = EBDCommon.config;
        return cfg.enableCursesAmplifier ? appliedEnchantments - curses : appliedEnchantments;
    }

    public static boolean containsEnchantments(ItemStack stack){
        return !stack.getEnchantments().isEmpty();
    }

    public static boolean containsSameEnchantments(Map<Holder<Enchantment>, Integer> m1, Map<Holder<Enchantment>, Integer> m2){
        if(m1.size() != m2.size()){
            return false;
        } else {
            int matches = 0;
            for (Holder<Enchantment> e : m1.keySet()){
                if(m2.containsKey(e)) matches++;
            }
            return matches == m2.size();
        }
    }

    public static boolean containsSameEnchantmentsOfSameLevel(Map<Holder<Enchantment>, Integer> m1, Map<Holder<Enchantment>, Integer> m2){
        if(m1.size() != m2.size()){
            return false;
        } else {
            int matches = 0;
            for (Map.Entry<Holder<Enchantment>, Integer> e : m1.entrySet()){
                Integer lvl = m2.get(e.getKey());
                if(lvl != null && lvl.intValue() == e.getValue()) matches++;
            }
            return matches == m2.size();
        }
    }

    public static LinkedHashMap<Holder<Enchantment>, Integer> filterToNewMap(Map<Holder<Enchantment>, Integer> map, BiPredicate<Holder<Enchantment>, Integer> predicate){
        LinkedHashMap<Holder<Enchantment>, Integer>  enchs = new LinkedHashMap<>();
        for (Map.Entry<Holder<Enchantment>, Integer> e : map.entrySet()){
            if(predicate.test(e.getKey(), e.getValue())){
                enchs.put(e.getKey(), e.getValue());
            }
        }
        return enchs;
    }

    public static int getEnchantmentLevel(ItemStack stack, String path) {
        if (stack.isEmpty()) return 0;
        for (net.minecraft.core.Holder<Enchantment> holder : stack.getEnchantments().keySet()) {
            net.minecraft.resources.ResourceLocation loc = holder.unwrapKey().map(net.minecraft.resources.ResourceKey::location).orElse(null);
            if (loc != null && loc.getNamespace().equals(EBDCommon.MOD_ID) && loc.getPath().equals(path)) {
                return stack.getEnchantments().getLevel(holder);
            }
        }
        return 0;
    }

    public static boolean containsEnchantment(ItemStack stack, String path) {
        return getEnchantmentLevel(stack, path) > 0;
    }

    /**
     * Перегрузка — работает напрямую с ItemEnchantments (ESO-напространство).
     */
    public static int getEnchantmentLevel(net.minecraft.world.item.enchantment.ItemEnchantments enchantments, String path) {
        if (enchantments == null || enchantments.isEmpty()) return 0;
        for (net.minecraft.core.Holder<Enchantment> holder : enchantments.keySet()) {
            net.minecraft.resources.ResourceLocation loc = holder.unwrapKey().map(net.minecraft.resources.ResourceKey::location).orElse(null);
            if (loc != null && loc.getNamespace().equals(EBDCommon.MOD_ID) && loc.getPath().equals(path)) {
                return enchantments.getLevel(holder);
            }
        }
        return 0;
    }

    public static boolean containsEnchantment(net.minecraft.world.item.enchantment.ItemEnchantments enchantments, String path) {
        return getEnchantmentLevel(enchantments, path) > 0;
    }

    public static final net.minecraft.tags.TagKey<Item> ENCHANTABLE_BOAT =
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                    net.minecraft.resources.ResourceLocation.withDefaultNamespace("boats"));

    public static final net.minecraft.tags.TagKey<Item> ENCHANTABLE_STORAGE =
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "enchantable/storage"));

    public static boolean isEnchantableItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem().isEnchantable(stack) || stack.is(ENCHANTABLE_BOAT) || stack.is(ENCHANTABLE_STORAGE);
    }

    public static int getSkillLevel(Player player, String skill) {
        if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
            return ((IServerPlayerAcc) sp).ebd$getSkillLevel(skill);
        } else {
            return aiefu.ebd.client.SkillHUDRenderer.CLIENT_SKILL_LEVELS.getOrDefault(skill, 1);
        }
    }
}

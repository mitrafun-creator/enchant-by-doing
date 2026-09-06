package aiefu.ebd;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;

public interface IServerPlayerAcc {
    Object2IntOpenHashMap<ResourceLocation> enchantment_overhaul$getUnlockedEnchantments();
    void enchantment_overhaul$setUnlockedEnchantments(Object2IntOpenHashMap<ResourceLocation> map);

    int ebd$getSkillLevel(String skill);
    double ebd$getSkillXP(String skill);
    void ebd$setSkillLevel(String skill, int level);
    void ebd$setSkillXP(String skill, double xp);
    void ebd$addSkillXP(String skill, double amount, net.minecraft.server.level.ServerPlayer player);
    
    long ebd$getMinerPenaltyTime();
    void ebd$setMinerPenaltyTime(long time);
    long ebd$getLumberjackPenaltyTime();
    void ebd$setLumberjackPenaltyTime(long time);
    long ebd$getSecondBreathCooldown();
    void ebd$setSecondBreathCooldown(long time);

    int ebd$getGlobalLevel();
    void ebd$setGlobalLevel(int level);
    double ebd$getGlobalXP();
    void ebd$setGlobalXP(double xp);
    int ebd$getSkillPoints();
    void ebd$setSkillPoints(int points);
    int ebd$getPerkLevel(String perkId);
    void ebd$setPerkLevel(String perkId, int level);
    java.util.Map<String, Integer> ebd$getAllPerks();
    void ebd$addGlobalXP(double amount, net.minecraft.server.level.ServerPlayer player);
    void ebd$applyGlobalPerkAttributes();
}

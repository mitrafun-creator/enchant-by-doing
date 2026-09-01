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
}

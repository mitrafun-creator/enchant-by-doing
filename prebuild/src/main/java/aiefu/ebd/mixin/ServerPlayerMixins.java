package aiefu.ebd.mixin;

import aiefu.ebd.EBDCommon;
import aiefu.ebd.IServerPlayerAcc;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import aiefu.ebd.LBDConfig;
import aiefu.ebd.SkillType;
import java.util.HashMap;
import java.util.Map;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixins implements IServerPlayerAcc {
    @Unique
    private Object2IntOpenHashMap<ResourceLocation> unlockedEnchantments = new Object2IntOpenHashMap<>();
    @Unique
    private final Map<String, Integer> skillLevels = new HashMap<>();
    @Unique
    private final Map<String, Double> skillXPs = new HashMap<>();

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void saveUnlockedEnchantmentsDataEOVR(CompoundTag compound, CallbackInfo ci){
        ListTag enchantments = new ListTag();
        unlockedEnchantments.forEach((k, v) -> {
            CompoundTag enchantmentData = new CompoundTag();
            enchantmentData.putString("identifier", k.toString());
            enchantmentData.putInt("level", v);
            enchantments.add(enchantmentData);
        });
        CompoundTag tag = new CompoundTag();
        tag.put("LearnedEnchantments", enchantments);

        CompoundTag skillsTag = new CompoundTag();
        skillLevels.forEach((k, v) -> skillsTag.putInt(k + "_level", v));
        skillXPs.forEach((k, v) -> skillsTag.putDouble(k + "_xp", v));
        tag.put("Skills", skillsTag);
        tag.putLong("SecondBreathCooldown", this.secondBreathCooldown);

        compound.put("ebddata", tag);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readUnlockedEnchantmentsDataEOVR(CompoundTag compound, CallbackInfo ci){
        this.unlockedEnchantments.clear();
        this.skillLevels.clear();
        this.skillXPs.clear();
        Registry<Enchantment> registry = ((ServerPlayer)(Object)this).level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);

        if(compound.contains("UnlockedEnchs", Tag.TAG_LIST)){
            ListTag enchantments = compound.getList("UnlockedEnchs", Tag.TAG_STRING);
            for (Tag t : enchantments){
                String id = t.getAsString();
                ResourceLocation loc = ResourceLocation.parse(id);
                Optional<Holder.Reference<Enchantment>> holderOpt = registry.getHolder(ResourceKey.create(Registries.ENCHANTMENT, loc));
                holderOpt.ifPresent(holder -> {
                    this.unlockedEnchantments.put(loc, EBDCommon.getMaximumPossibleEnchantmentLevel(holder));
                });
            }
            compound.remove("UnlockedEnchs");
        }
        CompoundTag ebdData = null;
        if(compound.contains("ebddata", Tag.TAG_COMPOUND)){
            ebdData = compound.getCompound("ebddata");
        } else if(compound.contains("esodata", Tag.TAG_COMPOUND)){
            ebdData = compound.getCompound("esodata");
        }
        if(ebdData != null){
            if(ebdData.contains("SecondBreathCooldown", Tag.TAG_LONG)){
                this.secondBreathCooldown = ebdData.getLong("SecondBreathCooldown");
            }
            if(ebdData.contains("LearnedEnchantments", Tag.TAG_LIST)){
                ListTag enchantments = ebdData.getList("LearnedEnchantments", Tag.TAG_COMPOUND);
                for (Tag t : enchantments){
                    CompoundTag ct = (CompoundTag) t;
                    String id = ct.getString("identifier");
                    int level = ct.getInt("level");
                    ResourceLocation loc = ResourceLocation.parse(id);
                    this.unlockedEnchantments.put(loc, level);
                }
            }
            if(ebdData.contains("Skills", Tag.TAG_COMPOUND)){
                CompoundTag skillsTag = ebdData.getCompound("Skills");
                for (SkillType type : SkillType.values()) {
                    String name = type.id;
                    if (skillsTag.contains(name)) {
                        if (skillsTag.contains(name, Tag.TAG_INT)) {
                            this.skillLevels.put(name, skillsTag.getInt(name));
                        } else if (skillsTag.contains(name, Tag.TAG_DOUBLE)) {
                            this.skillXPs.put(name, skillsTag.getDouble(name));
                        }
                    }
                    // Handle dynamic defaults or separate checks
                    if (skillsTag.contains(name + "_level")) {
                        this.skillLevels.put(name, skillsTag.getInt(name + "_level"));
                    }
                    if (skillsTag.contains(name + "_xp")) {
                        this.skillXPs.put(name, skillsTag.getDouble(name + "_xp"));
                    }
                }
            }
        }
    }

    @Override
    public Object2IntOpenHashMap<ResourceLocation> enchantment_overhaul$getUnlockedEnchantments() {
        return unlockedEnchantments;
    }

    @Override
    public void enchantment_overhaul$setUnlockedEnchantments(Object2IntOpenHashMap<ResourceLocation> map) {
        this.unlockedEnchantments = map;
    }

    @Override
    public int ebd$getSkillLevel(String skill) {
        return skillLevels.getOrDefault(skill, 1);
    }

    @Override
    public double ebd$getSkillXP(String skill) {
        return skillXPs.getOrDefault(skill, 0.0);
    }

    @Override
    public void ebd$setSkillLevel(String skill, int level) {
        skillLevels.put(skill, level);
    }

    @Override
    public void ebd$setSkillXP(String skill, double xp) {
        skillXPs.put(skill, xp);
    }

    @Override
    public void ebd$addSkillXP(String skill, double amount, ServerPlayer player) {
        if (player.level().isClientSide()) return;
        int currentLevel = ebd$getSkillLevel(skill);
        double currentXp = ebd$getSkillXP(skill);
        double newXp = currentXp + amount;
        double needed = LBDConfig.INSTANCE.getXPNeededForLevel(skill, currentLevel);
        boolean levelUp = false;
        while (newXp >= needed) {
            newXp -= needed;
            currentLevel++;
            needed = LBDConfig.INSTANCE.getXPNeededForLevel(skill, currentLevel);
            levelUp = true;
        }
        ebd$setSkillLevel(skill, currentLevel);
        ebd$setSkillXP(skill, newXp);

        if (levelUp) {
            SkillType sType = SkillType.fromId(skill);
            String skillName = sType != null ? sType.displayName : skill;
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§6❖ §eВаш уровень в навыке " + skillName + " повысился до " + currentLevel + "!"));
            player.playNotifySound(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.2f);
        }

        aiefu.ebd.network.ServersideNetworkManager.sendSkillUpdate(player, skill, currentLevel, newXp, needed, amount);
    }

    @Unique
    private long minerPenaltyTime = 0;
    @Unique
    private long lumberjackPenaltyTime = 0;

    @Override
    public long ebd$getMinerPenaltyTime() { return minerPenaltyTime; }
    @Override
    public void ebd$setMinerPenaltyTime(long time) { this.minerPenaltyTime = time; }
    @Override
    public long ebd$getLumberjackPenaltyTime() { return lumberjackPenaltyTime; }
    @Override
    public void ebd$setLumberjackPenaltyTime(long time) { this.lumberjackPenaltyTime = time; }

    @Unique
    private long secondBreathCooldown = 0;

    @Override
    public long ebd$getSecondBreathCooldown() { return secondBreathCooldown; }
    @Override
    public void ebd$setSecondBreathCooldown(long time) { this.secondBreathCooldown = time; }
}

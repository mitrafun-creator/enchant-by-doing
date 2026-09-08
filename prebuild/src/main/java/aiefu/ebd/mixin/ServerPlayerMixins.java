package aiefu.ebd.mixin;

import aiefu.ebd.EBDCommon;
import aiefu.ebd.IServerPlayerAcc;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
    @Unique
    private int globalLevel = 1;
    @Unique
    private double globalXP = 0.0;
    @Unique
    private int skillPoints = 0;
    @Unique
    private final Map<String, Integer> perkLevels = new HashMap<>();

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

        CompoundTag globalTag = new CompoundTag();
        globalTag.putInt("GlobalLevel", this.globalLevel);
        globalTag.putDouble("GlobalXP", this.globalXP);
        globalTag.putInt("SkillPoints", this.skillPoints);
        CompoundTag perksTag = new CompoundTag();
        perkLevels.forEach(perksTag::putInt);
        globalTag.put("Perks", perksTag);
        tag.put("Global", globalTag);

        compound.put("ebddata", tag);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readUnlockedEnchantmentsDataEOVR(CompoundTag compound, CallbackInfo ci){
        this.unlockedEnchantments.clear();
        this.skillLevels.clear();
        this.skillXPs.clear();
        this.globalLevel = 1;
        this.globalXP = 0.0;
        this.skillPoints = 0;
        this.perkLevels.clear();
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
            if (ebdData.contains("Global", Tag.TAG_COMPOUND)) {
                CompoundTag globalTag = ebdData.getCompound("Global");
                this.globalLevel = Math.max(1, globalTag.getInt("GlobalLevel"));
                this.globalXP = globalTag.getDouble("GlobalXP");
                this.skillPoints = Math.max(0, globalTag.getInt("SkillPoints"));
                if (globalTag.contains("Perks", Tag.TAG_COMPOUND)) {
                    CompoundTag perksTag = globalTag.getCompound("Perks");
                    for (String key : perksTag.getAllKeys()) {
                        this.perkLevels.put(key, perksTag.getInt(key));
                    }
                }
            } else {
                double retroactiveXP = 0;
                for (SkillType st : SkillType.values()) {
                    int lvl = this.skillLevels.getOrDefault(st.id, 1);
                    for (int l = 2; l <= lvl; l++) {
                        retroactiveXP += l;
                    }
                }
                if (retroactiveXP > 0) {
                    this.globalLevel = 1;
                    this.globalXP = 0.0;
                    this.skillPoints = 0;
                    double needed = aiefu.ebd.GlobalPerks.getNeededXPForLevel(this.globalLevel);
                    while (retroactiveXP >= needed) {
                        retroactiveXP -= needed;
                        this.globalLevel++;
                        this.skillPoints++;
                        needed = aiefu.ebd.GlobalPerks.getNeededXPForLevel(this.globalLevel);
                    }
                    this.globalXP = retroactiveXP;
                }
            }
        }
        ebd$applyGlobalPerkAttributes();
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
        double levelsGainedXP = 0;
        while (newXp >= needed) {
            newXp -= needed;
            currentLevel++;
            needed = LBDConfig.INSTANCE.getXPNeededForLevel(skill, currentLevel);
            levelUp = true;
            levelsGainedXP += currentLevel;
        }
        ebd$setSkillLevel(skill, currentLevel);
        ebd$setSkillXP(skill, newXp);

        if (levelUp) {
            SkillType sType = SkillType.fromId(skill);
            Component skillComp = sType != null ? sType.getDisplayName() : Component.literal(skill);
            player.sendSystemMessage(Component.literal("§6❖ ").append(Component.translatable("skill.enchant_by_doing.level_up", skillComp, currentLevel).withStyle(net.minecraft.ChatFormatting.YELLOW)));
            player.playNotifySound(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.2f);
            ebd$addGlobalXP(levelsGainedXP, player);
        }

        aiefu.ebd.network.ServersideNetworkManager.sendSkillUpdate(player, skill, currentLevel, newXp, needed, amount);
    }

    @Override
    public int ebd$getGlobalLevel() { return globalLevel; }
    @Override
    public void ebd$setGlobalLevel(int level) { this.globalLevel = Math.max(1, level); }
    @Override
    public double ebd$getGlobalXP() { return globalXP; }
    @Override
    public void ebd$setGlobalXP(double xp) { this.globalXP = xp; }
    @Override
    public int ebd$getSkillPoints() { return skillPoints; }
    @Override
    public void ebd$setSkillPoints(int points) { this.skillPoints = Math.max(0, points); }
    @Override
    public int ebd$getPerkLevel(String perkId) { return perkLevels.getOrDefault(perkId, 0); }
    @Override
    public void ebd$setPerkLevel(String perkId, int level) { perkLevels.put(perkId, level); }
    @Override
    public Map<String, Integer> ebd$getAllPerks() { return new HashMap<>(perkLevels); }

    @Override
    public void ebd$addGlobalXP(double amount, ServerPlayer player) {
        if (!LBDConfig.INSTANCE.enableGlobalLevelSystem) return;
        if (player.level().isClientSide() || amount <= 0) return;
        double currentXp = this.globalXP + amount;
        double needed = aiefu.ebd.GlobalPerks.getNeededXPForLevel(this.globalLevel);
        boolean leveledUp = false;
        while (currentXp >= needed) {
            currentXp -= needed;
            this.globalLevel++;
            this.skillPoints++;
            needed = aiefu.ebd.GlobalPerks.getNeededXPForLevel(this.globalLevel);
            leveledUp = true;
        }
        this.globalXP = currentXp;

        if (leveledUp) {
            player.sendSystemMessage(Component.literal("§6❖ ").append(
                Component.translatable("global_level.enchant_by_doing.level_up", this.globalLevel, this.skillPoints)
                    .withStyle(net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD)
            ));
            player.playNotifySound(net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, net.minecraft.sounds.SoundSource.PLAYERS, 0.7f, 1.0f);
            player.playNotifySound(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.3f);
        }

        aiefu.ebd.network.ServersideNetworkManager.sendGlobalSync(player);
    }

    @Override
    public void ebd$applyGlobalPerkAttributes() {
        ServerPlayer player = (ServerPlayer) (Object) this;
        var attr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (attr != null) {
            ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "global_perk_health");
            attr.removeModifier(modId);
            int healthRank = ebd$getPerkLevel("health_boost");
            if (healthRank > 0) {
                attr.addOrUpdateTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    modId, healthRank * 1.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }
        var waterAttr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.WATER_MOVEMENT_EFFICIENCY);
        if (waterAttr != null) {
            ResourceLocation waveModId = ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "global_perk_water_movement");
            waterAttr.removeModifier(waveModId);
            int waveRank = ebd$getPerkLevel("wave_rider");
            if (waveRank > 0) {
                waterAttr.addOrUpdateTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    waveModId, waveRank * 0.5, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }
        var oxygenAttr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.OXYGEN_BONUS);
        if (oxygenAttr != null) {
            ResourceLocation oxyModId = ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "global_perk_oxygen");
            oxygenAttr.removeModifier(oxyModId);
            int waveRank = ebd$getPerkLevel("wave_rider");
            if (waveRank > 0) {
                oxygenAttr.addOrUpdateTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    oxyModId, waveRank * 2.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }
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

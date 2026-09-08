package aiefu.ebd;

import aiefu.ebd.entity.ThrownAxeEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

import java.util.ArrayList;
import java.util.List;

public class EBDGameplayEvents {
    public static final ThreadLocal<net.minecraft.world.item.enchantment.ItemEnchantments> ACTIVE_PLACING_BOAT_ENCHANTMENTS =
            ThreadLocal.withInitial(() -> net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);

    public static class OffhandAttackScheduled {
        public final int targetId;
        public int ticksRemaining;

        public OffhandAttackScheduled(int targetId, int ticksRemaining) {
            this.targetId = targetId;
            this.ticksRemaining = ticksRemaining;
        }
    }

    public static final java.util.Map<java.util.UUID, OffhandAttackScheduled> SCHEDULED_OFFHAND_ATTACKS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> IS_PERFORMING_OFFHAND_ATTACK = ThreadLocal.withInitial(() -> false);



    @SubscribeEvent
    public void onLivingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim instanceof ServerPlayer player) {
            int warriorLevel = Utils.getSkillLevel(player, "warrior");
            if (warriorLevel >= 40) {
                ItemStack offhand = player.getOffhandItem();
                if (offhand.getItem() instanceof net.minecraft.world.item.ShieldItem) {
                    net.minecraft.world.item.Item shieldItem = offhand.getItem();
                    if (!player.getCooldowns().isOnCooldown(shieldItem)) {
                        if (player.level().random.nextFloat() < 0.20f) {
                            event.setCanceled(true);
                            if (!player.level().isClientSide) {
                                offhand.hurtAndBreak(10, (ServerLevel) player.level(), player, item -> {});
                            }
                            player.getCooldowns().addCooldown(shieldItem, 100);
                            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                    SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 0.8F + player.level().random.nextFloat() * 0.4F);
                            return;
                        }
                    }
                }
            }

            if (player instanceof IServerPlayerAcc acc) {
                int lightStep = acc.ebd$getPerkLevel("light_step");
                if (lightStep > 0 && event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
                    float mult = Math.max(0.0f, 1.0f - lightStep * 0.20f);
                    event.setAmount(event.getAmount() * mult);
                }
                int ironWill = acc.ebd$getPerkLevel("iron_will");
                if (ironWill > 0) {
                    if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)
                            || event.getSource().is(net.minecraft.world.damagesource.DamageTypes.WITHER)
                            || player.hasEffect(MobEffects.POISON)
                            || player.hasEffect(MobEffects.WITHER)) {
                        float mult = Math.max(0.0f, 1.0f - ironWill * 0.15f);
                        event.setAmount(event.getAmount() * mult);
                    }
                }
            }
        }

        if (victim.getVehicle() instanceof Boat boat) {
            if (boat instanceof IBoatEnchanted enchanted && Utils.getEnchantmentLevel(enchanted.ebd$getBoatEnchantments(), "fireproof_boat") > 0) {
                if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }

        Entity directAttacker = event.getSource().getDirectEntity();
        Entity sourceEntity = event.getSource().getEntity();
        if (sourceEntity instanceof Player player) {
            float damage = event.getAmount();
            float modifier = 1.0f;
            if (directAttacker instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow && !(directAttacker instanceof net.minecraft.world.entity.projectile.ThrownTrident)) {
                int hunterLevel = Utils.getSkillLevel(player, "hunter");
                modifier += hunterLevel * 0.01f;
            } else if (directAttacker == player) {
                ItemStack weapon = player.getMainHandItem();
                if (!weapon.isEmpty()) {
                    String itemKey = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(weapon.getItem()).toString();
                    if (weapon.getItem() instanceof SwordItem || LBDConfig.INSTANCE.warriorWeapons.contains(itemKey)) {
                        int warriorLevel = Utils.getSkillLevel(player, "warrior");
                        modifier += warriorLevel * 0.01f;
                    } else if (weapon.getItem() instanceof AxeItem || LBDConfig.INSTANCE.lumberjackWeapons.contains(itemKey)) {
                        int lumberjackLevel = Utils.getSkillLevel(player, "lumberjack");
                        modifier += lumberjackLevel * 0.01f;
                    }
                }
            }
            if (modifier != 1.0f) {
                event.setAmount(damage * modifier);
            }
        }

        if (directAttacker instanceof LivingEntity attacker) {
            ItemStack weapon = attacker.getItemInHand(InteractionHand.MAIN_HAND);
            if (weapon.isEmpty()) return;

            // 1. Quicksand (Shovel)
            if (weapon.getItem() instanceof ShovelItem && EBDCommon.isEnchantmentEnabled(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "quicksand"))) {
                int level = Utils.getEnchantmentLevel(weapon, "quicksand");
                if (level > 0) {
                    int slownessLevel = level - 1; // Level 1 -> Slowness I (0), Level 2 -> Slowness II (1)
                    victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, slownessLevel));
                }
            }

            // 2. Sting (Hoe)
            if (weapon.getItem() instanceof HoeItem && EBDCommon.isEnchantmentEnabled(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "sting"))) {
                int level = Utils.getEnchantmentLevel(weapon, "sting");
                if (level > 0) {
                    int poisonLevel = level - 1; // Level 1 -> Poison I (0), Level 2 -> Poison II (1)
                    victim.addEffect(new MobEffectInstance(MobEffects.POISON, 40, poisonLevel));
                }
            }

            // 3. Disarm (Shovel)
            if (weapon.getItem() instanceof ShovelItem && EBDCommon.isEnchantmentEnabled(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "disarm"))) {
                int level = Utils.getEnchantmentLevel(weapon, "disarm");
                if (level > 0 && !victim.level().isClientSide()) {
                    ItemStack victimWeapon = victim.getItemBySlot(EquipmentSlot.MAINHAND);
                    if (!victimWeapon.isEmpty()) {
                        double chance = 0.05 + 0.05 * level; // 10%, 15%, 20%
                        if (victim instanceof Player) {
                            chance /= 2.0; // Halved for players
                        }
                        if (victim.level().random.nextFloat() < chance) {
                            // Safe disarm logic
                            ItemEntity itemEntity = new ItemEntity(victim.level(), victim.getX(), victim.getY() + 0.5, victim.getZ(), victimWeapon.copy());
                            itemEntity.setNoPickUpDelay();
                            victim.level().addFreshEntity(itemEntity);
                            victim.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                            
                            // Воспроизводим звук
                            Level levelObj = victim.level();
                            levelObj.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                                    SoundEvents.ANVIL_FALL, SoundSource.PLAYERS,
                                    0.9F, 1.0F + levelObj.random.nextFloat() * 0.2F);
                            levelObj.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                                    SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS,
                                    0.9F, 1.0F + levelObj.random.nextFloat() * 0.2F);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        Entity killer = event.getSource().getEntity();
        if (killer instanceof ServerPlayer player) {
            int warriorLevel = Utils.getSkillLevel(player, "warrior");
            if (warriorLevel >= 20) {
                if (player.level().random.nextFloat() < 0.20f) {
                    List<ItemEntity> extraDrops = new ArrayList<>();
                    for (ItemEntity drop : event.getDrops()) {
                        ItemStack stack = drop.getItem();
                        if (!stack.isEmpty()) {
                            ItemEntity extra = new ItemEntity(
                                player.level(), 
                                drop.getX(), drop.getY(), drop.getZ(), 
                                stack.copy()
                            );
                            extra.setDeltaMovement(drop.getDeltaMovement());
                            extraDrops.add(extra);
                        }
                    }
                    if (!extraDrops.isEmpty()) {
                        event.getDrops().addAll(extraDrops);
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6F, 1.2F);
                    }
                }
            }
        }

        // Rare Dusty Book drop from Skeletons, Zombies, Wither Skeletons, Drowned, Husks
        LivingEntity victim = event.getEntity();
        if (!victim.level().isClientSide()) {
            boolean isWitherSkeleton = victim instanceof net.minecraft.world.entity.monster.WitherSkeleton;
            boolean isEligible = isWitherSkeleton
                    || victim instanceof net.minecraft.world.entity.monster.Skeleton
                    || victim instanceof net.minecraft.world.entity.monster.Zombie
                    || victim instanceof net.minecraft.world.entity.monster.Husk
                    || victim instanceof net.minecraft.world.entity.monster.Drowned;

            if (isEligible && (event.isRecentlyHit() || killer instanceof Player)) {
                double baseChance = isWitherSkeleton
                        ? LBDConfig.INSTANCE.dustyBookWitherSkeletonDropChance
                        : LBDConfig.INSTANCE.dustyBookMonsterDropChance;

                int looting = 0;
                if (killer instanceof LivingEntity livingKiller) {
                    ItemStack killerWeapon = livingKiller.getItemInHand(InteractionHand.MAIN_HAND);
                    Entity directEntity = event.getSource().getDirectEntity();
                    if (directEntity instanceof ThrownAxeEntity thrownAxe) {
                        killerWeapon = thrownAxe.getAxeItem();
                    }
                    if (!killerWeapon.isEmpty()) {
                        var regOpt = victim.level().registryAccess().registry(net.minecraft.core.registries.Registries.ENCHANTMENT);
                        if (regOpt.isPresent()) {
                            var lootingHolderOpt = regOpt.get().getHolder(net.minecraft.world.item.enchantment.Enchantments.LOOTING);
                            if (lootingHolderOpt.isPresent()) {
                                looting = killerWeapon.getEnchantments().getLevel(lootingHolderOpt.get());
                            }
                        }
                    }
                }

                double finalChance = baseChance + (looting * 0.01);
                if (victim.level().random.nextFloat() < finalChance) {
                    event.getDrops().add(new ItemEntity(
                            victim.level(),
                            victim.getX(),
                            victim.getY() + 0.5,
                            victim.getZ(),
                            new ItemStack(EBDCommon.DUSTY_BOOK.get())
                    ));
                }
            }
        }

        if (killer instanceof LivingEntity livingKiller) {
            ItemStack weapon = livingKiller.getItemInHand(InteractionHand.MAIN_HAND);
            Entity direct = event.getSource().getDirectEntity();
            if (direct instanceof ThrownAxeEntity thrownAxe) {
                weapon = thrownAxe.getAxeItem();
            }
            if (weapon.isEmpty()) return;

            if (Utils.containsEnchantment(weapon, "executioner") && EBDCommon.isEnchantmentEnabled(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "executioner"))) {
                double x = victim.getX();
                double y = victim.getY();
                double z = victim.getZ();
                Level level = victim.level();

                var registry = livingKiller.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                int looting = weapon.getEnchantments().getLevel(registry.getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING));
                float skullChance = 0.025f + 0.01f * looting;

                if (victim instanceof net.minecraft.world.entity.monster.WitherSkeleton) {
                    boolean alreadyDropped = false;
                    for (ItemEntity drop : event.getDrops()) {
                        if (drop.getItem().is(Items.WITHER_SKELETON_SKULL)) {
                            alreadyDropped = true;
                            break;
                        }
                    }
                    if (alreadyDropped) {
                        event.getDrops().add(new ItemEntity(level, x, y, z, new ItemStack(Items.WITHER_SKELETON_SKULL)));
                    } else {
                        if (level.random.nextFloat() < skullChance) {
                            event.getDrops().add(new ItemEntity(level, x, y, z, new ItemStack(Items.WITHER_SKELETON_SKULL)));
                        }
                    }
                } else if (victim instanceof net.minecraft.world.entity.monster.Skeleton) {
                    if (level.random.nextFloat() < skullChance) {
                        event.getDrops().add(new ItemEntity(level, x, y, z, new ItemStack(Items.SKELETON_SKULL)));
                    }
                } else if (victim instanceof net.minecraft.world.entity.monster.Zombie) {
                    if (level.random.nextFloat() < skullChance) {
                        event.getDrops().add(new ItemEntity(level, x, y, z, new ItemStack(Items.ZOMBIE_HEAD)));
                    }
                } else if (victim instanceof net.minecraft.world.entity.monster.Creeper) {
                    if (level.random.nextFloat() < skullChance) {
                        event.getDrops().add(new ItemEntity(level, x, y, z, new ItemStack(Items.CREEPER_HEAD)));
                    }
                } else if (victim instanceof net.minecraft.world.entity.monster.piglin.Piglin) {
                    if (level.random.nextFloat() < skullChance) {
                        event.getDrops().add(new ItemEntity(level, x, y, z, new ItemStack(Items.PIGLIN_HEAD)));
                    }
                } else if (victim instanceof Player playerVictim) {
                    ItemStack head = new ItemStack(Items.PLAYER_HEAD);
                    head.set(DataComponents.PROFILE, new ResolvableProfile(playerVictim.getGameProfile()));
                    event.getDrops().add(new ItemEntity(level, x, y, z, head));
                }
            }
        }
    }

    @SubscribeEvent
    public void onUseItemTick(LivingEntityUseItemEvent.Tick event) {
        ItemStack stack = event.getItem();
        if (stack.is(Items.FLINT_AND_STEEL) && event.getEntity() instanceof Player player) {
            if (Utils.containsEnchantment(stack, "dragons_breath") && EBDCommon.isEnchantmentEnabled(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "dragons_breath"))) {
                int elapsed = stack.getUseDuration(player) - event.getDuration();
                Level level = player.level();

                // Durability consumption
                if (elapsed > 0 && elapsed % 10 == 0 && !level.isClientSide) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        stack.hurtAndBreak(1, (ServerLevel) level, serverPlayer, item -> {});
                    }
                }

                // Flamethrower cone math
                Vec3 look = player.getLookAngle();
                Vec3 eyePos = player.getEyePosition();
                
                // 1. Spawning fire particles — конус с движением вперёд и вверх
                if (level.isClientSide) {
                    net.minecraft.util.RandomSource random = level.random;
                    // Перпендикулярные векторы к направлению взгляда
                    Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
                    if (right.lengthSqr() < 0.001) {
                        right = look.cross(new Vec3(1, 0, 0)).normalize();
                    }
                    Vec3 up = right.cross(look).normalize();
                    Vec3 muzzle = eyePos.add(look.scale(0.5)).subtract(0, 0.1, 0);
                    for (int i = 0; i < 10; i++) {
                        // Спавним ближе к мушке (0.2 — 1.5 блока)
                        double dist = 0.2 + random.nextDouble() * 1.3;
                        double coneRadius = dist * 0.25;
                        double angle = random.nextDouble() * Math.PI * 2.0;
                        double r = random.nextDouble() * coneRadius;
                        double offRight = Math.cos(angle) * r;
                        double offUp = Math.sin(angle) * r;
                        Vec3 spawnPos = muzzle
                                .add(look.scale(dist))
                                .add(right.scale(offRight))
                                .add(up.scale(offUp));
                        // Скорость: сильно вперёд + естественный подъём + малый боковой разброс
                        double forwardSpeed = 0.25 + random.nextDouble() * 0.2;
                        double upwardDrift  = 0.04 + random.nextDouble() * 0.06;
                        Vec3 velocity = look.scale(forwardSpeed)
                                .add(0, upwardDrift, 0)
                                .add(right.scale(offRight * 0.08))
                                .add(up.scale(offUp * 0.08));
                        level.addParticle(ParticleTypes.FLAME,
                                spawnPos.x, spawnPos.y, spawnPos.z,
                                velocity.x, velocity.y, velocity.z);
                    }
                }

                // Play flame sound
                if (elapsed % 5 == 0) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.65F, 1.1F + level.random.nextFloat() * 0.2F);
                }

                // 2. Scan and ignite entities in 8-block cone
                if (!level.isClientSide) {
                    AABB scanArea = player.getBoundingBox().inflate(8.0);
                    List<Entity> targets = level.getEntities(player, scanArea, net.minecraft.world.entity.EntitySelector.NO_CREATIVE_OR_SPECTATOR);
                    for (Entity target : targets) {
                        Vec3 toTarget = target.position().add(0, target.getBbHeight() / 2.0, 0).subtract(eyePos);
                        double distance = toTarget.length();
                        if (distance <= 8.0) {
                            double dot = toTarget.normalize().dot(look);
                            if (dot >= 0.866) { // 30 degrees
                                target.igniteForSeconds(4);
                                target.hurt(player.damageSources().playerAttack(player), 2.0F);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onUseItemStop(LivingEntityUseItemEvent.Stop event) {
        ItemStack stack = event.getItem();
        if (stack.getItem() instanceof AxeItem && event.getEntity() instanceof Player player) {
            if (Utils.containsEnchantment(stack, "leviathan") && EBDCommon.isEnchantmentEnabled(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "leviathan"))) {
                int elapsed = stack.getUseDuration(player) - event.getDuration();
                if (elapsed >= 10) { // requires charge time
                    Level level = player.level();
                    if (!level.isClientSide) {
                        ThrownAxeEntity thrownAxe = new ThrownAxeEntity(level, player, stack);
                        boolean isOffHand = (player.getItemInHand(InteractionHand.OFF_HAND) == stack);
                        thrownAxe.setFromOffHand(isOffHand);
                        thrownAxe.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.8F, 1.0F);
                        
                        if (player.getAbilities().instabuild) {
                            thrownAxe.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                        }

                        level.addFreshEntity(thrownAxe);
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Farmer Golden Beetroot Attraction logic (Server side)
        if (!player.level().isClientSide() && player instanceof ServerPlayer sp) {
            if (sp.hasEffect(EBDCommon.GOLDEN_BEETROOT_EFFECT)) {
                if (sp.level().getGameTime() % 5 == 0) {
                    sp.serverLevel().sendParticles(
                        net.minecraft.core.particles.ParticleTypes.HEART,
                        sp.getX(), sp.getY() + 1.0, sp.getZ(),
                        3, 0.5, 0.5, 0.5, 0.05
                    );
                    double radius = 16.0;
                    java.util.List<net.minecraft.world.entity.PathfinderMob> mobs = sp.level().getEntitiesOfClass(
                        net.minecraft.world.entity.PathfinderMob.class,
                        sp.getBoundingBox().inflate(radius),
                        entity -> entity instanceof net.minecraft.world.entity.animal.Animal
                               || entity instanceof net.minecraft.world.entity.npc.AbstractVillager
                    );
                    for (net.minecraft.world.entity.PathfinderMob mob : mobs) {
                        mob.getNavigation().moveTo(sp, 1.25D);
                    }
                }
            }

            // Global Perks: Soul Magnet, Iron Will, Wave Rider, Well-Fed
            if (sp instanceof IServerPlayerAcc acc) {
                int magnetRank = acc.ebd$getPerkLevel("soul_magnet");
                if (magnetRank > 0 && !sp.isSpectator()) {
                    double radius = 1.5 + magnetRank * 2.5;
                    java.util.List<net.minecraft.world.entity.ExperienceOrb> orbs = sp.level().getEntitiesOfClass(
                        net.minecraft.world.entity.ExperienceOrb.class,
                        sp.getBoundingBox().inflate(radius)
                    );
                    for (var orb : orbs) {
                        if (orb.isAlive() && orb.getValue() > 0) {
                            Vec3 toPlayer = sp.position().add(0, 0.5, 0).subtract(orb.position());
                            double dist = toPlayer.length();
                            if (dist > 0.2) {
                                Vec3 motion = toPlayer.normalize().scale(0.35 * (magnetRank == 2 ? 1.5 : 1.0));
                                orb.setDeltaMovement(orb.getDeltaMovement().scale(0.5).add(motion));
                            }
                            if (magnetRank >= 2 && dist < 1.5) {
                                orb.playerTouch(sp);
                            }
                        }
                    }
                }

                int ironWillRank = acc.ebd$getPerkLevel("iron_will");
                if (ironWillRank >= 3 && sp.isOnFire()) {
                    int remainingFire = sp.getRemainingFireTicks();
                    if (remainingFire > 2) {
                        sp.setRemainingFireTicks(remainingFire - 1);
                    }
                }

                int waveRank = acc.ebd$getPerkLevel("wave_rider");
                if (waveRank >= 2 && sp.isUnderWater()) {
                    sp.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 60, 0, false, false, false));
                }

                int wellFedRank = acc.ebd$getPerkLevel("well_fed");
                if (wellFedRank >= 3 && sp.isSprinting() && sp.tickCount % 40 == 0) {
                    float currentSat = sp.getFoodData().getSaturationLevel();
                    if (currentSat > 0 && currentSat < 20.0f) {
                        sp.getFoodData().setSaturation(Math.min(20.0f, currentSat + 0.1f));
                    }
                }
            }
        }

        // Passive 3: +25% attack speed with an axe (Level 60+)
        int lumberjackLevel = Utils.getSkillLevel(player, "lumberjack");
        var attackSpeedAttr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);
        if (attackSpeedAttr != null) {
            ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "lumberjack_attack_speed");
            boolean hasModifier = attackSpeedAttr.hasModifier(modifierId);
            ItemStack mainHand = player.getMainHandItem();
            boolean hasAxe = mainHand.getItem() instanceof net.minecraft.world.item.AxeItem;
            if (lumberjackLevel >= 60 && hasAxe) {
                if (!hasModifier) {
                    net.minecraft.world.entity.ai.attributes.AttributeModifier modifier = new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                            modifierId, 0.25, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    );
                    attackSpeedAttr.addOrUpdateTransientModifier(modifier);
                }
            } else {
                if (hasModifier) {
                    attackSpeedAttr.removeModifier(modifierId);
                }
            }
        }

        // Passive 4: Scheduled offhand attacks ticking (Level 80+)
        if (!player.level().isClientSide && player instanceof ServerPlayer sp) {
            OffhandAttackScheduled scheduled = SCHEDULED_OFFHAND_ATTACKS.get(sp.getUUID());
            if (scheduled != null) {
                scheduled.ticksRemaining--;
                if (scheduled.ticksRemaining <= 0) {
                    SCHEDULED_OFFHAND_ATTACKS.remove(sp.getUUID());
                    Entity target = sp.level().getEntity(scheduled.targetId);
                    if (target != null && target.isAlive() && sp.distanceToSqr(target) <= 36.0) { // check reach
                        ItemStack main = sp.getMainHandItem();
                        ItemStack off = sp.getOffhandItem();
                        if (main.getItem() instanceof net.minecraft.world.item.AxeItem && off.getItem() instanceof net.minecraft.world.item.AxeItem) {
                            IS_PERFORMING_OFFHAND_ATTACK.set(true);
                            try {
                                target.invulnerableTime = 0;
                                if (target instanceof LivingEntity livingTarget) {
                                    livingTarget.hurtTime = 0;
                                    ((aiefu.ebd.mixin.LivingEntityAccessor) livingTarget).ebd$setLastHurt(0.0f);
                                }
                                ((aiefu.ebd.mixin.LivingEntityTickerAccessor) sp).ebd$setAttackStrengthTicker(200);

                                sp.setItemInHand(InteractionHand.MAIN_HAND, off);
                                sp.setItemInHand(InteractionHand.OFF_HAND, main);
                                sp.attack(target);
                                sp.swing(InteractionHand.OFF_HAND, true);
                            } finally {
                                sp.setItemInHand(InteractionHand.MAIN_HAND, main);
                                sp.setItemInHand(InteractionHand.OFF_HAND, off);
                                IS_PERFORMING_OFFHAND_ATTACK.set(false);
                            }
                        }
                    }
                }
            }
        }

        if (!player.level().isClientSide && player.level().getGameTime() % 100 == 0) { // every 5 seconds
            // Check if player is holding crossbow in mainhand or offhand
            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();
            
            // Loop player inventory
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(Items.CROSSBOW) && stack != main && stack != off) {
                    if (Utils.containsEnchantment(stack, "auto_reload") && EBDCommon.isEnchantmentEnabled(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "auto_reload"))) {
                        ChargedProjectiles charged = stack.get(DataComponents.CHARGED_PROJECTILES);
                        List<ItemStack> loaded = charged == null ? new ArrayList<>() : new ArrayList<>(charged.getItems());
                        
                        var registry = player.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                        var multishotHolder = registry.getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.MULTISHOT);
                        int multishotLevel = stack.getEnchantments().getLevel(multishotHolder);
                        int shotCount = multishotLevel > 0 ? 3 : 1;
                        
                        int magazineLevel = Utils.getEnchantmentLevel(stack, "magazine");
                        int maxCapacity = (magazineLevel + 1) * shotCount;
                        
                        if (loaded.size() < maxCapacity) {
                            boolean loadedAny = false;
                            for (int k = 0; k < shotCount; k++) {
                                ItemStack ammo = player.getProjectile(stack);
                                if (!ammo.isEmpty()) {
                                    ItemStack ammoCopy = ammo.copy();
                                    ammoCopy.setCount(1);
                                    loaded.add(ammoCopy);
                                    
                                    if (!player.getAbilities().instabuild) {
                                        ammo.shrink(1);
                                        if (ammo.isEmpty()) {
                                            player.getInventory().removeItem(ammo);
                                        }
                                    }
                                    loadedAny = true;
                                } else {
                                    break;
                                }
                            }
                            
                            if (loadedAny) {
                                stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(loaded));
                                if (player instanceof ServerPlayer serverPlayer) {
                                    serverPlayer.containerMenu.broadcastChanges();
                                }
                                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                        SoundEvents.CROSSBOW_LOADING_END, SoundSource.PLAYERS, 0.8F, 1.0F);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof AxeItem && Utils.containsEnchantment(stack, "leviathan") && EBDCommon.isEnchantmentEnabled(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "leviathan"))) {
            player.startUsingItem(event.getHand());
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        } else if (stack.is(Items.FLINT_AND_STEEL) && Utils.containsEnchantment(stack, "dragons_breath") && EBDCommon.isEnchantmentEnabled(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "dragons_breath"))) {
            player.startUsingItem(event.getHand());
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        } else if (stack.is(Items.EXPERIENCE_BOTTLE)) {
            // Make XP bottle drinkable instead of throwable
            player.startUsingItem(event.getHand());
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof AxeItem && Utils.containsEnchantment(stack, "leviathan") && EBDCommon.isEnchantmentEnabled(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "leviathan"))) {
            player.startUsingItem(event.getHand());
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        } else if (stack.is(Items.FLINT_AND_STEEL) && Utils.containsEnchantment(stack, "dragons_breath") && EBDCommon.isEnchantmentEnabled(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "dragons_breath"))) {
            player.startUsingItem(event.getHand());
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        } else if (stack.is(Items.EXPERIENCE_BOTTLE)) {
            // Make XP bottle drinkable instead of throwable (also intercept block right-click)
            player.startUsingItem(event.getHand());
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        } else if (stack.is(Items.GOLDEN_CARROT)) {
            // Allow planting vanilla golden carrot as GoldenCarrotBlock at Farming level 100
            net.minecraft.core.BlockPos clickedPos = event.getPos();
            net.minecraft.core.BlockPos above = clickedPos.above();
            net.minecraft.world.level.block.state.BlockState clickedState = player.level().getBlockState(clickedPos);
            net.minecraft.world.level.block.state.BlockState aboveState = player.level().getBlockState(above);
            if (clickedState.is(net.minecraft.world.level.block.Blocks.FARMLAND) && aboveState.isAir()) {
                if (!player.level().isClientSide()) {
                    int farmerLevel = Utils.getSkillLevel(player, "farmer");
                    if (farmerLevel >= 100) {
                        player.level().setBlockAndUpdate(above, EBDCommon.GOLDEN_CARROT_BLOCK.get().defaultBlockState());
                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                        player.level().playSound(null, above, net.minecraft.sounds.SoundEvents.CROP_PLANTED,
                                net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                    } else {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§6[ESO] §eТребуется уровень Фермерства 100 для посадки золотой моркови! (Текущий: " + farmerLevel + ")"));
                    }
                }
                // Cancel on both sides so vanilla doesn't eat the item or play animations
                event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide()));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onBreakSpeed(net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();
        if (tool.isEmpty()) return;

        if (tool.getItem() instanceof AxeItem && event.getState().is(net.minecraft.tags.BlockTags.LEAVES)) {
            event.setNewSpeed(9999.0F);
            return;
        }

        // Emergency Obsidian Mining with Iron Pickaxe (very slow: ~25-30 seconds)
        if (tool.is(Items.IRON_PICKAXE) && (event.getState().is(net.minecraft.world.level.block.Blocks.OBSIDIAN) || event.getState().is(net.minecraft.world.level.block.Blocks.CRYING_OBSIDIAN))) {
            event.setNewSpeed(9.0F);
            return;
        }

        if (tool.getItem() instanceof PickaxeItem) {
            int minerLevel = Utils.getSkillLevel(player, "miner");
            float speedFactor = 1.0f + LBDConfig.INSTANCE.minerBaseSpeedModifier + (minerLevel - 1) * 0.01f;
            event.setNewSpeed(event.getOriginalSpeed() * speedFactor);
        } else if (tool.getItem() instanceof AxeItem) {
            int lumberjackLevel = Utils.getSkillLevel(player, "lumberjack");
            float speedFactor = 1.0f + LBDConfig.INSTANCE.lumberjackBaseSpeedModifier + (lumberjackLevel - 1) * 0.01f;
            
            if (lumberjackLevel >= 80) {
                ItemStack off = player.getOffhandItem();
                if (off.getItem() instanceof net.minecraft.world.item.AxeItem) {
                    speedFactor *= 1.5f;
                }
            }
            event.setNewSpeed(event.getOriginalSpeed() * speedFactor);
        }
    }

    @SubscribeEvent
    public void onLivingChangeTarget(net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.monster.piglin.Piglin piglin) {
            if (event.getNewAboutToBeSetTarget() instanceof Player player) {
                int warriorLevel = Utils.getSkillLevel(player, "warrior");
                if (warriorLevel >= 60) {
                    if (piglin.getLastHurtByMob() != player) {
                        event.setCanceled(true);
                    }
                }
            }
        } else if (event.getEntity() instanceof net.minecraft.world.entity.monster.EnderMan enderman) {
            if (event.getNewAboutToBeSetTarget() instanceof Player player) {
                int warriorLevel = Utils.getSkillLevel(player, "warrior");
                if (warriorLevel >= 100) {
                    event.setCanceled(true);
                    try {
                        java.lang.reflect.Method teleportMethod = net.minecraft.world.entity.monster.EnderMan.class.getDeclaredMethod("teleport");
                        teleportMethod.setAccessible(true);
                        teleportMethod.invoke(enderman);
                    } catch (Exception e) {
                        double rx = enderman.getX() + (enderman.getRandom().nextDouble() - 0.5) * 16.0;
                        double ry = enderman.getY() + (double)(enderman.getRandom().nextInt(8) - 4);
                        double rz = enderman.getZ() + (enderman.getRandom().nextDouble() - 0.5) * 16.0;
                        enderman.teleportTo(rx, ry, rz);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onMobEffectAdded(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Added event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            int warriorLevel = Utils.getSkillLevel(player, "warrior");
            if (warriorLevel >= 60) {
                MobEffectInstance instance = event.getEffectInstance();
                if (instance.getEffect().value().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL) {
                    float reduction = 0.25f;
                    if (warriorLevel >= 80) {
                        reduction = 0.50f;
                    }
                    int originalDuration = instance.getDuration();
                    if (originalDuration > 20 && originalDuration < 1000000) {
                        int newDuration = Math.round(originalDuration * (1.0f - reduction));
                        ((aiefu.ebd.mixin.MobEffectInstanceAccessor) instance).ebd$setDuration(newDuration);
                    }
                }
            }
            int enchanterLevel = Utils.getSkillLevel(player, "enchanter");
            if (enchanterLevel >= 75) {
                MobEffectInstance instance = event.getEffectInstance();
                if (instance.getEffect().value().getCategory() != net.minecraft.world.effect.MobEffectCategory.HARMFUL) {
                    float multiplier = 1.5f;
                    if (enchanterLevel >= 100) {
                        multiplier = 2.0f;
                    }
                    int originalDuration = instance.getDuration();
                    if (originalDuration > 20 && originalDuration < 1000000) {
                        int newDuration = Math.round(originalDuration * multiplier);
                        ((aiefu.ebd.mixin.MobEffectInstanceAccessor) instance).ebd$setDuration(newDuration);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeathSave(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            int warriorLevel = Utils.getSkillLevel(player, "warrior");
            if (warriorLevel >= 100) {
                long currentTime = System.currentTimeMillis();
                long cooldownEnd = ((IServerPlayerAcc) player).ebd$getSecondBreathCooldown();
                if (currentTime >= cooldownEnd) {
                    event.setCanceled(true);
                    ((IServerPlayerAcc) player).ebd$setSecondBreathCooldown(currentTime + 300000L);
                    player.setHealth(1.0F);
                    var maxAbsAttr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_ABSORPTION);
                    if (maxAbsAttr != null && maxAbsAttr.getBaseValue() < 20.0) {
                        maxAbsAttr.setBaseValue(20.0);
                    }
                    player.setAbsorptionAmount(Math.max(player.getAbsorptionAmount(), 20.0F));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 4));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 2));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 2));
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 50, 0.5, 0.5, 0.5, 0.15);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onUseItemStart(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack stack = event.getItem();
            if (stack.getUseAnimation() == net.minecraft.world.item.UseAnim.DRINK) {
                int enchanterLevel = Utils.getSkillLevel(player, "enchanter");
                if (enchanterLevel >= 75) {
                    event.setDuration(event.getDuration() / 2);
                }
            }
        }
    }

    @SubscribeEvent
    public void onUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack stack = event.getItem();
            if (stack.getItem() instanceof net.minecraft.world.item.PotionItem) {
                int enchanterLevel = Utils.getSkillLevel(player, "enchanter");
                if (enchanterLevel >= 75) {
                    if (!player.getAbilities().instabuild) {
                        if (event.getResultStack().is(Items.GLASS_BOTTLE)) {
                            event.setResultStack(ItemStack.EMPTY);
                        } else {
                            // Scan and remove one glass bottle from player's inventory
                            for (int idx = 0; idx < player.getInventory().getContainerSize(); idx++) {
                                ItemStack invStack = player.getInventory().getItem(idx);
                                if (invStack.is(Items.GLASS_BOTTLE)) {
                                    invStack.shrink(1);
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (!player.level().isClientSide()) {
                        aiefu.ebd.entity.ThrownBottleEntity bottleProj = new aiefu.ebd.entity.ThrownBottleEntity(player.level(), player);
                        bottleProj.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
                        player.level().addFreshEntity(bottleProj);
                    }
                }
            }
            if (stack.is(Items.GLISTERING_MELON_SLICE)) {
                player.heal(4.0f);
            }
            if (stack.is(EBDCommon.GOLDEN_BEETROOT.get())) {
                player.removeAllEffects();
                player.getFoodData().setFoodLevel(Math.max(0, player.getFoodData().getFoodLevel() - 6));
            }
            // XP Bottle: drinkable, gives XP Boost effect
            if (stack.is(Items.EXPERIENCE_BOTTLE)) {
                if (!player.level().isClientSide()) {
                    net.minecraft.world.effect.MobEffectInstance existing = player.getEffect(EBDCommon.XP_BOOST);
                    // Apply level 1 effect (0 = amplifier 0 = 50% boost)
                    int amplifier = 0;
                    int duration = 6000; // 5 minutes
                    player.addEffect(new MobEffectInstance(EBDCommon.XP_BOOST, duration, amplifier));
                }
                // Return empty (bottle consumed)
                if (!player.getAbilities().instabuild) {
                    event.setResultStack(new ItemStack(Items.GLASS_BOTTLE));
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        if (player instanceof ServerPlayer sp && sp instanceof IServerPlayerAcc acc) {
            int silverTongue = acc.ebd$getPerkLevel("silver_tongue");
            if (silverTongue > 0 && event.getTarget() instanceof net.minecraft.world.entity.npc.Villager villager) {
                villager.getGossips().add(sp.getUUID(), net.minecraft.world.entity.ai.gossip.GossipType.MINOR_POSITIVE, silverTongue * 25);
            }
        }

        ItemStack stack = event.getItemStack();
        if (stack.is(EBDCommon.GOLDEN_WHEAT.get())) {
            if (event.getTarget() instanceof net.minecraft.world.entity.animal.Animal animal) {
                if (animal instanceof net.minecraft.world.entity.animal.Cow || animal instanceof net.minecraft.world.entity.animal.Sheep) {
                    if (animal.isBaby()) {
                        animal.setAge(0);
                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                        animal.level().playSound(null, animal.blockPosition(), net.minecraft.sounds.SoundEvents.GENERIC_EAT, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
                        if (animal.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, animal.getX(), animal.getY() + 0.5, animal.getZ(), 7, 0.25, 0.25, 0.25, 0.05);
                        }
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        event.setCanceled(true);
                    } else if (animal.getAge() == 0 && !animal.isInLove()) {
                        animal.setInLove(player);
                        animal.getPersistentData().putBoolean("ebd$golden_bred", true);
                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                        animal.level().playSound(null, animal.blockPosition(), net.minecraft.sounds.SoundEvents.GENERIC_EAT, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
                        if (animal.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART, animal.getX(), animal.getY() + 0.5, animal.getZ(), 7, 0.25, 0.25, 0.25, 0.05);
                        }
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockDrops(BlockDropsEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getBreaker() instanceof ServerPlayer player) {
            int lumberjackLevel = Utils.getSkillLevel(player, "lumberjack");
            if (lumberjackLevel >= 40) {
                net.minecraft.world.level.block.state.BlockState state = event.getState();
                if (state.is(net.minecraft.world.level.block.Blocks.OAK_LEAVES) || state.is(net.minecraft.world.level.block.Blocks.DARK_OAK_LEAVES)) {
                    ItemStack tool = event.getTool();
                    if (tool.isEmpty() || tool.getItem() instanceof net.minecraft.world.item.AxeItem) {
                        boolean hasApple = false;
                        for (net.minecraft.world.entity.item.ItemEntity drop : event.getDrops()) {
                            if (drop.getItem().is(net.minecraft.world.item.Items.APPLE)) {
                                hasApple = true;
                                break;
                            }
                        }

                        if (!hasApple) {
                            int fortune = 0;
                            if (!tool.isEmpty()) {
                                var registry = player.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                                var fortuneHolder = registry.getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE);
                                fortune = tool.getEnchantments().getLevel(fortuneHolder);
                            }

                            double baseChance = 0.005;
                            if (fortune == 1) baseChance = 0.00625;
                            else if (fortune == 2) baseChance = 0.00833;
                            else if (fortune >= 3) baseChance = 0.025;

                            double targetChance = baseChance * 10.0;
                            double additionalChance = (targetChance - baseChance) / (1.0 - baseChance);
                            if (player.getRandom().nextDouble() < additionalChance) {
                                net.minecraft.core.BlockPos pos = event.getPos();
                                net.minecraft.world.entity.item.ItemEntity appleDrop = new net.minecraft.world.entity.item.ItemEntity(
                                        player.level(),
                                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                        new ItemStack(net.minecraft.world.item.Items.APPLE)
                                );
                                appleDrop.setDefaultPickUpDelay();
                                event.getDrops().add(appleDrop);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        int lumberjackLevel = Utils.getSkillLevel(player, "lumberjack");
        if (lumberjackLevel >= 80) {
            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();
            if (main.getItem() instanceof net.minecraft.world.item.AxeItem && off.getItem() instanceof net.minecraft.world.item.AxeItem) {
                Entity target = event.getTarget();
                if (!IS_PERFORMING_OFFHAND_ATTACK.get()) {
                    SCHEDULED_OFFHAND_ATTACKS.put(player.getUUID(), new OffhandAttackScheduled(target.getId(), 4));
                }
            }
        }
    }

    @SubscribeEvent
    public void onItemUseFinish(net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player && player instanceof IServerPlayerAcc acc) {
            int wellFed = acc.ebd$getPerkLevel("well_fed");
            if (wellFed > 0) {
                ItemStack stack = event.getItem();
                var food = stack.get(DataComponents.FOOD);
                if (food != null) {
                    float extraSat = food.saturation() * (wellFed * 0.25f);
                    player.getFoodData().setSaturation(Math.min(20.0f, player.getFoodData().getSaturationLevel() + extraSat));
                }
            }
        }
    }

    @SubscribeEvent
    public void onFarmlandTrample(net.neoforged.neoforge.event.level.BlockEvent.FarmlandTrampleEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player instanceof IServerPlayerAcc acc) {
            if (acc.ebd$getPerkLevel("light_step") >= 3) {
                event.setCanceled(true);
            }
        }
    }
}

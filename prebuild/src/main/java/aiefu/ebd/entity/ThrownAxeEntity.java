package aiefu.ebd.entity;

import aiefu.ebd.EBDCommon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ThrownAxeEntity extends AbstractArrow {
    private static final EntityDataAccessor<Byte> ID_LOYALTY = SynchedEntityData.defineId(ThrownAxeEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<ItemStack> DATA_AXE_ITEM = SynchedEntityData.defineId(ThrownAxeEntity.class, EntityDataSerializers.ITEM_STACK);
    private boolean dealtDamage;
    public int clientSideReturnTicks;
    private boolean fromOffHand;

    public boolean isFromOffHand() {
        return this.fromOffHand;
    }

    public void setFromOffHand(boolean fromOffHand) {
        this.fromOffHand = fromOffHand;
    }

    public ThrownAxeEntity(EntityType<? extends ThrownAxeEntity> type, Level level) {
        super(type, level);
    }

    public ThrownAxeEntity(Level level, LivingEntity shooter, ItemStack axeStack) {
        super(EBDCommon.THROWN_AXE.get(), shooter, level, axeStack, null);
        this.entityData.set(DATA_AXE_ITEM, axeStack.copy());
        byte loyalty = 0;
        if (level instanceof ServerLevel serverLevel) {
            loyalty = (byte) EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverLevel, axeStack, this);
            if (loyalty == 0) {
                int leviLevel = aiefu.ebd.Utils.getEnchantmentLevel(axeStack, "leviathan");
                if (leviLevel > 0) {
                    loyalty = (byte) leviLevel;
                }
            }
        }
        this.entityData.set(ID_LOYALTY, loyalty);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_LOYALTY, (byte) 0);
        builder.define(DATA_AXE_ITEM, new ItemStack(Items.IRON_AXE));
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        Entity owner = this.getOwner();
        int loyalty = this.entityData.get(ID_LOYALTY);
        if (loyalty > 0 && (this.dealtDamage || this.isNoPhysics()) && owner != null) {
            if (!this.isAcceptableReturnOwner()) {
                if (!this.level().isClientSide && this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }
                this.discard();
            } else {
                this.setNoPhysics(true);
                Vec3 vec3 = owner.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + vec3.y * 0.02 * (double) loyalty, this.getZ());
                if (this.level().isClientSide) {
                    this.yOld = this.getY();
                }

                // Скорость возврата: 0.12 за тик * уровень лоялти (было 0.05)
                double speed = 0.12 * (double) loyalty;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.97).add(vec3.normalize().scale(speed)));
                if (this.clientSideReturnTicks == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }
                this.clientSideReturnTicks++;
            }
        }

        super.tick();
    }

    private boolean isAcceptableReturnOwner() {
        Entity owner = this.getOwner();
        return owner != null && owner.isAlive() && (!(owner instanceof ServerPlayer) || !owner.isSpectator());
    }

    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 start, Vec3 end) {
        return this.dealtDamage ? null : super.findHitEntity(start, end);
    }

    public boolean isAxeInGround() {
        return this.inGround;
    }

    public ItemStack getAxeItem() {
        return this.entityData.get(DATA_AXE_ITEM);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        Entity target = hitResult.getEntity();
        float baseDamage = 8.0F;
        
        ItemStack axe = this.getAxeItem();
        if (axe.getItem() instanceof net.minecraft.world.item.AxeItem axeItem) {
            net.minecraft.world.item.Tier tier = axeItem.getTier();
            if (tier == net.minecraft.world.item.Tiers.NETHERITE) baseDamage = 10.0F;
            else if (tier == net.minecraft.world.item.Tiers.DIAMOND) baseDamage = 9.0F;
            else if (tier == net.minecraft.world.item.Tiers.IRON) baseDamage = 9.0F;
            else if (tier == net.minecraft.world.item.Tiers.STONE) baseDamage = 9.0F;
            else baseDamage = 7.0F;
        }
        
        Entity shooter = this.getOwner();
        DamageSource source = this.damageSources().trident(this, shooter == null ? this : shooter);
        if (this.level() instanceof ServerLevel serverLevel) {
            baseDamage = EnchantmentHelper.modifyDamage(serverLevel, this.getWeaponItem(), target, source, baseDamage);
        }

        this.dealtDamage = true;
        if (target.hurt(source, baseDamage)) {
            if (target.getType() == net.minecraft.world.entity.EntityType.ENDERMAN) {
                return;
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, target, source, this.getWeaponItem());
                
                // Manual Fire Aspect
                var registry = serverLevel.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                var fireAspectHolder = registry.getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT);
                int fireAspect = axe.getEnchantments().getLevel(fireAspectHolder);
                if (fireAspect > 0) {
                    target.igniteForSeconds(fireAspect * 4);
                }

                // Manual Knockback
                var knockbackHolder = registry.getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK);
                int knockbackLevel = axe.getEnchantments().getLevel(knockbackHolder);
                if (knockbackLevel > 0 && target instanceof LivingEntity living) {
                    Vec3 knockbackVec = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale((double)knockbackLevel * 0.6);
                    if (knockbackVec.lengthSqr() > 0.0) {
                        living.push(knockbackVec.x, 0.1, knockbackVec.z);
                    }
                }
            }

            if (target instanceof LivingEntity living) {
                this.doKnockback(living, source);
                this.doPostHurtEffects(living);
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
    }

    @Override
    protected void hitBlockEnchantmentEffects(ServerLevel level, BlockHitResult hitResult, ItemStack stack) {
        Vec3 vec3 = hitResult.getBlockPos().clampLocationWithin(hitResult.getLocation());
        EnchantmentHelper.onHitBlock(
            level,
            stack,
            this.getOwner() instanceof LivingEntity living ? living : null,
            this,
            null,
            vec3,
            level.getBlockState(hitResult.getBlockPos()),
            item -> this.kill()
        );
    }

    @Override
    public ItemStack getWeaponItem() {
        return this.getPickupItemStackOrigin();
    }

    @Override
    protected boolean tryPickup(Player player) {
        ItemStack axeStack = this.getPickupItem();
        if (this.fromOffHand && !player.getAbilities().instabuild) {
            net.minecraft.world.InteractionHand hand = net.minecraft.world.InteractionHand.OFF_HAND;
            ItemStack offhandStack = player.getItemInHand(hand);
            if (offhandStack.isEmpty()) {
                player.setItemInHand(hand, axeStack);
                return true;
            }
        }
        return super.tryPickup(player) || (this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(axeStack));
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.IRON_AXE);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void playerTouch(Player player) {
        if (this.ownedBy(player) || this.getOwner() == null) {
            super.playerTouch(player);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.dealtDamage = tag.getBoolean("DealtDamage");
        this.fromOffHand = tag.getBoolean("FromOffHand");
        if (tag.contains("AxeItem", 10)) {
            this.entityData.set(DATA_AXE_ITEM, ItemStack.parseOptional(this.registryAccess(), tag.getCompound("AxeItem")));
        }
        ItemStack weapon = this.getAxeItem();
        byte loyalty = 0;
        if (this.level() instanceof ServerLevel serverLevel) {
            loyalty = (byte) EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverLevel, weapon, this);
            if (loyalty == 0) {
                int leviLevel = aiefu.ebd.Utils.getEnchantmentLevel(weapon, "leviathan");
                if (leviLevel > 0) {
                    loyalty = (byte) leviLevel;
                }
            }
        }
        this.entityData.set(ID_LOYALTY, loyalty);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("DealtDamage", this.dealtDamage);
        tag.putBoolean("FromOffHand", this.fromOffHand);
        tag.put("AxeItem", this.getAxeItem().save(this.registryAccess()));
    }

    @Override
    public void tickDespawn() {
        int loyalty = this.entityData.get(ID_LOYALTY);
        if (this.pickup != AbstractArrow.Pickup.ALLOWED || loyalty <= 0) {
            super.tickDespawn();
        }
    }

    @Override
    protected float getWaterInertia() {
        return 0.99F;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }
}

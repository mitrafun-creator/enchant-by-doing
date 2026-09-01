package aiefu.ebd.mixin;

import aiefu.ebd.EBDCommon;
import aiefu.ebd.IBoatEnchanted;
import aiefu.ebd.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Boat.class)
public abstract class BoatMixin implements IBoatEnchanted {


    // --- Unique fields ---

    @Unique
    private ItemEnchantments ebd$enchantments = ItemEnchantments.EMPTY;

    /** true = игрок нажал вперёд хотя бы раз, автодрайв активен */
    @Unique
    private boolean ebd$autodriving = false;

    // --- Shadows ---

    @Shadow
    private boolean inputUp;

    @Shadow
    private boolean inputDown;

    @Shadow
    private float outOfControlTicks;

    @Shadow
    public abstract Boat.Status getStatus();

    @Shadow
    public abstract boolean isUnderWater();

    @Shadow
    public abstract LivingEntity getControllingPassenger();

    @Shadow
    protected abstract int getMaxPassengers();




    // ============================
    // IBoatEnchanted implementation
    // ============================

    @Override
    public ItemEnchantments ebd$getBoatEnchantments() {
        return ebd$enchantments;
    }


    @Override
    public void ebd$setBoatEnchantments(ItemEnchantments enchantments) {
        this.ebd$enchantments = enchantments == null ? ItemEnchantments.EMPTY : enchantments;
        Boat self = (Boat)(Object)this;
        if (!self.level().isClientSide()) {
            CompoundTag tag = new CompoundTag();
            if (!this.ebd$enchantments.isEmpty()) {
                net.minecraft.resources.RegistryOps<net.minecraft.nbt.Tag> ops =
                        net.minecraft.resources.RegistryOps.create(NbtOps.INSTANCE, self.level().registryAccess());
                ItemEnchantments.CODEC.encodeStart(ops, this.ebd$enchantments)
                        .result()
                        .ifPresent(nbt -> tag.put("ESOEnch", nbt));
            }
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(self, new aiefu.ebd.network.S2CBoatSyncPayload(self.getId(), tag));
        }
    }

    // ============================
    // NBT persistence
    // ============================

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void ebd$save(CompoundTag tag, CallbackInfo ci) {
        Boat self = (Boat)(Object)this;
        if (!ebd$enchantments.isEmpty()) {
            net.minecraft.resources.RegistryOps<net.minecraft.nbt.Tag> ops =
                    net.minecraft.resources.RegistryOps.create(NbtOps.INSTANCE, self.level().registryAccess());
            ItemEnchantments.CODEC.encodeStart(ops, ebd$enchantments)
                    .result()
                    .ifPresent(nbt -> tag.put("ESOEnch", nbt));
        }
        if (ebd$autodriving) tag.putBoolean("ESOAutodrive", true);
    }



    @Inject(method = "controlBoat", at = @At("HEAD"))
    private void ebd$applyAutodriveInput(CallbackInfo ci) {
        if (Utils.getEnchantmentLevel(ebd$getBoatEnchantments(), "autodrive") > 0 && getControllingPassenger() instanceof Player) {
            if (this.inputDown) {
                ebd$autodriving = false;
            } else if (this.inputUp) {
                ebd$autodriving = true;
            }

            if (ebd$autodriving) {
                this.inputUp = true;
            }
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void ebd$load(CompoundTag tag, CallbackInfo ci) {
        Boat self = (Boat)(Object)this;
        if (tag.contains("ESOEnch")) {
            net.minecraft.resources.RegistryOps<net.minecraft.nbt.Tag> ops =
                    net.minecraft.resources.RegistryOps.create(NbtOps.INSTANCE, self.level().registryAccess());
            ItemEnchantments.CODEC.parse(ops, tag.get("ESOEnch"))
                    .result()
                    .ifPresent(this::ebd$setBoatEnchantments);
        }
        ebd$autodriving = tag.getBoolean("ESOAutodrive");
    }

    // ============================
    // Main tick — all mechanics
    // ============================

    @Inject(method = "tick", at = @At("TAIL"))
    private void ebd$tick(CallbackInfo ci) {
        Boat self = (Boat)(Object)this;
        ItemEnchantments enc = ebd$getBoatEnchantments();
        Level level = self.level();

        if (enc.isEmpty()) return;

        LivingEntity driver = getControllingPassenger();
        Boat.Status status = getStatus();

        // 1. СКОЛЬЖЕНИЕ — логика перенесена в getGroundFriction

        // 2. МОРЕПЛАВАТЕЛЬ — ускорение на воде
        int seafarerLvl = Utils.getEnchantmentLevel(enc, "seafarer");
        if (seafarerLvl > 0) {
            ebd$tickSeafarer(self, seafarerLvl, status);
        }

        // 3. НЕГОРИТ — иммунитет к огню и плавание в лаве
        if (Utils.getEnchantmentLevel(enc, "fireproof_boat") > 0) {
            self.clearFire();
            for (Entity passenger : self.getPassengers()) {
                passenger.clearFire();
            }
            if (self.isInLava()) {
                Vec3 vel = self.getDeltaMovement();
                self.setDeltaMovement(vel.x, Math.min(vel.y + 0.15, 0.20), vel.z);
                self.setOnGround(false);
            }
        }

        // 4. ВЕСЛОСИПЕД — автодрайв
        if (Utils.getEnchantmentLevel(enc, "autodrive") > 0) {
            if (!(driver instanceof Player)) {
                ebd$autodriving = false;
            }
        }

        // 5. АЭРОФЛОТ — парение при падении
        if (Utils.getEnchantmentLevel(enc, "aeroflot") > 0) {
            ebd$tickAeroflot(self, status);
        }

        // 6. СУБМАРИНА — управление по вертикали + эффекты пассажиру
        if (Utils.getEnchantmentLevel(enc, "submarine") > 0) {
            this.outOfControlTicks = 0.0f; // Предотвращаем сброс игрока под водой
            if (driver instanceof Player player) {
                ebd$tickSubmarine(self, player, status, level);
            }
        }
    }

    // Fireproof: иммунитет к огню обеспечивается в tick() через clearFire()
    // @Inject(method="hurt") нельзя использовать т.к. hurt() объявлен в VehicleEntity, не в Boat

    // Вёсла в руки: логика перенесена в VehicleEntityMixin
    // (removePassenger объявлен в Entity, не в Boat)

    // ============================
    // Механики
    // ============================

    @Inject(method = "getGroundFriction", at = @At("RETURN"), cancellable = true)
    private void ebd$modifyGroundFriction(CallbackInfoReturnable<Float> cir) {
        if (Utils.getEnchantmentLevel(ebd$getBoatEnchantments(), "slipway") > 0) {
            return;
        }
        float friction = cir.getReturnValue();
        if (friction > 0.6F) {
            cir.setReturnValue(0.6F);
        }
    }

    @Unique
    private void ebd$tickSeafarer(Boat self, int lvl, Boat.Status status) {
        if ((status == Boat.Status.IN_WATER || status == Boat.Status.UNDER_WATER || status == Boat.Status.UNDER_FLOWING_WATER) && this.inputUp) {
            Vec3 look = self.getLookAngle();
            double boost = 0.015 * lvl;
            Vec3 vel = self.getDeltaMovement();
            self.setDeltaMovement(vel.x + look.x * boost, vel.y, vel.z + look.z * boost);
        }
    }



    @Unique
    private void ebd$tickAeroflot(Boat self, Boat.Status status) {
        if (status == Boat.Status.IN_AIR || (!self.onGround() && status != Boat.Status.IN_WATER && status != Boat.Status.UNDER_WATER && status != Boat.Status.UNDER_FLOWING_WATER && !self.isInLava())) {
            Vec3 vel = self.getDeltaMovement();
            // Медленное падение
            if (vel.y < -0.04) {
                double newY = Math.max(vel.y + 0.05, -0.04);
                self.setDeltaMovement(vel.x, newY, vel.z);
            }
            // Поддерживаем горизонтальную скорость
            Vec3 v = self.getDeltaMovement();
            double h = Math.sqrt(v.x * v.x + v.z * v.z);
            double target = 0.18;
            if (h > 0.01 && h < target) {
                double factor = target / h;
                factor = Math.min(factor, 1.15);
                self.setDeltaMovement(v.x * factor, v.y, v.z * factor);
            }
        }
    }

    @Unique
    private void ebd$tickSubmarine(Boat self, Player player, Boat.Status status, Level level) {
        if (status == Boat.Status.IN_WATER || status == Boat.Status.UNDER_WATER
                || status == Boat.Status.UNDER_FLOWING_WATER) {
            Vec3 vel = self.getDeltaMovement();

            boolean isJumping = ebd$getJumping(player);
            boolean isSprinting = player.isSprinting();
            boolean isZDown = level.isClientSide() && ebd$isClientZPressed();

            if (isJumping) {
                self.setDeltaMovement(vel.x, 0.15, vel.z);
            } else if (isSprinting || isZDown) {
                self.setDeltaMovement(vel.x, -0.15, vel.z);
            } else {
                self.setDeltaMovement(vel.x, 0.0, vel.z);
            }

            // Эффекты под водой
            if (isUnderWater() && !level.isClientSide()) {
                MobEffectInstance vision = player.getEffect(MobEffects.NIGHT_VISION);
                MobEffectInstance breath = player.getEffect(MobEffects.WATER_BREATHING);
                if (vision == null || vision.getDuration() < 40) {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 2400, 0, false, false));
                }
                if (breath == null || breath.getDuration() < 40) {
                    player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 2400, 0, false, false));
                }
            }
        }
    }

    @Unique
    private static boolean ebd$isClientZPressed() {
        try {
            Class<?> clazz = Class.forName("aiefu.ebd.client.EBDClient");
            return (boolean) clazz.getMethod("isZPressed").invoke(null);
        } catch (Exception e) {
            return false;
        }
    }

    /** Читаем защищённое поле jumping через рефлексию */
    @Unique
    private static boolean ebd$getJumping(Player player) {
        try {
            java.lang.reflect.Field f = net.minecraft.world.entity.LivingEntity.class.getDeclaredField("jumping");
            f.setAccessible(true);
            return f.getBoolean(player);
        } catch (Exception e) {
            return false;
        }
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "interact", at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
    private void ebd$beforeInteract(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        if (Utils.getEnchantmentLevel(ebd$getBoatEnchantments(), "submarine") > 0) {
            this.outOfControlTicks = 0.0f;
        }
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "canAddPassenger", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    private void ebd$canAddSubmarinePassenger(Entity passenger, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (Utils.getEnchantmentLevel(ebd$getBoatEnchantments(), "submarine") > 0) {
            Boat self = (Boat)(Object)this;
            cir.setReturnValue(self.getPassengers().size() < this.getMaxPassengers());
        }
    }

}

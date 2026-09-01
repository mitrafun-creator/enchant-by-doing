package aiefu.ebd.entity;

import aiefu.ebd.EBDCommon;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownBottleEntity extends ThrowableItemProjectile {
    public ThrownBottleEntity(EntityType<? extends ThrownBottleEntity> type, Level level) {
        super(type, level);
    }

    public ThrownBottleEntity(Level level, LivingEntity shooter) {
        super(EBDCommon.THROWN_BOTTLE.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.GLASS_BOTTLE;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);

        if (!this.level().isClientSide) {
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.GLASS_BOTTLE)),
                        this.getX(), this.getY(), this.getZ(), 15, 0.1, 0.1, 0.1, 0.05);
            }
            
            if (result.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) result;
                Entity target = entityHit.getEntity();
                Entity owner = this.getOwner();
                DamageSource source = this.damageSources().thrown(this, owner);
                target.hurt(source, 6.0F);
            }
            
            this.discard();
        }
    }
}

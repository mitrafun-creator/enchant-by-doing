package aiefu.ebd.mixin;

import aiefu.ebd.EBDCommon;
import com.google.common.collect.Maps;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootPool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(LootPool.class)
public class LootPoolMixins {

    @ModifyArg(
        method = "addRandomItems",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/functions/LootItemFunction;decorate(Ljava/util/function/BiFunction;Ljava/util/function/Consumer;Lnet/minecraft/world/level/storage/loot/LootContext;)Ljava/util/function/Consumer;"),
        index = 1
    )
    private Consumer<ItemStack> ESOPatchEnchantmentsInLoot(Consumer<ItemStack> original) {
        return stack -> {
            original.accept(stack);
            if(!stack.getEnchantments().isEmpty()){
                this.ESOPatchItemStackEnchantments(stack);
            }
        };
    }

    @Unique
    private void ESOPatchItemStackEnchantments(ItemStack stack){
        net.minecraft.world.item.enchantment.ItemEnchantments enchsComponent = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        Map<Holder<Enchantment>, Integer> enchs = new java.util.HashMap<>();
        enchsComponent.entrySet().forEach(entry -> enchs.put(entry.getKey(), entry.getValue()));

        Map<Holder<Enchantment>, Integer> enchantments = Maps.newLinkedHashMap();

        List<Holder<Enchantment>> list = new ArrayList<>(enchs.keySet());
        Collections.shuffle(list);
        int size = Math.min(list.size(), stack.getItem() == Items.ENCHANTED_BOOK ?
                EBDCommon.config.maxEnchantmentsOnLootBooks : EBDCommon.config.maxEnchantmentsOnLootItems);
        for (int i = 0; i < size; i++) {
            Holder<Enchantment> e = list.get(i);
            enchantments.put(e, enchs.get(e));
        }

        net.minecraft.world.item.enchantment.ItemEnchantments.Mutable builder = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        enchantments.forEach(builder::set);
        net.minecraft.world.item.enchantment.ItemEnchantments newItemEnchs = builder.toImmutable();

        if(stack.is(Items.ENCHANTED_BOOK)){
            stack.set(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS, newItemEnchs);
        } else {
            EnchantmentHelper.setEnchantments(stack, newItemEnchs);
        }
    }
}

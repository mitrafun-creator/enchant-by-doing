package aiefu.ebd.loot;

import aiefu.ebd.EBDCommon;
import aiefu.ebd.LBDConfig;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class DustyBookLootModifier extends LootModifier {
    public static final MapCodec<DustyBookLootModifier> CODEC = RecordCodecBuilder.mapCodec(
        inst -> codecStart(inst).apply(inst, DustyBookLootModifier::new)
    );

    public DustyBookLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ObjectArrayList<ItemStack> newLoot = new ObjectArrayList<>();
        boolean hasDustyBook = false;

        for (ItemStack stack : generatedLoot) {
            if (stack.is(Items.ENCHANTED_BOOK)) {
                newLoot.add(new ItemStack(EBDCommon.DUSTY_BOOK.get(), stack.getCount()));
                hasDustyBook = true;
            } else {
                if (stack.is(EBDCommon.DUSTY_BOOK.get())) {
                    hasDustyBook = true;
                }
                newLoot.add(stack);
            }
        }

        ResourceLocation tableId = context.getQueriedLootTableId();
        String path = tableId != null ? tableId.getPath().toLowerCase() : "";

        // 1. Archaeology injection (brushing suspicious sand / gravel in desert pyramid, well, ocean ruins, trail ruins, etc.)
        boolean isArchaeology = path.contains("archaeology") || path.contains("brush")
                || (context.hasParam(LootContextParams.TOOL) && context.getParam(LootContextParams.TOOL).is(Items.BRUSH));

        if (isArchaeology && !hasDustyBook) {
            double archChance = LBDConfig.INSTANCE.dustyBookArchaeologyChance;
            if (archChance > 0 && context.getRandom().nextDouble() < archChance) {
                newLoot.clear();
                newLoot.add(new ItemStack(EBDCommon.DUSTY_BOOK.get()));
                hasDustyBook = true;
            }
        }

        // 2. Fishing injection
        boolean isFishing = path.contains("fishing")
                || (context.hasParam(LootContextParams.TOOL) && context.getParam(LootContextParams.TOOL).getItem() instanceof net.minecraft.world.item.FishingRodItem);

        if (isFishing && !hasDustyBook) {
            boolean isTreasure = path.contains("treasure");
            double fishChance = isTreasure ? LBDConfig.INSTANCE.dustyBookFishingTreasureChance : LBDConfig.INSTANCE.dustyBookFishingChance;
            if (fishChance > 0 && context.getRandom().nextDouble() < fishChance) {
                newLoot.add(new ItemStack(EBDCommon.DUSTY_BOOK.get()));
                hasDustyBook = true;
            }
        }

        return newLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}

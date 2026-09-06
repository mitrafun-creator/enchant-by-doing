package aiefu.ebd.workstation;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.function.Supplier;

public enum WorkstationType {
    ANVIL(
        0,
        "anvil",
        "workstation.enchant_by_doing.anvil",
        () -> new ItemStack(Items.ANVIL),
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("enchant_by_doing", "requires_anvil")),
        List.of(Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL)
    ),
    ENCHANTING_TABLE(
        1,
        "enchanting_table",
        "workstation.enchant_by_doing.enchanting_table",
        () -> new ItemStack(Items.ENCHANTING_TABLE),
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("enchant_by_doing", "requires_enchanting_table")),
        List.of(Blocks.ENCHANTING_TABLE)
    ),
    FLETCHING_TABLE(
        2,
        "fletching_table",
        "workstation.enchant_by_doing.fletching_table",
        () -> new ItemStack(Items.FLETCHING_TABLE),
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("enchant_by_doing", "requires_fletching_table")),
        List.of(Blocks.FLETCHING_TABLE)
    ),
    ARMORER(
        3,
        "armorer",
        "workstation.enchant_by_doing.armorer",
        () -> new ItemStack(Items.SMITHING_TABLE),
        TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("enchant_by_doing", "requires_armorer")),
        List.of(Blocks.SMITHING_TABLE, Blocks.BLAST_FURNACE)
    );

    public final int bitIndex;
    public final String id;
    public final String translationKey;
    public final Supplier<ItemStack> iconSupplier;
    public final TagKey<Item> requiredItemTag;
    public final List<Block> matchingBlocks;

    WorkstationType(int bitIndex, String id, String translationKey, Supplier<ItemStack> iconSupplier, TagKey<Item> requiredItemTag, List<Block> matchingBlocks) {
        this.bitIndex = bitIndex;
        this.id = id;
        this.translationKey = translationKey;
        this.iconSupplier = iconSupplier;
        this.requiredItemTag = requiredItemTag;
        this.matchingBlocks = matchingBlocks;
    }

    public Component getDisplayName() {
        return Component.translatable(translationKey);
    }

    public static WorkstationType fromId(String id) {
        for (WorkstationType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}
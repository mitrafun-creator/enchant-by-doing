package aiefu.ebd;

import net.minecraft.world.item.enchantment.ItemEnchantments;

public interface IBlockEntityEnchanted {
    ItemEnchantments ebd$getBlockEntityEnchantments();
    void ebd$setBlockEntityEnchantments(ItemEnchantments enchantments);
    default void ebd$onEnchantmentsChanged() {}
}

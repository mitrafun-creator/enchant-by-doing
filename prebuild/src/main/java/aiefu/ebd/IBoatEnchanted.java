package aiefu.ebd;

import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Интерфейс для хранения зачарований на сущности лодки.
 * Используется через Mixin для передачи данных из предмета в энтити.
 */
public interface IBoatEnchanted {
    ItemEnchantments ebd$getBoatEnchantments();
    void ebd$setBoatEnchantments(ItemEnchantments enchantments);
}

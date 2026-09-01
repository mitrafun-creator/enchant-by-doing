package aiefu.ebd.mixin;

import aiefu.ebd.EBDCommon;
import aiefu.ebd.IServerPlayerAcc;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Mixin(EnchantedBookItem.class)
public abstract class EnchantedBookMixins extends Item {

    public EnchantedBookMixins(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if(!level.isClientSide() && player instanceof IServerPlayerAcc acc){
            Object2IntOpenHashMap<ResourceLocation> set = acc.enchantment_overhaul$getUnlockedEnchantments();
            net.minecraft.world.item.enchantment.ItemEnchantments enchsComponent = EnchantmentHelper.getEnchantmentsForCrafting(stack);
            Map<Holder<Enchantment>, Integer> map = new HashMap<>();
            enchsComponent.entrySet().forEach(entry -> map.put(entry.getKey(), entry.getValue()));

            int originalSize = map.size();
            Iterator<Map.Entry<Holder<Enchantment>, Integer>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<Holder<Enchantment>, Integer> entry = iterator.next();
                Holder<Enchantment> e = entry.getKey();
                ResourceLocation loc = e.unwrapKey().map(ResourceKey::location).orElse(null);
                if (loc == null) continue;
                int i = set.getInt(loc);
                int r = entry.getValue();
                if(i > 0){
                    if(EBDCommon.config.enableEnchantmentsLeveling && r > i){
                        set.put(loc, r);
                        iterator.remove();
                        this.esoSendMessage(e, r, player);
                    }
                } else {
                    set.put(loc, r);
                    iterator.remove();
                    this.esoSendMessage(e, r, player);
                }
            }
            if(map.size() != originalSize) {
                this.esoApplyEnchantments(map, stack);
                player.displayClientMessage(Component.translatable("eso.absorbingknowledge")
                        .withStyle(ChatFormatting.DARK_PURPLE), false);
                if(map.size() == 0){
                    stack.shrink(1);
                    player.displayClientMessage(Component.translatable("eso.booktoashes")
                            .withStyle(ChatFormatting.GOLD), false);
                    if (aiefu.ebd.Utils.getSkillLevel(player, "enchanter") >= 25) {
                        ItemStack bookStack = new ItemStack(net.minecraft.world.item.Items.BOOK);
                        if (stack.isEmpty()) {
                            stack = bookStack;
                        } else {
                            if (!player.getInventory().add(bookStack)) {
                                player.drop(bookStack, false);
                            }
                        }
                    }
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            } else {
                player.displayClientMessage(Component.translatable("eso.allreadylearned")
                        .withStyle(ChatFormatting.DARK_GREEN), false);
                return InteractionResultHolder.fail(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    @Unique
    private void esoSendMessage(Holder<Enchantment> e, int level, Player player){
        ChatFormatting style = ChatFormatting.AQUA;
        if(e.is(net.minecraft.tags.EnchantmentTags.CURSE)){
            if(!EBDCommon.config.enableCursesAmplifier) return;
            else {
                style = ChatFormatting.RED;
            }
        }
        MutableComponent c = Component.literal("[").withStyle(style);
        MutableComponent eName = EBDCommon.config.enableEnchantmentsLeveling ? ((MutableComponent) Enchantment.getFullname(e, level)).withStyle(style) : e.value().description().copy().withStyle(style);
        c.append(eName);
        c.append(Component.literal("]"));
        player.displayClientMessage(Component.translatable("eso.youlearned", c).withStyle(ChatFormatting.GOLD), false);
    }

    @Unique
    private void esoApplyEnchantments(Map<Holder<Enchantment>, Integer> map, ItemStack stack){
        net.minecraft.world.item.enchantment.ItemEnchantments.Mutable builder = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        map.forEach(builder::set);
        stack.set(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
    }

}

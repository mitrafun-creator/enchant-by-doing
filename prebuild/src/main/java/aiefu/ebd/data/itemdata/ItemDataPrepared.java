package aiefu.ebd.data.itemdata;

import aiefu.ebd.TagsUtils;
import aiefu.ebd.exception.ItemDoesNotExistException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ItemDataPrepared {
    protected boolean isEmpty = false;

    public Item item = Items.AIR;

    public int amount;

    public int baseAmount = -1;

    public List<Item> applicableItems = new ArrayList<>();

    public List<ItemDataPrepared> itemList;

    public int pos = 0;

    @Nullable
    public TagKey<Item> tagKey;

    public CompoundTag compoundTag;

    public Item remainderItem = Items.AIR;

    public boolean remainderEmpty = false;

    public CompoundTag remainderCompoundTag;

    public int remainderAmount;

    public boolean arrayOverride = false;

    //For sync
    public ItemData data;

    public ItemDataPrepared() {
    }

    public ItemDataPrepared(Void v) {
        this.isEmpty = true;
    }

    public ItemDataPrepared(ItemData data, ResourceLocation location, String eid, boolean shouldProcessArrays) throws ItemDoesNotExistException {
        this.data = data;
        if (data.itemArray != null && shouldProcessArrays) {
            this.itemList = new ArrayList<>();
            for(ItemData d : data.itemArray) {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(d.id));
                if (item == Items.AIR)
                    throw new ItemDoesNotExistException("Item id " + d + " in id array not found in game registry for enchantment recipe " + eid);

                this.applicableItems.add(item);

                this.itemList.add(new ItemDataPrepared(d, location, eid, false));
                this.arrayOverride = true;
            }
        } else {
            if (data.id == null) {
                this.isEmpty = true;
                return;
            }
            if (data.id.startsWith("tags#")) {
                this.tagKey = TagKey.create(Registries.ITEM, ResourceLocation.parse(data.id.substring(5)));
            } else {
                this.item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(data.id));
                if (this.item == Items.AIR)
                    throw new ItemDoesNotExistException("Item id " + data.id + " not found in game registry for enchantment recipe " + eid);
            }

            this.amount = data.amount;
            this.baseAmount = data.amount;

            if (data.tag != null) {
                try {
                    this.compoundTag = new TagParser(new StringReader(data.tag)).readStruct();
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            }
            if (data.remainderId != null) {
                if (!data.remainderId.isEmpty() && !data.remainderId.isBlank() && !data.remainderId.equalsIgnoreCase("empty")) {
                    this.remainderItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(data.remainderId));
                    if (remainderItem == Items.AIR)
                        throw new ItemDoesNotExistException("Remainder item id " + data.remainderId + " not found in game registry for enchantment recipe " + eid);
                } else remainderEmpty = true;
                remainderAmount = data.remainderAmount;
                if (data.remainderTag != null) {
                    try {
                        this.remainderCompoundTag = new TagParser(new StringReader(data.remainderTag)).readStruct();
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void applyTagToStack(ItemStack stack, @Nullable CompoundTag tag) {
        if (tag == null) return;
        if (tag.contains("Potion", 8)) {
            String potionId = tag.getString("Potion");
            net.minecraft.world.item.alchemy.Potion potion = BuiltInRegistries.POTION.get(ResourceLocation.parse(potionId));
            if (potion != null) {
                net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> holder = BuiltInRegistries.POTION.wrapAsHolder(potion);
                stack.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS, new net.minecraft.world.item.alchemy.PotionContents(holder));
            }
        } else {
            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        }
    }

    public @Nullable ItemStack getRemainderForStack(ItemStack stack) {
        if (this.remainderEmpty) {
            return stack.getItem().hasCraftingRemainingItem() ? new ItemStack(stack.getItem().getCraftingRemainingItem()) : ItemStack.EMPTY;
        } else if (this.remainderItem != Items.AIR) {
            ItemStack s = new ItemStack(this.remainderItem, this.remainderAmount);
            applyTagToStack(s, this.remainderCompoundTag);
            return s;
        } else return stack.getItem().hasCraftingRemainingItem() ? new ItemStack(stack.getItem().getCraftingRemainingItem()) : ItemStack.EMPTY;
    }

    public boolean testTag(ItemStack stack) {
        if (this.compoundTag == null) {
            return true;
        }
        if (this.compoundTag.contains("Potion", 8)) {
            String reqPotion = this.compoundTag.getString("Potion");
            net.minecraft.world.item.alchemy.PotionContents potionContents = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
            if (potionContents != null && potionContents.potion().isPresent()) {
                ResourceLocation potionId = potionContents.potion().get().unwrapKey().map(net.minecraft.resources.ResourceKey::location).orElse(null);
                return potionId != null && potionId.toString().equals(reqPotion);
            }
            return false;
        }
        net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }
        return TagsUtils.havePartialMatch(this.compoundTag, customData.copyTag());
    }

    public boolean testTag(ItemStack stack, CompoundTag ref) {
        if (ref == null) {
            return true;
        }
        if (ref.contains("Potion", 8)) {
            String reqPotion = ref.getString("Potion");
            net.minecraft.world.item.alchemy.PotionContents potionContents = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
            if (potionContents != null && potionContents.potion().isPresent()) {
                ResourceLocation potionId = potionContents.potion().get().unwrapKey().map(net.minecraft.resources.ResourceKey::location).orElse(null);
                return potionId != null && potionId.toString().equals(reqPotion);
            }
            return false;
        }
        net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }
        return TagsUtils.havePartialMatch(ref, customData.copyTag());
    }

    public boolean testItemStackMatch(ItemStack stack) {
        return testItemStackMatch(stack, 1.0f);
    }

    public boolean testItemStackMatch(ItemStack stack, float discountMultiplier) {
        if (arrayOverride) {
            for (ItemDataPrepared ids : itemList) {
                if (ids.item == stack.getItem()) {
                    int originalAmt = (ids.baseAmount >= 0) ? ids.baseAmount : ids.amount;
                    int discountedAmount = Math.max(1, Math.round(originalAmt * discountMultiplier));
                    if (stack.getCount() >= discountedAmount && testTag(stack, ids.compoundTag)) {
                        this.amount = discountedAmount;
                        this.remainderItem = ids.remainderItem;
                        this.remainderCompoundTag = ids.remainderCompoundTag;
                        this.remainderAmount = ids.remainderAmount;
                        return true;
                    }
                }
            }
        } else {
            int originalAmt = (this.baseAmount >= 0) ? this.baseAmount : this.amount;
            int discountedAmount = Math.max(1, Math.round(originalAmt * discountMultiplier));
            if ((tagKey != null && stack.is(tagKey) || stack.is(item)) && stack.getCount() >= discountedAmount) {
                if (testTag(stack)) {
                    this.amount = discountedAmount;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return this.isEmpty;
    }

    public void processTags() {
        if (tagKey != null) {
            for(Item i : BuiltInRegistries.ITEM){
                if (i.builtInRegistryHolder().is(tagKey)) {
                    this.applicableItems.add(i);
                }
            }
        }
    }

    public void resetPos() {
        this.pos = 0;
    }

    public void next() {
        if (++pos >= this.applicableItems.size()) {
            pos = 0;
        }
    }

    public ItemDataPrepared getNotNestedData() {
        return itemList.get(pos);
    }

    public Item getApplicableItem() {
        return this.applicableItems.get(pos);
    }
}

package aiefu.ebd.data.itemdata;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RecipeViewerItemData {
    protected ItemDataPrepared itemData;
    protected ItemStack stack = ItemStack.EMPTY;
    protected List<ItemStack> animatedStacks;

    protected boolean isAnimated = false;
    protected transient int pos = 0;

    public RecipeViewerItemData(ItemDataPrepared itemData) {
        this.itemData = itemData;
        this.animatedStacks = new ArrayList<>();
        
        float discountMultiplier = 1.0f;
        net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
        if (player != null) {
            int enchanterLevel = aiefu.ebd.Utils.getSkillLevel(player, "enchanter");
            discountMultiplier = 1.25f - ((Math.max(1, Math.min(100, enchanterLevel)) - 1) / 99.0f);
        }

        if(!itemData.applicableItems.isEmpty()){
            if(itemData.itemList != null){
                for (ItemDataPrepared prepared : itemData.itemList){
                    int origAmt = (prepared.baseAmount >= 0) ? prepared.baseAmount : prepared.amount;
                    int amount = Math.max(1, Math.round(origAmt * discountMultiplier));
                    ItemStack s = new ItemStack(prepared.item, amount);
                    if(prepared.compoundTag != null){
                        ItemDataPrepared.applyTagToStack(s, prepared.compoundTag);
                    }
                    this.animatedStacks.add(s);
                }
            } else if(itemData.tagKey != null){
                int origAmt = (itemData.baseAmount >= 0) ? itemData.baseAmount : itemData.amount;
                int amount = Math.max(1, Math.round(origAmt * discountMultiplier));
                for (Item item : itemData.applicableItems){
                    this.animatedStacks.add(new ItemStack(item, amount));
                }
            }
            this.isAnimated = animatedStacks.size() > 0;
        } else {
            int origAmt = (itemData.baseAmount >= 0) ? itemData.baseAmount : itemData.amount;
            int amount = Math.max(1, Math.round(origAmt * discountMultiplier));
            this.stack = new ItemStack(itemData.item, amount);
            if(itemData.compoundTag != null){
                ItemDataPrepared.applyTagToStack(stack, itemData.compoundTag);
            }
        }
    }

    public void next(){
        if(pos + 1 >= animatedStacks.size()){
            pos = 0;
        } else pos += 1;
    }

    public ItemStack getNextStack(){
        return animatedStacks.get(pos);
    }

    public ItemStack getStack() {
        return stack;
    }

    public boolean isAnimated(){
        return this.isAnimated;
    }

}

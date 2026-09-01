package aiefu.ebd.menu;

import aiefu.ebd.EBDCommon;
import aiefu.ebd.IServerPlayerAcc;
import aiefu.ebd.Utils;
import aiefu.ebd.client.EBDClient;
import aiefu.ebd.data.RecipeHolder;
import aiefu.ebd.data.materialoverrides.MaterialData;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OverhauledEnchantmentMenu extends AbstractContainerMenu {
    public static final ResourceLocation[] TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS,
            InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET};
    protected static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public static final ResourceLocation LAZURITE_EMPTY_ICON = ResourceLocation.fromNamespaceAndPath("minecraft", "item/empty_slot_lapis_lazuli");
    public static final ResourceLocation SWORD_EMPTY_ICON = ResourceLocation.fromNamespaceAndPath("minecraft", "item/empty_slot_sword");
    public static final ResourceLocation INGOT_EMPTY_ICON = ResourceLocation.fromNamespaceAndPath("minecraft", "item/empty_slot_ingot");
    private final ContainerLevelAccess access;
    public Object2IntOpenHashMap<Holder<Enchantment>> allEnchantments = new Object2IntOpenHashMap<>();
    public Object2IntOpenHashMap<Holder<Enchantment>> enchantments = new Object2IntOpenHashMap<>();
    public Object2IntOpenHashMap<Holder<Enchantment>> curses = new Object2IntOpenHashMap<>();

    public boolean isClientSide = false;

    protected SimpleContainer tableInv;

    public OverhauledEnchantmentMenu(int syncId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(syncId, inventory, ContainerLevelAccess.NULL, EBDClient.getClientPlayer());
        this.isClientSide = true;
        int r = buf.readVarInt();
        net.minecraft.core.Registry<Enchantment> registry = inventory.player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        for (int i = 0; i < r; i++) {
            String s = buf.readUtf();
            int l = buf.readVarInt();
            ResourceLocation loc = ResourceLocation.parse(s);
            Optional<Holder.Reference<Enchantment>> holderOpt = registry.getHolder(ResourceKey.create(Registries.ENCHANTMENT, loc));
            holderOpt.ifPresent(holder -> {
                allEnchantments.put(holder, l);
                if(holder.is(net.minecraft.tags.EnchantmentTags.CURSE)){
                    curses.put(holder, l);
                } else enchantments.put(holder, l);
            });
        }
    }

    public OverhauledEnchantmentMenu(int syncId, Inventory inventory, ContainerLevelAccess access, Player owner) {
        super(EBDCommon.enchantment_menu_ovr.get(), syncId);
        this.access = access;
        this.tableInv = new SimpleContainer(5){
            @Override
            public void setChanged() {
                super.setChanged();
                OverhauledEnchantmentMenu.this.slotsChanged(this);
            }
        };
        //Inventory Slots
        int i;
        for(i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 9 + j * 18, 99 + i * 18));
            }
        }
        //Hot-bar
        for(i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 9 + i * 18, 157));
        }
        //Armor
        int slotId = 0;
        for (int j = 0; j < 2; j++) {
            for(i = 0; i < 2; ++i) {
                final EquipmentSlot equipmentSlot = SLOT_IDS[slotId];
                this.addSlot(new Slot(inventory, 39 - slotId, 175 + i * 18, 108 + j * 18) {
                    public void setByPlayer(ItemStack stack) {
                        OverhauledEnchantmentMenu.onEquipItem(owner, equipmentSlot, stack, this.getItem());
                        super.setByPlayer(stack);
                    }

                    public int getMaxStackSize() {
                        return 1;
                    }

                    public boolean mayPlace(ItemStack stack) {
                        return equipmentSlot == owner.getEquipmentSlotForItem(stack);
                    }

                    public boolean mayPickup(Player player) {
                        ItemStack itemStack = this.getItem();
                        return (player.isCreative() || !EnchantmentHelper.has(itemStack, net.minecraft.world.item.enchantment.EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) && super.mayPickup(player);
                    }

                    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                        return Pair.of(InventoryMenu.BLOCK_ATLAS, TEXTURE_EMPTY_SLOTS[equipmentSlot.getIndex()]);
                    }
                });
                slotId++;
            }
        }
        //Offhand
        this.addSlot(new Slot(inventory, 40, 175, 157) {
            public void setByPlayer(ItemStack stack) {
                OverhauledEnchantmentMenu.onEquipItem(owner, EquipmentSlot.OFFHAND, stack, this.getItem());
                super.setByPlayer(stack);
            }

            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });

        this.addSlot(new Slot(this.tableInv, 0, 24,31){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return Utils.isEnchantableItem(stack) || stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, SWORD_EMPTY_ICON);
            }
        });
        this.addSlot(new Slot(this.tableInv, 1, 42,31){
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, LAZURITE_EMPTY_ICON);
            }
        });
        for (int j = 0; j < 3; j++) {
            this.addSlot(new Slot(this.tableInv, j + 2, 15 + j * 18, 49){
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, INGOT_EMPTY_ICON);
                }
            });
        }
    }

    public void checkRequirementsAndConsume(ResourceLocation location, Player player, int ordinal){
        EBDCommon.LOGGER.info("checkRequirementsAndConsume called for: {}, player: {}, ordinal: {}", location, player.getName().getString(), ordinal);
        this.access.execute((level, blockPos) -> {
            EBDCommon.LOGGER.info("checkRequirementsAndConsume: inside lambda");
            boolean instabuild = player.getAbilities().instabuild;
            ItemStack stack = this.tableInv.getItem(0);
            boolean isBook = stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK);
            EBDCommon.LOGGER.info("checkRequirementsAndConsume: stack: {}, isBook: {}, isEnchantable: {}", stack, isBook, Utils.isEnchantableItem(stack));
            if(!stack.isEmpty() && (isBook || Utils.isEnchantableItem(stack))) {
                net.minecraft.core.Registry<Enchantment> registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
                Holder<Enchantment> target = registry.getHolder(ResourceKey.create(Registries.ENCHANTMENT, location)).orElse(null);
                if (target != null) {
                    EBDCommon.LOGGER.info("checkRequirementsAndConsume: target enchantment found");
                    Object2IntOpenHashMap<ResourceLocation> learnedEnchantments = ((IServerPlayerAcc)player).enchantment_overhaul$getUnlockedEnchantments();
                    int learnedLevel = learnedEnchantments.getInt(location);
                    EBDCommon.LOGGER.info("checkRequirementsAndConsume: learnedLevel: {}, disableDiscoverySystem: {}, instabuild: {}", learnedLevel, EBDCommon.config.disableDiscoverySystem, instabuild);
                    if(learnedLevel == 0 && !EBDCommon.config.disableDiscoverySystem && !instabuild){
                        EBDCommon.LOGGER.info("checkRequirementsAndConsume: learnedLevel is 0, returning");
                        return;
                    }
                    if(target.is(net.minecraft.tags.EnchantmentTags.CURSE) && !EBDCommon.config.enableCursesAmplifier){
                        EBDCommon.LOGGER.info("checkRequirementsAndConsume: curse not enabled, returning");
                        return;
                    }

                    net.minecraft.world.item.enchantment.ItemEnchantments enchsComponent = EnchantmentHelper.getEnchantmentsForCrafting(stack);
                    Map<Holder<Enchantment>, Integer> enchs = new HashMap<>();
                    enchsComponent.entrySet().forEach(entry -> enchs.put(entry.getKey(), entry.getValue()));

                    // Collect incompatible enchantments
                    java.util.List<Holder<Enchantment>> incompatible = new java.util.ArrayList<>();
                    if (!isBook) {
                        for (Holder<Enchantment> e : enchs.keySet()) {
                            if (e != target && !Enchantment.areCompatible(e, target)) {
                                incompatible.add(e);
                            }
                        }
                    }

                    // Create hypothetical map after applying target
                    Map<Holder<Enchantment>, Integer> hypothetical = new HashMap<>(enchs);
                    for (Holder<Enchantment> e : incompatible) {
                        hypothetical.remove(e);
                    }
                    int currentLvl = enchs.getOrDefault(target, 0);
                    hypothetical.put(target, currentLvl + 1);

                    int cursesCount = 0;
                    for (Holder<Enchantment> e : hypothetical.keySet()){
                        if(e.is(net.minecraft.tags.EnchantmentTags.CURSE)) cursesCount++;
                    }
                    MaterialData data = Utils.getMatData(stack.getItem());
                    boolean canEnchant = isBook || target.value().canEnchant(stack);
                    
                    // The limit is checked against the hypothetical state
                    boolean satisfiesLimit = target.is(net.minecraft.tags.EnchantmentTags.CURSE) && cursesCount < data.getMaxCurses()
                            || Utils.getCurrentLimit(hypothetical.keySet().size(), cursesCount) <= Utils.getEnchantmentsLimit(player, cursesCount, data);
                            
                    EBDCommon.LOGGER.info("checkRequirementsAndConsume: canEnchant: {}, satisfiesLimit: {}", canEnchant, satisfiesLimit);
                    if(canEnchant && satisfiesLimit) {
                        // Remove incompatible ones from the actual map
                        for (Holder<Enchantment> e : incompatible) {
                            enchs.remove(e);
                        }

                        int targetLevel = currentLvl + 1;
                        EBDCommon.LOGGER.info("checkRequirementsAndConsume: targetLevel: {}, enableEnchantmentsLeveling: {}", targetLevel, EBDCommon.config.enableEnchantmentsLeveling);
                        if(targetLevel > learnedLevel && EBDCommon.config.enableEnchantmentsLeveling && !instabuild){
                            EBDCommon.LOGGER.info("checkRequirementsAndConsume: targetLevel > learnedLevel, returning");
                            return;
                        }
                        List<RecipeHolder> holders = EBDCommon.getRecipeHolders(location);
                        if (holders != null && !holders.isEmpty() && ordinal != -1 && ordinal < holders.size()) {
                            RecipeHolder holder = holders.get(ordinal);
                            boolean checkAndConsumeResult = holder.checkAndConsume(this.tableInv, targetLevel, player);
                            EBDCommon.LOGGER.info("checkRequirementsAndConsume: checkAndConsumeResult: {}", checkAndConsumeResult);
                            if (instabuild || targetLevel <= holder.getMaxLevel(target) && checkAndConsumeResult) {
                                enchs.put(target, targetLevel);
                                this.applyAndBroadcast(player, enchs, stack, isBook);
                                EBDCommon.LOGGER.info("checkRequirementsAndConsume: applied via recipe successfully!");
                            }
                        } else if (instabuild && targetLevel <= target.value().getMaxLevel()) {
                            enchs.put(target, targetLevel);
                            this.applyAndBroadcast(player, enchs, stack, isBook);
                            EBDCommon.LOGGER.info("checkRequirementsAndConsume: applied via instabuild successfully!");
                        } else {
                            EBDCommon.LOGGER.info("checkRequirementsAndConsume: no recipe or invalid ordinal");
                        }
                    } else {
                        EBDCommon.LOGGER.info("checkRequirementsAndConsume: canEnchant or satisfiesLimit failed");
                    }
                }
            } else {
                EBDCommon.LOGGER.info("checkRequirementsAndConsume: stack empty or not enchantable");
            }
        });
    }

    public void applyAndBroadcast(Player player, Map<Holder<Enchantment>, Integer> map, ItemStack stack, boolean isBook){
        net.minecraft.world.item.enchantment.ItemEnchantments.Mutable builder = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        map.forEach(builder::set);
        net.minecraft.world.item.enchantment.ItemEnchantments newItemEnchs = builder.toImmutable();

        if(isBook){
            if(stack.is(Items.BOOK)){
                stack = new ItemStack(Items.ENCHANTED_BOOK);
                this.tableInv.setItem(0, stack);
            }
            stack.set(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS, newItemEnchs);
        } else {
            EnchantmentHelper.setEnchantments(stack, newItemEnchs);
        }
        player.onEnchantmentPerformed(stack, 0);
        if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
            ((IServerPlayerAcc) sp).ebd$addSkillXP("enchanter", aiefu.ebd.LBDConfig.INSTANCE.xpItemEnchanted, sp);
        }
        this.tableInv.setChanged();
        this.broadcastChanges();
    }

    public SimpleContainer getTableInv() {
        return tableInv;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, blockPos) -> this.clearContainer(player, this.tableInv));
    }

    @Override
    protected void clearContainer(Player player, Container container) {
        super.clearContainer(player, container);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
    }

    public static void onEquipItem(Player player, EquipmentSlot slot, ItemStack newItem, ItemStack oldItem) {
        Equipable equipable = Equipable.get(newItem);
        if (equipable != null) {
            player.onEquipItem(slot, oldItem, newItem);
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int i) {
        ItemStack returnStack = ItemStack.EMPTY;
        Slot slot = slots.get(i);
        if(slot.hasItem()){
            ItemStack stack = slot.getItem();
            returnStack = stack.copy();
            if(i == 41){
                EquipmentSlot eqs = player.getEquipmentSlotForItem(stack);
                if(eqs.isArmor()){
                    int o = 39 - eqs.getIndex();
                    if(!moveItemStackTo(stack, o, o + 1, true) && !moveItemStackTo(stack, 0, 36, true)){
                        return ItemStack.EMPTY;
                    }
                } else if(eqs == EquipmentSlot.OFFHAND){
                    if(!moveItemStackTo(stack, 40 , 41 , true) && !moveItemStackTo(stack, 0, 36, true)){
                        return ItemStack.EMPTY;
                    }
                } else {
                    if(!moveItemStackTo(stack, 0, 36, true)){
                        return ItemStack.EMPTY;
                    }
                }
            } else if(i > 41 && i < 46){
                if(!moveItemStackTo(stack, 0 , 36, true)){
                    return ItemStack.EMPTY;
                }
            } else {
                ItemStack stack2 = stack.copyWithCount(1);
                if(!this.slots.get(41).hasItem() && this.slots.get(41).mayPlace(stack2)){
                    stack.shrink(1);
                    this.slots.get(41).setByPlayer(stack2);
                    returnStack = ItemStack.EMPTY;
                } else if(!moveItemStackTo(stack, 42, 46, false)){
                    return ItemStack.EMPTY;
                } else return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == returnStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return returnStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, Blocks.ENCHANTING_TABLE);
    }
}

package aiefu.ebd;

import aiefu.ebd.data.EnchantmentRecipesReloadListener;
import aiefu.ebd.data.MaterialDataReloadListener;
import aiefu.ebd.data.RecipeHolder;
import aiefu.ebd.data.itemdata.ItemData;
import aiefu.ebd.data.materialoverrides.MaterialOverrides;
import aiefu.ebd.menu.OverhauledEnchantmentMenu;
import aiefu.ebd.network.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.CocoaBlock;
import aiefu.ebd.block.GoldenCarrotBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ItemParticleOption;
import java.util.ArrayList;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Mod(EBDCommon.MOD_ID)
public class EBDCommon {

    public static final String MOD_ID = "enchant_by_doing";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static ConfigurationFile config;
    public static MaterialOverrides mat_config;

    public static final ResourceLocation defaultRecipe = ResourceLocation.fromNamespaceAndPath(MOD_ID, "default");
    public static ConcurrentHashMap<ResourceLocation, List<RecipeHolder>> recipeMap = new ConcurrentHashMap<>();

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<OverhauledEnchantmentMenu>> enchantment_menu_ovr =
            MENUS.register("enchs_menu_ovr", () -> IMenuTypeExtension.create(OverhauledEnchantmentMenu::new));

    public static final DeferredRegister<net.minecraft.world.effect.MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MOD_ID);
    public static final DeferredRegister<net.minecraft.world.item.alchemy.Potion> POTIONS = DeferredRegister.create(Registries.POTION, MOD_ID);

    public static final DeferredHolder<net.minecraft.world.effect.MobEffect, net.minecraft.world.effect.MobEffect> SHRINKING =
            MOB_EFFECTS.register("shrinking", () -> new aiefu.ebd.effect.ShrinkingEffect(net.minecraft.world.effect.MobEffectCategory.NEUTRAL, 0x5599FF)
                    .addAttributeModifier(net.minecraft.world.entity.ai.attributes.Attributes.SCALE,
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "shrinking_scale_v4"),
                            -0.555556D,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));

    public static final DeferredHolder<net.minecraft.world.effect.MobEffect, net.minecraft.world.effect.MobEffect> GROWING =
            MOB_EFFECTS.register("growing", () -> new aiefu.ebd.effect.GrowingEffect(net.minecraft.world.effect.MobEffectCategory.NEUTRAL, 0x44CC44)
                    .addAttributeModifier(net.minecraft.world.entity.ai.attributes.Attributes.SCALE,
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "growing_scale_v4"),
                            0.444444D,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));

    public static final DeferredHolder<net.minecraft.world.effect.MobEffect, net.minecraft.world.effect.MobEffect> GOLDEN_BEETROOT_EFFECT =
            MOB_EFFECTS.register("golden_beetroot", () -> new aiefu.ebd.effect.GoldenBeetrootEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 0xFF69B4));

    // XP Boost effect: Level 1 = 50% XP boost, Level 2 = 100% XP boost
    public static final DeferredHolder<net.minecraft.world.effect.MobEffect, net.minecraft.world.effect.MobEffect> XP_BOOST =
            MOB_EFFECTS.register("xp_boost", () -> new aiefu.ebd.effect.XPBoostEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 0x80FF00));

    public static final DeferredHolder<net.minecraft.world.item.alchemy.Potion, net.minecraft.world.item.alchemy.Potion> SHRINKING_POTION =
            POTIONS.register("shrinking", () -> new net.minecraft.world.item.alchemy.Potion("shrinking", new net.minecraft.world.effect.MobEffectInstance(SHRINKING, 3600)));

    public static final DeferredHolder<net.minecraft.world.item.alchemy.Potion, net.minecraft.world.item.alchemy.Potion> LONG_SHRINKING_POTION =
            POTIONS.register("long_shrinking", () -> new net.minecraft.world.item.alchemy.Potion("shrinking", new net.minecraft.world.effect.MobEffectInstance(SHRINKING, 9600)));

    public static final DeferredHolder<net.minecraft.world.item.alchemy.Potion, net.minecraft.world.item.alchemy.Potion> GROWING_POTION =
            POTIONS.register("growing", () -> new net.minecraft.world.item.alchemy.Potion("growing", new net.minecraft.world.effect.MobEffectInstance(GROWING, 3600)));

    public static final DeferredHolder<net.minecraft.world.item.alchemy.Potion, net.minecraft.world.item.alchemy.Potion> LONG_GROWING_POTION =
            POTIONS.register("long_growing", () -> new net.minecraft.world.item.alchemy.Potion("growing", new net.minecraft.world.effect.MobEffectInstance(GROWING, 9600)));

    public static final DeferredHolder<net.minecraft.world.item.alchemy.Potion, net.minecraft.world.item.alchemy.Potion> XP_BOOST_POTION =
            POTIONS.register("xp_boost", () -> new net.minecraft.world.item.alchemy.Potion("xp_boost", new net.minecraft.world.effect.MobEffectInstance(XP_BOOST, 6000, 0)));

    public static final DeferredHolder<net.minecraft.world.item.alchemy.Potion, net.minecraft.world.item.alchemy.Potion> STRONG_XP_BOOST_POTION =
            POTIONS.register("strong_xp_boost", () -> new net.minecraft.world.item.alchemy.Potion("xp_boost", new net.minecraft.world.effect.MobEffectInstance(XP_BOOST, 6000, 1)));

    public static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MOD_ID);
    public static final DeferredHolder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.Block> GOLDEN_CARROT_BLOCK =
            BLOCKS.register("golden_carrot_crop", () -> new GoldenCarrotBlock(
                    net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(Blocks.CARROTS)));

    public static final DeferredRegister<net.minecraft.world.item.Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);
    public static final DeferredHolder<net.minecraft.world.item.Item, net.minecraft.world.item.Item> DUSTY_BOOK =
            ITEMS.register("dusty_book", () -> new aiefu.ebd.item.DustyBookItem(new net.minecraft.world.item.Item.Properties().stacksTo(64)));

    // "Luminous Straw" - thematic name for golden wheat
    public static final DeferredHolder<net.minecraft.world.item.Item, net.minecraft.world.item.Item> GOLDEN_WHEAT =
            ITEMS.register("golden_wheat", () -> new net.minecraft.world.item.Item(new net.minecraft.world.item.Item.Properties()));

    public static final DeferredHolder<net.minecraft.world.item.Item, net.minecraft.world.item.Item> GOLDEN_MELON_SLICE =
            ITEMS.register("golden_melon_slice", () -> new net.minecraft.world.item.Item(
                    new net.minecraft.world.item.Item.Properties().food(
                            new net.minecraft.world.food.FoodProperties.Builder()
                                    .nutrition(0)
                                    .saturationModifier(0.0f)
                                    .alwaysEdible()
                                    .build()
                    )
            ));

    // "Auric Tuber" - thematic name for golden potato
    public static final DeferredHolder<net.minecraft.world.item.Item, net.minecraft.world.item.Item> GOLDEN_POTATO =
            ITEMS.register("golden_potato", () -> new net.minecraft.world.item.Item(
                    new net.minecraft.world.item.Item.Properties().food(
                            new net.minecraft.world.food.FoodProperties.Builder()
                                    .nutrition(1)
                                    .saturationModifier(0.1f)
                                    .alwaysEdible()
                                    .effect(() -> new net.minecraft.world.effect.MobEffectInstance(SHRINKING, 200), 1.0f)
                                    .build()
                    )
            ));

    // "Crimson Root" - thematic name for golden beetroot
    public static final DeferredHolder<net.minecraft.world.item.Item, net.minecraft.world.item.Item> GOLDEN_BEETROOT =
            ITEMS.register("golden_beetroot", () -> new net.minecraft.world.item.Item(
                    new net.minecraft.world.item.Item.Properties().food(
                            new net.minecraft.world.food.FoodProperties.Builder()
                                    .nutrition(0)
                                    .saturationModifier(0.0f)
                                    .alwaysEdible()
                                    .build()
                    )
            ));

    // No custom golden_carrot item — we use vanilla Items.GOLDEN_CARROT as seed for GoldenCarrotBlock

    public static final DeferredRegister<com.mojang.serialization.MapCodec<? extends net.neoforged.neoforge.common.loot.IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MOD_ID);
    public static final DeferredHolder<com.mojang.serialization.MapCodec<? extends net.neoforged.neoforge.common.loot.IGlobalLootModifier>, com.mojang.serialization.MapCodec<aiefu.ebd.loot.DustyBookLootModifier>> DUSTY_BOOK_MODIFIER =
            LOOT_MODIFIERS.register("replace_enchanted_books", () -> aiefu.ebd.loot.DustyBookLootModifier.CODEC);

    public static final DeferredRegister<net.minecraft.world.entity.EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);
    public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<aiefu.ebd.entity.ThrownAxeEntity>> THROWN_AXE =
            ENTITY_TYPES.register("thrown_axe", () -> net.minecraft.world.entity.EntityType.Builder.<aiefu.ebd.entity.ThrownAxeEntity>of(aiefu.ebd.entity.ThrownAxeEntity::new, net.minecraft.world.entity.MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("thrown_axe"));
    public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<aiefu.ebd.entity.ThrownBottleEntity>> THROWN_BOTTLE =
            ENTITY_TYPES.register("thrown_bottle", () -> net.minecraft.world.entity.EntityType.Builder.<aiefu.ebd.entity.ThrownBottleEntity>of(aiefu.ebd.entity.ThrownBottleEntity::new, net.minecraft.world.entity.MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("thrown_bottle"));

    public EBDCommon(IEventBus modBus) {
        MENUS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        MOB_EFFECTS.register(modBus);
        POTIONS.register(modBus);
        LOOT_MODIFIERS.register(modBus);
        ENTITY_TYPES.register(modBus);
        modBus.addListener(this::registerPayloads);
        modBus.addListener(this::buildCreativeTabContents);

        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(this::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onBlockInteract);
        NeoForge.EVENT_BUS.addListener(this::onRightClickItem);
        NeoForge.EVENT_BUS.addListener(this::onUseItemTick);
        NeoForge.EVENT_BUS.addListener(this::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(this::onLivingExperienceDrop);
        NeoForge.EVENT_BUS.addListener(this::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerXpChange);
        NeoForge.EVENT_BUS.addListener(this::onStartTracking);
        NeoForge.EVENT_BUS.addListener(this::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(this::onBlockPlace);
        NeoForge.EVENT_BUS.addListener(this::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(this::onBabySpawn);
        NeoForge.EVENT_BUS.addListener(this::onAnvilRepair);
        NeoForge.EVENT_BUS.addListener(this::onRegisterBrewingRecipes);

        EBDGameplayEvents gameplayEvents = new EBDGameplayEvents();
        NeoForge.EVENT_BUS.addListener(gameplayEvents::onLivingDamage);
        NeoForge.EVENT_BUS.addListener(gameplayEvents::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(gameplayEvents::onUseItemTick);
        NeoForge.EVENT_BUS.addListener(gameplayEvents::onUseItemStop);
        NeoForge.EVENT_BUS.addListener(gameplayEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(gameplayEvents::onRightClickItem);
        NeoForge.EVENT_BUS.addListener(gameplayEvents::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(gameplayEvents::onBreakSpeed);
        NeoForge.EVENT_BUS.addListener(gameplayEvents::onLivingChangeTarget);
        NeoForge.EVENT_BUS.addListener(gameplayEvents::onMobEffectAdded);
        NeoForge.EVENT_BUS.addListener(gameplayEvents::onLivingDeathSave);
        NeoForge.EVENT_BUS.addListener(gameplayEvents::onUseItemStart);
        NeoForge.EVENT_BUS.addListener(gameplayEvents::onUseItemFinish);
        NeoForge.EVENT_BUS.addListener(gameplayEvents::onBlockDrops);
        NeoForge.EVENT_BUS.addListener(gameplayEvents::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(gameplayEvents::onEntityInteract);
        
        if (net.neoforged.fml.ModList.get().isLoaded("treechop")) {
            NeoForge.EVENT_BUS.register(new aiefu.ebd.compat.TreeChopCompat());
            LOGGER.info("ESO: Registered TreeChop compatibility!");
        }
        
        try {
            this.genConfig();
            EBDCommon.readConfig();
            SkillsXPConfig.INSTANCE.load();
            this.genDefaultRecipe();
            MaterialOverrides.generateDefault();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        LOGGER.info("ESO Initialized");
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MOD_ID).versioned("1.0.0");
        
        // Client to Server
        registrar.playToServer(
            C2SEnchantItemPayload.TYPE,
            C2SEnchantItemPayload.CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                net.minecraft.world.entity.player.Player player = context.player();
                if (player.containerMenu instanceof OverhauledEnchantmentMenu m) {
                    m.checkRequirementsAndConsume(ResourceLocation.parse(payload.enchantmentId()), player, payload.ordinal());
                }
            })
        );
        
        // Server to Client
        registrar.playToClient(
            S2CDataSyncPayload.TYPE,
            S2CDataSyncPayload.CODEC,
            (payload, context) -> context.enqueueWork(() -> ClientsideNetworkManager.handleDataSync(payload))
        );
        registrar.playToClient(
            S2CMatConfigSyncPayload.TYPE,
            S2CMatConfigSyncPayload.CODEC,
            (payload, context) -> context.enqueueWork(() -> ClientsideNetworkManager.handleMatConfigSync(payload))
        );
        registrar.playToClient(
            S2CStringToClipboardPayload.TYPE,
            S2CStringToClipboardPayload.CODEC,
            (payload, context) -> context.enqueueWork(() -> ClientsideNetworkManager.handleStringToClipboard(payload))
        );
        registrar.playToClient(
            S2CSyncConfigPayload.TYPE,
            S2CSyncConfigPayload.CODEC,
            (payload, context) -> context.enqueueWork(() -> ClientsideNetworkManager.handleSyncConfig(payload))
        );
        registrar.playToClient(
            S2CBoatSyncPayload.TYPE,
            S2CBoatSyncPayload.CODEC,
            (payload, context) -> context.enqueueWork(() -> ClientsideNetworkManager.handleBoatSync(payload))
        );
        registrar.playToClient(
            S2CSkillUpdatePayload.TYPE,
            S2CSkillUpdatePayload.CODEC,
            (payload, context) -> context.enqueueWork(() -> ClientsideNetworkManager.handleSkillUpdate(payload))
        );
        registrar.playToClient(
            S2CWorkstationStatusPayload.TYPE,
            S2CWorkstationStatusPayload.CODEC,
            (payload, context) -> context.enqueueWork(() -> ClientsideNetworkManager.handleWorkstationStatus(payload))
        );
    }

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new EnchantmentRecipesReloadListener());
        event.addListener(new MaterialDataReloadListener());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        EBDCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.getEntity().level().isClientSide()) {
            IServerPlayerAcc old = (IServerPlayerAcc) event.getOriginal();
            IServerPlayerAcc np = (IServerPlayerAcc) event.getEntity();
            np.enchantment_overhaul$setUnlockedEnchantments(old.enchantment_overhaul$getUnlockedEnchantments());
            for (SkillType type : SkillType.values()) {
                np.ebd$setSkillLevel(type.id, old.ebd$getSkillLevel(type.id));
                np.ebd$setSkillXP(type.id, old.ebd$getSkillXP(type.id));
            }
            if (event.getEntity() instanceof ServerPlayer sp) {
                syncPlayerSkills(sp);
            }
        }
    }

    public static void syncPlayerSkills(ServerPlayer player) {
        for (SkillType type : SkillType.values()) {
            int level = ((IServerPlayerAcc) player).ebd$getSkillLevel(type.id);
            double xp = ((IServerPlayerAcc) player).ebd$getSkillXP(type.id);
            double needed = LBDConfig.INSTANCE.getXPNeededForLevel(type.id, level);
            ServersideNetworkManager.sendSkillUpdate(player, type.id, level, xp, needed, 0.0);
        }
    }

    @SubscribeEvent
    public void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            ServersideNetworkManager.syncData(event.getPlayer());
            ServersideNetworkManager.syncMatConfig(event.getPlayer());
            ServersideNetworkManager.syncConfig(event.getPlayer());
            syncPlayerSkills(event.getPlayer());
        } else {
            event.getPlayerList().getPlayers().forEach(player -> {
                ServersideNetworkManager.syncData(player);
                ServersideNetworkManager.syncMatConfig(player);
                ServersideNetworkManager.syncConfig(player);
                syncPlayerSkills(player);
            });
        }
    }

    @SubscribeEvent
    public void onBlockBreak(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer sp)) return;

        BlockState state = event.getState();
        Block block = state.getBlock();
        ItemStack tool = sp.getMainHandItem();

        // Emergency Obsidian Mining with Iron Pickaxe: guaranteed drop and pickaxe destruction
        if (!sp.getAbilities().instabuild && tool.is(Items.IRON_PICKAXE) && (state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN))) {
            Item dropItem = state.is(Blocks.CRYING_OBSIDIAN) ? Items.CRYING_OBSIDIAN : Items.OBSIDIAN;
            Block.popResource(sp.serverLevel(), event.getPos(), new ItemStack(dropItem));

            // Guaranteed complete destruction of the iron pickaxe
            sp.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            sp.serverLevel().sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.IRON_PICKAXE)), sp.getX(), sp.getY() + 1.0, sp.getZ(), 15, 0.2, 0.2, 0.2, 0.05);
        }

        String blockKey = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString();
        boolean inConfig = LBDConfig.INSTANCE.customBlockXp.containsKey(blockKey);

        boolean isFarmerBlock = block == Blocks.MELON || block == Blocks.PUMPKIN 
            || block instanceof CropBlock || block instanceof NetherWartBlock || block instanceof CocoaBlock
            || state.is(net.minecraft.tags.BlockTags.CROPS);

        // 1. Miner checks
        if (tool.getItem() instanceof net.minecraft.world.item.PickaxeItem && !isFarmerBlock) {
            boolean isOre = state.is(net.neoforged.neoforge.common.Tags.Blocks.ORES) 
                || block.getDescriptionId().contains("ore");
            
            boolean isStone = state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD)
                || state.is(net.minecraft.tags.BlockTags.BASE_STONE_NETHER)
                || block == Blocks.STONE 
                || block == Blocks.COBBLESTONE 
                || block == Blocks.DEEPSLATE 
                || block == Blocks.COBBLED_DEEPSLATE
                || block == Blocks.ANDESITE 
                || block == Blocks.DIORITE 
                || block == Blocks.GRANITE;

            if (isOre || isStone || inConfig) {
                boolean hasSilk = false;
                for (var entry : tool.getEnchantments().entrySet()) {
                    if (entry.getKey().unwrapKey().map(k -> k.location().getPath().equals("silk_touch")).orElse(false)) {
                        hasSilk = true;
                        break;
                    }
                }
                if (!hasSilk) {
                    double xp;
                    if (inConfig) {
                        xp = LBDConfig.INSTANCE.customBlockXp.get(blockKey);
                    } else {
                        float hardness = state.getDestroySpeed(sp.level(), event.getPos());
                        xp = Math.max(0.1, hardness * LBDConfig.INSTANCE.blockXpHardnessMultiplier);
                    }
                    long penaltyTime = ((IServerPlayerAcc) sp).ebd$getMinerPenaltyTime();
                    if (System.currentTimeMillis() < penaltyTime) {
                        xp *= (1.0f - LBDConfig.INSTANCE.antiAbuseXpReduction);
                    }
                    if (xp > 0) {
                        ((IServerPlayerAcc) sp).ebd$addSkillXP("miner", xp, sp);
                    }
                    if (isOre) {
                        int minerLevel = Utils.getSkillLevel(sp, "miner");
                        if (minerLevel >= 25) {
                            float coalChance = 0.10f + 0.05f * (minerLevel / 25);
                            if (sp.level().random.nextFloat() < coalChance) {
                                int coalCount = 1 + sp.level().random.nextInt(3);
                                Block.popResource(sp.level(), event.getPos(), new ItemStack(Items.COAL, coalCount));
                            }
                        }
                        if (minerLevel >= 50) {
                            if (sp.level().random.nextFloat() < 0.15f) {
                                sp.getFoodData().eat(1, 0.5f);
                            }
                        }
                        if (minerLevel >= 75 && (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE)) {
                            applyOrExtendEffect(sp, net.minecraft.world.effect.MobEffects.DIG_SPEED, 400, 0);
                            applyOrExtendEffect(sp, net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 400, 0);
                            applyOrExtendEffect(sp, net.minecraft.world.effect.MobEffects.NIGHT_VISION, 400, 0);
                        }
                        if (minerLevel >= 100 && block == Blocks.NETHER_GOLD_ORE) {
                            if (sp.level().random.nextFloat() < 0.02f) {
                                Block.popResource(sp.level(), event.getPos(), new ItemStack(Items.ANCIENT_DEBRIS));
                            }
                        }
                    }
                }
            }
        }

        // 2. Lumberjack checks
        if (tool.getItem() instanceof net.minecraft.world.item.AxeItem && !isFarmerBlock) {
            boolean isLog = state.is(net.minecraft.tags.BlockTags.LOGS);
            boolean isLeaves = state.is(net.minecraft.tags.BlockTags.LEAVES);
            if (isLog || isLeaves || inConfig) {
                double xp;
                if (inConfig) {
                    xp = LBDConfig.INSTANCE.customBlockXp.get(blockKey);
                } else {
                    float hardness = state.getDestroySpeed(sp.level(), event.getPos());
                    xp = Math.max(0.1, hardness * LBDConfig.INSTANCE.blockXpHardnessMultiplier);
                }
                long penaltyTime = ((IServerPlayerAcc) sp).ebd$getLumberjackPenaltyTime();
                if (System.currentTimeMillis() < penaltyTime) {
                    xp = 0.0;
                }
                if (xp > 0) {
                    ((IServerPlayerAcc) sp).ebd$addSkillXP("lumberjack", xp, sp);
                }
            }
        }

        // 3. Farmer checks
        boolean isFullyGrown = false;
        if (block instanceof CropBlock crop) {
            isFullyGrown = crop.isMaxAge(state);
        } else if (block instanceof NetherWartBlock) {
            isFullyGrown = state.getValue(NetherWartBlock.AGE) >= 3;
        } else if (block instanceof CocoaBlock) {
            isFullyGrown = state.getValue(CocoaBlock.AGE) >= 2;
        } else if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
            isFullyGrown = true;
        } else if (state.is(net.minecraft.tags.BlockTags.CROPS)) {
            java.util.Optional<net.minecraft.world.level.block.state.properties.Property<?>> ageProp = state.getProperties().stream().filter(p -> p.getName().equals("age")).findFirst();
            if (ageProp.isPresent()) {
                net.minecraft.world.level.block.state.properties.Property<?> prop = ageProp.get();
                if (prop instanceof net.minecraft.world.level.block.state.properties.IntegerProperty intProp) {
                    int maxAge = intProp.getPossibleValues().stream().mapToInt(v -> v).max().orElse(0);
                    isFullyGrown = state.getValue(intProp) == maxAge;
                }
            } else {
                isFullyGrown = true;
            }
        }
        if (isFullyGrown) {
            double xp = inConfig ? LBDConfig.INSTANCE.customBlockXp.get(blockKey) : LBDConfig.INSTANCE.xpCropHarvested;
            ((IServerPlayerAcc) sp).ebd$addSkillXP("farmer", xp, sp);

            // Farmer passive: Golden crop drops (0.1% per level)
            int farmerLevel = Utils.getSkillLevel(sp, "farmer");
            if (farmerLevel > 0) {
                float chance = farmerLevel * 0.001f;
                if (sp.level().random.nextFloat() < chance) {
                    ItemStack goldenDrop = ItemStack.EMPTY;
                    if (block == Blocks.WHEAT) {
                        goldenDrop = new ItemStack(GOLDEN_WHEAT.get());
                    } else if (block == Blocks.POTATOES) {
                        goldenDrop = new ItemStack(GOLDEN_POTATO.get());
                    } else if (block == Blocks.BEETROOTS) {
                        goldenDrop = new ItemStack(GOLDEN_BEETROOT.get());
                    } else if (block == Blocks.CARROTS) {
                        goldenDrop = new ItemStack(Items.GOLDEN_CARROT);
                    } else if (block == GOLDEN_CARROT_BLOCK.get()) {
                        // Golden carrot crop gives vanilla golden carrot (already the drop)
                        goldenDrop = ItemStack.EMPTY; // handled by loot table
                    }
                    if (!goldenDrop.isEmpty()) {
                        Block.popResource(sp.level(), event.getPos(), goldenDrop);
                    }
                }
            }
        }

        if (block == Blocks.MELON) {
            PlacedMelonsTracker tracker = PlacedMelonsTracker.get(sp.serverLevel());
            if (tracker.isPlaced(event.getPos())) {
                tracker.removeMelon(event.getPos());
            } else {
                int farmerLevel = Utils.getSkillLevel(sp, "farmer");
                if (farmerLevel > 0) {
                    float chance = farmerLevel * 0.001f;
                    if (sp.level().random.nextFloat() < chance) {
                        Block.popResource(sp.level(), event.getPos(), new ItemStack(net.minecraft.world.item.Items.GLISTERING_MELON_SLICE));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockPlace(net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof ServerPlayer sp) {
            BlockState state = event.getPlacedBlock();
            Block block = state.getBlock();
            
            boolean isStone = state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD)
                || state.is(net.minecraft.tags.BlockTags.BASE_STONE_NETHER)
                || block == Blocks.STONE 
                || block == Blocks.COBBLESTONE 
                || block == Blocks.DEEPSLATE 
                || block == Blocks.COBBLED_DEEPSLATE
                || block == Blocks.ANDESITE 
                || block == Blocks.DIORITE 
                || block == Blocks.GRANITE;

            if (isStone) {
                long duration = LBDConfig.INSTANCE.antiAbuseCooldownSeconds * 1000L;
                ((IServerPlayerAcc) sp).ebd$setMinerPenaltyTime(System.currentTimeMillis() + duration);
            }

            boolean isLog = state.is(net.minecraft.tags.BlockTags.LOGS);
            if (isLog) {
                long duration = LBDConfig.INSTANCE.antiAbuseCooldownSeconds * 1000L;
                ((IServerPlayerAcc) sp).ebd$setLumberjackPenaltyTime(System.currentTimeMillis() + duration);
            }

            if (block == Blocks.MELON) {
                PlacedMelonsTracker.get(sp.serverLevel()).addMelon(event.getPos());
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            net.minecraft.world.entity.LivingEntity victim = event.getEntity();
            
            boolean isHostile = (victim instanceof net.minecraft.world.entity.monster.Enemy)
                || (victim.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER);

            boolean isAnimal = (victim instanceof net.minecraft.world.entity.animal.Animal);

            String mobKey = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString();
            boolean inConfig = LBDConfig.INSTANCE.customMobXp.containsKey(mobKey);

            if (isHostile || isAnimal || inConfig) {
                double xp;
                if (inConfig) {
                    xp = LBDConfig.INSTANCE.customMobXp.get(mobKey);
                } else {
                    xp = Math.max(1.0, victim.getMaxHealth() * LBDConfig.INSTANCE.mobXpHealthMultiplier);
                }

                boolean isRanged = event.getSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile;
                if (isHostile) {
                    if (isRanged) {
                        ((IServerPlayerAcc) player).ebd$addSkillXP("hunter", xp, player);
                    } else {
                        ((IServerPlayerAcc) player).ebd$addSkillXP("warrior", xp, player);
                    }
                } else if (isAnimal) {
                    ((IServerPlayerAcc) player).ebd$addSkillXP("hunter", xp, player);
                } else if (inConfig) {
                    if (victim instanceof net.minecraft.world.entity.monster.Enemy) {
                        if (isRanged) {
                            ((IServerPlayerAcc) player).ebd$addSkillXP("hunter", xp, player);
                        } else {
                            ((IServerPlayerAcc) player).ebd$addSkillXP("warrior", xp, player);
                        }
                    } else {
                        ((IServerPlayerAcc) player).ebd$addSkillXP("hunter", xp, player);
                    }
                }

                // Warrior Passive 4: Bloody Frenzy (heal or absorption on hostile kill with 20% chance)
                if (isHostile) {
                    int warriorLevel = Utils.getSkillLevel(player, "warrior");
                    if (warriorLevel >= 80) {
                        if (player.level().random.nextFloat() < 0.20f) {
                            boolean healed = false;
                            if (player.getHealth() < player.getMaxHealth()) {
                                player.heal(2.0F);
                                healed = true;
                            } else {
                                var maxAbsAttr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_ABSORPTION);
                                if (maxAbsAttr != null && maxAbsAttr.getBaseValue() < 10.0) {
                                    maxAbsAttr.setBaseValue(10.0);
                                }
                                float currentAbs = player.getAbsorptionAmount();
                                player.setAbsorptionAmount(Math.min(10.0F, currentAbs + 2.0F));
                            }
                            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.8F);
                            if (player.level() instanceof ServerLevel serverLevel) {
                                if (healed) {
                                    serverLevel.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.0, player.getZ(), 5, 0.2, 0.2, 0.2, 0.0);
                                } else {
                                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 8, 0.2, 0.2, 0.2, 0.0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onBabySpawn(net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent event) {
        if (event.getParentA().level().isClientSide()) return;
        if (event.getCausedByPlayer() instanceof ServerPlayer player) {
            ((IServerPlayerAcc) player).ebd$addSkillXP("farmer", LBDConfig.INSTANCE.xpAnimalBred, player);
        }
        // Golden Wheat Multi-birth logic
        net.minecraft.world.entity.Mob parentA = event.getParentA();
        net.minecraft.world.entity.Mob parentB = event.getParentB();
        if (parentA instanceof net.minecraft.world.entity.animal.Cow || parentA instanceof net.minecraft.world.entity.animal.Sheep) {
            if (parentA.getPersistentData().getBoolean("ebd$golden_bred") || parentB.getPersistentData().getBoolean("ebd$golden_bred")) {
                parentA.getPersistentData().remove("ebd$golden_bred");
                parentB.getPersistentData().remove("ebd$golden_bred");
                int extraCount = parentA.level().random.nextInt(3) + 1; // 2-4 babies total (1 standard + 1-3 extra)
                for (int i = 0; i < extraCount; i++) {
                    net.minecraft.world.entity.AgeableMob extraChild = (net.minecraft.world.entity.AgeableMob) parentA.getType().create(parentA.level());
                    if (extraChild != null) {
                        extraChild.setBaby(true);
                        extraChild.moveTo(parentA.getX(), parentA.getY(), parentA.getZ(), 0.0f, 0.0f);
                        parentA.level().addFreshEntity(extraChild);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onAnvilRepair(net.neoforged.neoforge.event.entity.player.AnvilRepairEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            ((IServerPlayerAcc) player).ebd$addSkillXP("enchanter", LBDConfig.INSTANCE.xpItemRepaired, player);
            
            // Enchanter Passive 1: get a book back when applying from enchanted book
            int enchanterLevel = Utils.getSkillLevel(player, "enchanter");
            if (enchanterLevel >= 25) {
                ItemStack rightStack = event.getRight();
                if (rightStack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK)) {
                    ItemStack bookStack = new ItemStack(net.minecraft.world.item.Items.BOOK);
                    if (!player.getInventory().add(bookStack)) {
                        player.drop(bookStack, false);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        event.getServer().execute(() -> recipeMap.values().forEach(l -> l.forEach(RecipeHolder::processTags)));
    }

    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        net.minecraft.world.level.Level level = event.getLevel();
        net.minecraft.core.BlockPos pos = event.getPos();
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.ENCHANTING_TABLE)) {
            if (!level.isClientSide() && event.getEntity() instanceof ServerPlayer player) {
                net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof net.minecraft.world.level.block.entity.EnchantingTableBlockEntity tableEntity) {
                    player.openMenu(new net.minecraft.world.MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return tableEntity.getDisplayName();
                        }
                        
                        @Override
                        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int i, net.minecraft.world.entity.player.Inventory inventory, net.minecraft.world.entity.player.Player playerEntity) {
                            return new OverhauledEnchantmentMenu(i, inventory, net.minecraft.world.inventory.ContainerLevelAccess.create(level, pos), playerEntity);
                        }
                    }, buf -> {
                        if(player.getAbilities().instabuild || EBDCommon.config.disableDiscoverySystem){
                            net.minecraft.core.Registry<Enchantment> registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
                            List<Holder.Reference<Enchantment>> holders = registry.holders()
                                .filter(h -> isEnchantmentEnabled(h.key().location()))
                                .toList();
                            buf.writeVarInt(holders.size());
                            for (Holder.Reference<Enchantment> holder : holders){
                                ResourceLocation loc = holder.key().location();
                                buf.writeUtf(loc.toString());
                                buf.writeVarInt(EBDCommon.getMaximumPossibleEnchantmentLevel(holder));
                            }
                        } else {
                            Object2IntOpenHashMap<ResourceLocation> enchantments = ((IServerPlayerAcc) player).enchantment_overhaul$getUnlockedEnchantments();
                            List<Object2IntMap.Entry<ResourceLocation>> list = new ArrayList<>();
                            for (Object2IntMap.Entry<ResourceLocation> e : enchantments.object2IntEntrySet()) {
                                if (isEnchantmentEnabled(e.getKey())) {
                                    list.add(e);
                                }
                            }
                            buf.writeVarInt(list.size());
                            for (Object2IntMap.Entry<ResourceLocation> e : list) {
                                buf.writeUtf(e.getKey().toString());
                                buf.writeVarInt(e.getIntValue());
                            }
                        }
                    });
                }
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    public void genConfig() throws IOException {
        Path p = Paths.get("./config/ebd");
        if(!Files.exists(p)){
            Files.createDirectories(p);
        }
        Path oldP = Paths.get("./config/eso/config.json");
        Path newP = Paths.get("./config/ebd/config.json");
        if(!Files.exists(newP)){
            if(Files.exists(oldP)){
                Files.copy(oldP, newP);
            } else {
                try(FileWriter writer = new FileWriter(newP.toFile())){
                    gson.toJson(ConfigurationFile.getDefault(), writer);
                }
            }
        }
    }

    public static void readConfig() throws IOException {
        JsonObject jsonObject = JsonParser.parseReader(new FileReader("./config/ebd/config.json")).getAsJsonObject();
        boolean shouldSave = false;
        if(!jsonObject.has("enableEnchantability")){
            jsonObject.addProperty("enableEnchantability", true);
            shouldSave = true;
        }
        if(!jsonObject.has("enableDefaultRecipe")){
            jsonObject.addProperty("enableDefaultRecipe", true);
            shouldSave = true;
        }
        if(!jsonObject.has("disableDiscoverySystem")){
            jsonObject.addProperty("disableDiscoverySystem", false);
            shouldSave = true;
        }
        if(!jsonObject.has("enableEnchantmentsLeveling")){
            jsonObject.addProperty("enableEnchantmentsLeveling", false);
            shouldSave = true;
        }
        if(!jsonObject.has("hideEnchantmentsWithoutRecipe")){
            jsonObject.addProperty("hideEnchantmentsWithoutRecipe", false);
            shouldSave = true;
        }
        if(!jsonObject.has("disableAnvilEnchanting")){
            jsonObject.addProperty("disableAnvilEnchanting", false);
            shouldSave = true;
        }
        if(!jsonObject.has("disableBookCombining")){
            jsonObject.addProperty("disableBookCombining", false);
            shouldSave = true;
        }
        if(!jsonObject.has("enableQuicksand")){
            jsonObject.addProperty("enableQuicksand", true);
            shouldSave = true;
        }
        if(!jsonObject.has("enableDisarm")){
            jsonObject.addProperty("enableDisarm", true);
            shouldSave = true;
        }
        if(!jsonObject.has("enableSting")){
            jsonObject.addProperty("enableSting", true);
            shouldSave = true;
        }
        if(!jsonObject.has("enableExecutioner")){
            jsonObject.addProperty("enableExecutioner", true);
            shouldSave = true;
        }
        if(!jsonObject.has("enableLeviathan")){
            jsonObject.addProperty("enableLeviathan", true);
            shouldSave = true;
        }
        if(!jsonObject.has("enableDragonsBreath")){
            jsonObject.addProperty("enableDragonsBreath", true);
            shouldSave = true;
        }
        if(!jsonObject.has("enableMagazine")){
            jsonObject.addProperty("enableMagazine", true);
            shouldSave = true;
        }
        if(!jsonObject.has("enableAutoReload")){
            jsonObject.addProperty("enableAutoReload", true);
            shouldSave = true;
        }
        if(!jsonObject.has("enableSlipway")){
            jsonObject.addProperty("enableSlipway", true);
            shouldSave = true;
        }
        if(!jsonObject.has("enableSeafarer")){
            jsonObject.addProperty("enableSeafarer", true);
            shouldSave = true;
        }
        if(!jsonObject.has("enableFireproofBoat")){
            jsonObject.addProperty("enableFireproofBoat", true);
            shouldSave = true;
        }
        if(!jsonObject.has("enableAutodrive")){
            jsonObject.addProperty("enableAutodrive", true);
            shouldSave = true;
        }
        if(!jsonObject.has("enableAeroflot")){
            jsonObject.addProperty("enableAeroflot", true);
            shouldSave = true;
        }
        if(!jsonObject.has("enablePocketBoat")){
            jsonObject.addProperty("enablePocketBoat", true);
            shouldSave = true;
        }
        if(!jsonObject.has("enableSubmarine")){
            jsonObject.addProperty("enableSubmarine", true);
            shouldSave = true;
        }
        if(!jsonObject.has("enableCapacity")){
            jsonObject.addProperty("enableCapacity", true);
            shouldSave = true;
        }
        EBDCommon.config = gson.fromJson(jsonObject, ConfigurationFile.class);
        if(shouldSave){
            try(FileWriter writer = new FileWriter("./config/ebd/config.json")){
                gson.toJson(config, writer);
            }
        }
    }

    public static boolean isEnchantmentEnabled(ResourceLocation location) {
        if (config == null) return true;
        if (!location.getNamespace().equals(MOD_ID)) {
            return true;
        }
        String path = location.getPath();
        return switch (path) {
            case "quicksand" -> config.enableQuicksand;
            case "disarm" -> config.enableDisarm;
            case "sting" -> config.enableSting;
            case "executioner" -> config.enableExecutioner;
            case "leviathan" -> config.enableLeviathan;
            case "dragons_breath" -> config.enableDragonsBreath;
            case "magazine" -> config.enableMagazine;
            case "auto_reload" -> config.enableAutoReload;
            case "slipway" -> config.enableSlipway;
            case "seafarer" -> config.enableSeafarer;
            case "fireproof_boat" -> config.enableFireproofBoat;
            case "autodrive" -> config.enableAutodrive;
            case "aeroflot" -> config.enableAeroflot;
            case "pocket_boat" -> config.enablePocketBoat;
            case "submarine" -> config.enableSubmarine;
            case "capacity" -> config.enableCapacity;
            default -> true;
        };
    }

    public void genDefaultRecipe() throws IOException{
        String p = "./config/ebd/default-recipe.json";
        if(!Files.exists(Paths.get(p))){
            LinkedHashMap<Integer, ItemData[]> levels = new LinkedHashMap<>();
            int level = 1;
            for (int i = 0; i < 3; i++) {
                ItemData[] dataArr = new ItemData[4];
                dataArr[0] = new ItemData("minecraft:lapis_lazuli", 12);
                dataArr[1] = new ItemData("minecraft:amethyst_shard", 3);
                dataArr[2] = new ItemData("minecraft:gold_ingot", 6);
                dataArr[3] = new ItemData("minecraft:diamond", 3);
                levels.put(level, dataArr);
                level++;
            }
            for (int i = 0; i < 4; i++) {
                ItemData[] dataArr = new ItemData[4];
                dataArr[0] = new ItemData("minecraft:lapis_lazuli", 32);
                dataArr[1] = new ItemData("minecraft:amethyst_shard", 7);
                dataArr[2] = new ItemData("minecraft:gold_ingot", 24);
                dataArr[3] = new ItemData("minecraft:diamond", 7);
                levels.put(level, dataArr);
                level++;
            }
            for (int i = 0; i < 3; i++) {
                ItemData[] dataArr = new ItemData[4];
                dataArr[0] = new ItemData("minecraft:lapis_lazuli", 48);
                dataArr[1] = new ItemData("minecraft:amethyst_shard", 16);
                dataArr[2] = new ItemData("minecraft:gold_ingot", 32);
                dataArr[3] = new ItemData("minecraft:diamond", 16);
                levels.put(level, dataArr);
                level++;
            }
            try(FileWriter writer = new FileWriter(p)){
                gson.toJson(levels, writer);
            }
        }
        String p2 = "./config/ebd/default-xp-map.json";
        if(!Files.exists(Paths.get(p2))){
            JsonObject tree = new JsonObject();
            tree.addProperty("useExpPoints", false);
            tree.add("xp", gson.toJsonTree(new Int2IntOpenHashMap()));
            try(FileWriter writer = new FileWriter(p2)){
                gson.toJson(tree, writer);
            }
        }
    }

    @Nullable
    public static List<RecipeHolder> getRecipeHolders(ResourceLocation location){
        List<RecipeHolder> holders = EBDCommon.recipeMap.get(location);
        return holders != null ? holders : config.enableDefaultRecipe ? EBDCommon.recipeMap.get(defaultRecipe) : null;
    }

    public static int getMaximumPossibleEnchantmentLevel(Holder<Enchantment> enchantment){
        ResourceLocation location = enchantment.unwrapKey().map(ResourceKey::location).orElse(null);
        if (location == null) return enchantment.value().getMaxLevel();
        List<RecipeHolder> holders = EBDCommon.recipeMap.get(location);
        int maxLevel = 0;
        if(holders != null && !holders.isEmpty()){
            for (RecipeHolder holder : holders){
                int l = holder.getMaxLevel(enchantment);
                if(l > maxLevel){
                    maxLevel = l;
                }
            }
        } else maxLevel = enchantment.value().getMaxLevel();
        return maxLevel;
    }

    public static Gson getGson(){
        return gson;
    }

    private void buildCreativeTabContents(final net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.INGREDIENTS) {
            event.accept(DUSTY_BOOK.get());
            event.accept(GOLDEN_WHEAT.get());
        }
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(GOLDEN_POTATO.get());
            event.accept(GOLDEN_BEETROOT.get());
        }
    }

    public void onRegisterBrewingRecipes(net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent event) {
        net.minecraft.world.item.alchemy.PotionBrewing.Builder builder = event.getBuilder();
        builder.addMix(net.minecraft.world.item.alchemy.Potions.AWKWARD, GOLDEN_POTATO.get(), SHRINKING_POTION);
        builder.addMix(SHRINKING_POTION, net.minecraft.world.item.Items.REDSTONE, LONG_SHRINKING_POTION);
        builder.addMix(SHRINKING_POTION, net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE, GROWING_POTION);
        builder.addMix(LONG_SHRINKING_POTION, net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE, LONG_GROWING_POTION);
        // XP Boost I: Awkward Potion + Bottle o' Enchanting → Potion of Experience Surge
        builder.addMix(net.minecraft.world.item.alchemy.Potions.AWKWARD, net.minecraft.world.item.Items.EXPERIENCE_BOTTLE, XP_BOOST_POTION);
        // XP Boost II: Potion of Experience Surge + Wither Rose → Potion of Experience Surge II
        builder.addMix(XP_BOOST_POTION, net.minecraft.world.item.Items.WITHER_ROSE, STRONG_XP_BOOST_POTION);
    }

    @SubscribeEvent
    public void onPlayerXpChange(net.neoforged.neoforge.event.entity.player.PlayerXpEvent.XpChange event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer sp) {
            net.minecraft.world.effect.MobEffectInstance boostEffect = sp.getEffect(XP_BOOST);
            if (boostEffect != null) {
                // Level 0 = 50% boost, Level 1 = 100% boost
                float multiplier = boostEffect.getAmplifier() == 0 ? 1.5f : 2.0f;
                event.setAmount(Math.round(event.getAmount() * multiplier));
            }
        }
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        
        if (stack.is(Items.BRUSH)) {
            ItemStack offhand = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (offhand.is(DUSTY_BOOK.get())) {
                player.startUsingItem(hand);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onUseItemTick(net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Tick event) {
        if (event.getEntity() instanceof Player player && event.getItem().is(Items.BRUSH)) {
            InteractionHand hand = player.getUsedItemHand();
            ItemStack offhand = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (offhand.is(DUSTY_BOOK.get())) {
                int elapsed = event.getItem().getUseDuration(player) - event.getDuration();
                
                if (elapsed % 10 == 0) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
                        SoundEvents.BRUSH_SAND, SoundSource.PLAYERS, 0.5F, 1.0F);
                    if (player.level().isClientSide) {
                        for (int i = 0; i < 5; i++) {
                            player.level().addParticle(ParticleTypes.SMOKE,
                                    player.getX() + player.level().random.nextGaussian() * 0.2,
                                    player.getY() + player.getEyeHeight() * 0.8 + player.level().random.nextGaussian() * 0.1,
                                    player.getZ() + player.level().random.nextGaussian() * 0.2,
                                    0.0, 0.1, 0.0);
                        }
                    }
                }

                if (elapsed >= 60) {
                    if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                        net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) player.level();
                        
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
                            SoundEvents.BRUSH_SAND_COMPLETED, SoundSource.PLAYERS, 0.7F, 1.0F);

                        if (!player.getAbilities().instabuild) {
                            event.getItem().hurtAndBreak(1, serverLevel, serverPlayer, item -> {});
                        }

                        if (!player.getAbilities().instabuild) {
                            offhand.shrink(1);
                        }

                        net.minecraft.core.Registry<Enchantment> registry = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
                        Object2IntOpenHashMap<ResourceLocation> unlocked = ((IServerPlayerAcc) serverPlayer).enchantment_overhaul$getUnlockedEnchantments();
                        
                        List<Holder<Enchantment>> allEnchantments = registry.holders().map(h -> (Holder<Enchantment>) h).toList();
                        List<Holder<Enchantment>> unlearned = new ArrayList<>();

                        for (Holder<Enchantment> h : allEnchantments) {
                            ResourceLocation loc = h.unwrapKey().map(key -> key.location()).orElse(null);
                            if (loc != null && isEnchantmentEnabled(loc)) {
                                if (!unlocked.containsKey(loc)) {
                                    unlearned.add(h);
                                }
                            }
                        }

                        List<Holder<Enchantment>> candidates = unlearned.isEmpty() ? 
                            allEnchantments.stream().filter(h -> h.unwrapKey().map(key -> isEnchantmentEnabled(key.location())).orElse(false)).toList() 
                            : unlearned;

                        if (!candidates.isEmpty()) {
                            int totalWeight = 0;
                            for (Holder<Enchantment> h : candidates) {
                                totalWeight += h.value().definition().weight();
                            }

                            if (totalWeight > 0) {
                                int randomWeight = player.level().random.nextInt(totalWeight);
                                Holder<Enchantment> selected = null;
                                int runningWeight = 0;
                                for (Holder<Enchantment> h : candidates) {
                                    runningWeight += h.value().definition().weight();
                                    if (randomWeight < runningWeight) {
                                        selected = h;
                                        break;
                                    }
                                }

                                if (selected != null) {
                                    int maxLvl = selected.value().getMaxLevel();
                                    int lvl = player.level().random.nextInt(maxLvl) + 1;
                                    ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                                    
                                    net.minecraft.world.item.enchantment.ItemEnchantments.Mutable builder = 
                                            new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                                    builder.set(selected, lvl);
                                    
                                    enchantedBook.set(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
                                    
                                    if (!player.getInventory().add(enchantedBook)) {
                                        player.drop(enchantedBook, false);
                                    }
                                }
                            }
                        }
                    }
                    player.stopUsingItem();
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoinLevel(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.ExperienceOrb) {
            event.setCanceled(true);
        }
        if (event.getEntity() instanceof net.minecraft.world.entity.projectile.Projectile projectile) {
            net.minecraft.world.entity.Entity owner = projectile.getOwner();
            if (owner instanceof Player player) {
                int hunterLevel = Utils.getSkillLevel(player, "hunter");
                if (hunterLevel >= 100) {
                    projectile.setDeltaMovement(projectile.getDeltaMovement().scale(1.5));
                }
            }
        }
        if (event.getEntity() instanceof Boat boat) {
            if (boat.level().isClientSide()) {
                aiefu.ebd.network.ClientsideNetworkManager.applyPendingEnchantments(boat);
            } else {
                net.minecraft.world.item.enchantment.ItemEnchantments placing = EBDGameplayEvents.ACTIVE_PLACING_BOAT_ENCHANTMENTS.get();
                if (placing != null && !placing.isEmpty()) {
                    if (boat instanceof IBoatEnchanted enchanted) {
                        enchanted.ebd$setBoatEnchantments(placing);
                    }
                    EBDGameplayEvents.ACTIVE_PLACING_BOAT_ENCHANTMENTS.set(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                }
            }
        }
    }


    @SubscribeEvent
    public void onLivingExperienceDrop(net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent event) {
        event.setDroppedExperience(0);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() && player.level().getGameTime() % 20 == 0) {
            Registry<Enchantment> registry = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            Optional<Holder.Reference<Enchantment>> mendingHolderOpt = registry.getHolder(Enchantments.MENDING);
            if (mendingHolderOpt.isPresent()) {
                Holder<Enchantment> mendingHolder = mendingHolderOpt.get();
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ItemStack stack = player.getItemBySlot(slot);
                    if (!stack.isEmpty() && stack.isDamageableItem() && stack.getDamageValue() > 0) {
                        if (stack.getEnchantments().getLevel(mendingHolder) > 0) {
                            stack.setDamageValue(stack.getDamageValue() - 1);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onStartTracking(net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Boat boat && boat instanceof IBoatEnchanted enchanted) {
            net.minecraft.world.item.enchantment.ItemEnchantments enc = enchanted.ebd$getBoatEnchantments();
            if (enc != null && !enc.isEmpty() && event.getEntity() instanceof ServerPlayer player) {
                net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
                net.minecraft.resources.RegistryOps<net.minecraft.nbt.Tag> ops =
                        net.minecraft.resources.RegistryOps.create(net.minecraft.nbt.NbtOps.INSTANCE, boat.level().registryAccess());
                net.minecraft.world.item.enchantment.ItemEnchantments.CODEC.encodeStart(ops, enc)
                        .result()
                        .ifPresent(nbt -> tag.put("ESOEnch", nbt));
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new S2CBoatSyncPayload(boat.getId(), tag));
            }
        }
    }

    public static void handleGrindstoneDisenchant(ServerPlayer serverPlayer, ItemStack input1, ItemStack input2) {
        List<Holder<Enchantment>> enchsOnItems = new ArrayList<>();
        collectNonCurseEnchantments(input1, enchsOnItems);
        collectNonCurseEnchantments(input2, enchsOnItems);

        if (enchsOnItems.isEmpty()) return;

        // Grant Enchanter XP
        ((IServerPlayerAcc) serverPlayer).ebd$addSkillXP("enchanter", LBDConfig.INSTANCE.xpItemDisenchanted, serverPlayer);

        // Enchanter Passive 2: 15% chance to get an enchanted book back
        if (Utils.getSkillLevel(serverPlayer, "enchanter") >= 50) {
            if (serverPlayer.level().random.nextFloat() < 0.15f) {
                int bookSlot = -1;
                for (int idx = 0; idx < serverPlayer.getInventory().getContainerSize(); idx++) {
                    ItemStack invStack = serverPlayer.getInventory().getItem(idx);
                    if (invStack.is(net.minecraft.world.item.Items.BOOK)) {
                        bookSlot = idx;
                        break;
                    }
                }
                if (bookSlot != -1) {
                    serverPlayer.getInventory().getItem(bookSlot).shrink(1);
                    Holder<Enchantment> bookEnch = enchsOnItems.get(serverPlayer.level().random.nextInt(enchsOnItems.size()));
                    int level = Math.max(input1.getEnchantments().getLevel(bookEnch), input2.getEnchantments().getLevel(bookEnch));
                    if (level == 0) level = 1;
                    
                    ItemStack enchantedBook = new ItemStack(net.minecraft.world.item.Items.ENCHANTED_BOOK);
                    net.minecraft.world.item.enchantment.ItemEnchantments.Mutable builder = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                    builder.set(bookEnch, level);
                    enchantedBook.set(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
                    
                    if (!serverPlayer.getInventory().add(enchantedBook)) {
                        serverPlayer.drop(enchantedBook, false);
                    }
                    
                    if (serverPlayer.level() instanceof ServerLevel serverLevel) {
                        serverLevel.playSeededSound(null,
                                serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                                1.0F, 1.5F, serverLevel.random.nextLong());
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, serverPlayer.getX(), serverPlayer.getY() + 1.0, serverPlayer.getZ(), 10, 0.3, 0.3, 0.3, 0.0);
                    }
                    serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§6❖ §eМагия точильного камня сохранила чары в книгу!"));
                }
            }
        }

        if (config != null && config.disableDiscoverySystem) return;

        // Фильтруем: оставляем только те, что игрок ещё не изучил
        Object2IntOpenHashMap<ResourceLocation> unlocked =
                ((IServerPlayerAcc) serverPlayer).enchantment_overhaul$getUnlockedEnchantments();

        List<Holder<Enchantment>> candidates = new ArrayList<>();
        for (Holder<Enchantment> h : enchsOnItems) {
            ResourceLocation loc = h.unwrapKey().map(k -> k.location()).orElse(null);
            if (loc == null) continue;
            if (!unlocked.containsKey(loc)) {
                candidates.add(h);
            }
        }

        if (candidates.isEmpty()) return;

        // 100% шанс — выбираем случайное из кандидатов
        Holder<Enchantment> selected = candidates.get(
                serverPlayer.level().random.nextInt(candidates.size())
        );

        ResourceLocation loc = selected.unwrapKey().map(k -> k.location()).orElse(null);
        if (loc == null) return;

        // Открываем зачарование (уровень 1)
        unlocked.put(loc, 1);
        ((IServerPlayerAcc) serverPlayer).enchantment_overhaul$setUnlockedEnchantments(unlocked);

        // Повреждаем предмет в основной руке на 60-90% от макс. прочности (если уровень Enchanter < 25)
        if (Utils.getSkillLevel(serverPlayer, "enchanter") < 25) {
            ItemStack held = serverPlayer.getMainHandItem();
            if (!held.isEmpty() && held.isDamageableItem()) {
                int maxDur = held.getMaxDamage();
                float fraction = 0.60f + serverPlayer.level().random.nextFloat() * 0.30f;
                int damage = (int)(maxDur * fraction);
                held.setDamageValue(Math.min(held.getDamageValue() + damage, maxDur - 1));
            }
        }

        // Звуки
        if (serverPlayer.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSeededSound(null,
                    serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS,
                    1.0F, 1.0F, serverLevel.random.nextLong());
            serverLevel.playSeededSound(null,
                    serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS,
                    0.5F, 1.2F, serverLevel.random.nextLong());
        }

        // Сообщение
        serverPlayer.sendSystemMessage(
            net.minecraft.network.chat.Component.literal("§6❖ §eВы изучили зачарование: ")
                .append(selected.value().description().copy()
                    .withStyle(net.minecraft.ChatFormatting.AQUA))
                .append(net.minecraft.network.chat.Component.literal("§e, исследуя магические следы!"))
        );

        LOGGER.info("[ESO Grindstone] {} learned enchantment: {}", serverPlayer.getName().getString(), loc);
    }

    private static void collectNonCurseEnchantments(ItemStack stack, List<Holder<Enchantment>> out) {
        if (stack.isEmpty()) return;
        net.minecraft.world.item.enchantment.ItemEnchantments enchantments = net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentsForCrafting(stack);
        for (Holder<Enchantment> holder : enchantments.keySet()) {
            if (!holder.is(net.minecraft.tags.EnchantmentTags.CURSE)) {
                out.add(holder);
            }
        }
    }

    private void applyOrExtendEffect(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int durationToAdd, int amplifier) {
        net.minecraft.world.effect.MobEffectInstance existing = player.getEffect(effect);
        int newDuration = durationToAdd;
        if (existing != null) {
            newDuration = existing.getDuration() + durationToAdd;
        }
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect, newDuration, amplifier));
    }
}
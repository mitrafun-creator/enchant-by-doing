package aiefu.ebd.client;

import aiefu.ebd.EBDCommon;
import aiefu.ebd.client.gui.EnchantingTableScreen;
import aiefu.ebd.data.ColorsDataReloadListener;
import aiefu.ebd.data.LanguageReloadListener;
import aiefu.ebd.data.client.ColorDataHolder;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.lwjgl.glfw.GLFW;

import net.minecraft.world.item.ItemStack;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = EBDCommon.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EBDClient {
    public static ColorDataHolder colorData;
    private static final ConcurrentHashMap<Holder<Enchantment>, MutableComponent> descriptions = new ConcurrentHashMap<>();
    public static final KeyMapping recipeKey = new KeyMapping("eso.recipekeybind",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U, "eso.modname");
    public static final KeyMapping skillsMenuKey = new KeyMapping("eso.skillsmenukeybind",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "eso.modname");

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(recipeKey);
        event.register(skillsMenuKey);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(EBDCommon.enchantment_menu_ovr.get(), EnchantingTableScreen::new);
    }

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new LanguageReloadListener());
        event.registerReloadListener(new ColorsDataReloadListener());
    }

    @SubscribeEvent
    public static void registerEntityRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EBDCommon.THROWN_AXE.get(), aiefu.ebd.client.render.ThrownAxeRenderer::new);
        event.registerEntityRenderer(EBDCommon.THROWN_BOTTLE.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
    }

    public static MutableComponent getEnchantmentDescription(Holder<Enchantment> e){
        return descriptions.computeIfAbsent(e, (enchantment) -> {
            String desc = getDescriptionId(enchantment);
            String ed = desc + ".desc";
            Language language = Language.getInstance();
            if (!language.has(ed) && language.has(desc + ".description")) {
                ed = desc + ".description";
            }
           return Component.translatable(ed).withStyle(ChatFormatting.DARK_GRAY);
        });
    }

    private static String getDescriptionId(Holder<Enchantment> e) {
        if (e.value().description().getContents() instanceof TranslatableContents tc) {
            return tc.getKey();
        }
        return e.unwrapKey().map(key -> "enchantment." + key.location().getNamespace() + "." + key.location().getPath()).orElse("enchantment.unknown");
    }

    public static Player getClientPlayer(){
        return Minecraft.getInstance().player;
    }

    public static boolean isZPressed() {
        Minecraft client = Minecraft.getInstance();
        if (client.getWindow() != null) {
            return InputConstants.isKeyDown(
                client.getWindow().getWindow(),
                GLFW.GLFW_KEY_Z
            );
        }
        return false;
    }

    public static boolean isSkillsTreeScreenOpen() {
        return Minecraft.getInstance().screen instanceof aiefu.ebd.client.gui.SkillsTreeScreen;
    }

    @EventBusSubscriber(modid = EBDCommon.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class EBDClientEvents {
        @SubscribeEvent
        public static void onRenderGuiLayerPre(net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre event) {
            if (event.getName().equals(net.neoforged.neoforge.client.gui.VanillaGuiLayers.EXPERIENCE_BAR)) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onRenderGuiLayerPost(net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post event) {
            if (event.getName().equals(net.neoforged.neoforge.client.gui.VanillaGuiLayers.HOTBAR)) {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                aiefu.ebd.client.SkillHUDRenderer.render(event.getGuiGraphics(), mc.font, event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight());
            }
        }

        @SubscribeEvent
        public static void onScreenOpening(net.neoforged.neoforge.client.event.ScreenEvent.Opening event) {
            if (event.getNewScreen() instanceof net.minecraft.client.gui.screens.inventory.CraftingScreen ||
                event.getNewScreen() instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null && mc.player != null) {
                    aiefu.ebd.network.ClientsideNetworkManager.clientNearbyWorkstationsMask = aiefu.ebd.workstation.WorkstationHelper.getNearbyWorkstationsMask(
                        mc.level, mc.player.blockPosition(), aiefu.ebd.LBDConfig.INSTANCE.workstationDetectionRadius
                    );
                    aiefu.ebd.network.ClientsideNetworkManager.clientRequiredWorkstationsMask = 0;
                    aiefu.ebd.network.ClientsideNetworkManager.clientMissingWorkstationsMask = 0;
                }
            }
        }

        @SubscribeEvent
        public static void onScreenClosing(net.neoforged.neoforge.client.event.ScreenEvent.Closing event) {
            aiefu.ebd.network.ClientsideNetworkManager.reset();
        }

        @SubscribeEvent
        public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player != null && mc.screen == null) {
                while (skillsMenuKey.consumeClick()) {
                    float originalPitch = mc.player.getXRot();
                    mc.setScreen(new aiefu.ebd.client.gui.SkillsTransitionScreen(originalPitch));
                }

                // Hunter Level 100 Auto-shoot logic
                if (mc.gameMode != null) {
                    int hunterLevel = aiefu.ebd.Utils.getSkillLevel(mc.player, "hunter");
                    if (hunterLevel >= 100 && mc.options.keyUse.isDown()) {
                        // 1. Bow auto-shoot
                        if (mc.player.isUsingItem()) {
                            ItemStack usingItem = mc.player.getUseItem();
                            if (usingItem.getItem() instanceof net.minecraft.world.item.BowItem) {
                                int elapsed = usingItem.getUseDuration(mc.player) - mc.player.getUseItemRemainingTicks();
                                int modifiedElapsed = (int) (elapsed * 1.50); // 50% draw speed
                                float power = net.minecraft.world.item.BowItem.getPowerForTime(modifiedElapsed);
                                if (power >= 1.0F) {
                                    net.minecraft.world.InteractionHand hand = mc.player.getUsedItemHand();
                                    mc.gameMode.releaseUsingItem(mc.player);
                                    if (mc.options.keyUse.isDown()) {
                                        mc.gameMode.useItem(mc.player, hand);
                                    }
                                }
                            }
                        }

                        // 2. Crossbow auto-shoot
                        for (net.minecraft.world.InteractionHand hand : net.minecraft.world.InteractionHand.values()) {
                            ItemStack stack = mc.player.getItemInHand(hand);
                            if (stack.getItem() instanceof net.minecraft.world.item.CrossbowItem) {
                                net.minecraft.world.item.component.ChargedProjectiles charged = stack.get(net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES);
                                if (charged != null && !charged.isEmpty()) {
                                    mc.gameMode.useItem(mc.player, hand);
                                }
                            }
                        }
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onItemTooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
            if (event.getEntity() != null && !event.getItemStack().isEmpty()) {
                if (aiefu.ebd.GlobalPerks.isItemLockedForPlayer(event.getEntity(), event.getItemStack())) {
                    aiefu.ebd.GlobalPerks.Perk perk = aiefu.ebd.GlobalPerks.getRequiredPerk(event.getItemStack());
                    if (perk != null) {
                        event.getToolTip().add(
                                Component.literal("§c🔒 Требуется перк: §6").append(perk.getDisplayName()).append(" §c(§e" + perk.costPerLevel + " очк.§c)")
                        );
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onRenderBlockScreenEffect(net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent event) {
            if (event.getOverlayType() == net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent.OverlayType.FIRE) {
                net.minecraft.world.entity.player.Player player = event.getPlayer();
                if (player != null && player.getVehicle() instanceof net.minecraft.world.entity.vehicle.Boat boat) {
                    if (boat instanceof aiefu.ebd.IBoatEnchanted enchanted && aiefu.ebd.Utils.getEnchantmentLevel(enchanted.ebd$getBoatEnchantments(), "fireproof_boat") > 0) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}

package aiefu.ebd.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

public class SkillsTransitionScreen extends Screen {
    private final float originalPitch;
    private final long startTime;
    private static final long DURATION = 500; // 0.5s
    private boolean soundPlayed = false;

    public SkillsTransitionScreen(float originalPitch) {
        super(Component.literal("Transition"));
        this.originalPitch = originalPitch;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        super.init();
        Minecraft mc = Minecraft.getInstance();
        if (!soundPlayed) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.END_PORTAL_FRAME_FILL, 0.7F));
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F));
            try {
                mc.gameRenderer.loadEffect(ResourceLocation.parse("minecraft:shaders/post/blur.json"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            soundPlayed = true;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        long elapsed = System.currentTimeMillis() - startTime;
        float progress = Math.min(1.0F, (float) elapsed / DURATION);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            float currentPitch = originalPitch + (-90.0F - originalPitch) * progress;
            mc.player.setXRot(currentPitch);
        }

        int alpha = (int) (progress * 220);
        int color = (alpha << 24) | 0x12041d;
        graphics.fillGradient(0, 0, width, height, color, color);

        if (progress >= 1.0F) {
            try {
                mc.gameRenderer.shutdownEffect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mc.setScreen(new SkillsTreeScreen(originalPitch));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

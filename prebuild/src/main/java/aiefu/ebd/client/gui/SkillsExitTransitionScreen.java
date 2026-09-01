package aiefu.ebd.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

import java.util.Map;

public class SkillsExitTransitionScreen extends Screen {
    private final float originalPitch;
    private final Map<SoundSource, Float> originalVolumes;
    private final SkillsTreeScreen.CustomMusicSoundInstance musicInstance;
    private final long startTime;
    private static final long DURATION = 500; // 0.5s
    private boolean shaderLoaded = false;

    public SkillsExitTransitionScreen(float originalPitch, Map<SoundSource, Float> originalVolumes, SkillsTreeScreen.CustomMusicSoundInstance musicInstance) {
        super(Component.literal("Exit Transition"));
        this.originalPitch = originalPitch;
        this.originalVolumes = originalVolumes;
        this.musicInstance = musicInstance;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        super.init();
        Minecraft mc = Minecraft.getInstance();
        if (!shaderLoaded) {
            try {
                mc.gameRenderer.loadEffect(ResourceLocation.parse("minecraft:shaders/post/blur.json"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            shaderLoaded = true;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        long elapsed = System.currentTimeMillis() - startTime;
        float progress = Math.min(1.0F, (float) elapsed / DURATION);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            // Interpolate pitch from -90.0F back to originalPitch
            float currentPitch = -90.0F + (originalPitch - (-90.0F)) * progress;
            mc.player.setXRot(currentPitch);
        }

        // Smoothly restore sound volumes
        originalVolumes.forEach((source, originalVol) -> {
            float currentMuffledVol = originalVol * 0.15F;
            float newVol = currentMuffledVol + (originalVol - currentMuffledVol) * progress;
            mc.options.getSoundSourceOptionInstance(source).set((double) newVol);
        });

        // Fade out dark purple background: alpha goes from 220 down to 0
        int alpha = (int) ((1.0F - progress) * 220);
        int color = (alpha << 24) | 0x12041d;
        graphics.fillGradient(0, 0, width, height, color, color);

        if (progress >= 1.0F) {
            // Transition finished, clean up everything
            if (musicInstance != null) {
                mc.getSoundManager().stop(musicInstance);
            }

            originalVolumes.forEach((source, vol) -> mc.options.getSoundSourceOptionInstance(source).set((double) vol));
            originalVolumes.clear();

            try {
                mc.gameRenderer.shutdownEffect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mc.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

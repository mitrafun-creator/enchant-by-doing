package aiefu.ebd.client;

import aiefu.ebd.SkillType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class SkillHUDRenderer {
    public static final ResourceLocation EXPERIENCE_BAR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/experience_bar_background");

    public static final java.util.Map<String, Integer> CLIENT_SKILL_LEVELS = new java.util.HashMap<>();
    public static final java.util.Map<String, Double> CLIENT_SKILL_XP = new java.util.HashMap<>();
    public static final java.util.Map<String, Double> CLIENT_SKILL_NEEDED_XP = new java.util.HashMap<>();

    private static String lastActiveSkill = null;
    private static int lastLevel = 1;
    private static double lastXp = 0.0;
    private static double lastNeededXp = 100.0;
    private static long lastGainTime = 0;

    public static void updateSkill(String skill, int level, double xp, double neededXp) {
        CLIENT_SKILL_LEVELS.put(skill, level);
        CLIENT_SKILL_XP.put(skill, xp);
        CLIENT_SKILL_NEEDED_XP.put(skill, neededXp);
        lastActiveSkill = skill;
        lastLevel = level;
        lastXp = xp;
        lastNeededXp = neededXp;
        lastGainTime = System.currentTimeMillis();
    }

    public static void render(GuiGraphics graphics, Font font, int width, int height) {
        if (lastActiveSkill == null || lastLevel >= 100) return;
        long elapsed = System.currentTimeMillis() - lastGainTime;
        if (elapsed >= 4000) {
            return; // Fade out completely after 4 seconds
        }

        float alpha = 1.0f;
        if (elapsed > 3000) {
            alpha = 1.0f - (elapsed - 3000) / 1000.0f;
        }

        int alphaInt = (int) (alpha * 255) << 24;
        SkillType type = SkillType.fromId(lastActiveSkill);
        if (type == null) return;

        int x = width / 2 - 91;
        int y = 15;

        // Render Experience Bar Background (centered)
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        graphics.blitSprite(EXPERIENCE_BAR_BACKGROUND_SPRITE, x, y, 182, 5);
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // Reset

        // Render Custom Colored Progress Bar inside the background frame
        double progress = Math.max(0.0, Math.min(1.0, lastXp / lastNeededXp));
        int progressWidth = (int) (progress * 180);
        if (progressWidth > 0) {
            // Цветная полоска имеет 25% прозрачности (непрозрачность 75%)
            int barAlpha = (int) (alpha * 0.75f * 255) << 24;
            int barColor = (type.color & 0x00FFFFFF) | barAlpha;
            graphics.fill(x + 1, y + 1, x + 1 + progressWidth, y + 4, barColor);
        }

        // Render level text centered above the bar
        String levelStr = String.valueOf(lastLevel);
        int textX = (width - font.width(levelStr)) / 2;
        int textY = y - 9;
        
        // Shadow outline
        int shadowColor = 0x00000000 | alphaInt;
        graphics.drawString(font, levelStr, textX + 1, textY, shadowColor, false);
        graphics.drawString(font, levelStr, textX - 1, textY, shadowColor, false);
        graphics.drawString(font, levelStr, textX, textY + 1, shadowColor, false);
        graphics.drawString(font, levelStr, textX, textY - 1, shadowColor, false);
        
        // Foreground text
        int textColor = (type.color & 0x00FFFFFF) | alphaInt;
        graphics.drawString(font, levelStr, textX, textY, textColor, false);
        
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
    }
}

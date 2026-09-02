package aiefu.ebd.client.gui;

import aiefu.ebd.SkillType;
import aiefu.ebd.client.SkillHUDRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.util.RandomSource;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.item.ItemStack;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillsTreeScreen extends Screen {

    public static class StarNode {
        public String id;
        public float x;
        public float y;
        public String type;
        public String name;
        public String description;
        public int unlockLevel;
    }

    public static class Constellation {
        public final List<StarNode> stars = new ArrayList<>();
        public final List<String[]> connections = new ArrayList<>();
    }

    private static final Map<String, Constellation> CONSTELLATIONS = new HashMap<>();

    private void loadConstellations() {
        CONSTELLATIONS.clear();
        java.nio.file.Path path = java.nio.file.Paths.get("./ebd_skills_layout.json");
        if (!java.nio.file.Files.exists(path)) {
            path = java.nio.file.Paths.get("./config/ebd/ebd_skills_layout.json");
        }
        if (!java.nio.file.Files.exists(path)) {
            path = java.nio.file.Paths.get("./eso_skills_layout.json");
        }
        if (!java.nio.file.Files.exists(path)) {
            path = java.nio.file.Paths.get("./config/ebd/eso_skills_layout.json");
        }
        
        if (java.nio.file.Files.exists(path)) {
            try (java.io.BufferedReader reader = java.nio.file.Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                for (String skillId : root.keySet()) {
                    JsonObject skillObj = root.getAsJsonObject(skillId);
                    Constellation constellation = new Constellation();
                    
                    if (skillObj.has("stars")) {
                        JsonArray starsArray = skillObj.getAsJsonArray("stars");
                        for (int i = 0; i < starsArray.size(); i++) {
                            JsonObject starObj = starsArray.get(i).getAsJsonObject();
                            StarNode star = new StarNode();
                            star.id = starObj.get("id").getAsString();
                            star.x = starObj.get("x").getAsFloat();
                            star.y = starObj.get("y").getAsFloat();
                            star.type = starObj.get("type").getAsString();
                            star.name = starObj.has("name") ? starObj.get("name").getAsString() : "";
                            star.description = starObj.has("description") ? starObj.get("description").getAsString() : "";
                            
                            if (starObj.has("unlockLevel")) {
                                star.unlockLevel = starObj.get("unlockLevel").getAsInt();
                            } else {
                                int totalStars = starsArray.size();
                                if (star.type.equals("start") || i == 0) {
                                    star.unlockLevel = 1;
                                } else if (totalStars > 1) {
                                    star.unlockLevel = (int) Math.round(1.0 + (double) i * (99.0 / (double) (totalStars - 1)));
                                } else {
                                    star.unlockLevel = 1;
                                }
                            }
                            
                            constellation.stars.add(star);
                        }
                    }
                    
                    if (skillObj.has("connections")) {
                        JsonArray connsArray = skillObj.getAsJsonArray("connections");
                        for (int i = 0; i < connsArray.size(); i++) {
                            JsonArray connPair = connsArray.get(i).getAsJsonArray();
                            if (connPair.size() == 2) {
                                constellation.connections.add(new String[]{
                                    connPair.get(0).getAsString(),
                                    connPair.get(1).getAsString()
                                });
                            }
                        }
                    }
                    
                    CONSTELLATIONS.put(skillId.toLowerCase(), constellation);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (CONSTELLATIONS.isEmpty()) {
            for (SkillType type : SkillType.values()) {
                Constellation constellation = new Constellation();
                StarNode star = new StarNode();
                star.id = type.id + "_start";
                star.x = 0;
                star.y = 0;
                star.type = "start";
                star.name = "";
                star.description = "";
                star.unlockLevel = 1;
                constellation.stars.add(star);
                CONSTELLATIONS.put(type.id, constellation);
            }
        }
    }

    private float bottomBarYOffset = 30.0f;
    private final float originalPitch;
    private CustomMusicSoundInstance musicInstance;
    private final Map<SoundSource, Float> originalVolumes = new HashMap<>();
    private final Map<SkillType, ItemStack> skillItemCache = new HashMap<>();
    
    private final List<Star> stars = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    
    private int selectedSkillIndex = 0;
    private boolean transitioning = false;
    private float currentPanX;
    private float targetPanX;
    
    public SkillsTreeScreen(float originalPitch) {
        super(Component.translatable("skill.enchant_by_doing.screen_title"));
        this.originalPitch = originalPitch;
        
        this.selectedSkillIndex = 0;
        this.targetPanX = 0.0f;
        this.currentPanX = 0.0f;
        
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < 150; i++) {
            stars.add(new Star(rand.nextFloat(), rand.nextFloat(), 0.5f + rand.nextFloat() * 1.5f, 0.3f + rand.nextFloat() * 0.7f));
        }
    }

    @Override
    protected void init() {
        super.init();
        Minecraft mc = Minecraft.getInstance();
        
        loadConstellations();
        
        if (musicInstance == null) {
            musicInstance = new CustomMusicSoundInstance(SoundEvents.MUSIC_CREDITS.value());
            mc.getSoundManager().play(musicInstance);
        }

        for (SoundSource source : new SoundSource[]{
                SoundSource.BLOCKS,
                SoundSource.NEUTRAL,
                SoundSource.HOSTILE,
                SoundSource.WEATHER
        }) {
            if (!originalVolumes.containsKey(source)) {
                double currentVol = mc.options.getSoundSourceOptionInstance(source).get();
                originalVolumes.put(source, (float) currentVol);
                mc.options.getSoundSourceOptionInstance(source).set(currentVol * 0.15);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        currentPanX += (targetPanX - currentPanX) * 0.15f;

        // Draw solid background to overwrite the environment/world
        int bgGradientColor = 0xFF12041d;
        graphics.fillGradient(0, 0, width, height, bgGradientColor, bgGradientColor);

        // Call super.render here so default background blurs/shadows render behind our content
        super.render(graphics, mouseX, mouseY, partialTick);

        float dx = mouseX - width / 2.0F;
        float dy = mouseY - height / 2.0F;

        // Draw stars with parallax using currentPanX
        for (Star star : stars) {
            float sx = (star.x * width + dx * 0.1f * star.speed + currentPanX * 0.3f * star.speed) % width;
            float sy = (star.y * height + dy * 0.1f * star.speed) % height;
            if (sx < 0) sx += width;
            if (sy < 0) sy += height;

            float alpha = star.alpha * (0.7f + 0.3f * (float) Math.sin(System.currentTimeMillis() * 0.003f + star.x * 100));
            int color = ((int) (alpha * 255) << 24) | 0xFFFFFF;
            
            int size = Math.round(star.size);
            graphics.fill((int) sx, (int) sy, (int) sx + size, (int) sy + size, color);
        }

        updateParticles();
        for (Particle p : particles) {
            int pColor = ((int) (p.alpha * 255) << 24) | 0xa020f0;
            graphics.fill((int) p.x - 1, (int) p.y - 1, (int) p.x + 2, (int) p.y + 2, pColor);
        }

        SkillType skillType = SkillType.values()[selectedSkillIndex];
        
        int centerX = width / 2;
        int centerY = height / 2;

        // Render all constellations side-by-side shifted by currentPanX with cyclical wrap
        SkillType[] skills = SkillType.values();
        float totalWidth = skills.length * 300.0f;
        float halfWidth = totalWidth / 2.0f;

        for (int i = 0; i < skills.length; i++) {
            SkillType skill = skills[i];
            
            float baseX = i * 300.0f;
            float xDiff = baseX + currentPanX;
            while (xDiff < -halfWidth) xDiff += totalWidth;
            while (xDiff > halfWidth) xDiff -= totalWidth;
            
            float skillCenterX = centerX + xDiff;
            
            // Skip rendering if far off screen
            if (skillCenterX < -150 || skillCenterX > width + 150) {
                continue;
            }

            // Рендерим туманность на фоне созвездия с минимальным параллаксом
            float nebulaX = skillCenterX + dx * 0.01f;
            float nebulaY = centerY + dy * 0.01f;

            ItemStack skillItem = getSkillItem(skill);
            
            graphics.pose().pushPose();
            // Сдвигаем назад по оси Z, чтобы предмет гарантированно рисовался на заднем плане
            graphics.pose().translate(nebulaX, nebulaY, -180.0f);
            graphics.pose().scale(16.0f, 16.0f, 1.0f);
            graphics.pose().translate(-8.0f, -8.0f, 0.0f);
            
            graphics.renderFakeItem(skillItem, 0, 0);
            
            graphics.pose().popPose();

            // Render connections and loaded stars
            int currentLevel = SkillHUDRenderer.CLIENT_SKILL_LEVELS.getOrDefault(skill.id, 1);
            Constellation constellation = CONSTELLATIONS.get(skill.id);
            if (constellation != null) {
                // Connections (flowing glyph lines)
                List<String[]> connectionsToRender = constellation.connections;
                if (connectionsToRender.isEmpty() && constellation.stars.size() > 1) {
                    connectionsToRender = new ArrayList<>();
                    for (int idx = 0; idx < constellation.stars.size() - 1; idx++) {
                        connectionsToRender.add(new String[]{constellation.stars.get(idx).id, constellation.stars.get(idx + 1).id});
                    }
                }
                for (String[] conn : connectionsToRender) {
                    StarNode starA = null;
                    StarNode starB = null;
                    for (StarNode s : constellation.stars) {
                        if (s.id.equals(conn[0])) starA = s;
                        if (s.id.equals(conn[1])) starB = s;
                    }
                    if (starA != null && starB != null) {
                        float xA = skillCenterX + starA.x;
                        float yA = centerY + starA.y;
                        float xB = skillCenterX + starB.x;
                        float yB = centerY + starB.y;
                        
                        boolean aUnlocked = currentLevel >= starA.unlockLevel;
                        boolean bUnlocked = currentLevel >= starB.unlockLevel;
                        
                        drawGlyphLine(graphics, xA, yA, xB, yB, skill.color, aUnlocked && bUnlocked);
                    }
                }

                // Stars
                for (int starIdx = 0; starIdx < constellation.stars.size(); starIdx++) {
                    StarNode star = constellation.stars.get(starIdx);
                    float starX = skillCenterX + star.x;
                    float starY = centerY + star.y;
                    
                    boolean unlocked = currentLevel >= star.unlockLevel;
                    
                    if (unlocked) {
                        // Pulsing experience orb (XP Orb / Green sphere)
                        float pulse = 0.8f + 0.2f * (float) Math.sin(System.currentTimeMillis() * 0.005f + starIdx * 1.5f);
                        int outerSize = (int) (5 * pulse);
                        int innerSize = (int) (2.5f * pulse);
                        int xpColor = (((System.currentTimeMillis() + starIdx * 300) / 250) % 2 == 0) ? 0xFF55FF55 : 0xFFFFFF55;
                        
                        if (i == selectedSkillIndex && (star.type.equals("start") || starIdx == 0)) {
                            outerSize += 1;
                            innerSize += 0.5f;
                        }

                        graphics.pose().pushPose();
                        graphics.pose().translate(0.0f, 0.0f, 100.0f);
                        graphics.fill((int) starX - outerSize - 1, (int) starY - outerSize - 1, (int) starX + outerSize + 1, (int) starY + outerSize + 1, 0xAA000000);
                        graphics.fill((int) starX - outerSize, (int) starY - outerSize, (int) starX + outerSize, (int) starY + outerSize, 0xFF44AA44);
                        graphics.fill((int) starX - innerSize, (int) starY - innerSize, (int) starX + innerSize, (int) starY + innerSize, xpColor);
                        graphics.pose().popPose();
                    } else {
                        // Locked star node (dark gray)
                        graphics.pose().pushPose();
                        graphics.pose().translate(0.0f, 0.0f, 100.0f);
                        graphics.fill((int) starX - 4, (int) starY - 4, (int) starX + 4, (int) starY + 4, 0xAA000000);
                        graphics.fill((int) starX - 3, (int) starY - 3, (int) starX + 3, (int) starY + 3, 0xFF555555);
                        graphics.fill((int) starX - 1, (int) starY - 1, (int) starX + 1, (int) starY + 1, 0xFF888888);
                        graphics.pose().popPose();
                    }
                }
            }
        }

        int skillLevel = SkillHUDRenderer.CLIENT_SKILL_LEVELS.getOrDefault(skillType.id, 1);
        
        // Interpolate slide-up offset
        bottomBarYOffset += (0.0f - bottomBarYOffset) * 0.15f;

        // Render bottom experience bar
        int barWidth = 182;
        int barHeight = 5;
        int barX = centerX - barWidth / 2;
        int barY = height - 25 + (int) bottomBarYOffset;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // Draw background
        graphics.blitSprite(SkillHUDRenderer.EXPERIENCE_BAR_BACKGROUND_SPRITE, barX, barY, barWidth, barHeight);

        // Draw colored progress bar
        double xp = SkillHUDRenderer.CLIENT_SKILL_XP.getOrDefault(skillType.id, 0.0);
        double neededXp = SkillHUDRenderer.CLIENT_SKILL_NEEDED_XP.getOrDefault(skillType.id, 100.0 * Math.pow(1.07, skillLevel - 1));
        
        double progress;
        String xpText;
        if (skillLevel >= 100) {
            progress = 1.0;
            xpText = "MAX";
        } else {
            progress = Math.max(0.0, Math.min(1.0, xp / neededXp));
            xpText = String.format(java.util.Locale.US, "%.1f / %.1f", xp, neededXp);
        }

        int progressWidth = (int) (progress * (barWidth - 2));
        if (progressWidth > 0) {
            int barAlpha = 0xBF000000; // ~75% opacity
            int barColor = (skillType.color & 0x00FFFFFF) | barAlpha;
            graphics.fill(barX + 1, barY + 1, barX + 1 + progressWidth, barY + 4, barColor);
        }

        // Draw centered XP text above the bar
        graphics.drawCenteredString(font, xpText, centerX, barY - 10, 0xFFE0E0E0);

        // Draw centered Skill Name and Level above the XP text
        Component skillNameWithLevel = skillType.getDisplayName().copy().append(": " + skillLevel);
        graphics.drawCenteredString(font, skillNameWithLevel, centerX, barY - 20, skillType.color | 0xFF000000);

        // Tooltip checking for any star in any constellation
        for (int i = 0; i < skills.length; i++) {
            SkillType skill = skills[i];
            float baseX = i * 300.0f;
            float xDiff = baseX + currentPanX;
            while (xDiff < -halfWidth) xDiff += totalWidth;
            while (xDiff > halfWidth) xDiff -= totalWidth;
            float skillCenterX = centerX + xDiff;
            
            if (skillCenterX < -150 || skillCenterX > width + 150) {
                continue;
            }
            
            int activeLevel = SkillHUDRenderer.CLIENT_SKILL_LEVELS.getOrDefault(skill.id, 1);
            Constellation constellation = CONSTELLATIONS.get(skill.id);
            if (constellation != null) {
                boolean tooltipFound = false;
                for (StarNode star : constellation.stars) {
                    float starX = skillCenterX + star.x;
                    float starY = centerY + star.y;
                    
                    double distSq = (mouseX - starX) * (mouseX - starX) + (mouseY - starY) * (mouseY - starY);
                    if (distSq <= 64) {
                        graphics.renderComponentTooltip(font, getStarTooltip(star, activeLevel), mouseX, mouseY);
                        tooltipFound = true;
                        break;
                    }
                }
                if (tooltipFound) break;
            }
        }
    }

    private void drawGlyphLine(GuiGraphics graphics, float x1, float y1, float x2, float y2, int baseColor, boolean active) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance < 5) return;

        // One glyph every 10 pixels
        float stepSize = 10.0f;
        int numGlyphs = (int) (distance / stepSize);
        if (numGlyphs <= 0) numGlyphs = 1;

        // Flow phase: moves along the line
        float timePhase = (float) (System.currentTimeMillis() % 1500) / 1500.0f;

        // Standard ASCII characters guaranteed to be visible in default Minecraft font sheet
        char[] glyphsList = {'*', '+', 'o', 'x', '~', '=', '-', '.'};

        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();

        graphics.pose().pushPose();
        graphics.pose().translate(0.0f, 0.0f, 90.0f);

        for (int j = 0; j <= numGlyphs; j++) {
            // Calculate progress t along the line, shifted by the flow phase
            float t = ((float) j + timePhase) / (float) (numGlyphs + 1);
            if (t > 1.0f) t -= 1.0f; // Wrap around

            float px = x1 + t * dx;
            float py = y1 + t * dy;

            // Shimmer: alpha transitions dynamically
            float shimmer = (float) Math.sin(t * Math.PI * 2.0f - (System.currentTimeMillis() * 0.006f));
            shimmer = 0.4f + 0.6f * ((shimmer + 1.0f) / 2.0f); // Keep alpha between 0.4 and 1.0

            // Tremble: small high-frequency offset
            float offsetX = (float) Math.sin(System.currentTimeMillis() * 0.04f + j * 17) * 0.7f;
            float offsetY = (float) Math.cos(System.currentTimeMillis() * 0.04f + j * 31) * 0.7f;

            // Stable glyph selection for this position
            int glyphIndex = Math.abs((j + (int)(x1 + y1)) % glyphsList.length);
            char rune = glyphsList[glyphIndex];

            // Calculate final color with alpha
            int alpha = (int) (shimmer * 255.0f);
            int finalColor;
            if (active) {
                // Shimmer between yellow/golden and theme color
                int r = (baseColor >> 16) & 0xFF;
                int g = (baseColor >> 8) & 0xFF;
                int b = baseColor & 0xFF;
                
                // Pulsing blend factor
                float blend = (float) Math.sin(System.currentTimeMillis() * 0.003f + t * 5.0f);
                blend = (blend + 1.0f) / 2.0f;
                
                // Blend with gold/yellow (255, 220, 50)
                r = (int) (r * (1.0f - blend) + 255.0f * blend);
                g = (int) (g * (1.0f - blend) + 220.0f * blend);
                b = (int) (b * (1.0f - blend) + 50.0f * blend);
                
                finalColor = (alpha << 24) | (r << 16) | (g << 8) | b;
            } else {
                // Inactive line: dark gray with shimmer
                finalColor = (alpha << 24) | 0x444444;
            }

            graphics.drawCenteredString(font, String.valueOf(rune), (int) (px + offsetX), (int) (py + offsetY - 4), finalColor);
        }

        graphics.pose().popPose();
    }

    private List<Component> getStarTooltip(StarNode star, int currentLevel) {
        List<Component> tooltip = new ArrayList<>();
        
        String nameKey = "star.enchant_by_doing." + star.id + ".name";
        Component starTitle = net.minecraft.locale.Language.getInstance().has(nameKey) ?
                Component.translatable(nameKey) :
                Component.literal(star.name != null && !star.name.isEmpty() ? star.name : star.id);
        tooltip.add(starTitle.copy().withStyle(net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD));
        
        boolean unlocked = currentLevel >= star.unlockLevel;
        if (unlocked) {
            tooltip.add(Component.translatable("skill.enchant_by_doing.unlocked", star.unlockLevel).withStyle(net.minecraft.ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("skill.enchant_by_doing.locked", star.unlockLevel).withStyle(net.minecraft.ChatFormatting.RED));
        }
        
        String descKey = "star.enchant_by_doing." + star.id + ".desc";
        if (net.minecraft.locale.Language.getInstance().has(descKey)) {
            String translatedDesc = net.minecraft.locale.Language.getInstance().getOrDefault(descKey);
            String[] lines = translatedDesc.split("\n");
            for (String line : lines) {
                tooltip.add(Component.literal(line).withStyle(net.minecraft.ChatFormatting.YELLOW));
            }
        } else if (star.description != null && !star.description.isEmpty()) {
            String[] lines = star.description.split("\n");
            for (String line : lines) {
                tooltip.add(Component.literal(line).withStyle(net.minecraft.ChatFormatting.YELLOW));
            }
        }
        
        return tooltip;
    }

    private void updateParticles() {
        particles.removeIf(p -> p.life <= 0);
        for (Particle p : particles) {
            p.x += p.vx;
            p.y += p.vy;
            p.life--;
            p.alpha = Math.max(0.0f, (float) p.life / 30.0f);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (aiefu.ebd.client.EBDClient.skillsMenuKey.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        if (keyCode == 262) { // Right Arrow
            selectedSkillIndex = (selectedSkillIndex + 1) % SkillType.values().length;
            targetPanX -= 300.0f;
            bottomBarYOffset = 30.0f;
            playSkillSwitchEffects();
            return true;
        } else if (keyCode == 263) { // Left Arrow
            selectedSkillIndex = (selectedSkillIndex - 1 + SkillType.values().length) % SkillType.values().length;
            targetPanX += 300.0f;
            bottomBarYOffset = 30.0f;
            playSkillSwitchEffects();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left Click
            if (mouseX < width * 0.15f) {
                // Switch left
                selectedSkillIndex = (selectedSkillIndex - 1 + SkillType.values().length) % SkillType.values().length;
                targetPanX += 300.0f;
                bottomBarYOffset = 30.0f;
                playSkillSwitchEffects();
                return true;
            } else if (mouseX > width * 0.85f) {
                // Switch right
                selectedSkillIndex = (selectedSkillIndex + 1) % SkillType.values().length;
                targetPanX -= 300.0f;
                bottomBarYOffset = 30.0f;
                playSkillSwitchEffects();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void playSkillSwitchEffects() {
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F));
        
        int centerX = width / 2;
        int centerY = height / 2;
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < 20; i++) {
            float angle = rand.nextFloat() * 2.0F * (float) Math.PI;
            float speed = 1.0F + rand.nextFloat() * 2.0F;
            float vx = (float) Math.cos(angle) * speed;
            float vy = (float) Math.sin(angle) * speed;
            particles.add(new Particle(centerX, centerY, vx, vy, 30));
        }
    }

    @Override
    public void onClose() {
        this.transitioning = true;
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new SkillsExitTransitionScreen(originalPitch, originalVolumes, musicInstance));
    }

    @Override
    public void removed() {
        super.removed();
        if (!transitioning) {
            Minecraft mc = Minecraft.getInstance();
            if (musicInstance != null) {
                mc.getSoundManager().stop(musicInstance);
                musicInstance = null;
            }

            originalVolumes.forEach((source, vol) -> mc.options.getSoundSourceOptionInstance(source).set((double) vol));
            originalVolumes.clear();

            if (mc.player != null) {
                mc.player.setXRot(originalPitch);
            }

            try {
                mc.gameRenderer.shutdownEffect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class Star {
        float x, y, size, alpha, speed;

        Star(float x, float y, float size, float alpha) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.alpha = alpha;
            this.speed = size;
        }
    }

    private static class Particle {
        float x, y, vx, vy, alpha;
        int life;

        Particle(float x, float y, float vx, float vy, int maxLife) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = maxLife;
            this.alpha = 1.0F;
        }
    }

    public static class CustomMusicSoundInstance extends AbstractTickableSoundInstance {
        public CustomMusicSoundInstance(net.minecraft.sounds.SoundEvent event) {
            super(event, SoundSource.MUSIC, RandomSource.create());
            this.looping = true;
            this.delay = 0;
            this.volume = 0.15F;
            this.pitch = 1.0F;
            this.relative = true;
        }

        @Override
        public void tick() {
        }
    }

    private static ItemStack createEnchantedItem(net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item);
        net.minecraft.client.multiplayer.ClientLevel level = net.minecraft.client.Minecraft.getInstance().level;
        if (level != null) {
            java.util.Optional<net.minecraft.core.Holder.Reference<net.minecraft.world.item.enchantment.Enchantment>> unbreaking = 
                level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .holders()
                .filter(h -> h.key().location().getPath().equals("unbreaking"))
                .findFirst();
            if (unbreaking.isPresent()) {
                net.minecraft.world.item.enchantment.ItemEnchantments.Mutable builder = 
                    new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                builder.set(unbreaking.get(), 1);
                stack.set(net.minecraft.core.component.DataComponents.ENCHANTMENTS, builder.toImmutable());
            }
        }
        return stack;
    }

    private ItemStack getSkillItem(SkillType skillType) {
        return skillItemCache.computeIfAbsent(skillType, type -> {
            net.minecraft.world.item.Item item;
            switch (type) {
                case MINER:
                    item = net.minecraft.world.item.Items.DIAMOND_PICKAXE;
                    break;
                case WARRIOR:
                    item = net.minecraft.world.item.Items.DIAMOND_SWORD;
                    break;
                case HUNTER:
                    item = net.minecraft.world.item.Items.BOW;
                    break;
                case LUMBERJACK:
                    item = net.minecraft.world.item.Items.DIAMOND_AXE;
                    break;
                case FARMER:
                    item = net.minecraft.world.item.Items.DIAMOND_HOE;
                    break;
                case ENCHANTER:
                    item = net.minecraft.world.item.Items.ENCHANTED_BOOK;
                    break;
                default:
                    item = net.minecraft.world.item.Items.NETHER_STAR;
                    break;
            }
            return createEnchantedItem(item);
        });
    }
}

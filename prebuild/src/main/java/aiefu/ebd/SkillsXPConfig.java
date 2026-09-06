package aiefu.ebd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class SkillsXPConfig {
    public static final SkillsXPConfig INSTANCE = new SkillsXPConfig();

    private final Map<String, Map<Integer, Double>> skillXpRequirements = new HashMap<>();

    private SkillsXPConfig() {
    }

    public void load() {
        Path configPath = Paths.get("./config/ebd/skills_xp.yml");
        Path oldConfigPath = Paths.get("./config/ebd/skills_xp.yml");
        if (!Files.exists(configPath)) {
            try {
                Files.createDirectories(configPath.getParent());
                if (Files.exists(oldConfigPath)) {
                    Files.copy(oldConfigPath, configPath);
                } else {
                    String defaults = getDefaultsContent();
                    Files.writeString(configPath, defaults);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        skillXpRequirements.clear();
        try {
            String content = Files.readString(configPath);
            Map<String, String> map = YAMLConfig.parse(content);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey().trim();
                int dotIdx = key.indexOf('.');
                if (dotIdx != -1) {
                    String skillId = key.substring(0, dotIdx).trim().toLowerCase();
                    try {
                        int level = Integer.parseInt(key.substring(dotIdx + 1).trim());
                        double xp = Double.parseDouble(entry.getValue());
                        skillXpRequirements.computeIfAbsent(skillId, k -> new HashMap<>()).put(level, xp);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Double getXPNeeded(String skillId, int level) {
        Map<Integer, Double> levels = skillXpRequirements.get(skillId.toLowerCase());
        if (levels != null) {
            return levels.get(level);
        }
        return null;
    }

    private String getDefaultsContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Experience required to level UP from the given level to the next level.\n");
        sb.append("# Format: skill_name.level: xp_amount\n");
        sb.append("# If a level is not listed, it falls back to the formula in config.yml.\n\n");

        for (SkillType type : SkillType.values()) {
            sb.append("# ").append(type.displayName).append(" (").append(type.id).append(")\n");
            double divisor = (type == SkillType.ENCHANTER) ? 5.0 : 1.0;
            for (int lvl = 1; lvl <= 100; lvl++) {
                double xpNeeded = (100.0 * Math.pow(1.07, lvl - 1)) / divisor;
                sb.append(type.id).append(".").append(lvl).append(": ").append(String.format(java.util.Locale.US, "%.1f", xpNeeded)).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}

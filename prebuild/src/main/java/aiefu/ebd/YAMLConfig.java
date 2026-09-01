package aiefu.ebd;

import java.util.HashMap;
import java.util.Map;

public class YAMLConfig {
    public static Map<String, String> parse(String content) {
        Map<String, String> map = new HashMap<>();
        String[] lines = content.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            int commentIdx = trimmed.indexOf('#');
            if (commentIdx != -1) {
                trimmed = trimmed.substring(0, commentIdx).trim();
            }
            if (trimmed.isEmpty()) continue;
            int firstCol = trimmed.indexOf(':');
            if (firstCol != -1) {
                String firstKey = trimmed.substring(0, firstCol).trim();
                boolean isStandardKey = firstKey.equals("xp_growth_type") || firstKey.equals("xp_base") 
                    || firstKey.equals("xp_multiplier") || firstKey.equals("miner_base_speed_modifier")
                    || firstKey.equals("lumberjack_base_speed_modifier") || firstKey.equals("anti_abuse_cooldown_seconds")
                    || firstKey.equals("anti_abuse_xp_reduction") || firstKey.equals("xp_ore_mined")
                    || firstKey.equals("xp_stone_mined") || firstKey.equals("xp_wood_chopped")
                    || firstKey.equals("xp_mob_killed_hostile") || firstKey.equals("xp_mob_killed_peaceful")
                    || firstKey.equals("xp_animal_bred") || firstKey.equals("xp_crop_harvested")
                    || firstKey.equals("xp_item_enchanted") || firstKey.equals("xp_item_repaired")
                    || firstKey.equals("xp_item_disenchanted") || firstKey.equals("xp_dusty_book_read")
                    || firstKey.equals("enchanter_discount_per_level") || firstKey.equals("warrior_weapons")
                    || firstKey.equals("lumberjack_weapons") || firstKey.equals("block_xp_hardness_multiplier")
                    || firstKey.equals("mob_xp_health_multiplier");
                
                int idx = isStandardKey ? firstCol : trimmed.lastIndexOf(':');
                String key = trimmed.substring(0, idx).trim();
                String val = trimmed.substring(idx + 1).trim();
                if (val.startsWith("\"") && val.endsWith("\"")) {
                    val = val.substring(1, val.length() - 1);
                } else if (val.startsWith("'") && val.endsWith("'")) {
                    val = val.substring(1, val.length() - 1);
                }
                map.put(key, val);
            }
        }
        return map;
    }
}

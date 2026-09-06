package aiefu.ebd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class LBDConfig {
    public static final LBDConfig INSTANCE = new LBDConfig();

    public String xpGrowthType = "exponential";
    public double xpBase = 100.0;
    public double xpMultiplier = 1.07;

    public float minerBaseSpeedModifier = -0.25f;
    public float lumberjackBaseSpeedModifier = -0.25f;

    public int antiAbuseCooldownSeconds = 5;
    public float antiAbuseXpReduction = 0.75f;

    public double xpOreMined = 10.0;
    public double xpStoneMined = 1.0;
    public double xpWoodChopped = 2.0;
    public double xpMobKilledHostile = 20.0;
    public double xpMobKilledPeaceful = 10.0;
    public double xpAnimalBred = 25.0;
    public double xpCropHarvested = 5.0;
    public double xpItemEnchanted = 30.0;
    public double xpItemRepaired = 15.0;
    public double xpItemDisenchanted = 15.0;
    public double xpDustyBookRead = 50.0;

    public double dustyBookArchaeologyChance = 0.20;
    public double dustyBookFishingChance = 0.10;
    public double dustyBookFishingTreasureChance = 0.35;

    public boolean enableCraftingWorkstations = true;
    public int workstationDetectionRadius = 5;

    public float enchanterDiscountPerLevel = 1.0f;

    public Set<String> warriorWeapons = new HashSet<>();
    public Set<String> lumberjackWeapons = new HashSet<>();

    public Map<String, Double> customBlockXp = new HashMap<>();
    public Map<String, Double> customMobXp = new HashMap<>();
    public double blockXpHardnessMultiplier = 1.0;
    public double mobXpHealthMultiplier = 1.0;

    private LBDConfig() {
        load();
    }

    public void load() {
        Path configPath = Paths.get("./config/ebd/config.yml");
        Path oldConfigPath = Paths.get("./config/ebd/config.yml");
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

        try {
            String content = Files.readString(configPath);
            Map<String, String> map = YAMLConfig.parse(content);

            xpGrowthType = map.getOrDefault("xp_growth_type", "exponential");
            xpBase = Double.parseDouble(map.getOrDefault("xp_base", "100.0"));
            xpMultiplier = Double.parseDouble(map.getOrDefault("xp_multiplier", "1.5"));

            minerBaseSpeedModifier = Float.parseFloat(map.getOrDefault("miner_base_speed_modifier", "-0.25"));
            lumberjackBaseSpeedModifier = Float.parseFloat(map.getOrDefault("lumberjack_base_speed_modifier", "-0.25"));

            antiAbuseCooldownSeconds = Integer.parseInt(map.getOrDefault("anti_abuse_cooldown_seconds", "5"));
            antiAbuseXpReduction = Float.parseFloat(map.getOrDefault("anti_abuse_xp_reduction", "0.75"));

            xpOreMined = Double.parseDouble(map.getOrDefault("xp_ore_mined", "10.0"));
            xpStoneMined = Double.parseDouble(map.getOrDefault("xp_stone_mined", "1.0"));
            xpWoodChopped = Double.parseDouble(map.getOrDefault("xp_wood_chopped", "2.0"));
            xpMobKilledHostile = Double.parseDouble(map.getOrDefault("xp_mob_killed_hostile", "20.0"));
            xpMobKilledPeaceful = Double.parseDouble(map.getOrDefault("xp_mob_killed_peaceful", "10.0"));
            xpAnimalBred = Double.parseDouble(map.getOrDefault("xp_animal_bred", "25.0"));
            xpCropHarvested = Double.parseDouble(map.getOrDefault("xp_crop_harvested", "5.0"));
            xpItemEnchanted = Double.parseDouble(map.getOrDefault("xp_item_enchanted", "30.0"));
            xpItemRepaired = Double.parseDouble(map.getOrDefault("xp_item_repaired", "15.0"));
            xpItemDisenchanted = Double.parseDouble(map.getOrDefault("xp_item_disenchanted", "15.0"));
            xpDustyBookRead = Double.parseDouble(map.getOrDefault("xp_dusty_book_read", "50.0"));

            dustyBookArchaeologyChance = Double.parseDouble(map.getOrDefault("dusty_book_archaeology_chance", "0.20"));
            dustyBookFishingChance = Double.parseDouble(map.getOrDefault("dusty_book_fishing_chance", "0.10"));
            dustyBookFishingTreasureChance = Double.parseDouble(map.getOrDefault("dusty_book_fishing_treasure_chance", "0.35"));

            enableCraftingWorkstations = Boolean.parseBoolean(map.getOrDefault("enable_crafting_workstations", "true"));
            workstationDetectionRadius = Integer.parseInt(map.getOrDefault("workstation_detection_radius", "5"));

            enchanterDiscountPerLevel = Float.parseFloat(map.getOrDefault("enchanter_discount_per_level", "1.0"));

            warriorWeapons.clear();
            String warriorStr = map.getOrDefault("warrior_weapons", "minecraft:wooden_sword,minecraft:stone_sword,minecraft:iron_sword,minecraft:golden_sword,minecraft:diamond_sword,minecraft:netherite_sword");
            for (String s : warriorStr.split(",")) {
                warriorWeapons.add(s.trim());
            }

            lumberjackWeapons.clear();
            String lumberjackStr = map.getOrDefault("lumberjack_weapons", "minecraft:wooden_axe,minecraft:stone_axe,minecraft:iron_axe,minecraft:golden_axe,minecraft:diamond_axe,minecraft:netherite_axe");
            for (String s : lumberjackStr.split(",")) {
                lumberjackWeapons.add(s.trim());
            }

            customBlockXp.clear();
            customMobXp.clear();
            blockXpHardnessMultiplier = Double.parseDouble(map.getOrDefault("block_xp_hardness_multiplier", "1.0"));
            mobXpHealthMultiplier = Double.parseDouble(map.getOrDefault("mob_xp_health_multiplier", "1.0"));

            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("block:")) {
                    String blockId = key.substring(6).trim();
                    try {
                        customBlockXp.put(blockId, Double.parseDouble(entry.getValue()));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                } else if (key.startsWith("mob:")) {
                    String mobId = key.substring(4).trim();
                    try {
                        customMobXp.put(mobId, Double.parseDouble(entry.getValue()));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

            initDefaultBlockXp();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initDefaultBlockXp() {
        customBlockXp.putIfAbsent("minecraft:coal_ore", 5.0);
        customBlockXp.putIfAbsent("minecraft:deepslate_coal_ore", 7.5);
        customBlockXp.putIfAbsent("minecraft:copper_ore", 5.0);
        customBlockXp.putIfAbsent("minecraft:deepslate_copper_ore", 7.5);
        customBlockXp.putIfAbsent("minecraft:iron_ore", 10.0);
        customBlockXp.putIfAbsent("minecraft:deepslate_iron_ore", 15.0);
        customBlockXp.putIfAbsent("minecraft:gold_ore", 15.0);
        customBlockXp.putIfAbsent("minecraft:deepslate_gold_ore", 20.0);
        customBlockXp.putIfAbsent("minecraft:redstone_ore", 10.0);
        customBlockXp.putIfAbsent("minecraft:deepslate_redstone_ore", 15.0);
        customBlockXp.putIfAbsent("minecraft:lapis_ore", 10.0);
        customBlockXp.putIfAbsent("minecraft:deepslate_lapis_ore", 15.0);
        customBlockXp.putIfAbsent("minecraft:nether_quartz_ore", 10.0);
        customBlockXp.putIfAbsent("minecraft:nether_gold_ore", 10.0);

        // Diamond ore: 4x base ore (10.0 * 4 = 40.0)
        customBlockXp.putIfAbsent("minecraft:diamond_ore", 40.0);
        customBlockXp.putIfAbsent("minecraft:deepslate_diamond_ore", 60.0);

        // Emerald ore: 4x diamond ore (40.0 * 4 = 160.0)
        customBlockXp.putIfAbsent("minecraft:emerald_ore", 160.0);
        customBlockXp.putIfAbsent("minecraft:deepslate_emerald_ore", 240.0);

        // Ancient Debris: 2x emerald ore (160.0 * 2 = 320.0)
        customBlockXp.putIfAbsent("minecraft:ancient_debris", 320.0);

        customBlockXp.putIfAbsent("minecraft:stone", 1.0);
        customBlockXp.putIfAbsent("minecraft:cobblestone", 1.0);
        customBlockXp.putIfAbsent("minecraft:deepslate", 1.2);
        customBlockXp.putIfAbsent("minecraft:cobbled_deepslate", 1.2);
    }

    public double getXPNeededForLevel(String skillId, int level) {
        Double custom = SkillsXPConfig.INSTANCE.getXPNeeded(skillId, level);
        if (custom != null) {
            return custom;
        }
        if ("linear".equalsIgnoreCase(xpGrowthType)) {
            return xpBase + (level - 1) * xpMultiplier;
        } else {
            return xpBase * Math.pow(xpMultiplier, level - 1);
        }
    }

    public double getXPNeededForLevel(int level) {
        return getXPNeededForLevel("", level);
    }

    private String getDefaultsContent() {
        return "# Learning by Doing Progression System Configuration\n\n" +
               "# Experience Formula Settings\n" +
               "# xp_growth_type can be 'exponential' or 'linear'\n" +
               "xp_growth_type: exponential\n" +
               "xp_base: 100.0\n" +
               "xp_multiplier: 1.07\n\n" +
               "# Profession Base Speed Modifiers at Level 1 (-0.25 means -25% speed)\n" +
               "miner_base_speed_modifier: -0.25\n" +
               "lumberjack_base_speed_modifier: -0.25\n\n" +
               "# Anti-Abuse (Anti-Cheat) Settings\n" +
               "anti_abuse_cooldown_seconds: 5\n" +
               "anti_abuse_xp_reduction: 0.75\n\n" +
               "# Default Experience Yields\n" +
               "xp_ore_mined: 10.0\n" +
               "xp_stone_mined: 1.0\n" +
               "xp_wood_chopped: 2.0\n" +
               "xp_mob_killed_hostile: 20.0\n" +
               "xp_mob_killed_peaceful: 10.0\n" +
               "xp_animal_bred: 25.0\n" +
               "xp_crop_harvested: 5.0\n" +
               "xp_item_enchanted: 30.0\n" +
               "xp_item_repaired: 15.0\n" +
               "xp_item_disenchanted: 15.0\n" +
               "xp_dusty_book_read: 50.0\n\n" +
               "# Dusty Book Discovery Chances in Archaeology and Fishing (0.0 to 1.0)\n" +
               "dusty_book_archaeology_chance: 0.20\n" +
               "dusty_book_fishing_chance: 0.10\n" +
               "dusty_book_fishing_treasure_chance: 0.35\n\n" +
               "# Proximity-based Crafting Workstations (requires Anvil, Enchanting Table, Fletching Table, etc. nearby)\n" +
               "enable_crafting_workstations: true\n" +
               "workstation_detection_radius: 5\n\n" +
               "# Default Multipliers for non-configured blocks/mobs\n" +
               "block_xp_hardness_multiplier: 1.0\n" +
               "mob_xp_health_multiplier: 1.0\n\n" +
               "# Enchanter Discount per Level (in percentage, e.g. 1.0 means 1% per level up to 50% max)\n" +
               "enchanter_discount_per_level: 1.0\n\n" +
               "# Eligible Weapons lists (comma separated)\n" +
               "warrior_weapons: minecraft:wooden_sword,minecraft:stone_sword,minecraft:iron_sword,minecraft:golden_sword,minecraft:diamond_sword,minecraft:netherite_sword\n" +
               "lumberjack_weapons: minecraft:wooden_axe,minecraft:stone_axe,minecraft:iron_axe,minecraft:golden_axe,minecraft:diamond_axe,minecraft:netherite_axe\n\n" +
               "# Custom Experience for specific Blocks (format: block:namespace:block_name: xp_value)\n" +
               "block:minecraft:coal_ore: 5.0\n" +
               "block:minecraft:deepslate_coal_ore: 7.5\n" +
               "block:minecraft:copper_ore: 5.0\n" +
               "block:minecraft:deepslate_copper_ore: 7.5\n" +
               "block:minecraft:iron_ore: 10.0\n" +
               "block:minecraft:deepslate_iron_ore: 15.0\n" +
               "block:minecraft:gold_ore: 15.0\n" +
               "block:minecraft:deepslate_gold_ore: 20.0\n" +
               "block:minecraft:redstone_ore: 10.0\n" +
               "block:minecraft:deepslate_redstone_ore: 15.0\n" +
               "block:minecraft:lapis_ore: 10.0\n" +
               "block:minecraft:deepslate_lapis_ore: 15.0\n" +
               "block:minecraft:nether_quartz_ore: 10.0\n" +
               "block:minecraft:nether_gold_ore: 10.0\n" +
               "block:minecraft:diamond_ore: 40.0\n" +
               "block:minecraft:deepslate_diamond_ore: 60.0\n" +
               "block:minecraft:emerald_ore: 160.0\n" +
               "block:minecraft:deepslate_emerald_ore: 240.0\n" +
               "block:minecraft:ancient_debris: 320.0\n" +
               "block:minecraft:stone: 1.0\n" +
               "block:minecraft:cobblestone: 1.0\n" +
               "block:minecraft:deepslate: 1.2\n" +
               "block:minecraft:cobbled_deepslate: 1.2\n\n" +
               "# Custom Experience for specific Mobs (format: mob:namespace:mob_name: xp_value)\n" +
               "mob:minecraft:zombie: 20.0\n" +
               "mob:minecraft:skeleton: 20.0\n" +
               "mob:minecraft:creeper: 20.0\n" +
               "mob:minecraft:spider: 15.0\n" +
               "mob:minecraft:enderman: 40.0\n" +
               "mob:minecraft:cow: 10.0\n" +
               "mob:minecraft:sheep: 10.0\n" +
               "mob:minecraft:pig: 10.0\n";
    }
}

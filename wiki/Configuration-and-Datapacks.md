# Configuration & Datapacks Guide

**Enchant by Doing (EbD)** is built from the ground up to be fully data-driven, allowing modpack authors and server administrators to customize every parameter, XP curve, recipe, and constellation layout.

---

## 📁 Configuration Files Breakdown

All server and gameplay configuration files are stored under `.minecraft/config/ebd/`:

```
.minecraft/
└── config/
    └── ebd/
        ├── config.yml           <- Global gameplay, multipliers & weapon lists
        ├── config.json          <- Enchanting table features & toggleable enchantments
        ├── skills_xp.yml        <- Exact per-level XP tables (Levels 1–100)
        ├── ebd_skills_layout.json <- Constellation star coordinates & unlock thresholds
        ├── default-recipe.json  <- Default fallback recipe ingredient tiers
        └── default-xp-map.json  <- Default level mapping
```

---

## ⚙️ 1. Global Settings (`config/ebd/config.yml`)

```yaml
# ==========================================
# Enchant by Doing (EbD) - Main Configuration
# ==========================================

# XP Formula: "exponential" or "linear"
xp_growth_type: "exponential"
xp_base: 100.0
xp_multiplier: 1.07

# Base speed penalties before skill progression (0.0 = vanilla, -0.25 = 25% slower)
miner_base_speed_modifier: -0.25
lumberjack_base_speed_modifier: -0.25

# Anti-abuse protection when placing/breaking own blocks
anti_abuse_cooldown_seconds: 5
anti_abuse_xp_reduction: 0.75

# Base Action XP values
xp_ore_mined: 10.0
xp_stone_mined: 1.0
xp_wood_chopped: 2.0
xp_mob_killed_hostile: 20.0
xp_mob_killed_peaceful: 10.0
xp_animal_bred: 25.0
xp_crop_harvested: 5.0
xp_item_enchanted: 30.0
xp_item_repaired: 15.0
xp_item_disenchanted: 15.0
xp_dusty_book_read: 50.0

# Enchanter material discount scaling per level (percentage)
enchanter_discount_per_level: 1.0

# Weapon tags and registry IDs for Warrior and Lumberjack XP
warrior_weapons: "minecraft:wooden_sword,minecraft:stone_sword,minecraft:iron_sword,minecraft:golden_sword,minecraft:diamond_sword,minecraft:netherite_sword"
lumberjack_weapons: "minecraft:wooden_axe,minecraft:stone_axe,minecraft:iron_axe,minecraft:golden_axe,minecraft:diamond_axe,minecraft:netherite_axe"
```

---

## 🔮 2. Enchanting System Settings (`config/ebd/config.json`)

```json
{
  "enableEnchantability": true,
  "enableDefaultRecipe": true,
  "disableDiscoverySystem": false,
  "enableEnchantmentsLeveling": false,
  "hideEnchantmentsWithoutRecipe": false,
  "disableAnvilEnchanting": false,
  "disableBookCombining": false,
  "enableQuicksand": true,
  "enableDisarm": true,
  "enableSting": true,
  "enableExecutioner": true,
  "enableLeviathan": true,
  "enableDragonsBreath": true,
  "enableMagazine": true,
  "enableAutoReload": true,
  "enableSlipway": true,
  "enableSeafarer": true,
  "enableFireproofBoat": true,
  "enableAutodrive": true,
  "enableAeroflot": true,
  "enablePocketBoat": true,
  "enableSubmarine": true,
  "enableCapacity": true
}
```

---

## 📈 3. Per-Level Flat XP Tables (`config/ebd/skills_xp.yml`)

You can define exact flat XP points required for every single level:

```yaml
# Custom XP per level table (1 to 100)
miner:
  1: 100
  2: 150
  3: 220
  # ... up to 100
warrior:
  1: 120
  2: 180
  # ...
```

---

## 📦 4. Custom Datapack Recipes

You can add custom crafting recipes for any vanilla or modded enchantment via standard Datapacks.

### 📁 Recipe Location:
`data/<namespace>/ench-recipes/<enchantment_name>.json`

### 📝 Example: Custom Recipe for `minecraft:mending`
Create `data/my_pack/ench-recipes/mending.json`:

```json
{
  "enchantment": "minecraft:mending",
  "levels": {
    "1": {
      "experience": 30,
      "items": [
        {
          "item": "minecraft:netherite_ingot",
          "count": 1
        },
        {
          "item": "minecraft:emerald",
          "count": 16
        },
        {
          "item": "minecraft:amethyst_shard",
          "count": 8
        },
        {
          "item": "minecraft:experience_bottle",
          "count": 4
        }
      ]
    }
  }
}
```

### 🏷️ Modifying Enchantable Item Tags:
EbD reads standard NeoForge & Vanilla item tags to determine which items can accept enchantments:
* `data/enchant_by_doing/tags/item/enchantable/weapon.json`
* `data/enchant_by_doing/tags/item/enchantable/armor.json`
* `data/enchant_by_doing/tags/item/enchantable/bow.json`
* `data/enchant_by_doing/tags/item/enchantable/boat.json`
# Proximity-Based Crafting Workstations

**Enchant by Doing (EbD)** transforms the standard Crafting Table from a generic "one-block-fits-all factory" into a true **artisan craftsman workstation**. To craft specialized, advanced, or heavy equipment, the Crafting Table requires corresponding functional workstation blocks placed nearby in your workshop.

---

## 🛠️ Workstations & Required Crafts

By default, the Crafting Table scans a **5-block radius** around itself for the following workstations:

| Workstation | Represented Blocks | Items & Equipment Requiring Station | Deadlock Exceptions |
|---|---|---|---|
| 🔨 **Anvil** | `minecraft:anvil`<br>`chipped_anvil`<br>`damaged_anvil` | **Metal Weapons & Tools:** Iron & Netherite Swords, Pickaxes, Axes, Shovels, Hoes.<br>**Hardware & Metal Items:** Shears, Flint and Steel, Buckets, Compasses, Clocks, Minecarts, Iron Bars, Iron Doors, Iron Trapdoors, Chains, Lanterns, Soul Lanterns, Hoppers, Rails, Powered Rails, Cauldron, Lightning Rods, Heavy Pressure Plates. | **Crafting the Anvil itself** (3 Iron Blocks + 4 Ingots) does NOT require an anvil. |
| 🔮 **Enchanting Table** | `minecraft:enchanting_table` | **Diamond Equipment:** Diamond Swords, Pickaxes, Axes, Shovels, Hoes, Armor.<br>**Magical & Mystic Artifacts:** Eye of Ender, End Crystal, Beacon, Conduit, Respawn Anchor, Recovery Compass. | **Crafting the Enchanting Table itself** does NOT require an enchanting table. |
| 🏹 **Fletching Table** | `minecraft:fletching_table` | **Ranged Weapons & Projectiles:** Bows, Crossbows, Regular Arrows, Spectral Arrows. | **Crafting the Fletching Table itself** does NOT require a fletching table. |
| 🛡️ **Armorer / Smithing Station** | `minecraft:smithing_table`<br>`minecraft:blast_furnace` | **Heavy Armor & Defenses:** Iron Armor, Golden Armor, Chainmail Armor, Shields, Iron/Golden/Diamond Horse Armor, Wolf Armor. | **Crafting the Smithing Table or Blast Furnace** does NOT require an armorer station. |

---

## 🖥️ Crafting Table GUI & Visual Indicators

When you open a **Crafting Table**, a dedicated **Workstation Status Bar** appears on the right edge of the screen:

```
    ┌───────────────────────┐  ┌──────┐
    │   CRAFTING TABLE      │  │ [🔨] │ ◄── Anvil: Present (Green Outline)
    │                       │  │ [🔮] │ ◄── Enchanting Table: Missing (Gray)
    │  [3x3 Grid]  ──> [ ? ]│  │ [🏹] │ ◄── Fletching: Missing & REQUIRED (Pulsing Red !)
    │                       │  │ [🛡️] │ ◄── Armorer: Present (Green Outline)
    └───────────────────────┘  └──────┘
```

1. **Active/Detected Indicator (🟢 Green Outline):**
   * Displays when the workstation is detected within the search radius.
   * Features a vibrant green border and corner checkmark badge.
2. **Inactive Indicator (⚪ Gray Outline):**
   * Displays when the workstation is absent, but the recipe in the grid does not require it.
3. **Missing & Required Alert (🔴 Pulsing Red Outline & Exclamation Badge):**
   * If you lay out a recipe (e.g. Iron Sword or Bow) without its required station nearby, the crafting result slot remains **empty/locked**.
   * The corresponding station icon **pulses with a smooth red glow** and renders an **`!`** alert badge to immediately inform you what is missing.
4. **Interactive Tooltips:**
   * Hovering over any station icon displays its name, current proximity status, and what categories of items it unlocks.

---

## ⚙️ Configuration & Customization

All workstation mechanics can be customized via `config/ebd/config.yml`:

```yaml
# Proximity-based Crafting Workstations settings
enable_crafting_workstations: true
workstation_detection_radius: 5
```

### 🧩 Adding Custom / Modded Items via Datapacks

All item requirements are data-driven using standard NeoForge/Minecraft Item Tags. Modpack creators can freely add or remove items using JSON datapacks located under `data/enchant_by_doing/tags/item/`:

* `#enchant_by_doing:requires_anvil`
* `#enchant_by_doing:requires_enchanting_table`
* `#enchant_by_doing:requires_fletching_table`
* `#enchant_by_doing:requires_armorer`
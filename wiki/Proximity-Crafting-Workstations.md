# Proximity-Based Crafting Workstations

**Enchant by Doing (EbD)** transforms crafting from a generic "one-block-fits-all" menu into a true **artisan workshop system**. To craft specialized, advanced, or heavy equipment, the player must be near the appropriate functional workstation blocks — whether crafting in a **3x3 Crafting Table** or directly in the **2x2 Player Inventory**!

---

## 🛠️ Workstations & Required Crafts

Workstations scan a **5-block radius** around the Crafting Table or the player (when using the 2x2 inventory grid):

| Workstation | Represented Blocks | Items & Equipment Requiring Station | Deadlock Exceptions |
|---|---|---|---|
| 🔨 **Anvil** | `minecraft:anvil`<br>`chipped_anvil`<br>`damaged_anvil` | **Metal Weapons & Tools:** Iron, Diamond, Netherite Swords, Pickaxes, Axes, Shovels, Hoes.<br>**All Heavy Armor Pieces:** Iron, Golden, Diamond Armor.<br>**Hardware & Metal Items:** Shears (2x2), Flint & Steel (2x2), Buckets, Compasses, Clocks, Minecarts, Iron Bars, Iron Doors, Iron Trapdoors, Chains, Lanterns, Soul Lanterns, Hoppers, Rails, Powered Rails, Cauldron, Lightning Rods, Heavy Pressure Plates, Shields, Crossbows. | **Crafting the Anvil itself** (3 Iron Blocks + 4 Ingots) does NOT require an anvil. |
| 🔮 **Enchanting Table** | `minecraft:enchanting_table` | **Diamond Equipment:** Diamond Swords, Pickaxes, Axes, Shovels, Hoes, Armor.<br>**Magical & Mystic Artifacts:** Eye of Ender (2x2), End Crystal, Beacon, Conduit, Respawn Anchor, Recovery Compass. | **Crafting the Enchanting Table itself** does NOT require an enchanting table. |
| 🏹 **Fletching Table** | `minecraft:fletching_table` | **Ranged Weapons & Projectiles:** Bows, Crossbows, Regular Arrows (2x2), Spectral Arrows. | **Crafting the Fletching Table itself** does NOT require a fletching table. |
| 🛡️ **Armorer / Smithing Station** | `minecraft:smithing_table`<br>`minecraft:blast_furnace` | **Heavy Armor & Defenses:** Iron Armor, Golden Armor, Diamond Armor, Chainmail Armor, Shields, Iron/Golden/Diamond Horse Armor, Wolf Armor. | **Crafting the Smithing Table or Blast Furnace** does NOT require an armorer station. |

---

## 🔗 Multi-Station Requirements ("AND" Conditions)

Advanced recipes require **multiple** workstations simultaneously:

* **🛡️ Iron Armor (Helmet, Chestplate, Leggings, Boots):**
  * Requires: **🔨 Anvil** AND **🛡️ Armorer / Smithing Station**
* **💎 Diamond Armor (Helmet, Chestplate, Leggings, Boots):**
  * Requires: **🔨 Anvil** AND **🛡️ Armorer / Smithing Station** AND **🔮 Enchanting Table**
* **⛏️ Diamond Tools & Weapons (Sword, Pickaxe, Axe, Shovel, Hoe):**
  * Requires: **🔨 Anvil** AND **🔮 Enchanting Table**
* **🏹 Crossbow:**
  * Requires: **🏹 Fletching Table** AND **🔨 Anvil**
* **🛡️ Shield:**
  * Requires: **🔨 Anvil** AND **🛡️ Armorer / Smithing Station**

> [!IMPORTANT]
> If an item requires multiple workstations, **all** of them must be present within 5 blocks. Any missing workstation will simultaneously pulse with a red alert animation!

---

## 🎒 2x2 Player Inventory Crafting Support

The proximity system also seamlessly governs the **2x2 player inventory grid**:
* Crafting **Shears** or **Flint and Steel** requires standing near an **Anvil**.
* Crafting **Arrows** requires standing near a **Fletching Table**.
* Crafting an **Eye of Ender** requires standing near an **Enchanting Table**.
* If the required station is not nearby, the craft is blocked, and the missing station's badge in your inventory window pulses red!

---

## 🖥️ Crafting GUI & Visual Indicators (3x3 Table & 2x2 Inventory)

Whenever you open a **Crafting Table** or your **Inventory (`E`)**, the **Workstation Status Bar** renders on the right side of the window:

```
    ┌───────────────────────┐  ┌──────┐
    │   CRAFTING WINDOW     │  │ [🔨] │ ◄── Anvil: Present (Green Outline)
    │                       │  │ [🔮] │ ◄── Enchanting Table: Missing (Gray)
    │  [Craft Grid] ──> [ ? ]│  │ [🏹] │ ◄── Fletching: Missing & REQUIRED (Pulsing Red !)
    │                       │  │ [🛡️] │ ◄── Armorer: Present (Green Outline)
    └───────────────────────┘  └──────┘
```

1. **Active/Detected Indicator (🟢 Green Outline):**
   * Displays when the workstation is detected within 5 blocks.
   * Features a vibrant green border and corner checkmark badge.
2. **Inactive Indicator (⚪ Gray Outline):**
   * Displays when the workstation is absent, but the recipe in the grid does not require it.
3. **Missing & Required Alert (🔴 Pulsing Red Outline & Exclamation Badge):**
   * If a recipe requires one or more stations that are missing, the crafting result slot remains **empty/locked**.
   * Every missing required station **pulses with a red glow** and renders an **`!`** alert badge.
4. **Interactive Tooltips:**
   * Hovering over any station badge displays its name, status, and item categories.

---

## ⚙️ Configuration & Datapacks

All workstation mechanics can be toggled and configured via `config/ebd/config.yml`:

```yaml
# Proximity-based Crafting Workstations settings
enable_crafting_workstations: true
workstation_detection_radius: 5
```

### 🧩 Custom Item Tags (Datapack Support)
Add or customize workstation requirements via item tags in `data/enchant_by_doing/tags/item/`:
* `#enchant_by_doing:requires_anvil`
* `#enchant_by_doing:requires_enchanting_table`
* `#enchant_by_doing:requires_fletching_table`
* `#enchant_by_doing:requires_armorer`
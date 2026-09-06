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
| 🗺️ **Cartography Table** | `minecraft:cartography_table` | **Maps & Navigation:** Empty Maps, Locator Maps, Compasses, Recovery Compasses, Spyglasses. | **Crafting the Cartography Table itself** does NOT require a cartography table. |

---

## ⛏️ Progression Softlock Solution: Emergency Obsidian Mining

Because diamond tools require an **Enchanting Table**, and an Enchanting Table requires **4 Obsidian**, players without diamond tools can obtain obsidian using an **Iron Pickaxe**:

* **Chipping Speed:** Mining Obsidian with an Iron Pickaxe is **very slow** (~30 seconds of continuous chipping per block).
* **Guaranteed Drop:** The mined Obsidian block is guaranteed to drop as an item.
* **Tool Shatter:** The Iron Pickaxe is **guaranteed to shatter/break completely** upon mining the obsidian block.
* **Cost:** Exactly **1 Iron Pickaxe per 1 Obsidian** (crafting an Enchanting Table requires sacrificing 4 Iron Pickaxes).

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
* **🧭 Compass / Navigation:**
  * Requires: **🔨 Anvil** AND **🗺️ Cartography Table**
* **🛡️ Shield:**
  * Requires: **🔨 Anvil** AND **🛡️ Armorer / Smithing Station**

---

## 🖥️ Vanilla-Style Crafting GUI & Minimalist Tooltips

The workstation panel in the Crafting Table and Inventory GUI features a clean, unobtrusive vanilla aesthetic:

* **Vanilla Slot Framing:** Each workstation icon is housed in a clean, dark vanilla-style slot.
* **Subtle Active Dot:** Stations present within 5 blocks feature a subtle green corner dot.
* **Zero Tooltip Clutter:** Hovering over workstations in normal states produces **no tooltip popup**, keeping your screen clean and distraction-free.
* **Missing & Required Alert:** If a craft is blocked because a station is missing:
  * The missing station's slot pulses with a red warning animation and a `!` badge.
  * Hovering over the missing station displays a concise, single-line tooltip:
    `Для крафта требуется: [Название станции]` / `Requires for crafting: [Station Name]`.

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
* `#enchant_by_doing:requires_cartography_table`
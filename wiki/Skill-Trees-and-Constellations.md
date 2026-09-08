# Skill Trees & Constellations

In **Enchant by Doing (EbD)**, player progression is divided into **6 unique paths**. As you perform relevant actions in the game world, you accumulate experience points (XP) towards that specific discipline. Each skill spans from **Level 1 to Level 100**.

---

## 🌠 The Constellation Sky GUI

Pressing **`K`** (default keybind) transports the player into a celestial Skyrim-style night-sky interface.

```
 +-------------------------------------------------------------------------+
 |                                                                         |
 |         * (Star 3)                                                      |
 |        / \                                                              |
 |       /   \                                                             |
 | (Star 1)---* (Star 2)                    [CONSTELLATION: MINER]         |
 |       \                                                                 |
 |        \                                                                |
 |         * (Star 4)                                                      |
 |                                                                         |
 | ----------------------------------------------------------------------- |
 |                             MINER: LEVEL 42                             |
 |                       [===============>            ]                    |
 |                                1,420 / 2,800 XP                         |
 +-------------------------------------------------------------------------+
```

### 🎮 GUI Controls & Navigation
* **`A` / `D` or `Left Arrow` / `Right Arrow`:** Smoothly rotate and pan between the 6 skill constellations in circular 360° carousel order.
* **`W` / `S` or `Up Arrow` / `Down Arrow`:** Glide between the horizontal Skills ribbon and the **Global Hero Constellation (Древо Героя)** via vertical looped navigation.
* **`Mouse Drag` (Left Click Hold):** Pan freely across the celestial sky to inspect star arrangements.
* **`Mouse Hover`:** Hovering over any star displays a detailed tooltip showing its perk name, unlock level, cost in skill points, current rank, and gameplay description.
* **`Left Click on Perk Star`:** (In Global Hero layer) Purchase or upgrade the perk if you have available Skill Points.
* **`Esc` or `K`:** Exit the sky screen and smoothly restore ambient game audio.

---

## 📊 Experience & Leveling Formulas

Experience requirements per level are governed by two distinct systems:

1. **Configurable Curve (`config/ebd/config.yml`):**
   * **Exponential Curve (Default):**
     $$\text{XP}_{\text{needed}}(\text{level}) = \text{xpBase} \times (\text{xpMultiplier})^{\text{level} - 1}$$
     *(Default: $\text{xpBase} = 100.0$, $\text{xpMultiplier} = 1.07$)*
   * **Linear Curve:**
     $$\text{XP}_{\text{needed}}(\text{level}) = \text{xpBase} \times \text{level}$$

2. **Per-Level Flat Tables (`config/ebd/skills_xp.yml`):**
   * Modpack authors can define custom per-level XP costs for every individual level (1 through 100) for each skill separately.

---

## ⛏️ 1. Miner (Шахтёр)

* **Theme Color:** Slate Gray (`#708090`)
* **Primary XP Triggers:**
  * Mining ores: Hardness-based scaling ($\text{hardness} \times \text{multiplier}$) or default flat rates ($\text{xpOreMined} = 10.0$).
  * Mining stone, deepslate, and netherrack ($\text{xpStoneMined} = 1.0$).
  * *Note:* Silk Touch tools do not grant mining XP to prevent repetitive block-placing exploits.
* **Anti-Abuse Protection:** Placing stone or cobblestone triggers a 5-second cooldown during which mined stone XP is reduced by 75%.

### ⭐ Miner Constellation Stars

| Star ID | Perk Name | Unlock Level | Description & Mechanics |
|---|---|:---:|---|
| `miner_start` | **Heart of the Mountain** | **Lvl 1** | Each Miner level grants **+1% pickaxe mining speed**. Base speed penalty (default -25%) is gradually eliminated, reaching **+74% net mining speed** at level 100. |
| `miner_node2` | **Coal Seam** | **Lvl 25** | Grants a **10% chance** (+5% per 25 levels, up to 25% at lvl 100) to receive **1–3 extra coal** whenever any ore is mined. |
| `miner_node3` | **Hearty Miner** | **Lvl 50** | Mining any ore has a **15% chance** to instantly restore **0.5 saturation** (1 food point), allowing prolonged subterranean mining expeditions. |
| `miner_node4` | **Diamond Rush** | **Lvl 75** | Mining diamond or deepslate diamond ore grants **Haste I**, **Speed I**, and **Night Vision I** for **20 seconds** (duration stacks with multiple diamond ores). |
| `miner_node5` | **Ancient Trace** | **Lvl 100** | Mining Nether Gold Ore has a **2% chance** to extract a rare chunk of **Ancient Debris**. |

---

## ⚔️ 2. Warrior (Воин)

* **Theme Color:** Dark Red (`#B71C1C`)
* **Primary XP Triggers:**
  * Slaying hostile monsters with melee weapons ($\text{xpMobKilledHostile} = 20.0$ or $\text{maxHealth} \times \text{multiplier}$).
  * Dealing melee weapon damage.

### ⭐ Warrior Constellation Stars

| Star ID | Perk Name | Unlock Level | Description & Mechanics |
|---|---|:---:|---|
| `warrior_start` | **Battle Inspiration** | **Lvl 1** | Each Warrior level increases melee sword attack damage by **+1%** (reaching **+100% bonus damage** at level 100). |
| `warrior_node2` | **Marauder** | **Lvl 20** | Slaying any hostile or neutral mob has a **20% chance** to duplicate all dropped loot. |
| `warrior_node3` | **Bastion** | **Lvl 40** | While holding a shield in your off-hand, incoming attacks have a **20% chance** to be **completely negated** (consumes 10 shield durability; 5-second internal cooldown). |
| `warrior_node4` | **Nether Reverence** | **Lvl 60** | Piglins remain neutral even if you wear no golden armor. All negative status effect (debuff) durations on the player are reduced by **25%** (and by **50%** at level 80). |
| `warrior_node5` | **Blood Frenzy** | **Lvl 80** | Slaying a hostile mob has a **20% chance** to heal **2 HP (1 heart)**. If already at full health, grants **2 HP of Absorption** (stacks up to 10 Absorption HP / 5 yellow hearts). |
| `warrior_node6` | **Second Wind** | **Lvl 100** | **Death Defiance:** Fatal damage is cancelled. Instantly grants **10 Absorption Hearts (20 HP)**, **Regeneration V**, **Speed III**, **Strength III**, and **Blindness I** for 3 seconds (5-minute cooldown). Endermen flee and teleport away when looked at. |

---

## 🏹 3. Hunter (Охотник)

* **Theme Color:** Forest Green (`#2E7D32`)
* **Primary XP Triggers:**
  * Slaying hostile mobs with ranged weapons ($\text{xpMobKilledHostile} = 20.0$).
  * Hunting wild animals ($\text{xpMobKilledPeaceful} = 10.0$).
  * Landing projectile hits from distance.

### ⭐ Hunter Constellation Stars

| Star ID | Perk Name | Unlock Level | Description & Mechanics |
|---|---|:---:|---|
| `hunter_start` | **Eagle Eye** | **Lvl 1** | Increases all arrow and projectile damage by **+1% per level** (reaching **+100% projectile damage** at level 100). |
| `hunter_node2` | **Arrow Scavenger** | **Lvl 25** | Gives a **25% chance** to not consume arrows or tipped arrows when firing (increases to **50% chance** at level 75). |
| `hunter_node3` | **Quickdraw** | **Lvl 50** | Increases bow draw speed and crossbow charge speed by **+25%** (increases to **+50% faster charge** at level 100). |
| `hunter_node4` | **Toxic Sting** | **Lvl 75** | Potion effect durations inflicted by tipped arrows and poison arrows are **doubled (2x)** on hit targets. |
| `hunter_node5` | **Supersonic Barrage** | **Lvl 100** | Arrow projectile velocity is boosted by **+50%**. Holding down right-click enables **continuous automatic shooting** for both bows and crossbows. |

---

## 🪓 4. Lumberjack (Дровосек)

* **Theme Color:** Saddle Brown (`#8B4513`)
* **Primary XP Triggers:**
  * Felling tree logs ($\text{xpWoodChopped} = 2.0$ or log hardness).
  * Stripping logs and breaking leaves.
* **Anti-Abuse Protection:** Placing wood logs triggers a 5-second cooldown during which placed logs award 0 XP.

### ⭐ Lumberjack Constellation Stars

| Star ID | Perk Name | Unlock Level | Description & Mechanics |
|---|---|:---:|---|
| `lumberjack_start` | **Forest Whirlwind** | **Lvl 1** | Each level increases axe woodchopping speed by **+1%** and axe combat damage by **+1%**. Base chopping penalty (-25%) is overcome. |
| `lumberjack_node2` | **Arboriculture** | **Lvl 20** | Felling a tree automatically selects and plants a matching sapling from your inventory onto the dirt/grass block below (fully integrated with TreeChop). |
| `lumberjack_node3` | **Apple Harvest** | **Lvl 40** | Multiplies the natural apple drop rate from oak and dark oak leaves by **10x** (when broken by hand, axe, or felling). |
| `lumberjack_node4` | **Lumberjack Combatant** | **Lvl 60** | Grants **+25% axe attack speed**. Leaves break **instantly** (infinite dig speed) when struck with an axe. |
| `lumberjack_node5` | **Berserker** | **Lvl 80** | **Dual-Wield Axes Mastery:** Axes never lose durability. Holding an axe in both hands triggers rapid alternating swing animations, double melee strikes, and **+50% chopping speed**. |

---

## 🌾 5. Farmer (Фермер)

* **Theme Color:** Golden Orange (`#F57F17`)
* **Primary XP Triggers:**
  * Harvesting mature crops (Wheat, Carrots, Potatoes, Beetroot, Melons, Pumpkins, Nether Wart, Cocoa) ($\text{xpCropHarvested} = 5.0$).
  * Breeding livestock ($\text{xpAnimalBred} = 25.0$).

### ⭐ Farmer Constellation Stars

| Star ID | Perk Name | Unlock Level | Description & Mechanics |
|---|---|:---:|---|
| `farmer_start` | **Gift of Fertility** | **Lvl 1** | Each level increases farming efficiency and adds **+0.1% chance per level** to harvest gilded crops (up to **10% chance** at level 100). |
| `farmer_node2` | **Golden Grain** | **Lvl 25** | Unlocks harvesting **Luminous Straw** (Golden Wheat). Feeding it to cows or sheep yields **2–4 offspring** and instantly matures baby animals. |
| `farmer_node3` | **Glistering Refreshment** | **Lvl 50** | Unlocks harvesting edible **Glistering Melons**. Eating a slice instantly restores **4 HP (2 full hearts)**. |
| `farmer_node4` | **Auric Tuber** | **Lvl 75** | Unlocks harvesting **Auric Tubers** (Golden Potatoes). Eating one shrinks the player to 1 block in height for 10s. Used for brewing **Shrinking Potions**. |
| `farmer_node5` | **Gilded Root** | **Lvl 100** | Unlocks harvesting **Gilded Beets**. Eating one clears all potion effects (like milk) while consuming 3 food bars. Also unlocks planting vanilla **Golden Carrots** as farmable crops. |

---

## 🔮 6. Enchanter (Зачарователь)

* **Theme Color:** Arcane Purple (`#6A1B9A`)
* **Primary XP Triggers:**
  * Discovering and reading ancient **Dusty Books** ($\text{xpDustyBookRead} = 50.0$).
  * Crafting enchantments at the Enchanting Table ($\text{xpItemEnchanted} = 30.0$).
  * Combining and repairing items on Anvils ($\text{xpItemRepaired} = 15.0$).
  * Disenchanting on Grindstones ($\text{xpItemDisenchanted} = 15.0$).

### ⭐ Enchanter Constellation Stars

| Star ID | Perk Name | Unlock Level | Description & Mechanics |
|---|---|:---:|---|
| `enchanter_start` | **Mystic Resonance** | **Lvl 1** | Scales material discounts for all enchanting recipes (from +25% cost at lvl 1 down to **-75% discount** at level 100). |
| `enchanter_node2` | **Knowledge Preservation** | **Lvl 25** | Applying an enchantment from an Enchanted Book on an anvil or reading a book returns a clean empty **Book**. Disenchanting does not degrade item durability. |
| `enchanter_node3` | **Arcane Extraction** | **Lvl 50** | Disenchanting at a Grindstone has a **15% chance** to extract a removed enchantment back into an empty book in your inventory. Increases max item enchantment capacity by **+1**. |
| `enchanter_node4` | **Alchemical Hurler** | **Lvl 75** | Beneficial potion durations increased by **+50%** (+100% at lvl 100), potion drink time cut in half. Drinking hurls the empty glass bottle as a kinetic projectile dealing **6 damage**. |
| `enchanter_node5` | **Supreme Capacity** | **Lvl 100** | Expands item enchantment limit by an additional **+1** (total **+2 enchantments above vanilla limit**). |

---

## 🌟 7. Global Character Level & Hero Constellation (Древо Героя)

Inspired by the Skyrim progression system, Enchant by Doing features a meta **Global Character Level** that ties together all individual disciplines.

### 📈 Global Progression Formula
* Whenever any of the 6 skills increases to Level $L$, the player earns **$+L$ Character XP**.
* Experience required to reach the next character level $N$:
  $$\text{XP}_{\text{needed}}(N) = (N + 3) \times 25$$
* Each global level-up grants **+1 Skill Point** (Очко навыков) and triggers a celestial sound fanfare and chat announcement.
* Existing player saves are automatically upgraded retroactively based on already earned skill levels.

### 🌌 Navigation & Layer Switching
In the sky GUI (`K`), press **`W` / `S`** or **`Up` / `Down` Arrow** to glide smoothly into the **Global Constellation (Древо Героя)**. The UI shows your total Hero Level, progress bar towards next level, and available Skill Points.

### ⭐ Global Hero Perks

Perks are purchased directly by clicking on their stars in the celestial sky:

| Perk ID | Name | Max Level | Cost (Points) | Icon | Description & Flavor |
|---|---|:---:|:---:|:---:|---|
| `health_boost` | **Крепкое здоровье** (Health Boost) | **20** | 1 per rank | Golden Apple | Increases player Max Health by **+0.5 heart (+1 HP)** per rank. Reaches **+10 extra hearts (+20 HP)** at rank 20. |
| `light_step` | **Легкая поступь** (Light Step) | **3** | 1 per rank | Feather | Reduces fall damage by **20% / 40% / 60%**. **Flavor:** At rank 3, running and jumping never tramples farmland crops! |
| `well_fed` | **Сытый путник** (Well-Fed Wanderer) | **3** | 1 per rank | Cooked Beef | Increases saturation gained from all food by **+25% / +50% / +75%**. **Flavor:** At rank 3, sprinting depletes hunger 30% slower. |
| `loot_magnet` | **Магнит добычи** (Loot Magnet) | **2** | 2 per rank | Hopper | Increases dropped item attraction radius by **+2.0 / +4.0 blocks**. **Flavor:** Crouching (Shift) pauses attraction so you can share items freely! |
| `iron_will` | **Закалка** (Iron Will) | **3** | 1 per rank | Shield | Reduces damage taken from fire, lava, poison, and wither by **15% / 30% / 45%**. **Flavor:** At rank 3, burning duration after stepping out of fire is cut in half! |
| `wave_rider` | **Морской волк** (Wave Rider) | **2** | 1 per rank | Heart of the Sea | Increases swim speed by **+25% / +50%** and underwater breath by **+50% / +100%**. **Flavor:** At rank 2, grants Conduit Power underwater vision for crystal-clear sight! |
| `silver_tongue` | **Торговая жилка** (Silver Tongue) | **2** | 2 per rank | Emerald | Grants permanent villager price discounts via elevated noble reputation. |

---

## 🛠️ Visual Constellation Web Editor (`skills_editor.html`)

EbD includes a standalone interactive HTML/Canvas visual constellation editor (`skills_editor.html`) located in the mod root.

* **Drag & Drop Star Nodes:** Reposition constellation stars dynamically.
* **Add / Delete Connections:** Create runic glyph line paths between nodes.
* **Export JSON:** Export directly to `ebd_skills_layout.json` to customize the constellation geometry for your modpack.
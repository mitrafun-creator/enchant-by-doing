# Experience Mechanics & Anti-Abuse System

**Enchant by Doing (EbD)** replaces arbitrary mob grinding and AFK farms with an **action-based RPG progression system**. To ensure fair gameplay, prevent exploits, and reward legitimate gameplay, EbD employs a comprehensive suite of anti-abuse security checks and dynamic formula calculations.

---

## 📊 Complete XP Sources Table

Every in-game action awards experience directly to its corresponding skill:

| Skill | Action / Event | Default XP | Config Key | Formula / Calculation |
|---|---|:---:|---|---|
| ⛏️ **Miner** | Mining Ore (without Silk Touch) | **10.0 XP** | `xp_ore_mined` | $\max(0.1, \text{BlockHardness} \times \text{HardnessMultiplier})$ |
| ⛏️ **Miner** | Mining Stone, Deepslate, Netherrack | **1.0 XP** | `xp_stone_mined` | Base stone hardness scaling |
| 🪓 **Lumberjack** | Chopping Wood Logs | **2.0 XP** | `xp_wood_chopped` | $\max(0.1, \text{LogHardness} \times \text{HardnessMultiplier})$ |
| 🪓 **Lumberjack** | Felling Tree with TreeChop | **Dynamic** | `xp_wood_chopped` | Sum of all connected felled logs in the tree |
| 🌾 **Farmer** | Harvesting Fully Mature Crops | **5.0 XP** | `xp_crop_harvested` | Fixed reward per harvested mature crop |
| 🌾 **Farmer** | Breeding Animals (Cows, Sheep, etc.) | **25.0 XP** | `xp_animal_bred` | Awarded on `BabyEntitySpawnEvent` |
| ⚔️ **Warrior** | Slaying Hostile Mob (Melee) | **20.0 XP** | `xp_mob_killed_hostile` | $\max(1.0, \text{MobMaxHealth} \times \text{HealthMultiplier})$ |
| 🏹 **Hunter** | Slaying Hostile Mob (Ranged / Bow) | **20.0 XP** | `xp_mob_killed_hostile` | $\max(1.0, \text{MobMaxHealth} \times \text{HealthMultiplier})$ |
| 🏹 **Hunter** | Hunting Peaceful Animal | **10.0 XP** | `xp_mob_killed_peaceful` | Fixed reward per hunted animal |
| 🔮 **Enchanter** | Absorbing a Dusty Book | **50.0 XP** | `xp_dusty_book_read` | Awarded on right-click knowledge absorption |
| 🔮 **Enchanter** | Crafting an Enchantment at Table | **30.0 XP** | `xp_item_enchanted` | Awarded upon successful enchantment craft |
| 🔮 **Enchanter** | Repairing / Combining on Anvil | **15.0 XP** | `xp_item_repaired` | Awarded on `AnvilRepairEvent` |
| 🔮 **Enchanter** | Disenchanting at Grindstone | **15.0 XP** | `xp_item_disenchanted`| Awarded on grindstone result removal |
| 🔮 **Enchanter** | Using Enchanted Gear (Durability Loss) | **Dynamic** | `xp_enchanted_item_use_*` | ~1 in 25 chance per durability hit; scales with enchant count, levels & Unbreaking |

### 🔮 Enchanted Gear Usage Experience Breakdown
When using enchanted tools, weapons, or wearing enchanted armor, players have a chance to channel magical resonance into their **Enchanter** skill:
* **Trigger Condition:** Whenever an enchanted item actually loses durability (`hurtAndBreak`).
* **Chance:** **1 in 25 (4%)** by default (`xp_enchanted_item_use_chance: 25`).
* **Base Experience Formula:**
  $$\text{XP}_{\text{base}} = (\text{EnchantmentCount} \times 2.0) + (\sum \text{EnchantmentLevels} \times 1.5)$$
* **Unbreaking (Прочность) Multiplier:**
  Because the Unbreaking enchantment reduces how frequently items take durability damage, having Unbreaking applies a proportional multiplier to the final experience yield:
  $$\text{Multiplier}_{\text{unbreaking}} = 1.0 + (\text{UnbreakingLevel} \times 1.0)$$
  * *No Unbreaking:* **1.0x**
  * *Unbreaking I:* **2.0x**
  * *Unbreaking II:* **3.0x**
  * *Unbreaking III:* **4.0x**
* **Final Experience Calculation:**
  $$\text{XP}_{\text{total}} = \text{XP}_{\text{base}} \times \text{Multiplier}_{\text{unbreaking}}$$
  *(e.g., God Pickaxe with 4 enchantments, 12 total levels, and Unbreaking III grants $(4 \times 2.0 + 12 \times 1.5) \times 4.0 = \mathbf{104.0\text{ XP}}$).*
* **Visual FX:** Emits enchanting table rune particles (`ParticleTypes.ENCHANT`) around the player upon each proc.

### ⛏️ Ore Mining Experience Breakdown
Ores award specialized Miner experience scaled to their rarity and preciousness:

| Ore Tier | Stone Variant | Deepslate Variant | Progression Multiplier / Note |
|---|:---:|:---:|---|
| **Coal / Copper Ore** | **5.0 XP** | **10.0 XP** | Common surface ores (Deepslate 2x) |
| **Iron / Redstone / Lapis Ore** | **10.0 XP** | **20.0 XP** | Standard base ore (`xp_ore_mined: 10.0`, Deepslate 2x) |
| **Gold / Nether Quartz / Nether Gold** | **15.0 XP** | **30.0 XP** | Precious conductive ores (Deepslate 2x) |
| **Diamond Ore** | **100.0 XP** | **200.0 XP** | High tier mineral (Deepslate 2x) |
| **Emerald Ore** | **400.0 XP** | **800.0 XP** | Rare mountain mineral (4x Diamond, Deepslate 2x) |
| **Ancient Debris** | **1000.0 XP** | — | Netherite precursor (Endgame mining) |

*Mining with Silk Touch awards **0.0 XP** to prevent placing and re-mining loops.*

---

## 🛡️ Anti-Abuse Security Architecture

EbD implements **5 distinct layers of anti-abuse protection** to prevent players from placing and breaking the same blocks or exploiting infinite XP loops:

```
                               ┌────────────────────────────────┐
                               │   EBD ANTI-ABUSE CONTROLS      │
                               └───────────────┬────────────────┘
                                               │
         ┌──────────────────┬──────────────────┼──────────────────┬──────────────────┐
         │                  │                  │                  │                  │
 ┌───────▼────────┐ ┌───────▼────────┐ ┌───────▼────────┐ ┌───────▼────────┐ ┌───────▼────────┐
 │ 1. SILK TOUCH  │ │ 2. STONE TIMER │ │ 3. LOG LOCKDOWN│ │ 4. CROP AGE    │ │ 5. MELON TRACK │
 │ ZERO XP        │ │ 75% XP Penalty │ │ 0 XP on Placed │ │ Max Stage Only │ │ WorldSavedData │
 └────────────────┘ └────────────────┘ └────────────────┘ └────────────────┘ └────────────────┘
```

### 🚫 1. Silk Touch Nullification
* **Rule:** If a pickaxe possesses the **Silk Touch** (`silk_touch`) enchantment, **0 Miner XP** is awarded for mining ores.
* **Purpose:** Prevents players from placing ores with Silk Touch, mining them with Fortune, placing them again, and repeating infinitely.

### ⏱️ 2. Stone Placement Penalty Timer (`minerPenaltyTime`)
* **Rule:** Whenever a player places any stone, cobblestone, deepslate, granite, andesite, or diorite block (`BlockTags.BASE_STONE_OVERWORLD` / `BASE_STONE_NETHER`), a **5-second penalty cooldown** is applied to that player (`anti_abuse_cooldown_seconds: 5`).
* **Penalty:** Any stone mined during this 5-second window has its experience reward **reduced by 75%** (`anti_abuse_xp_reduction: 0.75`).

### 🪓 3. Wood Log Placement Full Lockdown (`lumberjackPenaltyTime`)
* **Rule:** Whenever a player places a wood log (`BlockTags.LOGS`), an active penalty timer of 5 seconds is stamped onto the player.
* **Penalty:** Any wood log broken while this timer is active grants **exactly 0.0 XP**, completely negating placed-log chopping loops.

### 🌱 4. Mature Crop Stage Verification
* **Rule:** Harvesting crops awards Farmer XP **only if the crop is 100% fully grown**:
  * `CropBlock` (Wheat, Carrots, Potatoes, Beetroots) must satisfy `crop.isMaxAge(state)`.
  * `NetherWartBlock` must have `AGE >= 3`.
  * `CocoaBlock` must have `AGE >= 2`.
* **Purpose:** Prevents breaking freshly planted seeds or semi-grown crops for free XP.

### 🍉 5. World-Saved Melon Tracker (`PlacedMelonsTracker`)
* **Rule:** EbD registers a dedicated `WorldSavedData` tracker on the server (`PlacedMelonsTracker`).
* **Mechanic:** If a player manually places a Melon block from their inventory, its exact `BlockPos` is recorded in the tracker. When broken:
  * No golden slice drop chance is rolled.
  * No harvest XP is awarded.
  * The position is cleanly removed from the tracker.

---

## 🏹 Damage Source Discrimination (Warrior vs. Hunter)

EbD analyzes the incoming damage source to route combat XP to the appropriate discipline:

* **Direct Melee Damage (`DirectEntity == Player`):**
  * Slaying a hostile mob with swords or configured melee weapons awards **Warrior XP**.
* **Projectile Damage (`DirectEntity instanceof Projectile`):**
  * Slaying any mob via arrows, spectral arrows, tipped arrows, or crossbow fire awards **Hunter XP**.
* **Hunting Animals:** Slaying peaceful animals (cows, pigs, chickens) awards **Hunter XP** to represent hunting and survivalist gathering.

---

## 🌟 Experience Boosters: "Experience Surge"

Players can temporarily boost their EbD experience gains:

* **Drinkable Bottle o' Enchanting:** Drinking a vanilla **Bottle o' Enchanting** (`minecraft:experience_bottle`) applies the **Experience Surge** (`ebd:xp_boost`) status effect for **5 minutes (6000 ticks)**.
* **Amplifiers:**
  * **Experience Surge I:** Grants **+50% bonus XP** to all EbD actions.
  * **Experience Surge II:** Grants **+100% bonus XP** (double XP) to all EbD actions.

---

## 📐 Leveling Formulas & Curves

Experience needed to progress from level $L$ to $L+1$ is calculated using either:

### A. Exponential Curve (Default):
$$\text{XP}_{\text{required}}(L) = \text{xpBase} \times (\text{xpMultiplier})^{L - 1}$$
* *Default Values:* $\text{xpBase} = 100.0$, $\text{xpMultiplier} = 1.07$.
* *Example (Standard Skills):* Level 1 $\rightarrow$ 100 XP, Level 10 $\rightarrow$ 183.8 XP, Level 50 $\rightarrow$ 2,753.0 XP, Level 100 $\rightarrow$ 81,095.0 XP.
* *🔮 Enchanter Progression (5x Reduced):* To balance the rarity of enchanting interactions, the Enchanter skill curve is **reduced 5-fold** across all 100 levels:
  * Level 1 $\rightarrow$ **20.0 XP**
  * Level 10 $\rightarrow$ **36.8 XP**
  * Level 50 $\rightarrow$ **550.6 XP**
  * Level 100 $\rightarrow$ **16,219.0 XP**

### B. Linear Curve:
$$\text{XP}_{\text{required}}(L) = \text{xpBase} \times L$$

### C. Flat Table Configuration (`config/ebd/skills_xp.yml`):
Modpack creators can override all formulas and specify the exact XP required for every level from 1 to 100 on a per-skill basis.

---

## 📜 Dusty Book Discovery Sources
Players can discover rare **Dusty Books** (`ebd:dusty_book`) to absorb for **+50 Enchanter XP**:
* **Archaeology:** **20% chance** (`dusty_book_archaeology_chance: 0.20`) when brushing suspicious gravel / sand.
* **Fishing:** **10% chance** (`dusty_book_fishing_chance: 0.10`) on common catches, **35% chance** (`dusty_book_fishing_treasure_chance: 0.35`) in treasure pool.
* **Undead Monster Drops (Skeletons, Zombies, Drowned, Husks):** **3% base chance** (`dusty_book_monster_drop_chance: 0.03`) + **1% per Looting level**.
* **Wither Skeleton Drop:** **5% base chance** (`dusty_book_wither_skeleton_drop_chance: 0.05`) + **1% per Looting level**.
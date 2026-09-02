# Custom Enchantments, Items & Status Effects

**Enchant by Doing (EbD)** enriches Minecraft with a suite of custom enchantments, mystical gilded agriculture, and unique status effects.

---

## ⚔️ 1. Custom Weapon & Combat Enchantments

| Enchantment ID | Max Lvl | Applicable To | In-Game Effect |
|---|:---:|---|---|
| `ebd:quicksand` | **II** | Shovels | Striking a mob or player inflicts **Slowness I** (Lvl 1) or **Slowness II** (Lvl 2) for 2 seconds (40 ticks), trapping enemies in place. |
| `ebd:disarm` | **III** | Shovels | Has a **10% / 15% / 20% chance** on hit to disarm the target entity, causing their held main-hand weapon to drop onto the ground (halved against players). |
| `ebd:sting` | **II** | Hoes | Attacks inflict **Poison I** (Lvl 1) or **Poison II** (Lvl 2) for 2 seconds (40 ticks), turning farming tools into venomous light weapons. |
| `ebd:executioner` | **III** | Axes | Increases damage dealt against low-health enemies (below 30% HP) by **+20% / +40% / +60%**, executing weakened targets instantly. |
| `ebd:leviathan` | **III** | Tridents | Greatly boosts damage dealt against aquatic and underwater mobs, creating tidal shockwaves on impact. |
| `ebd:dragons_breath` | **II** | Swords, Bows | Attacks leave behind a lingering patch of dangerous purple dragon fire that damages and debuffs entities passing through it. |

---

## 🏹 2. Ranged Weapon Enchantments

| Enchantment ID | Max Lvl | Applicable To | In-Game Effect |
|---|:---:|---|---|
| `ebd:magazine` | **III** | Crossbows | Allows charging and storing multiple loaded projectiles (up to **2 / 3 / 4 arrows or fireworks**) inside a single crossbow, firing them in rapid succession. |
| `ebd:auto_reload` | **I** | Crossbows | Automatically rechamber and load a new arrow into the crossbow immediately after firing, removing manual reloads. |
| `ebd:capacity` | **I** | All Enchantables | Expands the maximum number of simultaneous enchantments this specific item can hold by **+1**. |

---

## ⛵ 3. Overhauled Boat Enchantments

EbD allows enchanting vanilla and modded boats with unique vehicular abilities:

```
                  ┌───────────────────────────────┐
                  │    ENCHANTED BOAT VEHICLES    │
                  └───────────────┬───────────────┘
                                  │
         ┌────────────────────────┼────────────────────────┐
         │                        │                        │
 ┌───────▼────────┐      ┌────────▼────────┐      ┌────────▼────────┐
 │   AEROFLOT     │      │   SUBMARINE     │      │    AUTODRIVE    │
 ├────────────────┤      ├─────────────────┤      ├─────────────────┤
 │ Glides in air  │      │ Dives into deep │      │ Motorized speed │
 │ Slow fall drop │      │ water with air  │      │ Drives on land  │
 └────────────────┘      └─────────────────┘      └─────────────────┘
```

| Enchantment ID | Max Lvl | Applicable To | In-Game Effect |
|---|:---:|---|---|
| `ebd:slipway` | **III** | Boats | Drastically reduces friction on land and solid ground, allowing high-speed sliding across any terrain (ice-like physics everywhere). |
| `ebd:seafarer` | **III** | Boats | Increases water acceleration and top cruising speed by **+30% / +60% / +100%**. |
| `ebd:fireproof_boat`| **I** | Boats | Makes the boat and its passengers completely immune to fire and lava damage, allowing navigation across Nether lava lakes. |
| `ebd:autodrive` | **I** | Boats | Enables perpetual motorized forward propulsion without constantly tapping directional keys. |
| `ebd:aeroflot` | **I** | Boats | Allows boats to glide gracefully through the air when launched off cliffs, providing steerable parachute descent. |
| `ebd:pocket_boat` | **I** | Boats | Sneak-clicking the boat instantly picks it up and returns it directly to your inventory with all enchantments preserved. |
| `ebd:submarine` | **I** | Boats | Allows submerging the boat underwater with full vertical dive controls and grants infinite Water Breathing to occupants. |

---

## 🌾 4. Gilded Crops & Agriculture

Harvesting crops with high **Farmer** skill levels triggers a chance to discover mystical golden crop variants:

```
 [ Wheat Harvest ]   ───> [ 🌾 Luminous Straw ]      ───> Multi-birth breeding & instant growth
 [ Melon Harvest ]   ───> [ 🍉 Glistering Melon ]    ───> Consumable: Instant 4 HP heal
 [ Potato Harvest ]  ───> [ 🥔 Auric Tuber ]         ───> Consumable: Shrinks player to 1 block
 [ Beetroot Harvest ]───> [ 🌰 Gilded Beet ]         ───> Consumable: Clears all potion effects
```

### 🌽 Crop Descriptions
1. **Luminous Straw (`ebd:golden_wheat`):**
   * Feed to cows or sheep to trigger **super-breeding** (yields 2–4 babies at once).
   * Feeding to baby animals instantly matures them into adults.
2. **Glistering Melon Slice (`minecraft:glistering_melon_slice`):**
   * Edible directly from inventory.
   * Does not restore hunger, but immediately restores **4 HP (2 full hearts)**.
3. **Auric Tuber (`ebd:golden_potato`):**
   * Eating shrinks the player to **1 block in height** for 10 seconds (allows crawling into 1-block tunnels).
   * Primary ingredient in brewing **Potions of Shrinking**.
4. **Gilded Beet (`ebd:golden_beetroot`):**
   * Eating clears all active potion effects (identical to a milk bucket) while consuming 3 food bars.
   * Primary ingredient in brewing **Potions of Animal Attraction**.
5. **Gilded Carrot Crop (`ebd:golden_carrot_crop`):**
   * At Farmer Level 100, right-clicking farmland with a vanilla Golden Carrot plants a farmable golden carrot crop!

---

## 🧪 5. Custom Status Effects & Potions

| Status Effect | Visual Icon | Amplifier / Duration | Brewing Recipe & Mechanics |
|---|:---:|---|---|
| **Shrinking** (`ebd:shrinking`) | 🔬 | Shrinks player hitbox to 1.0 block height | Brewed by adding **Auric Tuber** (Golden Potato) to Awkward Potion. |
| **Growing** (`ebd:growing`) | 📏 | Increases entity scale and reach | Brewed by adding **Fermented Spider Eye** to Potion of Shrinking. |
| **Animal Attraction** (`ebd:golden_beetroot`) | 🐾 | Passive mobs follow you from 32 blocks away | Brewed by adding **Gilded Beet** to Awkward Potion. |
| **Experience Surge** (`ebd:xp_boost`) | 🌟 | Boosts all EbD skill XP gains by **+50% (I)** or **+100% (II)** | Brewed by drinking **Bottle o' Enchanting** or brewing with lapis catalysts. |
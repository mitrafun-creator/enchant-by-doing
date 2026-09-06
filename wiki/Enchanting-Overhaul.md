# The Deterministic Enchanting Overhaul

In vanilla Minecraft, enchanting is locked behind opaque random number rolls, repetitive book grinding, and arbitrary XP levels. **Enchant by Doing (EbD)** completely replaces this with a **deterministic, discovery-based crafting system**.

---

## 📖 1. Discovery System: Dusty Books

Knowledge of enchantments must be discovered out in the world before you can apply them to your gear.

```
 [ Dungeon / Structure Chest ]
               │
               ▼
       [ 📜 Dusty Book ]
               │ (Hold Right-Click for 2 seconds)
               ▼
  [ 🌌 Knowledge Absorbed! ] ──> Unlocks Enchantment in Table
```

### 🔍 How to Discover Enchantments
1. **Explore World Structures:** **Dusty Books** generate naturally in dungeon chests, abandoned mineshafts, ancient cities, desert pyramids, bastion remnants, and end cities.
2. **Archaeological Excavation:** Brushing **Suspicious Sand** or **Suspicious Gravel** in Desert Pyramids, Desert Wells, Ocean Ruins, and Trail Ruins has a **20% chance** to uncover an ancient Dusty Book!
3. **Fishing & Deep Waters:** Reeling in catches with a fishing rod has a **10% chance** (and **35% chance on Treasure catches**) to haul up a waterlogged Dusty Book from the deep!
4. **Absorb Knowledge:** Hold right-click while holding a Dusty Book. Particles will swirl, the book's ancient glyphs dissolve into your consciousness, and the book crumbles to dust or empty parchment.
5. **Permanent Knowledge:** Once learned, the enchantment is permanently registered to your player profile and becomes selectable at any Enchanting Table.
6. **Vanilla Enchanted Books:** Absorbing or reading vanilla enchanted books also teaches you their contained enchantment.

> [!NOTE]
> Server operators can toggle the discovery requirement off by setting `"disableDiscoverySystem": true` in `config/ebd/config.json`, making all enchantments instantly available.

---

## 🔮 2. The Overhauled Enchanting Table

When you right-click an **Enchanting Table**, an interactive crafting interface opens:

```
 +-------------------------------------------------------------------------+
 |  ENCHANTING TABLE                           [ Search: Protection____ ]  |
 |                                                                         |
 |  [ Target Item Slot ]      Available Enchantments:                      |
 |  (e.g., Diamond Sword)     [★ Sharpness V       ] (Known)               |
 |                            [★ Fire Aspect II    ] (Known)               |
 |  Required Ingredients:     [★ Looting III       ] (Known)               |
 |  [ 8x Lapis ] [ 2x Diamond ] [ 4x Gold ] [ 1x Netherite Scrap ]         |
 |                                                                         |
 |  Cost Multiplier: 0.65x (Enchanter Lvl 35)                              |
 |                                                                         |
 |                              [ ENCHANT ]                                |
 +-------------------------------------------------------------------------+
```

### ⚡ Key Features of the Table GUI
* **Search Filter:** Type into the search bar at the top right to instantly filter by enchantment name or mod ID.
* **Material Cost Preview:** Selecting an enchantment dynamically populates the 4 requirement slots below with exact required items and quantities.
* **Level & Skill Requirements:** Displays the required Enchanter skill level and player level.
* **Recipe Viewer Integration:** Press `R` (Show Recipe) while hovering over an enchantment to inspect full item requirements and alternatives.

---

## 🧮 3. Material Cost Formula & Calculations

Enchantment ingredient quantities scale based on the enchantment tier, current number of enchantments on the item, and your **Enchanter skill level**:

$$\text{Final Cost} = \text{Base Quantity} \times \text{Item Scale Factor} \times \text{Enchanter Discount Factor}$$

### 📉 Enchanter Skill Discount
The Enchanter skill actively reduces ingredient requirements across all recipes:

$$\text{Discount Factor} = 1.25 - \left( \frac{\text{Enchanter Level}}{100} \right)$$

* **Level 1:** $+25\%$ material cost penalty.
* **Level 25:** $1.00\times$ (Standard cost).
* **Level 50:** $0.75\times$ ($-25\%$ discount).
* **Level 100:** $0.25\times$ (**$-75\%$ discount!**).

### 📈 Existing Enchantment Multiplier
Items that already possess multiple enchantments demand progressively rarer catalysts for each subsequent enchantment:

| Current Enchantments on Item | Cost Scaling Factor |
|:---:|:---:|
| 0 (Mundane Item) | $1.0\times$ |
| 1 Enchantment | $1.5\times$ |
| 2 Enchantments | $2.2\times$ |
| 3 Enchantments | $3.0\times$ |
| 4+ Enchantments | $4.0\times$ |

---

## 🔄 4. Smart Conflict Resolution

In vanilla Minecraft, attempting to combine conflicting enchantments (such as *Fortune* and *Silk Touch*, or *Sharpness* and *Smite*) results in an outright rejection or wasted XP.

EbD introduces **Smart Conflict Resolution**:
* **On Enchanting Tables:** Selecting an enchantment that conflicts with an existing one displays a warning and automatically **replaces** the conflicting enchantment with the new one upon crafting.
* **On Anvils:** Combining two items with mutually exclusive enchantments seamlessly replaces the lower-tier or target-item conflicting enchantment with the incoming book or source item.

---

## 💎 5. Enchantment Capacity System

Items have a maximum capacity for how many simultaneous enchantments they can hold:

* **Vanilla Base Limit:** Determined by item type (typically 4–6 depending on tags).
* **Enchanter Perk 1 (Lvl 50):** Adds **+1 to max enchantment capacity** on all tools, weapons, and armor.
* **Enchanter Perk 2 (Lvl 100):** Adds another **+1 capacity** (total **+2 above vanilla limits**).
* **Capacity Enchantment (`ebd:capacity`):** A custom enchantment that grants an extra enchantment slot when applied!
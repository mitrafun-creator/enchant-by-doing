# Compatibility & Integrations

**Enchant by Doing (EbD)** is engineered to seamlessly fit into large modpacks and integrate with popular gameplay, world generation, and quality-of-life mods.

---

## 🪓 1. TreeChop Integration

EbD features dedicated first-class integration with **[HT's TreeChop](https://www.curseforge.com/minecraft/mc-mods/treechop)**:

```
 [ Player chops TreeChop tree ]
               │
               ├─► Multi-block log hardness calculated accurately
               ├─► Lumberjack XP awarded for all connected chopped logs
               └─► Arboriculture Perk: Auto-replants sapling at stump base!
```

* **Accurate XP Scaling:** When a whole tree falls via TreeChop, EbD intercepts the event and accurately computes Lumberjack XP across all felled logs rather than only the single broken block.
* **Auto-Replanting:** The Level 20 Lumberjack perk (*Arboriculture*) automatically triggers upon tree felling, identifying the base stump and planting a matching sapling from the player's inventory.

---

## 🍳 2. Farmer's Delight & Addons

* **Crop Harvests:** All modded crops extending `CropBlock` or implementing vanilla crop age properties grant Farmer XP upon harvesting when fully mature.
* **Knives & Cutting Boards:** Slicing foods on cutting boards or slaying animals with Farmer's Delight knives awards relevant skill XP.

---

## 👁️ 3. Shaders, Iris & Sodium Compatibility

* **Celestial Sky Rendering:** The Constellation Sky GUI uses clean Blending and Matrix Transformations (`com.mojang.blaze3d.systems.RenderSystem`), fully compatible with **Sodium / Embeddium**, **Iris / Oculus Shaders**, and GUI darkening mods without visual clipping or artifacts.
* **Safe Mixin Architecture:** EbD isolates client renderer mixins to prevent early classloading crashes with other rendering frameworks (e.g. Zeta / Quark / Sodium).

---

## 🔍 4. Recipe Viewers (JEI / EMI / REI)

* Pressing **`R`** (*Show Recipe*) while hovering over any enchantment in the Enchanting Table opens the recipe breakdown displaying all required catalysts, lapis counts, and alternatives.
* All custom items (**Dusty Books**, **Luminous Straw**, **Auric Tubers**, **Glistering Melons**) feature fully registered recipe viewer tabs.

---

## 📦 5. Modpack Developer Guidelines

1. **Custom Weapons:** Register modded swords or battleaxes in `config/ebd/config.yml` under `warrior_weapons` or `lumberjack_weapons` so they trigger combat XP and perks.
2. **Datapack Recipes:** Drop custom enchantment recipes into `data/<your_pack>/ench-recipes/*.json` to customize costs for modded enchantments (e.g., *Apotheosis*, *Ensorcellation*, *Majrusz's Enchantments*).
3. **Disable Discovery for Sandbox Packs:** Set `"disableDiscoverySystem": true` in `config/ebd/config.json` if your modpack prefers unrestricted immediate enchantment access.
# Enchant by Doing (EbD)

**Master your craft through action, not RNG.**

A complete RPG progression and enchanting overhaul for Minecraft (NeoForge 1.21.1). Level up skills by performing real actions in the world, unlock mystical constellations on the night sky, master unique active/passive abilities, and discover enchantments with zero RNG grind.

*Based on and expanded from [Enchanting System Overhaul](https://github.com/CmdrJane/enchanting-system-overhaul) by CmdrJane.*

---

## ✨ Key Features

### 🌌 Celestial Skill Trees & Constellations
* **6 Skill Trees:** Miner, Warrior, Hunter, Lumberjack, Farmer, Enchanter.
* **Constellation Sky GUI:** Skyrim-inspired star-filled nebula screen with Norse runic glyph connections, animated particles, and pulsing experience spheres.
* **Leveling by Doing:** Gain skill XP by mining ores, chopping trees, shooting with bows/crossbows, breeding animals, harvesting crops, and crafting.
* **Customizable XP Configs:** Flat XP requirements configurable per level and skill via `config/ebd/skills_xp.yml`.
* **Visual Web Editor:** Create and customize constellation star coordinates and unlock levels using `skills_editor.html`.

### ⚔️ Unique Abilities & Perks
* **Lumberjack:** Auto-replanting saplings (with TreeChop integration), 10x apple drop chances, and dual-wielding axes mastery (alternate swinging animations, instant leaf chopping, infinite axe durability).
* **Hunter:** Continuous automatic shooting for bows and crossbows, 25–50% arrow saving chance, 1.5x projectile speed, and doubled tipped arrow effect durations.
* **Enchanter:** Throwable glass bottles with physics/damage, returning normal books upon discovery, and a 15% chance to retain enchantments on grindstones.

### 🔮 Overhauled Enchanting System
* **Discovery-Based:** Learn enchantments by finding enchanted books and absorbing their knowledge.
* **No RNG / Material Costs:** Enchant items using recipes and vanilla ingredients rather than arbitrary XP costs.
* **Smart Conflict Resolution:** Automatic replacement of mutually exclusive enchantments on both the enchanting table and anvil.
* **Data-Driven:** Fully customizable via datapacks and YAML/JSON configs.

---

## 🛠️ Commands
* `/ebd skill <player> <skill_id> <set|add|get>` — Manage player skill levels and experience.
* `/ebd learn <player> <enchantment_id|all>` — Discover enchantments for players.
* `/ebd forget <player> <enchantment_id|all>` — Revoke enchantment knowledge.
*(Aliases: `/enchantbydoing`, `/eso`)*

---

## 📚 Documentation & Wiki
Comprehensive documentation, game mechanics, and configuration guides are available in the [**Project Wiki**](wiki/Home.md):
* [🌌 Skill Trees & Constellations](wiki/Skill-Trees-and-Constellations.md)
* [🛠️ Proximity Crafting Workstations](wiki/Proximity-Crafting-Workstations.md)
* [🛡️ Experience Mechanics & Anti-Abuse System](wiki/Experience-Mechanics-and-Anti-Abuse.md)
* [🔮 Deterministic Enchanting Overhaul](wiki/Enchanting-Overhaul.md)
* [✨ Custom Enchantments, Items & Status Effects](wiki/Custom-Enchantments-and-Effects.md)
* [⚙️ Configuration & Datapacks Guide](wiki/Configuration-and-Datapacks.md)
* [💬 Commands & Administration](wiki/Commands-and-Admin.md)
* [🧩 Compatibility & Integrations](wiki/Compatibility-and-Integrations.md)
* [🔍 Codebase Audit & Roadmap](wiki/Codebase-Audit-and-Roadmap.md)

---

## 📜 License
Licensed under the [MIT License](LICENSE).
Copyright (c) 2024 CmdrJane, 2024-2026 MitraFun.


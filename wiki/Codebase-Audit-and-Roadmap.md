# Codebase Audit, Known Quirks & Development Roadmap

This document presents a comprehensive technical audit of the **Enchant by Doing (EbD)** codebase (NeoForge 1.21.1), analyzing its architectural foundations, network synchronization, mixin integrations, and listing identified edge cases and planned improvements.

---

## 🏗️ 1. Technical Architecture Overview

```
                      ┌─────────────────────────────────┐
                      │        EBD ARCHITECTURE         │
                      └────────────────┬────────────────┘
                                       │
         ┌─────────────────────────────┼─────────────────────────────┐
         │                             │                             │
 ┌───────▼────────┐           ┌────────▼────────┐           ┌────────▼────────┐
 │ MIXIN LAYER    │           │ NETWORK MANAGER │           │ EVENT DISPATCH  │
 ├────────────────┤           ├─────────────────┤           ├─────────────────┤
 │ ServerPlayer   │           │ S2CSkillUpdate  │           │ EBDGameplayEv.  │
 │ LivingEntity   │           │ S2CDataSync     │           │ EBDCommon       │
 │ ProjectileWeap │           │ S2CMatConfig    │           │ TreeChopCompat  │
 └────────────────┘           └─────────────────┘           └─────────────────┘
```

### 🛰️ Network Layer (`aiefu.ebd.network`)
* **Payload Types:** Fully modern NeoForge 1.21.1 `CustomPacketPayload` implementation.
* **Synchronization:** Server pushes `S2CSkillUpdatePayload` upon any XP delta or level change; Client stores levels in `SkillHUDRenderer.CLIENT_SKILL_LEVELS`.
* **Config Sync:** Data recipes and material overrides are synced from server to client upon login via `S2CDataSyncPayload` and `S2CMatConfigSyncPayload`.

### 🧬 Player Data Persistence
* Attached directly to `ServerPlayer` through `IServerPlayerAcc` mixin interface.
* Persisted to NBT tags across player dimensions and death/respawns (`ebd$setSkillLevel`, `ebd$setSkillXP`).

---

## 🔍 2. Codebase Audit: Edge Cases & Technical Notes

During our deep codebase review, the following nuances, potential edge cases, and incomplete elements were identified:

### ⚠️ A. Dual-Axe Scheduled Offhand Attack Range Check
* **Location:** `EBDGameplayEvents.java:873` (`SCHEDULED_OFFHAND_ATTACKS`).
* **Observation:** The offhand axe swing is scheduled 4 ticks after the main-hand strike. If the target entity teleports, dies, or moves more than 4.5 blocks away during those 4 ticks, the scheduled attack should re-verify line-of-sight and reach distance to prevent "phantom hits" at long distance.

### ⚠️ B. `ThrownAxeEntity` vs `ThrownBottleEntity`
* **Location:** `aiefu.ebd.entity.ThrownAxeEntity`.
* **Observation:** `ThrownBottleEntity` is fully wired into the Enchanter level 75 perk (throwing glass bottles when drinking potions). `ThrownAxeEntity` has complete physics, projectile rendering, and impact logic ready in the codebase, and can be bound to a dedicated active skill keybind in an upcoming update.

### ⚠️ C. Vanilla XP Zeroing vs Mod Compatibility
* **Location:** `PlayerMixins.java:17` (`zeroExperiencePoints`).
* **Observation:** Vanilla XP points are zeroed out so players do not accidentally engage with the vanilla enchanting table cost system. For modpacks containing mods that strictly require vanilla XP levels for non-enchanting actions (such as *Waystones* or *PlayerEx*), a config toggle `"disableVanillaXpZeroing": true` would provide maximum flexibility.

### ⚠️ D. Piglin Pacification (`warrior_node4`)
* **Location:** `EBDGameplayEvents.java:611` (`onLivingChangeTarget`).
* **Observation:** Piglins are pacified when targeting a Warrior with level $\ge 60$. If a player attacks a Piglin directly, the target check properly excludes that player (`piglin.getLastHurtByMob() != player`), ensuring angry Piglins still retaliate if attacked first.

---

## 🗺️ 3. Feature Roadmap & Planned Enhancements

| Target Milestone | Proposed Feature | Description |
|:---:|---|---|
| **v1.3.0** | 🪓 **Active Axe Throw Ability** | Bind `ThrownAxeEntity` to a dedicated combat hotkey for Lumberjacks (Level 80+). |
| **v1.3.0** | 🎵 **Celestial Star Unlock Soundscapes** | Play atmospheric chime/starfall sound effects upon unlocking a constellation star node. |
| **v1.3.5** | 🧩 **Public EbD Skill API** | Expose a clean API for third-party mod developers to register custom skill trees and constellation pages. |
| **v1.4.0** | 🛡️ **Blacksmith & Alchemy Skill Trees** | Optional expansion modules introducing dedicated *Blacksmith* (forging/tempering) and *Alchemist* (advanced brewing) constellations. |
| **v1.4.0** | 🌐 **In-Game Constellation Configurator** | Move the HTML visual editor into an integrated in-game GUI with real-time star repositioning. |
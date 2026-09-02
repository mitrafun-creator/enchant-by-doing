# Commands & Administration

**Enchant by Doing (EbD)** provides a robust set of in-game commands for server administrators, modpack creators, and map makers to inspect, modify, and grant skill progression or enchantment discoveries.

---

## 💻 Command Syntax & Aliases

All commands begin with `/ebd`.
* **Primary Command:** `/ebd`
* **Aliases:** `/enchantbydoing`, `/eso`
* **Required Permission Level:** **Level 2 (OP)** for modification commands; `/ebd skill <player> <skill> get` can be run with standard permissions or by server console.

---

## 📊 1. Skill Management Commands

### 🔧 Set Skill Level:
Sets a target player's specific skill directly to an exact level (1–100) and resets their current sub-level XP to 0.
```text
/ebd skill <player> <skill_id> set <level>
```
* **Example:** `/ebd skill Steve miner set 50`
* **Valid `skill_id` values:** `miner`, `warrior`, `hunter`, `lumberjack`, `farmer`, `enchanter`.

### ➕ Add Skill Levels:
Adds (or subtracts, using negative integers) levels to the player's current skill level.
```text
/ebd skill <player> <skill_id> add <amount>
```
* **Example:** `/ebd skill @p warrior add 5`

### 🔍 Query Current Skill & XP:
Outputs the target player's current level, current accumulated XP, and XP required for the next level.
```text
/ebd skill <player> <skill_id> get
```
* **Example:** `/ebd skill Alex hunter get`
* **Output:** `Alex's Hunter level is: 42 (XP: 1420.0/2800.0)`

---

## 🔮 2. Enchantment Knowledge Discovery Commands

### 📖 Discover Enchantment (`learn`):
Grants the player permanent knowledge to craft a specific enchantment (or all enchantments).
```text
/ebd learn <player> <enchantment_id|all>
```
* **Grant Specific Enchantment:**
  `/ebd learn @p minecraft:sharpness`
  `/ebd learn Steve ebd:fireproof_boat`
* **Grant All Known Enchantments:**
  `/ebd learn @p all`

---

### ❌ Revoke Enchantment (`forget`):
Removes the specified enchantment (or all enchantments) from the player's discovered knowledge.
```text
/ebd forget <player> <enchantment_id|all>
```
* **Revoke Specific Enchantment:**
  `/ebd forget @p minecraft:mending`
* **Revoke All Enchantments:**
  `/ebd forget @p all`

---

## 📜 3. Command Blocks & Automation

* All `/ebd` subcommands return standard integer success signals ($1$ on success, $0$ on error), making them fully compatible with **Command Blocks**, **Functions (`.mcfunction`)**, and **Questing Mods** (FTB Quests, Better Questing).
* Synchronization packets are dispatched automatically upon command execution, instantly updating client HUDs and open GUI menus without requiring player relog.
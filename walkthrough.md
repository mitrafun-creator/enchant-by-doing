# Walkthrough - Skill Level Commands, Enchantment Replacements, & Customizable XP

This walkthrough summarizes the design, implementation, and verification of the newly requested features.

## Changes Made

### 1. Customizable Experience Requirements Config
- Created [SkillsXPConfig.java](file:///c:/Users/lmrcr/Desktop/Новая%20папка/enchanting-system-overhaul-master/enchanting-system-overhaul-master/src/main/java/aiefu/eso/SkillsXPConfig.java) which loads skill experience requirements from `config/eso/skills_xp.yml`.
- Flat key format: `skill_id.level: xp_amount` (e.g. `miner.1: 100.0`, `miner.2: 150.0`).
- Generates a default file with levels 1 to 100 for all 6 skills on first launch.
- Modified `getXPNeededForLevel(String skillId, int level)` in [LBDConfig.java](file:///c:/Users/lmrcr/Desktop/Новая%20папка/enchanting-system-overhaul-master/enchanting-system-overhaul-master/src/main/java/aiefu/eso/LBDConfig.java) to load custom values before falling back to the default formula.
- Loaded the config in [ESOCommon.java](file:///c:/Users/lmrcr/Desktop/Новая%20папка/enchanting-system-overhaul-master/enchanting-system-overhaul-master/src/main/java/aiefu/eso/ESOCommon.java) on initialization.
- Updated player synchronization in [ESOCommon.java](file:///c:/Users/lmrcr/Desktop/Новая%20папка/enchanting-system-overhaul-master/enchanting-system-overhaul-master/src/main/java/aiefu/eso/ESOCommon.java) and [ServerPlayerMixins.java](file:///c:/Users/lmrcr/Desktop/Новая%20папка/enchanting-system-overhaul-master/enchanting-system-overhaul-master/src/main/java/aiefu/eso/mixin/ServerPlayerMixins.java) to check skill-specific needed XP.

### 2. Skill-Modification Commands
- Added subcommands under `/eso` in [ESOCommands.java](file:///c:/Users/lmrcr/Desktop/Новая%20папка/enchanting-system-overhaul-master/enchanting-system-overhaul-master/src/main/java/aiefu/eso/ESOCommands.java):
  - `/eso skill <player> <skill-id> set <level>`: Sets the target skill level and resets current level progress XP to 0.0.
  - `/eso skill <player> <skill-id> add <level>`: Adds or subtracts levels and resets current level progress XP.
  - `/eso skill <player> <skill-id> get`: Retrieves the current level and XP of the skill.
- Added Brigadier autocomplete suggestions for all 6 skill IDs (`miner`, `warrior`, `hunter`, `lumberjack`, `farmer`, `enchanter`).

### 3. Replacing Mutually Exclusive Enchantments
- **Enchanting Table**:
  - Modified [OverhauledEnchantmentMenu.java](file:///c:/Users/lmrcr/Desktop/Новая%20папка/enchanting-system-overhaul-master/enchanting-system-overhaul-master/src/main/java/aiefu/eso/menu/OverhauledEnchantmentMenu.java)'s `checkRequirementsAndConsume` to evaluate the limits based on a hypothetical state where any conflicting enchantments are removed.
  - Automatically discards conflicting enchantments when the target enchantment is successfully applied.
- **Anvil**:
  - Modified [AnvilMenuMixins.java](file:///c:/Users/lmrcr/Desktop/Новая%20папка/enchanting-system-overhaul-master/enchanting-system-overhaul-master/src/main/java/aiefu/eso/mixin/AnvilMenuMixins.java) using a `@Redirect` to force `Enchantment.areCompatible` to always return `true` inside `createResult()`. This allows vanilla to merge the items.
  - Implemented `resolveEnchantmentConflicts` inside `patchResultStack` to find conflicting enchantments in the final output and remove them, prioritizing incoming enchantments from the sacrifice slot.

## Verification Results

### Automated Verification
- Ran compile and build checks via Gradle:
  ```
  BUILD SUCCESSFUL in 8s
  ```
- The built jar was copied to the Modrinth mods folder successfully.

### 4. Constellation Integration
- Integrated custom JSON constellations from `eso_skills_layout.json` into the game.
- Implemented `loadConstellations()` in [SkillsTreeScreen.java](file:///c:/Users/lmrcr/Desktop/Новая%20папка/enchanting-system-overhaul-master/enchanting-system-overhaul-master/src/main/java/aiefu/eso/client/gui/SkillsTreeScreen.java) to load the JSON at runtime. Falls back to generating default starting stars if the layout file is missing.
- Dynamically calculates the unlock level for each star based on its order in the constellation:
  - First/Starting star: Unlocks at level 1.
  - Remaining stars: Distributed evenly across levels 1 to 100 using: `1 + index * (99 / (totalStars - 1))`.
- Automatically locks stars that are above the player's current skill level (renders them as dark gray nodes).
- Renders detailed tooltips for each star showing its name, unlock level, unlock status, and description.
- Draws connection lines using a custom `drawGlyphLine` helper which renders magical, trembling, and flowing Norse runic symbols that shimmer dynamically in the direction of the line.
- Renders all unlocked/upgraded skill nodes as pulsing green experience spheres/orbs.

### 5. Parallax Minimization & Scale Alignment
- Scaled the background nebula items in the game screen from `13.0f` to `16.0f` (exactly `256px`), matching the editor layout size pixel-for-pixel.
- Bound the background nebula items directly to the constellation's center coordinates (`1.0f` panning speed ratio) so that stars remain perfectly locked on top of their items when scrolling/panning.
- Retained a minimal mouse parallax of `0.01f` for immersive depth without drifting.

### 6. Исправление багов (бутылки, снаряды, книги, точильный камень)
- **Исчезновение пустых бутылок при питье зелий (Enchanter 75+):** Переписали логику `onUseItemFinish` в `ESOGameplayEvents.java` для работы на клиенте и сервере, добавив проверку на режим выживания. Если зелье пьётся из стака в 1 штуку, мы обнуляем результат через `setResultStack(ItemStack.EMPTY)`. Если из стака > 1 штуки, мы находим и удаляем один бутылёк из инвентаря.
- **Текстура снаряда бутылки:** Создали новый класс снаряда `ThrownBottleEntity` (наследующий `ThrowableItemProjectile`) с дефолтным предметом `Items.GLASS_BOTTLE`. Зарегистрировали его в `ESOCommon.java` и прописали рендер `ThrownItemRenderer` в `ESOClient.java`. Это позволило отображать летящую 3D-модель бутылки во время полёта и наносить урон при ударе без конфликтов со сторонними модами на частицы/физику.
- **Возврат книги при изучении чар (Enchanter 25+):** В `EnchantedBookMixins.java` исправили логику уменьшения стака. При обнулении стака книг теперь возвращается обычная книга (`stack = bookStack`), и метод возвращает `InteractionResultHolder.sidedSuccess`, благодаря чему Minecraft корректно обновляет инвентарь/руку игрока.
- **Пассивный навык 50 уровня на точильном камне:** В `ESOCommon.java` метод `collectNonCurseEnchantments` теперь считывает зачарования с помощью `EnchantmentHelper.getEnchantmentsForCrafting` вместо `stack.getEnchantments()`, что позволяет считывать чары в том числе с зачарованных книг, исправляя работу шанса 15% на сохранение чар при разэнчантировании любых предметов (включая книги).

### 7. Реализация пассивных навыков дровосека (Lumberjack)
- **Автопосадка саженцев (Уровень 20):** В `TreeChopCompat.java` подписались на событие `ChopEvent.AfterFellEvent`. При падении дерева автоматически находим саженец в инвентаре игрока (с помощью `ItemTags.SAPLINGS`) и сажаем его на самый нижний блок срубленного дерева (если блок пуст и условия блока подходят для выживания саженца).
- **Увеличение выпадения яблок (Уровень 40):** В `ESOGameplayEvents.java` добавили обработчик `BlockDropsEvent` для дубовой и тёмно-дубовой листвы. Если листва ломается рукой или топором, шанс выпадения яблока рассчитывается с учётом Fortune и умножается на 10 (максимально до 25%).
- **Увеличение скорости атаки (Уровень 60):** В `ESOGameplayEvents.java` внутри `onPlayerTick` динамически вешаем/снимаем с игрока временный модификатор на атрибут `Attributes.ATTACK_SPEED` (+25%), когда игрок удерживает топор в основной руке.
- **Мастер парного оружия (Уровень 80):** 
  - **Бесконечная прочность:** В `ItemStackMixin.java` перехватили вызовы `hurtAndBreak` и отменили урон прочности для топоров.
  - **Парная атака:** При ударе с топорами в обеих руках через 4 тика автоматически выполняется последовательный удар со второй руки. Исправили баг, из-за которого второй удар не наносил урон: теперь перед ударом сбрасывается время неуязвимости цели (`invulnerableTime = 0`, `hurtTime = 0`) и откатывается кулдаун атаки игрока (`resetAttackStrengthTicker()`).
  - **Скоростная рубка:** При добыче блоков с топорами в обеих руках скорость рубки дерева увеличивается на 50%. Добавили моментальное ломание листвы топором.
  - **Чередование рук:** Создали клиентский миксин [LocalPlayerMixin.java](file:///c:/Users/lmrcr/Desktop/Новая%20папка/enchanting-system-overhaul-master/enchanting-system-overhaul-master/src/main/java/aiefu/eso/mixin/LocalPlayerMixin.java) и зарегистрировали его под клиентом в `eso.mixins.json`. Это позволило перехватывать взмахи игрока на клиенте и чередовать руки при рубке блоков, корректно отсылая пакеты анимации на сервер для отображения другим игрокам без двойной альтернации.
- **Исправление автопосадки саженцев:** В [TreeChopCompat.java](file:///c:/Users/lmrcr/Desktop/Новая%20папка/enchanting-system-overhaul-master/enchanting-system-overhaul-master/src/main/java/aiefu/eso/compat/TreeChopCompat.java) завернули логику посадки в шедулер `sp.server.tell(new TickTask(...))` с задержкой в 1 тик, чтобы блоки дерева успели превратиться в воздух перед проверкой возможности выживания саженца. Также добавили x10 шанс выпадения яблок при падении листвы от TreeChop.

### 8. Реализация пассивных навыков стрелка (Hunter)
- **Экономия стрел (Уровень 25):** Создали [ProjectileWeaponItemMixin.java](file:///c:/Users/lmrcr/Desktop/Новая%20папка/enchanting-system-overhaul-master/enchanting-system-overhaul-master/src/main/java/aiefu/eso/mixin/ProjectileWeaponItemMixin.java). Перехватываем метод `useAmmo` и с шансом 25% (50% на уровне 75+) не тратим стрелу (она не удаляется из инвентаря, а выпущенный снаряд помечается как незабираемый для защиты от дюпа).
- **Скорость натягивания тетивы (Уровень 50):**
  - **Лук:** Создали [BowItemMixin.java](file:///c:/Users/lmrcr/Desktop/Новая%20папка/enchanting-system-overhaul-master/enchanting-system-overhaul-master/src/main/java/aiefu/eso/mixin/BowItemMixin.java), который изменяет `timeLeft` в методе `releaseUsing` для ускорения зарядки на 25% (на 50% на уровне 100).
  - **Арбалет:** В [CrossbowItemMixin.java](file:///c:/Users/lmrcr/Desktop/Новая%20папка/enchanting-system-overhaul-master/enchanting-system-overhaul-master/src/main/java/aiefu/eso/mixin/CrossbowItemMixin.java) в методе `getChargeDuration` делим базовое время зарядки на 1.25 (1.50 на уровне 100), ускоряя перезарядку.
- **Двойная длительность эффектов (Уровень 75):** Создали [ArrowMixin.java](file:///c:/Users/lmrcr/Desktop/Новая%20папка/enchanting-system-overhaul-master/enchanting-system-overhaul-master/src/main/java/aiefu/eso/mixin/ArrowMixin.java). Перехватываем наложение эффектов tipped-стрел на сущность в методе `doPostHurtEffects` и удваиваем длительность зелья через `MobEffectInstanceAccessor`.
- **Скорострельность и автострельба (Уровень 100):**
  - **Скорость снарядов:** В [ESOCommon.java](file:///c:/Users/lmrcr/Desktop/Новая%20папка/enchanting-system-overhaul-master/enchanting-system-overhaul-master/src/main/java/aiefu/eso/ESOCommon.java) в `onEntityJoinLevel` увеличиваем вектор скорости (delta movement) стрел и снарядов игрока на 50% (умножаем на 1.5).
  - **Автоматическая стрельба:** В [ESOClient.java](file:///c:/Users/lmrcr/Desktop/Новая%20папка/enchanting-system-overhaul-master/enchanting-system-overhaul-master/src/main/java/aiefu/eso/client/ESOClient.java) в событии `onClientTick` отслеживаем зажатие кнопки использования (правой кнопки мыши). Для лука — при полной зарядке автоматически отправляем пакет релиза и тут же начинаем новую зарядку. Для арбалета — при завершении перезарядки автоматически производим выстрел, не отжимая кнопку.

## Verification Results

### Automated Verification
- Успешно скомпилировали и собрали JAR с помощью Gradle:
  ```
  BUILD SUCCESSFUL in 8s
  ```
- Скомпилированный файл `enchanting_system_overhaul-1.2.2.jar` успешно скопирован в папку модов Modrinth: `C:\Users\lmrcr\AppData\Roaming\ModrinthApp\profiles\NeoForge 1.21.1\mods\`.

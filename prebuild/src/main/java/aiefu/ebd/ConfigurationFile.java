package aiefu.ebd;

public class ConfigurationFile {
    public int maxEnchantments;
    public boolean enableEnchantability;
    public boolean enableDefaultRecipe;
    public boolean disableDiscoverySystem;
    public boolean enableEnchantmentsLeveling;
    public int maxEnchantmentsOnLootBooks;
    public int maxEnchantmentsOnLootItems;
    public boolean enableCursesAmplifier;
    public int maxCurses;
    public int enchantmentLimitIncreasePerCurse;

    public boolean hideEnchantmentsWithoutRecipe;

    public boolean disableAnvilEnchanting;

    public boolean disableBookCombining;

    public boolean enableQuicksand;
    public boolean enableDisarm;
    public boolean enableSting;
    public boolean enableExecutioner;
    public boolean enableLeviathan;
    public boolean enableDragonsBreath;
    public boolean enableMagazine;
    public boolean enableAutoReload;

    // Boat enchantments
    public boolean enableSlipway;
    public boolean enableSeafarer;
    public boolean enableFireproofBoat;
    public boolean enableAutodrive;
    public boolean enableAeroflot;
    public boolean enablePocketBoat;
    public boolean enableSubmarine;

    // Block enchantments
    public boolean enableCapacity;

    public ConfigurationFile() {
    }

    public ConfigurationFile(int maxEnchantments, boolean enableEnchantability, boolean enableDefaultRecipe,
                             boolean disableDiscoverySystem, boolean enableEnchantmentsLeveling,
                             int maxEnchantmentsOnLootBooks, int maxEnchantmentsOnLootItems,
                             boolean enableCursesAmplifier, int maxCurses, int enchantmentLimitIncreasePerCurse,
                             boolean hideEnchantmentsWithoutRecipe, boolean disableAnvilEnchanting,
                             boolean disableBookCombining, boolean enableQuicksand, boolean enableDisarm,
                             boolean enableSting, boolean enableExecutioner, boolean enableLeviathan,
                             boolean enableDragonsBreath, boolean enableMagazine, boolean enableAutoReload,
                             boolean enableSlipway, boolean enableSeafarer, boolean enableFireproofBoat,
                             boolean enableAutodrive, boolean enableAeroflot, boolean enablePocketBoat,
                             boolean enableSubmarine, boolean enableCapacity) {
        this.maxEnchantments = maxEnchantments;
        this.enableEnchantability = enableEnchantability;
        this.enableDefaultRecipe = enableDefaultRecipe;
        this.disableDiscoverySystem = disableDiscoverySystem;
        this.enableEnchantmentsLeveling = enableEnchantmentsLeveling;
        this.maxEnchantmentsOnLootBooks = maxEnchantmentsOnLootBooks;
        this.maxEnchantmentsOnLootItems = maxEnchantmentsOnLootItems;
        this.enableCursesAmplifier = enableCursesAmplifier;
        this.maxCurses = maxCurses;
        this.enchantmentLimitIncreasePerCurse = enchantmentLimitIncreasePerCurse;
        this.hideEnchantmentsWithoutRecipe = hideEnchantmentsWithoutRecipe;
        this.disableAnvilEnchanting = disableAnvilEnchanting;
        this.disableBookCombining = disableBookCombining;
        this.enableQuicksand = enableQuicksand;
        this.enableDisarm = enableDisarm;
        this.enableSting = enableSting;
        this.enableExecutioner = enableExecutioner;
        this.enableLeviathan = enableLeviathan;
        this.enableDragonsBreath = enableDragonsBreath;
        this.enableMagazine = enableMagazine;
        this.enableAutoReload = enableAutoReload;
        this.enableSlipway = enableSlipway;
        this.enableSeafarer = enableSeafarer;
        this.enableFireproofBoat = enableFireproofBoat;
        this.enableAutodrive = enableAutodrive;
        this.enableAeroflot = enableAeroflot;
        this.enablePocketBoat = enablePocketBoat;
        this.enableSubmarine = enableSubmarine;
        this.enableCapacity = enableCapacity;
    }

    public static ConfigurationFile getDefault() {
        return new ConfigurationFile(
            4, true, true, false, false, 10, 3, true, 1, 1,
            false, false, false,
            true, true, true, true, true, true, true, true,
            // boat enchants
            true, true, true, true, true, true, true,
            // block enchants
            true
        );
    }
}

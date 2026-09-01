package aiefu.ebd;

public enum SkillType {
    MINER("miner", "Шахтер", 0x708090),       // Slate Gray
    WARRIOR("warrior", "Воин", 0xB71C1C),     // Dark Red
    HUNTER("hunter", "Охотник", 0x2E7D32),    // Forest Green
    LUMBERJACK("lumberjack", "Дровосек", 0x8B4513), // Saddle Brown
    FARMER("farmer", "Фермер", 0xF57F17),     // Golden/Orange
    ENCHANTER("enchanter", "Зачарователь", 0x6A1B9A); // Purple

    public final String id;
    public final String displayName;
    public final int color;

    SkillType(String id, String displayName, int color) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
    }

    public static SkillType fromId(String id) {
        for (SkillType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}

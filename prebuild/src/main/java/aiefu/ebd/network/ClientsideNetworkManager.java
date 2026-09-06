package aiefu.ebd.network;

import aiefu.ebd.ConfigurationFile;
import aiefu.ebd.EBDCommon;
import aiefu.ebd.data.RecipeData;
import aiefu.ebd.data.RecipeHolder;
import aiefu.ebd.data.itemdata.ItemData;
import aiefu.ebd.data.materialoverrides.MaterialData;
import aiefu.ebd.data.materialoverrides.MaterialOverrides;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import aiefu.ebd.IBoatEnchanted;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.nbt.NbtOps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ClientsideNetworkManager {

    public static byte clientNearbyWorkstationsMask = 0;
    public static byte clientRequiredWorkstationsMask = 0;
    public static byte clientMissingWorkstationsMask = 0;

    public static void handleWorkstationStatus(S2CWorkstationStatusPayload payload) {
        clientNearbyWorkstationsMask = payload.nearbyMask();
        clientRequiredWorkstationsMask = payload.requiredMask();
        clientMissingWorkstationsMask = (byte) (clientRequiredWorkstationsMask & ~clientNearbyWorkstationsMask);
    }

    public static void reset() {
        clientNearbyWorkstationsMask = 0;
        clientRequiredWorkstationsMask = 0;
        clientMissingWorkstationsMask = 0;
    }

    public static final java.util.Map<Integer, CompoundTag> PENDING_BOAT_ENCHANTMENTS = new java.util.concurrent.ConcurrentHashMap<>();

    public static void handleBoatSync(S2CBoatSyncPayload payload) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            if (client.level != null) {
                net.minecraft.world.entity.Entity entity = client.level.getEntity(payload.entityId());
                if (entity instanceof Boat boat && boat instanceof IBoatEnchanted enchanted) {
                    applyEnchantmentsFromTag(boat, payload.tag());
                } else {
                    PENDING_BOAT_ENCHANTMENTS.put(payload.entityId(), payload.tag());
                }
            } else {
                PENDING_BOAT_ENCHANTMENTS.put(payload.entityId(), payload.tag());
            }
        });
    }

    public static void applyPendingEnchantments(Boat boat) {
        CompoundTag tag = PENDING_BOAT_ENCHANTMENTS.remove(boat.getId());
        if (tag != null) {
            applyEnchantmentsFromTag(boat, tag);
        }
    }

    private static void applyEnchantmentsFromTag(Boat boat, CompoundTag tag) {
        if (boat instanceof IBoatEnchanted enchanted) {
            if (tag != null && tag.contains("ESOEnch")) {
                net.minecraft.resources.RegistryOps<net.minecraft.nbt.Tag> ops =
                        net.minecraft.resources.RegistryOps.create(NbtOps.INSTANCE, boat.level().registryAccess());
                ItemEnchantments.CODEC.parse(ops, tag.get("ESOEnch"))
                        .result()
                        .ifPresentOrElse(
                                enchanted::ebd$setBoatEnchantments,
                                () -> enchanted.ebd$setBoatEnchantments(ItemEnchantments.EMPTY)
                        );
            } else {
                enchanted.ebd$setBoatEnchantments(ItemEnchantments.EMPTY);
            }
        }
    }


    public static void handleStringToClipboard(S2CStringToClipboardPayload payload) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            if (client.keyboardHandler != null) {
                client.keyboardHandler.setClipboard(payload.text());
            }
        });
    }

    public static void handleMatConfigSync(S2CMatConfigSyncPayload payload) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.bytes()));
        Minecraft client = Minecraft.getInstance();
        readMatConfigData(buf, client);
    }

    public static void handleDataSync(S2CDataSyncPayload payload) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.bytes()));
        Minecraft client = Minecraft.getInstance();
        readRecipes(buf, client);
    }

    public static void handleSyncConfig(S2CSyncConfigPayload payload) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.bytes()));
        Minecraft client = Minecraft.getInstance();
        readConfig(buf, client);
    }

    public static void handleSkillUpdate(S2CSkillUpdatePayload payload) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            aiefu.ebd.client.SkillHUDRenderer.updateSkill(payload.skillId(), payload.level(), payload.xp(), payload.neededXp());
        });
    }

    private static void readMatConfigData(FriendlyByteBuf buf, Minecraft client){
        int ts = buf.readVarInt();
        HashMap<String, MaterialData> tools = new HashMap<>();
        for (int i = 0; i < ts; i++) {
            String id = buf.readUtf();
            MaterialData data = new MaterialData(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
            tools.put(id, data);
        }
        int as = buf.readVarInt();
        HashMap<String, MaterialData> armor = new HashMap<>();
        for (int i = 0; i < as; i++) {
            String id = buf.readUtf();
            MaterialData data = new MaterialData(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
            armor.put(id, data);
        }
        int hs = buf.readVarInt();
        HashMap<String, MaterialData> items = new HashMap<>();
        for (int i = 0; i < hs; i++) {
            String id = buf.readUtf();
            MaterialData data = new MaterialData(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
            items.put(id, data);
        }
        client.execute(() -> EBDCommon.mat_config = MaterialOverrides.reconstructFromPacket(tools, armor, items));
    }

    private static void readRecipes(FriendlyByteBuf buf, Minecraft client){
        Interner<String> interner = Interners.newWeakInterner();
        String n = "null";
        interner.intern(n);

        int i = buf.readVarInt();
        ConcurrentHashMap<ResourceLocation, List<RecipeData>> map = new ConcurrentHashMap<>();
        for (int j = 0; j < i; j++) {
            String s = buf.readUtf();
            ResourceLocation loc = ResourceLocation.parse(s);
            List<RecipeData> holders = new ArrayList<>();
            int jk = buf.readVarInt();
            for (int b = 0; b < jk; b++) {
                String eid = buf.readUtf();
                int maxLevel = buf.readVarInt();
                boolean xpMode = buf.readBoolean();
                int r = buf.readVarInt();
                Int2ObjectOpenHashMap<ItemData[]> int2ObjMap = new Int2ObjectOpenHashMap<>();
                for (int k = 0; k < r; k++) {
                    int level = buf.readVarInt();
                    int q = buf.readVarInt();
                    ItemData[] arr = new ItemData[q];
                    for (int l = 0; l < q; l++) {
                        String id = interner.intern(buf.readUtf());
                        id = n == id ? null : id;
                        boolean bl = buf.readBoolean();
                        ItemData[] idsArr = null;
                        if(bl){
                            int lk = buf.readVarInt();
                            idsArr = new ItemData[lk];
                            for (int m = 0; m < lk; m++) {
                                String itemId = buf.readUtf();
                                int itemAmount = buf.readVarInt();
                                String tag = interner.intern(buf.readUtf());
                                tag = tag == n ? null : tag;
                                String rid = interner.intern(buf.readUtf());
                                rid = rid == n ? null : rid;
                                int ridAmount = buf.readVarInt();
                                String rtag = interner.intern(buf.readUtf());
                                rtag = rtag == n ? null : rtag;
                                idsArr[m] = new ItemData(itemId, itemAmount, tag, rid, ridAmount, rtag);
                            }
                        }
                        int amount = buf.readVarInt();
                        String tag = interner.intern(buf.readUtf());
                        tag = n == tag ? null : tag;
                        String remainder = interner.intern(buf.readUtf());
                        remainder = n == remainder ? null : remainder;
                        int remainderAmount = buf.readVarInt();
                        String remainderTag = interner.intern(buf.readUtf());
                        remainderTag = n == remainderTag ? null : remainderTag;
                        arr[l] = new ItemData(id, tag, idsArr, amount, remainder, remainderAmount, remainderTag);
                    }
                    int2ObjMap.put(level, arr);
                }
                Int2IntOpenHashMap xpMap = new Int2IntOpenHashMap();
                int z = buf.readVarInt();
                for (int k = 0; k < z ; k++) {
                    xpMap.put(buf.readVarInt(), buf.readVarInt());
                }
                RecipeData holder = new RecipeData(eid, maxLevel, xpMode, int2ObjMap, xpMap);
                holders.add(holder);
            }
            map.put(loc, holders);
        }
        client.execute(() -> {
            ConcurrentHashMap<ResourceLocation, List<RecipeHolder>> holdersMap = new ConcurrentHashMap<>();
            map.forEach((k, v) -> {
                List<RecipeHolder> holders = new ArrayList<>();
                v.forEach(data -> {
                    RecipeHolder holder = data.getRecipeHolder();
                    holder.processTags();
                    holders.add(holder);
                });
                holdersMap.put(k, holders);
            });
            EBDCommon.recipeMap = holdersMap;
        });
    }

    private static void readConfig(FriendlyByteBuf buf, Minecraft client){
        ConfigurationFile file = new ConfigurationFile(buf.readVarInt(), buf.readBoolean(), buf.readBoolean(),
                buf.readBoolean(), buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(),
                buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                // boat enchantments
                buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                // block enchantments
                buf.readBoolean());
        client.execute(() -> EBDCommon.config = file);
    }
}

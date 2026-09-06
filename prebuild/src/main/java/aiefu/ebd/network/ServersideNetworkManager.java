package aiefu.ebd.network;

import aiefu.ebd.ConfigurationFile;
import aiefu.ebd.EBDCommon;
import aiefu.ebd.data.RecipeHolder;
import aiefu.ebd.data.itemdata.ItemData;
import aiefu.ebd.data.itemdata.ItemDataPrepared;
import aiefu.ebd.data.materialoverrides.MaterialData;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServersideNetworkManager {

    public static void syncMatConfig(ServerPlayer player){
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        HashMap<Item, MaterialData> tools = EBDCommon.mat_config.toolsMatOverridesCompiled;
        buf.writeVarInt(tools.size());
        tools.forEach((k, v) -> {
            String loc = BuiltInRegistries.ITEM.getKey(k).toString();
            buf.writeUtf(loc);
            buf.writeVarInt(v.getMaxEnchantments());
            buf.writeVarInt(v.getMaxCurses());
            buf.writeVarInt(v.getCurseMultiplier());
        });
        HashMap<Item, MaterialData> armor = EBDCommon.mat_config.armorMatOverridesCompiled;
        buf.writeVarInt(armor.size());
        armor.forEach((k, v) -> {
            String loc = BuiltInRegistries.ITEM.getKey(k).toString();
            buf.writeUtf(loc);
            buf.writeVarInt(v.getMaxEnchantments());
            buf.writeVarInt(v.getMaxCurses());
            buf.writeVarInt(v.getCurseMultiplier());
        });
        HashMap<Item, MaterialData> items = EBDCommon.mat_config.hardOverridesCompiled;
        buf.writeVarInt(items.size());
        items.forEach((k, v) -> {
            String loc = BuiltInRegistries.ITEM.getKey(k).toString();
            buf.writeUtf(loc);
            buf.writeVarInt(v.getMaxEnchantments());
            buf.writeVarInt(v.getMaxCurses());
            buf.writeVarInt(v.getCurseMultiplier());
        });
        
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        PacketDistributor.sendToPlayer(player, new S2CMatConfigSyncPayload(bytes));
    }

    public static void syncData(ServerPlayer player){
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        String n = "null";

        buf.writeVarInt(EBDCommon.recipeMap.size());
        for (Map.Entry<ResourceLocation, List<RecipeHolder>> e : EBDCommon.recipeMap.entrySet()){
            buf.writeUtf(e.getKey().toString());
            List<RecipeHolder> holders = e.getValue();
            buf.writeVarInt(holders.size());
            for (RecipeHolder holder : holders){
                buf.writeUtf(holder.enchantment_id);
                buf.writeVarInt(holder.maxLevel);
                buf.writeBoolean(holder.mode);

                buf.writeVarInt(holder.levels.size());
                for (Int2ObjectMap.Entry<ItemDataPrepared[]> entry : holder.levels.int2ObjectEntrySet()){
                    buf.writeVarInt(entry.getIntKey());
                    ItemDataPrepared[] arr = entry.getValue();
                    buf.writeVarInt(arr.length);
                    for (ItemDataPrepared data : arr){
                        String id = data.data.id == null ? n : data.data.id;
                        buf.writeUtf(id);
                        if(data.data.itemArray != null){
                            buf.writeBoolean(true);
                            buf.writeVarInt(data.data.itemArray.length);
                            for (ItemData ds : data.data.itemArray){
                                buf.writeUtf(ds.id);
                                buf.writeVarInt(ds.amount);
                                String tag = ds.tag == null ? n : ds.tag;
                                buf.writeUtf(tag);
                                String rid = ds.remainderId == null ? n : ds.remainderId;
                                buf.writeUtf(rid);
                                buf.writeVarInt(ds.remainderAmount);
                                String rtag = ds.remainderTag == null ? n : ds.remainderTag;
                                buf.writeUtf(rtag);
                            }
                        } else buf.writeBoolean(false);

                        buf.writeVarInt(data.amount);
                        String tag = data.data.tag == null ? n : data.data.tag;
                        buf.writeUtf(tag);
                        String remainder = data.data.remainderId == null ? n : data.data.remainderId;
                        buf.writeUtf(remainder);
                        buf.writeVarInt(data.remainderAmount);
                        String remainderTag = data.data.remainderTag == null ? n : data.data.remainderTag;
                        buf.writeUtf(remainderTag);
                    }
                }
                buf.writeVarInt(holder.xpMap.size());
                for (Int2IntMap.Entry entry : holder.xpMap.int2IntEntrySet()){
                    buf.writeVarInt(entry.getIntKey());
                    buf.writeVarInt(entry.getIntValue());
                }
            }
        }
        
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        PacketDistributor.sendToPlayer(player, new S2CDataSyncPayload(bytes));
    }

    public static void syncConfig(ServerPlayer player){
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        ConfigurationFile cfg = EBDCommon.config;

        buf.writeVarInt(cfg.maxEnchantments);
        buf.writeBoolean(cfg.enableEnchantability);
        buf.writeBoolean(cfg.enableDefaultRecipe);
        buf.writeBoolean(cfg.disableDiscoverySystem);
        buf.writeBoolean(cfg.enableEnchantmentsLeveling);
        buf.writeVarInt(cfg.maxEnchantmentsOnLootBooks);
        buf.writeVarInt(cfg.maxEnchantmentsOnLootItems);
        buf.writeBoolean(cfg.enableCursesAmplifier);
        buf.writeVarInt(cfg.maxCurses);
        buf.writeVarInt(cfg.enchantmentLimitIncreasePerCurse);
        buf.writeBoolean(cfg.hideEnchantmentsWithoutRecipe);
        buf.writeBoolean(cfg.disableAnvilEnchanting);
        buf.writeBoolean(cfg.disableBookCombining);
        buf.writeBoolean(cfg.enableQuicksand);
        buf.writeBoolean(cfg.enableDisarm);
        buf.writeBoolean(cfg.enableSting);
        buf.writeBoolean(cfg.enableExecutioner);
        buf.writeBoolean(cfg.enableLeviathan);
        buf.writeBoolean(cfg.enableDragonsBreath);
        buf.writeBoolean(cfg.enableMagazine);
        buf.writeBoolean(cfg.enableAutoReload);
        buf.writeBoolean(cfg.enableSlipway);
        buf.writeBoolean(cfg.enableSeafarer);
        buf.writeBoolean(cfg.enableFireproofBoat);
        buf.writeBoolean(cfg.enableAutodrive);
        buf.writeBoolean(cfg.enableAeroflot);
        buf.writeBoolean(cfg.enablePocketBoat);
        buf.writeBoolean(cfg.enableSubmarine);
        buf.writeBoolean(cfg.enableCapacity);

        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        PacketDistributor.sendToPlayer(player, new S2CSyncConfigPayload(bytes));
    }

    public static void sendSkillUpdate(ServerPlayer player, String skillId, int level, double xp, double neededXp, double xpGained) {
        PacketDistributor.sendToPlayer(player, new S2CSkillUpdatePayload(skillId, level, xp, neededXp, xpGained));
    }

    public static void sendGlobalSync(ServerPlayer player) {
        if (player instanceof aiefu.ebd.IServerPlayerAcc acc) {
            int level = acc.ebd$getGlobalLevel();
            double xp = acc.ebd$getGlobalXP();
            double needed = aiefu.ebd.GlobalPerks.getNeededXPForLevel(level);
            int points = acc.ebd$getSkillPoints();
            java.util.Map<String, Integer> perks = acc.ebd$getAllPerks();
            PacketDistributor.sendToPlayer(player, new S2CGlobalDataSyncPayload(level, xp, needed, points, perks));
        }
    }
}

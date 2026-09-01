package aiefu.ebd.data;

import aiefu.ebd.EBDCommon;
import aiefu.ebd.data.materialoverrides.MaterialData;
import aiefu.ebd.data.materialoverrides.MaterialOverrides;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MaterialDataReloadListener extends SimplePreparableReloadListener<MaterialOverrides> {

    @Override
    protected MaterialOverrides prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, Resource> data = manager.listResources("mat-overrides", loc -> loc.getPath().endsWith(".json"));
        HashMap<String, MaterialData> armor = new HashMap<>();
        HashMap<String, MaterialData> tools = new HashMap<>();
        HashMap<String, MaterialData> items = new HashMap<>();
        data.forEach((l, r) -> {
            try {
                JsonElement jsonTree = JsonParser.parseReader(r.openAsReader());
                JsonObject obj = jsonTree.getAsJsonObject();
                String id = obj.get("material_id").getAsString();
                String type = obj.get("type").getAsString();
                MaterialData md = EBDCommon.getGson().fromJson(jsonTree, MaterialData.class);
                if(type.equalsIgnoreCase("armor")){
                    armor.put(id, md);
                } else if(type.equalsIgnoreCase("tool")){
                    tools.put(id, md);
                } else if(type.equalsIgnoreCase("item")){
                    items.put(id, md);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            return MaterialOverrides.readWithAttachments(tools, armor, items);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void apply(MaterialOverrides data, ResourceManager manager, ProfilerFiller profiler) {
        EBDCommon.mat_config = data;
    }
}

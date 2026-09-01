package aiefu.ebd.data;

import aiefu.ebd.EBDCommon;
import aiefu.ebd.mixin.IClientLanguageAcc;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageReloadListener implements ResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        if(Language.getInstance() instanceof IClientLanguageAcc lacc){
            Map<String, String> lmap = lacc.getLanguageMap();
            HashMap<String, String> languageMap = new HashMap<>();
            
            // 1. Load English default first (fallback)
            List<Resource> enResources = resourceManager.getResourceStack(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "ench-desc/en_us_ench_desc.json"));
            for (Resource resource : enResources){
                try {
                    languageMap.putAll(EBDCommon.getGson().fromJson(resource.openAsReader(), new TypeToken<HashMap<String, String>>(){}.getType()));
                } catch (Exception ignored) {}
            }
            
            // 2. Load selected language if it's not English
            String selectedLang = Minecraft.getInstance().getLanguageManager().getSelected();
            if (!selectedLang.equals("en_us")) {
                List<Resource> resources = resourceManager.getResourceStack(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "ench-desc/" + selectedLang + "_ench_desc.json"));
                for (Resource resource : resources){
                    try {
                        languageMap.putAll(EBDCommon.getGson().fromJson(resource.openAsReader(), new TypeToken<HashMap<String, String>>(){}.getType()));
                    } catch (Exception ignored) {}
                }
            }
            
            languageMap.putAll(lmap);
            lacc.setLanguageMap(languageMap);
        }
    }
}

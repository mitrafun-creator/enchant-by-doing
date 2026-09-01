package aiefu.ebd.data;

import aiefu.ebd.EBDCommon;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class EnchantmentRecipesReloadListener extends SimplePreparableReloadListener<EnchantmentRecipesReloadListener.RecipesContainer> {

    @Override
    protected RecipesContainer prepare(ResourceManager manager, ProfilerFiller profiler) {
        return new RecipesContainer(
            manager.listResources("ench-recipes", loc -> loc.getPath().endsWith(".json")),
            manager.listResources("ench-recipes/fallbacks", loc -> loc.getPath().endsWith(".json"))
        );
    }

    @Override
    protected void apply(RecipesContainer container, ResourceManager manager, ProfilerFiller profiler) {
        EBDCommon.recipeMap.clear();
        container.recipes.forEach((key, value) -> {
            try {
                RecipeHolder holder = RecipeHolder.deserialize(JsonParser.parseReader(value.openAsReader()).getAsJsonObject(), key, container.fallbacks);
                if(holder != null){
                    holder.register();
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        try {
            RecipeHolder defaultHolder = RecipeHolder.deserializeDefaultRecipe(ResourceLocation.parse("config/eso/default-recipe.json"));
            defaultHolder.register();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public record RecipesContainer(Map<ResourceLocation, Resource> recipes, Map<ResourceLocation, Resource> fallbacks){
    }
}

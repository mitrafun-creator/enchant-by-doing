package aiefu.ebd.data;

import aiefu.ebd.EBDCommon;
import aiefu.ebd.client.EBDClient;
import aiefu.ebd.data.client.BackgroundColorData;
import aiefu.ebd.data.client.ColorDataHolder;
import aiefu.ebd.data.client.SliderColorData;
import aiefu.ebd.data.client.TextSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.IOException;
import java.util.Optional;

public class ColorsDataReloadListener extends SimplePreparableReloadListener<ColorDataHolder> {

    @Override
    protected ColorDataHolder prepare(ResourceManager manager, ProfilerFiller profiler) {
        Optional<Resource> background = manager.getResource(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "color-settings/background-color.json"));
        Optional<Resource> slider = manager.getResource(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "color-settings/slider-colors.json"));
        Optional<Resource> textColor = manager.getResource(ResourceLocation.fromNamespaceAndPath(EBDCommon.MOD_ID, "color-settings/text-colors.json"));
        try {
            BackgroundColorData bcd = background.isPresent() ? EBDCommon.getGson().fromJson(background.get().openAsReader(), BackgroundColorData.class) : BackgroundColorData.getDefault();
            SliderColorData scd = slider.isPresent() ? EBDCommon.getGson().fromJson(slider.get().openAsReader(), SliderColorData.class) : SliderColorData.getDefault();
            TextSettings ts = textColor.isPresent() ? EBDCommon.getGson().fromJson(textColor.get().openAsReader(), TextSettings.class) : TextSettings.getDefault();
            return new ColorDataHolder(bcd, scd, ts);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ColorDataHolder(BackgroundColorData.getDefault(), SliderColorData.getDefault(), TextSettings.getDefault());
    }

    @Override
    protected void apply(ColorDataHolder data, ResourceManager manager, ProfilerFiller profiler) {
        EBDClient.colorData = data;
    }
}

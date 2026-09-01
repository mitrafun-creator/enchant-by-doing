package aiefu.ebd;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.HashSet;
import java.util.Set;

public class PlacedMelonsTracker extends SavedData {
    private final Set<BlockPos> placedMelons = new HashSet<>();

    public PlacedMelonsTracker() {}

    public static PlacedMelonsTracker load(CompoundTag nbt, HolderLookup.Provider registries) {
        PlacedMelonsTracker tracker = new PlacedMelonsTracker();
        if (nbt.contains("Melons", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("Melons", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag posTag = list.getCompound(i);
                tracker.placedMelons.add(new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z")));
            }
        }
        return tracker;
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (BlockPos pos : placedMelons) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            list.add(posTag);
        }
        nbt.put("Melons", list);
        return nbt;
    }

    public boolean isPlaced(BlockPos pos) {
        return placedMelons.contains(pos);
    }

    public void addMelon(BlockPos pos) {
        placedMelons.add(pos);
        setDirty();
    }

    public void removeMelon(BlockPos pos) {
        placedMelons.remove(pos);
        setDirty();
    }

    public static final SavedData.Factory<PlacedMelonsTracker> FACTORY = new SavedData.Factory<>(
        PlacedMelonsTracker::new,
        PlacedMelonsTracker::load,
        net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    public static PlacedMelonsTracker get(net.minecraft.server.level.ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, "eso_placed_melons");
    }
}

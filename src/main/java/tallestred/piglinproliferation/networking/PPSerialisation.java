package tallestred.piglinproliferation.networking;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PPSerialisation {
    public static <T> CompoundTag writeTagMapToNBT(Map<TagKey<T>, Integer> map) {
        CompoundTag output = new CompoundTag();
        map.forEach((key, value) -> output.put(key.location().toString(), IntTag.valueOf(value)));
        return output;
    }

    public static <T> ConcurrentMap<TagKey<T>, Integer> readTagMapFromNBT(ResourceKey<? extends Registry<T>> tagRegistry, Tag nbt) {
        if (nbt instanceof CompoundTag compoundTag) {
            ConcurrentMap<TagKey<T>, Integer> output = new ConcurrentHashMap<>();
            compoundTag.getAllKeys().forEach(key -> output.put(TagKey.create(tagRegistry,new ResourceLocation(key)), compoundTag.getInt(key)));
            return output;
        }
        return null;
    }
}

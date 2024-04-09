package tallestred.piglinproliferation.common.tags;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.function.Supplier;

public record WhitelistOrBlacklistTag<T>(Supplier<Boolean> shouldUseWhitelist, TagKey<T> whitelist, TagKey<T> blacklist) {
    public WhitelistOrBlacklistTag(Supplier<Boolean> shouldUseWhitelist, ResourceKey<Registry<T>> registry, String modid, String tagPrefix) {
        this(shouldUseWhitelist, TagKey.create(registry, new ResourceLocation(modid, tagPrefix + "_whitelist")), TagKey.create(registry, new ResourceLocation(modid, tagPrefix + "_blacklist")));
    }

    public TagKey<T> tag() {
        return shouldUseWhitelist.get() ? whitelist : blacklist;
    }

    public boolean contains(Holder<T> holder) {
        return shouldUseWhitelist.get() ? holder.is(whitelist) : !holder.is(blacklist);
    }
}
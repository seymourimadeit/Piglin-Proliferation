package tallestred.piglinproliferation.common.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import tallestred.piglinproliferation.PiglinProliferation;

public class PPTags {
    public static final TagKey<Structure> TRAVELER_CAMPS = TagKey.create(Registries.STRUCTURE, new ResourceLocation(PiglinProliferation.MODID, "traveler_camps"));
    public static final EitherTag<Biome, Structure> TRAVELERS_COMPASS_SEARCH = new EitherTag<>(Registries.BIOME, Registries.STRUCTURE, new ResourceLocation(PiglinProliferation.MODID, "travelers_compass_search"));
}
package tallestred.piglinproliferation.common.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.configuration.PPConfig;

public class PPTags {
    public static final TagKey<Structure> TRAVELLER_BASES = TagKey.create(Registries.STRUCTURE, new ResourceLocation(PiglinProliferation.MODID, "traveller_camps"));
    public static final EitherTag<Biome, Structure> TRAVELLERS_COMPASS_SEARCH = new EitherTag<>(Registries.BIOME, Registries.STRUCTURE, new ResourceLocation(PiglinProliferation.MODID, "travellers_compass_search"));
}
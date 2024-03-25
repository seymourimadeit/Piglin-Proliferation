package tallestred.piglinproliferation.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import tallestred.piglinproliferation.PiglinProliferation;

public class PPTags {
    public static final TagKey<Biome> TRAVELLERS_COMPASS_BIOME_WHITELIST = TagKey.create(Registries.BIOME, new ResourceLocation(PiglinProliferation.MODID, "travellers_compass_whitelist"));
    public static final TagKey<Biome> TRAVELLERS_COMPASS_BIOME_BLACKLIST = TagKey.create(Registries.BIOME, new ResourceLocation(PiglinProliferation.MODID, "travellers_compass_blacklist"));


    public static final TagKey<Structure> TRAVELLER_BASES = TagKey.create(Registries.STRUCTURE, new ResourceLocation(PiglinProliferation.MODID, "traveller_bases"));
    public static final TagKey<Structure> TRAVELLERS_COMPASS_STRUCTURE_WHITELIST = TagKey.create(Registries.STRUCTURE, new ResourceLocation(PiglinProliferation.MODID, "travellers_compass_whitelist"));
    public static final TagKey<Structure> TRAVELLERS_COMPASS_STRUCTURE_BLACKLIST = TagKey.create(Registries.STRUCTURE, new ResourceLocation(PiglinProliferation.MODID, "travellers_compass_blacklist"));

    public static final TagKey<StructureSet> TRAVELLERS_COMPASS_STRUCTURE_SET_WHITELIST = TagKey.create(Registries.STRUCTURE_SET, new ResourceLocation(PiglinProliferation.MODID, "travellers_compass_whitelist"));
    public static final TagKey<StructureSet> TRAVELLERS_COMPASS_STRUCTURE_SET_BLACKLIST = TagKey.create(Registries.STRUCTURE_SET, new ResourceLocation(PiglinProliferation.MODID, "travellers_compass_blacklist"));

}

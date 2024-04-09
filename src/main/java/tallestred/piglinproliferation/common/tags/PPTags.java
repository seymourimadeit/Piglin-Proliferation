package tallestred.piglinproliferation.common.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.configuration.PPConfig;

public class PPTags {
    public static final TagKey<Structure> TRAVELLER_BASES = TagKey.create(Registries.STRUCTURE, new ResourceLocation(PiglinProliferation.MODID, "traveller_bases"));
    public static final WhitelistOrBlacklistTag<Biome> TRAVELLERS_COMPASS_VALID_BIOMES = new WhitelistOrBlacklistTag<>(PPConfig.COMMON.travellersCompassBiomeWhitelist, Registries.BIOME, PiglinProliferation.MODID, "travellers_compass");
    public static final StructureWhitelistTagsHolder TRAVELLERS_COMPASS_VALID_STRUCTURES = new StructureWhitelistTagsHolder(PPConfig.COMMON.travellersCompassStructureWhitelist, PiglinProliferation.MODID, "travellers_compass");
}

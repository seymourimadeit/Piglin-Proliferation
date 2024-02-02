package tallestred.piglinproliferation.common.entities;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

public interface TravellersCompassBarterer {
    void setBiomeAlreadyRolled(TagKey<Biome> biomeTag);
    boolean hasBiomeAlreadyRolled(TagKey<Biome> biomeTag);
    void setStructureAlreadyRolled(TagKey<Structure> structureTag);
    boolean hasStructureAlreadyRolled(TagKey<Structure> structureTag);
}

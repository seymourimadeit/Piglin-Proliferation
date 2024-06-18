package tallestred.piglinproliferation.common.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import tallestred.piglinproliferation.PiglinProliferation;

public class PPTags {
    public static final TagKey<Structure> TRAVELER_CAMPS = TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(PiglinProliferation.MODID, "traveler_camps"));
    public static final EitherTag<Biome, Structure> TRAVELERS_COMPASS_SEARCH = new EitherTag<>(Registries.BIOME, Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(PiglinProliferation.MODID, "travelers_compass_search"));
    public static final TagKey<Biome> WITHOUT_TRAVELER_SPAWNS = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(PiglinProliferation.MODID, "without_traveler_spawns"));
    public static final TagKey<Item> BUCKLER_ENCHANTABLE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(PiglinProliferation.MODID, "enchantable/buckler_enchantable"));
}
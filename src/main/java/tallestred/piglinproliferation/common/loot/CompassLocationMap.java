package tallestred.piglinproliferation.common.loot;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import tallestred.piglinproliferation.common.PPTags;
import tallestred.piglinproliferation.configuration.PPConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CompassLocationMap extends ConcurrentHashMap<CompassLocationMap.SearchObject, Integer> {
    public static final int DEFAULT_EXPIRY_TIME = 24000;
    private static final List<CompassLocationMap.SearchObject> OBJECTS_TO_SEARCH = Collections.synchronizedList(new ArrayList<>());

    public static class SearchObject {
        private final boolean isBiome;
        private final ResourceLocation location;
        public SearchObject(boolean isBiome, ResourceLocation location) {
            this.isBiome = isBiome;
            this.location = location;
        }

        public SearchObject(String serialised) {
            String[] parts = serialised.split("-");
            this.isBiome = "B".equals(parts[0]);
            this.location = new ResourceLocation(parts[1]);
        }

        public BlockPos locateObject(ServerLevel level, BlockPos sourcePos) {
            Pair<BlockPos, ?> output;
            if (this.isBiome)
                output = level.findClosestBiome3d(holder -> holder.is(location), sourcePos, 64000, 32, 64);
            else {
                Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
                output = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(structureRegistry.wrapAsHolder(Objects.requireNonNull(structureRegistry.get(location)))), sourcePos, 50, false);
            }
            return output != null ? output.getFirst() : null;
        }

        public boolean entityAtObjectType(LivingEntity entity) {
            BlockPos blockPos = entity.getOnPos();
            if (entity.level() instanceof ServerLevel level && level.isLoaded(blockPos)) {
                return this.isBiome ?
                        level.getBiome(blockPos).is(this.location) :
                        level.structureManager().getStructureWithPieceAt(blockPos, ResourceKey.create(Registries.STRUCTURE, location)).isValid();
            }
            return false;
        }

        public boolean isBiome() {
            return this.isBiome;
        }

        public ResourceLocation getLocation() {
            return this.location;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof SearchObject && this.toString().equals(obj.toString());
        }

        @Override
        public String toString() {
            return (this.isBiome ? "B-" : "S-") + location.toString();
        }
    }

    public CompassLocationMap() {
        super();
    }
    public CompassLocationMap(CompoundTag nbt) {
        super();
        nbt.getAllKeys().forEach(key -> this.put(new SearchObject(key), nbt.getInt(key)));
    }

    public CompoundTag toNBT() {
        CompoundTag output = new CompoundTag();
        this.forEach((key, value) -> output.put(key.toString(), IntTag.valueOf(value)));
        return output;
    }

    public static List<SearchObject> objectsToSearch(ServerLevel level) {
        if (OBJECTS_TO_SEARCH.isEmpty() && level.dimension() == ServerLevel.NETHER) {
            Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
            Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            Registry<StructureSet> structureSetRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
            Set<Biome> possibleBiomes = PPConfig.COMMON.travellersCompassBiomeWhitelist.get()
                    ? biomeRegistry.holders().filter(r -> r.is(PPTags.TRAVELLERS_COMPASS_BIOME_WHITELIST)).map(Holder.Reference::value).collect(Collectors.toSet())
                    : level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes().stream().filter(h -> !h.is(PPTags.TRAVELLERS_COMPASS_BIOME_BLACKLIST)).map(Holder::value).collect(Collectors.toSet());
            Set<Structure> possibleStructures = new HashSet<>();
            if (PPConfig.COMMON.travellersCompassStructureWhitelist.get()) {
                structureRegistry.holders().filter(r -> r.is(PPTags.TRAVELLERS_COMPASS_STRUCTURE_WHITELIST)).forEach(h -> possibleStructures.add(h.value()));
                structureSetRegistry.holders().filter(r -> r.is(PPTags.TRAVELLERS_COMPASS_STRUCTURE_SET_WHITELIST)).forEach(holder -> {
                    holder.value().structures().forEach(s -> possibleStructures.add(s.structure().value()));
                });
            } else {
                level.getChunkSource().getGeneratorState().possibleStructureSets().stream().filter(s -> !s.is(PPTags.TRAVELLERS_COMPASS_STRUCTURE_SET_BLACKLIST)).forEach(holder -> {
                    holder.value().structures().stream().map(StructureSet.StructureSelectionEntry::structure).filter(s -> !s.is(PPTags.TRAVELLERS_COMPASS_STRUCTURE_BLACKLIST)).forEach(h -> possibleStructures.add(h.value()));
                });
            }
            possibleStructures.removeIf(s -> s.biomes().stream().map(Holder::value).noneMatch(possibleBiomes::contains));
            possibleBiomes.forEach(b -> OBJECTS_TO_SEARCH.add(new SearchObject(true, biomeRegistry.getKey(b))));
            possibleStructures.forEach(s -> OBJECTS_TO_SEARCH.add(new SearchObject(false, structureRegistry.getKey(s))));
        }
        return OBJECTS_TO_SEARCH;
    }
}

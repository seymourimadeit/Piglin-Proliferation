package tallestred.piglinproliferation.common.loot_tables;

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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        if (OBJECTS_TO_SEARCH.isEmpty()) {
            if (level.dimension() == ServerLevel.NETHER) {
                Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
                Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
                Set<Holder<Biome>> possibleBiomes = level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes().stream().filter(h -> !h.containsTag(PPTags.TRAVELLERS_COMPASS_BIOME_BLACKLIST)).collect(Collectors.toSet());
                possibleBiomes.forEach(h -> OBJECTS_TO_SEARCH.add(new SearchObject(true, biomeRegistry.getKey(h.get()))));
                for (Holder<StructureSet> holder: level.getChunkSource().getGeneratorState().possibleStructureSets()) {
                    Set<Holder<Structure>> possibleStructures = holder.get().structures().stream().map(StructureSet.StructureSelectionEntry::structure).collect(Collectors.toSet());
                    possibleStructures.removeIf(s -> s.get().biomes().stream().noneMatch(possibleBiomes::contains) || s.containsTag(PPTags.TRAVELLERS_COMPASS_STRUCTURE_BLACKLIST));
                    possibleStructures.forEach(s -> OBJECTS_TO_SEARCH.add(new SearchObject(false, structureRegistry.getKey(s.get()))));
                }
            }
        }
        return OBJECTS_TO_SEARCH;
    }
}

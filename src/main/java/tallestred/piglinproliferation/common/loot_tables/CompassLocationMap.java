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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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


        //TODO test that this works for structures
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

        public ResourceLocation getLocation() {
            return this.location;
        }

        @Override
        public String toString() {
            return (isBiome ? "B-" : "S-") + location.toString();
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

    //TODO this being ran when the first piglin trade happens is causing lag - any way to do it earlier?
    public static List<SearchObject> objectsToSearch(ServerLevel level) {
        if (OBJECTS_TO_SEARCH.isEmpty()) {
            if (level.dimension() == ServerLevel.NETHER) {
                Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
                Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
                List<Biome> possibleBiomes = level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes().stream().map(Holder::get).toList();
                List<Structure> possibleStructures = new ArrayList<>();
                //TODO - add a blacklist for this!
                for (Holder<StructureSet> holder: level.getChunkSource().getGeneratorState().possibleStructureSets())
                    holder.get().structures().stream().filter(s -> s.structure().get().biomes().stream().anyMatch(b -> possibleBiomes.contains(b.get()))).forEach(s -> possibleStructures.add(s.structure().get()));
                OBJECTS_TO_SEARCH.addAll(biomeRegistry.stream().filter(possibleBiomes::contains).map(b -> new SearchObject(true, biomeRegistry.getKey(b))).toList());
                OBJECTS_TO_SEARCH.addAll(structureRegistry.stream().filter(possibleStructures::contains).map(s -> new SearchObject(false, structureRegistry.getKey(s))).toList());
            }
        }
        return OBJECTS_TO_SEARCH;
    }
}
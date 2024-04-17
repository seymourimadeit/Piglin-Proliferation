package tallestred.piglinproliferation.common.loot;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.structure.Structure;
import tallestred.piglinproliferation.common.tags.PPTags;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static tallestred.piglinproliferation.util.CodeUtilities.snakeCaseToEnglish;


public class CompassLocationMap extends ConcurrentHashMap<CompassLocationMap.SearchObject, Integer> {
    public static final int DEFAULT_EXPIRY_TIME = 24000;
    private static final List<SearchObject> OBJECTS_TO_SEARCH = Collections.synchronizedList(new ArrayList<>());

    public static class SearchObject {
        private static final String TRANSLATION_PREFIX = "item.piglinproliferation.travellers_compass.desc.";
        private final boolean isBiome;
        private final ResourceLocation location;
        public SearchObject(boolean isBiome, ResourceLocation location) {
            this.isBiome = isBiome;
            this.location = location;
        }

        public SearchObject(boolean isBiome, String location) {
            this(isBiome, new ResourceLocation(location));
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

        public MutableComponent translatable() {
            MutableComponent returnComponent;
            String objectKey = (isBiome ? "biome" : "structure") + "." + this.location.getNamespace() + "." + this.location.getPath();
            String manualKey = TRANSLATION_PREFIX + objectKey;
            returnComponent = Component.translatableWithFallback(manualKey, "");
            if (returnComponent.getString().isEmpty())
                returnComponent = Component.translatableWithFallback(objectKey, snakeCaseToEnglish(this.location.getPath()));
                //Biomes almost certainly use the same format as the object key, but structures have to rely on the fallback.
            return returnComponent;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof SearchObject && this.toString().equals(obj.toString());
        }

        @Override
        public String toString() {
            return (this.isBiome ? "B-" : "S-") + location.toString();
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
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
            ServerChunkCache chunkSource = level.getChunkSource();
            RegistryAccess registryAccess = level.registryAccess();
            chunkSource.getGenerator().getBiomeSource().possibleBiomes().stream().filter(PPTags.TRAVELLERS_COMPASS_VALID_BIOMES::contains).forEach(h -> {
                OBJECTS_TO_SEARCH.add(new SearchObject(true, registryAccess.registryOrThrow(Registries.BIOME).getKey(h.value())));
            });
            chunkSource.getGeneratorState().possibleStructureSets().forEach(s -> {
                s.value().structures().stream().filter(h -> PPTags.TRAVELLERS_COMPASS_VALID_STRUCTURES.contains(h.structure(), registryAccess)).forEach(e -> {
                    OBJECTS_TO_SEARCH.add(new SearchObject(false, registryAccess.registryOrThrow(Registries.STRUCTURE).getKey(e.structure().value())));
                });
            });
        }
        return OBJECTS_TO_SEARCH;
    }

    public static void clearCache() {
        OBJECTS_TO_SEARCH.clear();
    }
}
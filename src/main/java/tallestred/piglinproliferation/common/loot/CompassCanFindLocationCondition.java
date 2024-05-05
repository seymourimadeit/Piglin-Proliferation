package tallestred.piglinproliferation.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.fml.ModList;
import tallestred.piglinproliferation.common.entities.PiglinTraveler;
import tallestred.piglinproliferation.common.items.PPItems;
import tallestred.piglinproliferation.common.tags.EitherTag;
import tallestred.piglinproliferation.common.tags.PPTags;

import java.util.*;
import java.util.stream.Collectors;

public class CompassCanFindLocationCondition implements LootItemCondition {
    @Override
    public LootItemConditionType getType() {
        return PPLoot.COMPASS_CAN_FIND_LOCATION.get();
    }
    private static final Set<Either<Holder<Biome>, Holder<Structure>>> FINDABLE_SEARCH_OBJECTS = new HashSet<>();

    @Override
    public boolean test(LootContext lootContext) {
        if (lootContext.getParam(LootContextParams.THIS_ENTITY) instanceof PiglinTraveler traveler) {
            ServerLevel level = lootContext.getLevel();
            List<Either<Holder<Biome>, Holder<Structure>>> objectsToSearch = searchObjects(level);
            Collections.shuffle(objectsToSearch);
            for (Either<Holder<Biome>, Holder<Structure>> searchObject : objectsToSearch) {
                EitherTag.Location searchObjectLocation = EitherTag.elementLocation(searchObject);
                if (searchObjectLocation != null) {
                    if (!traveler.alreadyLocatedObjects.containsKey(searchObjectLocation) && !PPItems.TRAVELERS_COMPASS.get().entityAtSearchObject(searchObject, traveler)) {
                        Optional<BlockPos> pos = PPItems.TRAVELERS_COMPASS.get().search(searchObject, traveler.getOnPos(), level);
                        if (pos.isPresent()) {
                            traveler.currentlyLocatedObject = Map.entry(searchObjectLocation, pos.get());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    //Ugly method design tbh
    private static List<Either<Holder<Biome>, Holder<Structure>>> searchObjects(ServerLevel level) {
        if (FINDABLE_SEARCH_OBJECTS.isEmpty()) {
            Set<Holder<Biome>> possibleBiomes = level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes();
            for (Holder<Biome> biome : PPTags.TRAVELERS_COMPASS_SEARCH.leftValues(level.registryAccess()))
                if (possibleBiomes.contains(biome))
                    FINDABLE_SEARCH_OBJECTS.add(Either.left(biome));

            Set<Holder<Structure>> possibleStructures = level.getChunkSource().getGeneratorState().possibleStructureSets().stream().flatMap(
                    set -> set.value().structures().stream().map(StructureSet.StructureSelectionEntry::structure)
            ).collect(Collectors.toSet());
            for (Holder<Structure> structure : PPTags.TRAVELERS_COMPASS_SEARCH.rightValues(level.registryAccess())) {
                if (possibleStructures.contains(structure))
                    //Unfathomable
                    if (!(structure.is(BuiltinStructures.FORTRESS) && ModList.get().isLoaded("betterfortresses")))
                        FINDABLE_SEARCH_OBJECTS.add(Either.right(structure));
            }
        }
        return new ArrayList<>(FINDABLE_SEARCH_OBJECTS);
    }

    public static void clearSearchCache() {
        FINDABLE_SEARCH_OBJECTS.clear();
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<CompassCanFindLocationCondition> {
        @Override
        public void serialize(JsonObject jsonObject, CompassCanFindLocationCondition compassCanFindLocationCondition, JsonSerializationContext jsonSerializationContext) {
        }

        @Override
        public CompassCanFindLocationCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new CompassCanFindLocationCondition();
        }
    }
}
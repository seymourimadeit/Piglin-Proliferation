package tallestred.piglinproliferation.common.loot;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import tallestred.piglinproliferation.common.entities.PiglinTraveler;
import tallestred.piglinproliferation.common.items.PPItems;
import tallestred.piglinproliferation.common.tags.EitherTag;
import tallestred.piglinproliferation.common.tags.PPTags;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CompassCanFindLocationCondition implements LootItemCondition {
    public static final MapCodec<CompassCanFindLocationCondition> CODEC = MapCodec.unit(new CompassCanFindLocationCondition());

    @Override
    public LootItemConditionType getType() {
        return PPLoot.COMPASS_CAN_FIND_LOCATION.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        if (lootContext.getParam(LootContextParams.THIS_ENTITY) instanceof PiglinTraveler traveler) {
            ServerLevel level = lootContext.getLevel();
            List<Either<Holder<Biome>, Holder<Structure>>> objectsToSearch = PPTags.TRAVELERS_COMPASS_SEARCH.combinedValues(level.registryAccess());
            Collections.shuffle(objectsToSearch);
            for (Either<Holder<Biome>, Holder<Structure>> searchObject : objectsToSearch) {
                EitherTag.Location searchObjectLocation = EitherTag.elementLocation(searchObject);
                if (searchObjectLocation != null) {
                    if (!traveler.alreadyLocatedObjects.containsKey(searchObjectLocation) && !PPItems.TRAVELERS_COMPASS.get().entityAtSearchObject(searchObject, traveler)) {
                        BlockPos pos = PPItems.TRAVELERS_COMPASS.get().search(searchObject, traveler.getOnPos(), level);
                        if (pos != null) {
                            traveler.currentlyLocatedObject = Map.entry(searchObjectLocation, pos);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}

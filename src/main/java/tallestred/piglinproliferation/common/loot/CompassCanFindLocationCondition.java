package tallestred.piglinproliferation.common.loot;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import tallestred.piglinproliferation.common.entities.PiglinTraveller;
import tallestred.piglinproliferation.common.items.PPItems;
import tallestred.piglinproliferation.common.tags.EitherTag;
import tallestred.piglinproliferation.common.tags.PPTags;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CompassCanFindLocationCondition implements LootItemCondition {
    public static final Codec<CompassCanFindLocationCondition> CODEC = Codec.unit(new CompassCanFindLocationCondition());

    @Override
    public LootItemConditionType getType() {
        return PPLoot.COMPASS_CAN_FIND_LOCATION.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        if (lootContext.getParam(LootContextParams.THIS_ENTITY) instanceof PiglinTraveller traveller) {
            ServerLevel level = lootContext.getLevel();
            List<Either<Holder<Biome>, Holder<Structure>>> objectsToSearch = PPTags.TRAVELLERS_COMPASS_SEARCH.combinedValues(level.registryAccess());
            Collections.shuffle(objectsToSearch);
            for (Either<Holder<Biome>, Holder<Structure>> searchObject : objectsToSearch) {
                EitherTag.Location searchObjectLocation = EitherTag.elementLocation(searchObject);
                if (searchObjectLocation != null) {
                    if (!traveller.alreadyLocatedObjects.containsKey(searchObjectLocation) && !PPItems.TRAVELLERS_COMPASS.get().entityAtSearchObject(searchObject, traveller)) {
                        BlockPos pos = PPItems.TRAVELLERS_COMPASS.get().search(searchObject, traveller.getOnPos(), level);
                        if (pos != null) {
                            traveller.currentlyLocatedObject = Map.entry(searchObjectLocation, pos);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}

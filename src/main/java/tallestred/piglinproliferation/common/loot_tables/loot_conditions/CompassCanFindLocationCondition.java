package tallestred.piglinproliferation.common.loot_tables.loot_conditions;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import tallestred.piglinproliferation.common.entities.PiglinTraveller;
import tallestred.piglinproliferation.common.loot_tables.CompassLocationMap;
import tallestred.piglinproliferation.common.loot_tables.PPLootTables;

import java.util.Collections;
import java.util.List;
import java.util.Map;

//TODO: Might need to make this a predicate rather than a condition!!
public class CompassCanFindLocationCondition implements LootItemCondition {
    public static final Codec<CompassCanFindLocationCondition> CODEC = Codec.unit(new CompassCanFindLocationCondition());

    @Override
    public LootItemConditionType getType() {
        return PPLootTables.COMPASS_CAN_FIND_LOCATION.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        if (lootContext.getParam(LootContextParams.THIS_ENTITY) instanceof PiglinTraveller traveller) {
            ServerLevel level = lootContext.getLevel();
            List<CompassLocationMap.SearchObject> objectsToSearch = CompassLocationMap.objectsToSearch(level);
            Collections.shuffle(objectsToSearch);
            for (CompassLocationMap.SearchObject object : objectsToSearch) {
                if (!traveller.alreadyLocatedObjects.containsKey(object) && !object.entityAtObjectType(traveller)) {
                    BlockPos pos = object.locateObject(level, traveller.getOnPos());
                    if (pos != null) {
                        traveller.currentlyLocatedObject = Map.entry(object, pos);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

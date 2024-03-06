package tallestred.piglinproliferation.common.loot_tables.loot_conditions;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import tallestred.piglinproliferation.common.entities.PiglinTraveller;
import tallestred.piglinproliferation.common.loot_tables.CompassLocateObject;
import tallestred.piglinproliferation.common.loot_tables.PPLootTables;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TravellersCompassValidateCondition implements LootItemCondition {
    public static final Codec<TravellersCompassValidateCondition> CODEC = Codec.unit(new TravellersCompassValidateCondition());

    @Override
    public LootItemConditionType getType() {
        return PPLootTables.TRAVELLERS_COMPASS_VALIDATE.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        if (lootContext.getParam(LootContextParams.THIS_ENTITY) instanceof PiglinTraveller traveller) {
            List<CompassLocateObject> objectsToSearch = PPLootTables.getTravellersCompassSearchList(lootContext.getLevel()).stream().filter(o -> !traveller.alreadyLocatedObjects.contains(o)).collect(Collectors.toList());
            Collections.shuffle(objectsToSearch);
            for (CompassLocateObject object : objectsToSearch) {
                BlockPos pos = object.locateObject(lootContext.getLevel(), traveller.getOnPos());
                if (pos != null) {
                    traveller.currentlyLocatedObject = Map.entry(object, pos);
                    return true;
                }
            }
        }
        return false;
    }
}

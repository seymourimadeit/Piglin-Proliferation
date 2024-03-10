package tallestred.piglinproliferation.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import tallestred.piglinproliferation.common.entities.PiglinTraveller;
import tallestred.piglinproliferation.common.items.TravellersCompassItem;

public class AddLocationToCompassFunction extends LootItemConditionalFunction {

    AddLocationToCompassFunction(LootItemCondition[] pPredicates) {
        super(pPredicates);
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.getItem() instanceof TravellersCompassItem compass) {
            if (lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof PiglinTraveller traveller) {
                CompassLocationMap.SearchObject object = traveller.currentlyLocatedObject.getKey();
                BlockPos pos = traveller.currentlyLocatedObject.getValue();
                traveller.alreadyLocatedObjects.put(object, CompassLocationMap.DEFAULT_EXPIRY_TIME);
                traveller.currentlyLocatedObject = null;
                compass.addTags(lootContext.getLevel().dimension(), pos, itemStack.getOrCreateTag(), object.getLocation(), object.isBiome());
            }
        }
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return PPLoot.ADD_LOCATION_TO_COMPASS.get();
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<AddLocationToCompassFunction> {
        @Override
        public AddLocationToCompassFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            return new AddLocationToCompassFunction(lootItemConditions);
        }
    }
}
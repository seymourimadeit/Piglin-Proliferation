package tallestred.piglinproliferation.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import tallestred.piglinproliferation.common.entities.PiglinTraveler;
import tallestred.piglinproliferation.common.items.TravelersCompassItem;
import tallestred.piglinproliferation.common.tags.EitherTag;

public class AddLocationToCompassFunction extends LootItemConditionalFunction {

    AddLocationToCompassFunction(LootItemCondition[] pPredicates) {
        super(pPredicates);
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.getItem() instanceof TravelersCompassItem compass) {
            if (lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof PiglinTraveler traveler) {
                EitherTag.Location searchObjectLocation = traveler.currentlyLocatedObject.getKey();
                traveler.alreadyLocatedObjects.put(searchObjectLocation, PiglinTraveler.DEFAULT_EXPIRY_TIME);
                compass.addTags(lootContext.getLevel().dimension(), traveler.currentlyLocatedObject.getValue(), itemStack.getOrCreateTag(), searchObjectLocation.location(), searchObjectLocation.isLeft());
                traveler.currentlyLocatedObject = null;
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
package tallestred.piglinproliferation.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import tallestred.piglinproliferation.common.entities.PiglinTraveler;
import tallestred.piglinproliferation.common.items.TravelersCompassItem;
import tallestred.piglinproliferation.common.items.component.PPComponents;
import tallestred.piglinproliferation.common.items.component.TravelersCompassTracker;
import tallestred.piglinproliferation.common.tags.EitherTag;

import java.util.*;

public class AddLocationToCompassFunction extends LootItemConditionalFunction {
    public static final MapCodec<AddLocationToCompassFunction> CODEC =  RecordCodecBuilder.mapCodec(
            builder -> commonFields(builder).apply(builder, AddLocationToCompassFunction::new));

    AddLocationToCompassFunction(List<LootItemCondition> pPredicates) {
        super(pPredicates);
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof PiglinTraveler traveler) {
            EitherTag.Location searchObjectLocation = traveler.currentlyLocatedObject.getKey();
            traveler.alreadyLocatedObjects.put(searchObjectLocation, PiglinTraveler.DEFAULT_EXPIRY_TIME);
            itemStack.set(PPComponents.TRAVELERS_COMPASS_TRACKER, new TravelersCompassTracker(new GlobalPos(lootContext.getLevel().dimension(), traveler.currentlyLocatedObject.getValue()), searchObjectLocation.location(), searchObjectLocation.isLeft()));
            traveler.currentlyLocatedObject = null;
        }
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return PPLoot.ADD_LOCATION_TO_COMPASS.get();
    }
}

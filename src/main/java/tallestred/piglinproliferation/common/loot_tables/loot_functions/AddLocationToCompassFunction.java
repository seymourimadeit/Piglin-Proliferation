package tallestred.piglinproliferation.common.loot_tables.loot_functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import tallestred.piglinproliferation.common.entities.PiglinTraveller;
import tallestred.piglinproliferation.common.items.TravellersCompassItem;
import tallestred.piglinproliferation.common.loot_tables.CompassLocationMap;
import tallestred.piglinproliferation.common.loot_tables.PPLootTables;

import java.util.*;

public class AddLocationToCompassFunction extends LootItemConditionalFunction {
    public static final Codec<AddLocationToCompassFunction> CODEC =  RecordCodecBuilder.create(
            builder -> commonFields(builder).apply(builder, AddLocationToCompassFunction::new));

    AddLocationToCompassFunction(List<LootItemCondition> pPredicates) {
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
                compass.addTags(lootContext.getLevel().dimension(), pos, itemStack.getOrCreateTag(), object.getLocation().getPath(), object.isBiome());
            }
        }
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return PPLootTables.ADD_LOCATION_TO_COMPASS.get();
    }
}

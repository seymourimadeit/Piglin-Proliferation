package tallestred.piglinproliferation.common.loot_tables;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import tallestred.piglinproliferation.common.items.PPItems;
import tallestred.piglinproliferation.common.items.TravellersCompassItem;

public class TravellerCompassStructureLocateFunction extends LootItemConditionalFunction {
    final TagKey<Structure> destination;
    final boolean skipKnownStructures;

    TravellerCompassStructureLocateFunction(LootItemCondition[] pPredicates, TagKey<Structure> pDestination, boolean pSkipKnownStructures) {
        super(pPredicates);
        this.destination = pDestination;
        this.skipKnownStructures = pSkipKnownStructures;
    }

    @Override
    protected ItemStack run(ItemStack pStack, LootContext pContext) {
        if (pStack.getItem() == PPItems.TRAVELLERS_COMPASS.get()) {
            LivingEntity entity = (LivingEntity) pContext.getParamOrNull(LootContextParams.THIS_ENTITY);
            if (entity != null) {
                TravellersCompassItem compass = (TravellersCompassItem) pStack.getItem();
                ServerLevel serverlevel = pContext.getLevel();
                BlockPos blockpos = serverlevel.findNearestMapStructure(this.destination, entity.blockPosition(), 50, this.skipKnownStructures);
                if (blockpos != null) {
                    compass.addTags(serverlevel.dimension(), blockpos, pStack.getOrCreateTag(), this.destination.location().getPath(), false);
                    return pStack;
                }
            } else {
                return pStack;
            }
        }
        return pStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return PPLootTables.TRAVELLERS_BIOME_COMPASS_LOCATION.get();
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<TravellerCompassStructureLocateFunction> {
        private static TagKey<Structure> readStructure(JsonObject p_210661_) {
            if (p_210661_.has("destination")) {
                String s = GsonHelper.getAsString(p_210661_, "destination");
                return TagKey.create(Registries.STRUCTURE, new ResourceLocation(s));
            } else {
                return ExplorationMapFunction.DEFAULT_DESTINATION;
            }
        }

        public void serialize(JsonObject pJson, TravellerCompassStructureLocateFunction pValue, JsonSerializationContext pSerializationContext) {
            super.serialize(pJson, pValue, pSerializationContext);
            pJson.addProperty("destination", pValue.destination.location().toString());
            if (!pValue.skipKnownStructures) {
                pJson.addProperty("skip_existing_chunks", pValue.skipKnownStructures);
            }
        }

        public TravellerCompassStructureLocateFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
            TagKey<Structure> tagkey = readStructure(pObject);
            boolean flag = GsonHelper.getAsBoolean(pObject, "skip_existing_chunks", true);
            return new TravellerCompassStructureLocateFunction(pConditions, tagkey, flag);
        }
    }
}

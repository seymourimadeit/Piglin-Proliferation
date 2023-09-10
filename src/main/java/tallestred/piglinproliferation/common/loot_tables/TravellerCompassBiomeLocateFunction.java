package tallestred.piglinproliferation.common.loot_tables;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import tallestred.piglinproliferation.common.items.PPItems;
import tallestred.piglinproliferation.common.items.TravellersCompassItem;

import java.util.Optional;
import java.util.function.Predicate;

public class TravellerCompassBiomeLocateFunction extends LootItemConditionalFunction {
    final TagKey<Biome> destination;
    final boolean skipKnownStructures;

    TravellerCompassBiomeLocateFunction(LootItemCondition[] pPredicates, TagKey<Biome> pDestination, boolean pSkipKnownStructures) {
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
                Pair<BlockPos, Holder<Biome>> pair = serverlevel.findClosestBiome3d(biomeHolder -> biomeHolder.is(this.destination), entity.blockPosition(), 64000, 32, 64);
                if (pair != null) {
                    compass.addTags(serverlevel.dimension(), pair.getFirst(), pStack.getOrCreateTag(), this.destination.location().getPath(), true);
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
        return PPLootTables.TRAVELLERS_COMPASS_LOCATION.get();
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<TravellerCompassBiomeLocateFunction> {
        private static TagKey<Biome> readBiome(JsonObject p_210661_) {
            if (p_210661_.has("destination")) {
                String s = GsonHelper.getAsString(p_210661_, "destination");
                return TagKey.create(Registries.BIOME, new ResourceLocation(s));
            } else {
                return null;
            }
        }

        public void serialize(JsonObject pJson, TravellerCompassBiomeLocateFunction pValue, JsonSerializationContext pSerializationContext) {
            super.serialize(pJson, pValue, pSerializationContext);
            pJson.addProperty("destination", pValue.destination.location().toString());
            if (!pValue.skipKnownStructures) {
                pJson.addProperty("skip_existing_chunks", pValue.skipKnownStructures);
            }
        }

        public TravellerCompassBiomeLocateFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
            TagKey<Biome> tagkey = readBiome(pObject);
            boolean flag = GsonHelper.getAsBoolean(pObject, "skip_existing_chunks", true);
            return new TravellerCompassBiomeLocateFunction(pConditions, tagkey, flag);
        }
    }
}

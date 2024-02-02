package tallestred.piglinproliferation.common.loot_tables.loot_functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import tallestred.piglinproliferation.common.entities.TravellersCompassBarterer;
import tallestred.piglinproliferation.common.items.PPItems;
import tallestred.piglinproliferation.common.items.TravellersCompassItem;
import tallestred.piglinproliferation.common.loot_tables.PPLootTables;

import java.util.List;

public class TravellerCompassStructureLocateFunction extends LootItemConditionalFunction {
    public static final Codec<TravellerCompassStructureLocateFunction> CODEC =  RecordCodecBuilder.create(
            p_298123_ -> commonFields(p_298123_)
                    .and(TagKey.codec(Registries.STRUCTURE).fieldOf("destination").forGetter(p_298122_ -> p_298122_.destination))
                    .apply(p_298123_, TravellerCompassStructureLocateFunction::new)
    );
    final TagKey<Structure> destination;

    TravellerCompassStructureLocateFunction(List<LootItemCondition> pPredicates, TagKey<Structure> pDestination) {
        super(pPredicates);
        this.destination = pDestination;
    }

    @Override
    protected ItemStack run(ItemStack pStack, LootContext pContext) {
        if (pStack.getItem() == PPItems.TRAVELLERS_COMPASS.get()) {
            if (pContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof LivingEntity entity) {
                TravellersCompassItem compass = (TravellersCompassItem) pStack.getItem();
                ServerLevel serverlevel = pContext.getLevel();
                BlockPos blockpos = serverlevel.findNearestMapStructure(this.destination, entity.blockPosition(), 50, false);
                if (blockpos != null) {
                    compass.addTags(serverlevel.dimension(), blockpos, pStack.getOrCreateTag(), this.destination.location().getPath(), false);
                    if (entity instanceof TravellersCompassBarterer barterer)
                        barterer.setStructureAlreadyRolled(this.destination);
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
}

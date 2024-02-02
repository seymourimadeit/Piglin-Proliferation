package tallestred.piglinproliferation.common.loot_tables.loot_functions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
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

public class TravellerCompassBiomeLocateFunction extends LootItemConditionalFunction {
    public static final Codec<TravellerCompassBiomeLocateFunction> CODEC =  RecordCodecBuilder.create(
            p_298123_ -> commonFields(p_298123_)
                    .and(TagKey.codec(Registries.BIOME).fieldOf("destination").forGetter(p_298122_ -> p_298122_.destination))
                    .apply(p_298123_, TravellerCompassBiomeLocateFunction::new)
    );
    private final TagKey<Biome> destination;

    TravellerCompassBiomeLocateFunction(List<LootItemCondition> pPredicates, TagKey<Biome> pDestination) {
        super(pPredicates);
        this.destination = pDestination;
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
                    if (entity instanceof TravellersCompassBarterer barterer)
                        barterer.setBiomeAlreadyRolled(this.destination);
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
}

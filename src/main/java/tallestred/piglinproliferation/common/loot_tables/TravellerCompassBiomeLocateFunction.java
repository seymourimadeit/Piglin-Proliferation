package tallestred.piglinproliferation.common.loot_tables;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import tallestred.piglinproliferation.common.items.PPItems;
import tallestred.piglinproliferation.common.items.TravellersCompassItem;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class TravellerCompassBiomeLocateFunction extends LootItemConditionalFunction {
    private final TagKey<Biome> destination;
    public static final Codec<TravellerCompassBiomeLocateFunction> CODEC = RecordCodecBuilder.create((p_297135_) -> {
        return commonFields(p_297135_).and(TagKey.hashedCodec(Registries.BIOME).fieldOf("destination").forGetter((p_297134_) -> {
            return p_297134_.destination;
        })).apply(p_297135_, TravellerCompassBiomeLocateFunction::new);
    });

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

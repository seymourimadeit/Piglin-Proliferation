package tallestred.piglinproliferation.common.loot_tables.loot_conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import tallestred.piglinproliferation.common.entities.TravellersCompassBarterer;
import tallestred.piglinproliferation.common.loot_tables.PPLootTables;

public class TravellersCompassBiomeAlreadyRolledCondition implements LootItemCondition {
    public static final Codec<TravellersCompassBiomeAlreadyRolledCondition> CODEC =  RecordCodecBuilder.create(instance ->
            instance.group(
                    TagKey.codec(Registries.BIOME)
                            .fieldOf("destination")
                            .forGetter(condition -> condition.destination)
                            ).apply(instance, TravellersCompassBiomeAlreadyRolledCondition::new));
    final TagKey<Biome> destination;

    public TravellersCompassBiomeAlreadyRolledCondition(TagKey<Biome> destination) {
        this.destination = destination;
    }

    @Override
    public LootItemConditionType getType() {
        return PPLootTables.TRAVELLERS_COMPASS_BIOME_ALREADY_ROLLED.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        if (lootContext.getParam(LootContextParams.THIS_ENTITY) instanceof TravellersCompassBarterer entity)
            return entity.hasBiomeAlreadyRolled(this.destination);
        else return false;
    }
}

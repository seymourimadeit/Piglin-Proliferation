package tallestred.piglinproliferation.common.loot_tables.loot_conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import tallestred.piglinproliferation.common.entities.TravellersCompassBarterer;
import tallestred.piglinproliferation.common.loot_tables.PPLootTables;

public class TravellersCompassStructureAlreadyRolledCondition implements LootItemCondition {
    public static final Codec<TravellersCompassStructureAlreadyRolledCondition> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    TagKey.codec(Registries.STRUCTURE)
                            .fieldOf("destination")
                            .forGetter(condition -> condition.destination)
                            ).apply(builder, TravellersCompassStructureAlreadyRolledCondition::new));
    final TagKey<Structure> destination;

    public TravellersCompassStructureAlreadyRolledCondition(TagKey<Structure> destination) {
        this.destination = destination;
    }

    @Override
    public LootItemConditionType getType() {
        return PPLootTables.TRAVELLERS_COMPASS_STRUCTURE_ALREADY_ROLLED.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        if (lootContext.getParam(LootContextParams.THIS_ENTITY) instanceof TravellersCompassBarterer entity)
            return entity.hasStructureAlreadyRolled(this.destination);
        else return false;
    }
}

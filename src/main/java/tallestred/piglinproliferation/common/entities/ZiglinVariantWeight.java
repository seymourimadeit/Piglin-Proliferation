package tallestred.piglinproliferation.common.entities;

import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.EntityType;

public record ZiglinVariantWeight(EntityType<?> type, Weight weight) implements WeightedEntry {
    @Override
    public Weight getWeight() {
        return weight;
    }
}

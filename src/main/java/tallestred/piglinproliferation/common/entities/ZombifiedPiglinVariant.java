package tallestred.piglinproliferation.common.entities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.random.Weight;

public record ZombifiedPiglinVariant(Weight weight) {
    public static final Codec<ZombifiedPiglinVariant> WEIGHT_CODEC = Weight.CODEC
            .xmap(ZombifiedPiglinVariant::new, ZombifiedPiglinVariant::weight);
    public static final Codec<ZombifiedPiglinVariant> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    Weight.CODEC.fieldOf("weight").forGetter(ZombifiedPiglinVariant::weight)).apply(in, ZombifiedPiglinVariant::new)),
            WEIGHT_CODEC);
}

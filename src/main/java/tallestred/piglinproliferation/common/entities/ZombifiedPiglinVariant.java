package tallestred.piglinproliferation.common.entities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.random.Weight;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Function;

public record ZombifiedPiglinVariant(Weight weight, Item itemID) {
    public static final Codec<ZombifiedPiglinVariant> WEIGHT_CODEC = Weight.CODEC
            .xmap((Function<? super Weight, ? extends ZombifiedPiglinVariant>) ZombifiedPiglinVariant::new, ZombifiedPiglinVariant::weight);
    public static final Codec<ZombifiedPiglinVariant> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    Weight.CODEC.fieldOf("weight").forGetter(ZombifiedPiglinVariant::weight), BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ZombifiedPiglinVariant::itemID)).apply(in, ZombifiedPiglinVariant::new)),
            WEIGHT_CODEC);

    public ZombifiedPiglinVariant(Weight weight) {
        this(weight, Items.AIR);
    }
}

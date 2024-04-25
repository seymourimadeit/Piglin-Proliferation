package tallestred.piglinproliferation.common.items.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record TravelersCompassTracker(GlobalPos target, ResourceLocation targetID, boolean isBiome) {
    public static final Codec<TravelersCompassTracker> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    GlobalPos.CODEC.fieldOf("target").forGetter(TravelersCompassTracker::target),
                    ResourceLocation.CODEC.fieldOf("target_id").forGetter(TravelersCompassTracker::targetID),
                    Codec.BOOL.fieldOf("isBiome").forGetter(TravelersCompassTracker::isBiome)
            ).apply(instance, TravelersCompassTracker::new)
    );
    public static final StreamCodec<ByteBuf, TravelersCompassTracker> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, TravelersCompassTracker::target, ResourceLocation.STREAM_CODEC, TravelersCompassTracker::targetID, ByteBufCodecs.BOOL, TravelersCompassTracker::isBiome, TravelersCompassTracker::new
    );
}
package tallestred.piglinproliferation.client.particles;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestred.piglinproliferation.PiglinProliferation;

public class PPParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, PiglinProliferation.MODID);
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOption>> COLORED_SMOKE = PARTICLES.register("colored_smoke", () -> new ParticleType<ColorParticleOption>(false) {
        @Override
        public MapCodec<ColorParticleOption> codec() {
            return ColorParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
            return ColorParticleOption.streamCodec(this);
        }
    });
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOption>> SIGNAL_COLORED_SMOKE = PARTICLES.register("signal_colored_smoke", () -> new ParticleType<ColorParticleOption>(false) {
        @Override
        public MapCodec<ColorParticleOption> codec() {
            return ColorParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
            return ColorParticleOption.streamCodec(this);
        }
    });
}

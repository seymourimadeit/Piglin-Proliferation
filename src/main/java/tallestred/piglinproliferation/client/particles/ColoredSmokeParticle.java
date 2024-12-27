package tallestred.piglinproliferation.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ColorParticleOption;
import org.jetbrains.annotations.Nullable;

public class ColoredSmokeParticle extends CampfireSmokeParticle {
    public ColoredSmokeParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, boolean signal) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, signal);

    }

    public static class ColoredSmokeParticleProvider implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet spriteSet;

        public ColoredSmokeParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(ColorParticleOption type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ColoredSmokeParticle smoke = new ColoredSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, false);
            smoke.setColor(type.getRed(), type.getGreen(), type.getBlue());
            smoke.setAlpha(0.9F);
            smoke.pickSprite(this.spriteSet);
            return smoke;
        }
    }

    public static class SignalColoredSmokeParticleProvider implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet spriteSet;

        public SignalColoredSmokeParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(ColorParticleOption type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ColoredSmokeParticle smoke = new ColoredSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, true);
            smoke.setColor(type.getRed(), type.getGreen(), type.getBlue());
            smoke.setAlpha(0.95F);
            smoke.pickSprite(this.spriteSet);
            return smoke;
        }
    }

}

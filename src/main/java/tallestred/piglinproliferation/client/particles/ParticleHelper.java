package tallestred.piglinproliferation.client.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * A separate class is necessary to prevent accidental classloading of clientside-only {@link AfterImageParticle}
 * */
public class ParticleHelper {
    public static void createAfterImage(LivingEntity entity, Vec3 vector) {
        Minecraft.getInstance().particleEngine.add(new AfterImageParticle(entity, (ClientLevel) entity.level(), entity.xOld + (vector.x / 1.5), entity.yOld, entity.zOld + (vector.z / 1.5)));
    }
}

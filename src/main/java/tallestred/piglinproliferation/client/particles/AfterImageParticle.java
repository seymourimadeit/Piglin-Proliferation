package tallestred.piglinproliferation.client.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class AfterImageParticle extends Particle {
    private int life;
    private final LivingEntity entity;

    public AfterImageParticle(LivingEntity entity, ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.entity = entity;
    }

    @Override
    public void render(@NotNull VertexConsumer vertex, Camera camera, float tick) {
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        Vec3 vec3 = camera.getPosition();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F / Math.abs((float) life + 1));
        PoseStack stack = new PoseStack();
        multibuffersource$buffersource.getBuffer(RenderType.entityTranslucent(entityRenderDispatcher.getRenderer(this.entity).getTextureLocation(this.entity)));
        entityRenderDispatcher.render(this.entity, this.x - vec3.x(), this.y - vec3.y(), this.z - vec3.z(), this.entity.getYRot(), tick, stack, multibuffersource$buffersource, entityRenderDispatcher.getPackedLightCoords(this.entity, tick));
        multibuffersource$buffersource.endBatch();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public void tick() {
        ++this.life;
        if (this.life >= 3) {
            this.remove();
        }
    }
}

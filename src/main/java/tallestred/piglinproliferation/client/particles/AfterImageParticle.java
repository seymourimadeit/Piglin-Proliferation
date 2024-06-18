package tallestred.piglinproliferation.client.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import tallestred.piglinproliferation.configuration.PPConfig;
import tallestred.piglinproliferation.mixins.LivingEntityRendererInvoker;

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
        if (PPConfig.CLIENT.RenderAfterImageLayers.get()) {
            multibuffersource$buffersource.getBuffer(RenderType.entityTranslucent(entityRenderDispatcher.getRenderer(this.entity).getTextureLocation(this.entity)));
            entityRenderDispatcher.render(this.entity, this.x - vec3.x(), this.y - vec3.y(), this.z - vec3.z(), this.entity.getYRot(), tick, stack, multibuffersource$buffersource, entityRenderDispatcher.getPackedLightCoords(this.entity, tick));
        } else {
            stack.pushPose();
            multibuffersource$buffersource.getBuffer(RenderType.entityTranslucent(entityRenderDispatcher.getRenderer(this.entity).getTextureLocation(this.entity)));
            LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer = (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) entityRenderDispatcher.getRenderer(this.entity);
            EntityModel<LivingEntity> model = renderer.getModel();
            model.attackTime = this.entity.getAttackAnim(tick);
            boolean shouldSit = this.entity.isPassenger() && (this.entity.getVehicle() != null && this.entity.getVehicle().shouldRiderSit());
            model.riding = shouldSit;
            model.young = this.entity.isBaby();
            float f = Mth.rotLerp(tick, this.entity.yBodyRotO, this.entity.yBodyRot);
            float f1 = Mth.rotLerp(tick, this.entity.yHeadRotO, this.entity.yHeadRot);
            float f2 = f1 - f;
            stack.translate(this.x - vec3.x(), (this.y - vec3.y()) + 1.4F, this.z - vec3.z());
            if (shouldSit && this.entity.getVehicle() instanceof LivingEntity livingentity) {
                f = Mth.rotLerp(tick, livingentity.yBodyRotO, livingentity.yBodyRot);
                f2 = f1 - f;
                float f3 = Mth.wrapDegrees(f2);
                if (f3 < -85.0F) {
                    f3 = -85.0F;
                }

                if (f3 >= 85.0F) {
                    f3 = 85.0F;
                    f = f1 - f3;
                } else {
                    f = f1 - f3;
                }

                if (f3 * f3 > 2500.0F) {
                    f += f3 * 0.2F;
                }

                f2 = f1 - f;
            }

            float f6 = Mth.lerp(tick, this.entity.xRotO, this.entity.getXRot());
            if (this.entity.getPose() == Pose.SLEEPING) {
                Direction direction = this.entity.getBedOrientation();
                if (direction != null) {
                    float f4 = this.entity.getEyeHeight(Pose.STANDING) - 0.1F;
                    stack.translate((float) (-direction.getStepX()) * f4, 0.0D, (float) (-direction.getStepZ()) * f4);
                }
            }
            float f7 = (float) this.entity.tickCount + tick;
            stack.mulPose(Axis.YP.rotationDegrees(180.0F - f));
            ((LivingEntityRendererInvoker) renderer).invokeScale(this.entity, stack, tick);
            stack.scale(-1.0F, -1.0F, 1.0F);
            float f8 = 0.0F;
            float f5 = 0.0F;
            if (!shouldSit && this.entity.isAlive()) {
                f8 = this.entity.walkAnimation.speed(tick);
                f5 = this.entity.walkAnimation.position(tick);
                if (this.entity.isBaby()) {
                    f5 *= 3.0F;
                }

                if (f8 > 1.0F) {
                    f8 = 1.0F;
                }
            }

            model.prepareMobModel(this.entity, f5, f8, tick);
            model.setupAnim(this.entity, f5, f8, f7, f2, f6);
            Minecraft minecraft = Minecraft.getInstance();
            boolean flag = !this.entity.isInvisible();
            boolean flag1 = !flag && !this.entity.isInvisibleTo(minecraft.player);
            boolean flag2 = minecraft.shouldEntityAppearGlowing(this.entity);
            RenderType rendertype = getRenderType(this.entity, renderer, model, flag, flag1, flag2);
            if (rendertype != null) {
                VertexConsumer ivertexbuilder = multibuffersource$buffersource.getBuffer(rendertype);
                int overlay = LivingEntityRenderer.getOverlayCoords(this.entity, 0.0F);
                int color = FastColor.ARGB32.colorFromFloat(0.5F / Math.abs((float) life + 1), 1.0F, 1.0F, 1.0F);
                model.renderToBuffer(stack, ivertexbuilder, getLightColor(tick), overlay, flag1 ? 654311423 : -1);
            }
            stack.popPose();
        }
        multibuffersource$buffersource.endBatch();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static RenderType getRenderType(LivingEntity p_230496_1_, LivingEntityRenderer<LivingEntity, ?> renderer, EntityModel<?> model, boolean p_230496_2_, boolean p_230496_3_, boolean p_230496_4_) {
        ResourceLocation resourcelocation = renderer.getTextureLocation(p_230496_1_);
        if (p_230496_3_) {
            return RenderType.itemEntityTranslucentCull(resourcelocation);
        } else if (p_230496_2_) {
            return RenderType.entityTranslucent(resourcelocation);
        } else {
            return p_230496_4_ ? RenderType.outline(resourcelocation) : null;
        }

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

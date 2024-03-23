package tallestred.piglinproliferation.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import tallestred.piglinproliferation.common.blockentities.FireRingBlockEntity;

public class FireRingRenderer implements BlockEntityRenderer<FireRingBlockEntity> {
    private final ItemRenderer itemRenderer;

    public FireRingRenderer(BlockEntityRendererProvider.Context pContext) {
        this.itemRenderer = pContext.getItemRenderer();
    }

    @Override
    public void render(FireRingBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction blockDirection = blockEntity.getBlockState().getValue(CampfireBlock.FACING);
        NonNullList<ItemStack> items = blockEntity.getItems();
        int blockPos = (int)blockEntity.getBlockPos().asLong();

        for(int i = 0; i < items.size(); ++i) {
            ItemStack item = items.get(i);
            if (item != ItemStack.EMPTY) {
                poseStack.pushPose();
                poseStack.translate(0.5F, 0.32721875F, 0.5F);
                Direction direction = Direction.from2DDataValue((i + blockDirection.get2DDataValue()) % 4);
                float yRot = -direction.toYRot();
                poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.translate(-0.3125F, -0.3125F, 0.0F);
                poseStack.scale(0.375F, 0.375F, 0.375F);
                this.itemRenderer.renderStatic(item, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, blockEntity.getLevel(), blockPos + i);
                poseStack.popPose();
            }
        }
    }
}

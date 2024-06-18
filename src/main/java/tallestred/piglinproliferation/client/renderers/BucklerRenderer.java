package tallestred.piglinproliferation.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import tallestred.piglinproliferation.client.models.BucklerModel;
import tallestred.piglinproliferation.common.items.PPItems;

public class BucklerRenderer extends BlockEntityWithoutLevelRenderer {
    public final BucklerModel bucklerModel;

    public BucklerRenderer(BlockEntityRenderDispatcher berd, EntityModelSet set) {
        super(berd, set);
        this.bucklerModel = new BucklerModel(set.bakeLayer(PPRenderSetupEvents.BUCKLER));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext p_239207_2_, PoseStack matrixStack,
                             MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        Item item = stack.getItem();
        if (item == PPItems.BUCKLER.get()) {
            matrixStack.pushPose();
            matrixStack.scale(1.0F, -1.0F, -1.0F);
            Material rendermaterial = PPRenderSetupEvents.BUCKLER_TEXTURE;
            VertexConsumer ivertexbuilder = rendermaterial.sprite().wrap(ItemRenderer.getFoilBufferDirect(buffer,
                    this.bucklerModel.renderType(rendermaterial.atlasLocation()), true, stack.hasFoil()));
            this.bucklerModel.root.render(matrixStack, ivertexbuilder, combinedLight, combinedOverlay, -1);
            matrixStack.popPose();
        }
    }
}

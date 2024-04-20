package tallestred.piglinproliferation.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.common.items.BucklerItem;
import tallestred.piglinproliferation.common.items.PPItems;
import tallestred.piglinproliferation.configuration.PPConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = PiglinProliferation.MODID)
public class PPHandEvents {
    @SubscribeEvent
    public static void onMovementKeyPressed(MovementInputUpdateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (BucklerItem.getChargeTicks(PPItems.checkEachHandForBuckler(player)) > 0) {
            event.getInput().jumping = false;
            event.getInput().leftImpulse = 0;
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        PoseStack mStack = event.getPoseStack();
        ItemStack stack = event.getItemStack();
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        float partialTicks = event.getPartialTick();
        if (stack.getItem() instanceof BucklerItem && (player.isUsingItem() && player.getUseItem() == stack || BucklerItem.getChargeTicks(stack) > 0 && BucklerItem.isReady(stack))) {
            boolean mainHand = event.getHand() == InteractionHand.MAIN_HAND;
            HumanoidArm handside = mainHand ? player.getMainArm() : player.getMainArm().getOpposite();
            boolean rightHanded = handside == HumanoidArm.RIGHT;
            float f7 = (float) stack.getUseDuration() - ((float) player.getUseItemRemainingTicks() - partialTicks + 1.0F);
            float f11 = f7 / 10.0F;
            if (f11 > 1.0F) {
                f11 = 1.0F;
            }
            mStack.pushPose();
            int i = rightHanded ? 1 : -1;
            mStack.translate((float) i * 0.56F, -0.52F + event.getEquipProgress() * -0.6F, -0.72F);
            mStack.translate(f11 * (!rightHanded ? 0.2D : -0.2D), 0.0D, f11 * (!rightHanded ? 0.2D : -0.2D));
            ItemDisplayContext transform = rightHanded ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            Minecraft.getInstance().gameRenderer.itemInHandRenderer.renderItem(player, stack, transform, !rightHanded, mStack, event.getMultiBufferSource(), event.getPackedLight());
            mStack.popPose();
            event.setCanceled(true);
        }
    }
}

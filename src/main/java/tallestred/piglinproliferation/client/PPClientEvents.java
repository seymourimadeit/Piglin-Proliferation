package tallestred.piglinproliferation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.common.attribute.PPAttributes;
import tallestred.piglinproliferation.common.items.BucklerItem;
import tallestred.piglinproliferation.common.items.PPItems;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = PiglinProliferation.MODID)
public class PPClientEvents {
    @SubscribeEvent
    public static void onMovementKeyPressed(MovementInputUpdateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null && BucklerItem.getChargeTicks(PPItems.checkEachHandForBuckler(player)) > 0) {
            event.getInput().jumping = false;
            event.getInput().leftImpulse = 0;
        }
    }

    @SubscribeEvent
    public static void changeFov(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        if (player.getUseItem().getItem() instanceof BucklerItem) {
            int i = player.getTicksUsingItem();
            float f1 = (float) i * 0.3F;
            if (f1 < 1.0F) {
                f1 = 1.0F;
            }
            f1 = Math.max(f1, event.getFovModifier());
            event.setNewFovModifier(f1);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        PoseStack mStack = event.getPoseStack();
        ItemStack stack = event.getItemStack();
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        float partialTicks = event.getPartialTick();
        if (stack.getItem() instanceof BucklerItem && player != null && (player.isUsingItem() && player.getUseItem() == stack || BucklerItem.getChargeTicks(stack) > 0 && BucklerItem.isReady(stack))) {
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

    @SubscribeEvent
    public static void modifyItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() == PPItems.BUCKLER.get()) {
            List<Component> toAdd = new ArrayList<>();
            toAdd.add(Component.empty());
            toAdd.addAll(PPItems.BUCKLER.get().getDescription(stack));
            event.getToolTip().addAll(toAdd);
        }
    }

    @SubscribeEvent
    public static void onPlayerTurnCalculation(CalculatePlayerTurnEvent event) {
        double mouseSensitivity = event.getMouseSensitivity();
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            double turningValue = PPAttributes.turningValue(player);
            if (turningValue != 1)
                mouseSensitivity = (mouseSensitivity * turningValue) - 0.2F;
        }
        event.setMouseSensitivity(mouseSensitivity);
    }
}

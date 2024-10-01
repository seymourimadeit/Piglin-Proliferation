package tallestred.piglinproliferation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import tallestred.piglinproliferation.client.renderers.BucklerRenderer;
import tallestred.piglinproliferation.common.items.BucklerItem;
import tallestred.piglinproliferation.common.items.PPItems;
import tallestred.piglinproliferation.common.items.component.PPComponents;
import tallestred.piglinproliferation.common.items.component.TravelersCompassTracker;

@Mod(value = PiglinProliferation.MODID, dist = Dist.CLIENT)
public class PiglinProliferationClient {
    public PiglinProliferationClient(IEventBus modEventBus, Dist dist, ModContainer container) {
        modEventBus.addListener(this::doClientStuff);
        modEventBus.addListener(this::doClientExtensionsStuff);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private void doClientExtensionsStuff(final RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new BucklerRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
            }
        }, PPItems.BUCKLER.get());
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(PPItems.BUCKLER.get(), ResourceLocation.parse("blocking"),
                    (stack, clientWorld, livingEntity, useTime) -> {
                        boolean active = livingEntity != null && livingEntity.isUsingItem()
                                && livingEntity.getUseItem() == stack
                                || livingEntity != null && BucklerItem.isReady(stack);
                        return livingEntity != null && active ? 1.0F : 0.0F;
                    });
            ItemProperties.register(PPItems.TRAVELERS_COMPASS.get(), ResourceLocation.parse("angle"), new CompassItemPropertyFunction((level, itemStack, player) -> {
                TravelersCompassTracker tracker = itemStack.get(PPComponents.TRAVELERS_COMPASS_TRACKER);
                if (tracker != null)
                    return tracker.target();
                return null; //TODO not sure
            }));
        });
    }
}

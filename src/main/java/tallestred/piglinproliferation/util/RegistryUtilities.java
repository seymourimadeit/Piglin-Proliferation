package tallestred.piglinproliferation.util;

import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.util.MutableHashedLinkedMap;

import java.util.function.Function;

public class RegistryUtilities {
    public static void addToCreativeTabAfter(MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility> creativeTab, Item after, Item... toAdd) {
        if (toAdd.length > 0) {
            creativeTab.putAfter(new ItemStack(after), new ItemStack(toAdd[0]), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            if (toAdd.length > 1)
                for (int i = 1; i < toAdd.length; i++)
                    creativeTab.putAfter(new ItemStack(toAdd[i - 1]), new ItemStack(toAdd[i]), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    public static <T extends Mob, R extends LivingEntityRenderer<T, M>, M extends PiglinModel<T>> void addLayerToRenderer(EntityRenderersEvent.AddLayers event, EntityType<T> entityType, Function<R, ? extends RenderLayer<T, M>> factory) {
        R renderer = event.getRenderer(entityType);
        if (renderer != null) renderer.addLayer(factory.apply(renderer));
    }
}

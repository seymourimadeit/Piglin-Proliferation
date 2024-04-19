package tallestred.piglinproliferation.common.items;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.common.blocks.PPBlocks;
import tallestred.piglinproliferation.common.blocks.PiglinSkullBlock;
import tallestred.piglinproliferation.common.blocks.PiglinWallSkullBlock;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;

import java.util.HashMap;
import java.util.Map;

public class PPItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, PiglinProliferation.MODID);

    public static final DeferredHolder<Item, BucklerItem> BUCKLER = ITEMS.register("buckler", () -> new BucklerItem((new Item.Properties()).durability(128)));
    public static final DeferredHolder<Item, TravellersCompassItem> TRAVELLERS_COMPASS = ITEMS.register("travellers_compass", () -> new TravellersCompassItem(new Item.Properties()));

    //TODO this can be automated in the future
    public static final DeferredHolder<Item, DeferredSpawnEggItem> PIGLIN_ALCHEMIST_SPAWN_EGG = ITEMS.register("piglin_alchemist_spawn_egg", () -> new DeferredSpawnEggItem(PPEntityTypes.PIGLIN_ALCHEMIST, 10944611, 16380836, (new Item.Properties())));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> PIGLIN_TRAVELLER_SPAWN_EGG = ITEMS.register("piglin_traveller_spawn_egg", () -> new DeferredSpawnEggItem(PPEntityTypes.PIGLIN_TRAVELLER, 3848107, 16380836, (new Item.Properties())));

    public static ItemStack checkEachHandForBuckler(LivingEntity entity) {
        InteractionHand hand = entity.getMainHandItem().getItem() instanceof BucklerItem ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        return entity.getItemInHand(hand);
    }
}
package tallestred.piglinproliferation.common.items;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.common.blocks.PPBlocks;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;

public class PPItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, PiglinProliferation.MODID);

    public static final DeferredHolder<Item, BucklerItem> BUCKLER = ITEMS.register("buckler", () -> new BucklerItem((new Item.Properties()).durability(64)));
    public static final DeferredHolder<Item, TravellersCompassItem> TRAVELLERS_COMPASS = ITEMS.register("travellers_compass", () -> new TravellersCompassItem(new Item.Properties()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> PIGLIN_ALCHEMIST_SPAWN_EGG = ITEMS.register("piglin_alchemist_spawn_egg", () -> new DeferredSpawnEggItem(PPEntityTypes.PIGLIN_ALCHEMIST, 10944611, 16380836, (new Item.Properties())));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> PIGLIN_TRAVELLER_SPAWN_EGG = ITEMS.register("piglin_traveller_spawn_egg", () -> new DeferredSpawnEggItem(PPEntityTypes.PIGLIN_TRAVELLER, 3848107, 16380836, (new Item.Properties())));

    public static final DeferredHolder<Item, Item> PIGLIN_BRUTE_HEAD_ITEM = ITEMS.register("piglin_brute_head", () -> new StandingAndWallBlockItem(PPBlocks.PIGLIN_BRUTE_HEAD.get(), PPBlocks.PIGLIN_BRUTE_HEAD_WALL.get(), new Item.Properties().rarity(Rarity.UNCOMMON), Direction.DOWN));
    public static final DeferredHolder<Item, Item>ZOMBIFIED_PIGLIN_HEAD_ITEM = ITEMS.register("zombified_piglin_head", () -> new StandingAndWallBlockItem(PPBlocks.ZOMBIFIED_PIGLIN_HEAD.get(), PPBlocks.ZOMBIFIED_PIGLIN_HEAD_WALL.get(), new Item.Properties().rarity(Rarity.UNCOMMON), Direction.DOWN));
    public static final DeferredHolder<Item, Item> PIGLIN_ALCHEMIST_HEAD_ITEM = ITEMS.register("piglin_alchemist_head", () -> new StandingAndWallBlockItem(PPBlocks.PIGLIN_ALCHEMIST_HEAD.get(), PPBlocks.PIGLIN_ALCHEMIST_HEAD_WALL.get(), new Item.Properties().rarity(Rarity.UNCOMMON), Direction.DOWN));
    public static final DeferredHolder<Item, Item> PIGLIN_TRAVELLER_HEAD_ITEM = ITEMS.register("piglin_traveller_head", () -> new StandingAndWallBlockItem(PPBlocks.PIGLIN_TRAVELLER_HEAD.get(), PPBlocks.PIGLIN_TRAVELLER_HEAD_WALL.get(), new Item.Properties().rarity(Rarity.UNCOMMON), Direction.DOWN));

    public static final DeferredHolder<Item, Item> STONE_FIRE_RING_ITEM = ITEMS.register("stone_fire_ring", () -> new BlockItem(PPBlocks.STONE_FIRE_RING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> STONE_SOUL_FIRE_RING_ITEM = ITEMS.register("stone_soul_fire_ring", () -> new BlockItem(PPBlocks.STONE_SOUL_FIRE_RING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> DEEPSLATE_FIRE_RING_ITEM = ITEMS.register("deepslate_fire_ring", () -> new BlockItem(PPBlocks.DEEPSLATE_FIRE_RING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> DEEPSLATE_SOUL_FIRE_RING_ITEM = ITEMS.register("deepslate_soul_fire_ring", () -> new BlockItem(PPBlocks.DEEPSLATE_SOUL_FIRE_RING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> NETHERRACK_FIRE_RING_ITEM = ITEMS.register("netherrack_fire_ring", () -> new BlockItem(PPBlocks.NETHERRACK_FIRE_RING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> NETHERRACK_SOUL_FIRE_RING_ITEM = ITEMS.register("netherrack_soul_fire_ring", () -> new BlockItem(PPBlocks.NETHERRACK_SOUL_FIRE_RING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> BLACKSTONE_FIRE_RING_ITEM = ITEMS.register("blackstone_fire_ring", () -> new BlockItem(PPBlocks.BLACKSTONE_FIRE_RING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> BLACKSTONE_SOUL_FIRE_RING_ITEM = ITEMS.register("blackstone_soul_fire_ring", () -> new BlockItem(PPBlocks.BLACKSTONE_SOUL_FIRE_RING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> END_STONE_FIRE_RING_ITEM = ITEMS.register("end_stone_fire_ring", () -> new BlockItem(PPBlocks.END_STONE_FIRE_RING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> END_STONE_SOUL_FIRE_RING_ITEM = ITEMS.register("end_stone_soul_fire_ring", () -> new BlockItem(PPBlocks.END_STONE_SOUL_FIRE_RING.get(), new Item.Properties()));

    public static ItemStack checkEachHandForBuckler(LivingEntity entity) {
        InteractionHand hand = entity.getMainHandItem().getItem() instanceof BucklerItem ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        return entity.getItemInHand(hand);
    }
}
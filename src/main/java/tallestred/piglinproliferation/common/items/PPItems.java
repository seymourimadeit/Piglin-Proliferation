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
import tallestred.piglinproliferation.common.items.component.PPComponents;

import java.util.HashMap;
import java.util.Map;

public class PPItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, PiglinProliferation.MODID);

    public static final DeferredHolder<Item, BucklerItem> BUCKLER = ITEMS.register("buckler", () -> new BucklerItem((new Item.Properties()).component(PPComponents.BUCKLER_IS_READY, false).component(PPComponents.BUCKLER_CHARGE_TICKS, 0).durability(128)));
    public static final DeferredHolder<Item, TravelersCompassItem> TRAVELERS_COMPASS = ITEMS.register("travelers_compass", () -> new TravelersCompassItem(new Item.Properties()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> PIGLIN_ALCHEMIST_SPAWN_EGG = ITEMS.register("piglin_alchemist_spawn_egg", () -> new DeferredSpawnEggItem(PPEntityTypes.PIGLIN_ALCHEMIST, 10944611, 16380836, (new Item.Properties())));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> PIGLIN_TRAVELER_SPAWN_EGG = ITEMS.register("piglin_traveler_spawn_egg", () -> new DeferredSpawnEggItem(PPEntityTypes.PIGLIN_TRAVELER, 3848107, 16380836, (new Item.Properties())));

    public static final Map<DeferredHolder<Item, Item>, DeferredHolder<Block, PiglinSkullBlock>> PIGLIN_HEADS = new HashMap<>();

    public static final DeferredHolder<Item, Item> PIGLIN_BRUTE_HEAD_ITEM = registerHead(PPBlocks.PIGLIN_BRUTE_HEAD, PPBlocks.PIGLIN_BRUTE_HEAD_WALL);
    public static final DeferredHolder<Item, Item> ZOMBIFIED_PIGLIN_HEAD_ITEM = registerHead(PPBlocks.ZOMBIFIED_PIGLIN_HEAD, PPBlocks.ZOMBIFIED_PIGLIN_HEAD_WALL);
    public static final DeferredHolder<Item, Item> PIGLIN_ALCHEMIST_HEAD_ITEM = registerHead(PPBlocks.PIGLIN_ALCHEMIST_HEAD, PPBlocks.PIGLIN_ALCHEMIST_HEAD_WALL);
    public static final DeferredHolder<Item, Item> PIGLIN_TRAVELER_HEAD_ITEM = registerHead(PPBlocks.PIGLIN_TRAVELER_HEAD, PPBlocks.PIGLIN_TRAVELER_HEAD_WALL);

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

    //TODO do some dynamic block item thing in the future, but this works for now
    private static DeferredHolder<Item, Item> registerHead(DeferredHolder<Block, PiglinSkullBlock> head, DeferredHolder<Block, PiglinWallSkullBlock> wallHead) {
        DeferredHolder<Item, Item> registeredItem = ITEMS.register(head.unwrapKey().orElseThrow().location().getPath(), () -> new StandingAndWallBlockItem(head.get(), wallHead.get(), new Item.Properties().rarity(Rarity.UNCOMMON), Direction.DOWN));
        PIGLIN_HEADS.put(registeredItem, head);
        return registeredItem;
    }

    public static Item headItem(EntityType<?> entityType) {
        for (var entry : PIGLIN_HEADS.entrySet()) {
            if (entry.getValue().get().getType().getEntityType() == entityType)
                return entry.getKey().get();
        }
        return null;
    }
}
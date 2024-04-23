package tallestred.piglinproliferation.common.items;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.common.blocks.PPBlocks;
import tallestred.piglinproliferation.common.blocks.PiglinSkullBlock;
import tallestred.piglinproliferation.common.blocks.PiglinWallSkullBlock;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PiglinProliferation.MODID);

    public static final RegistryObject<BucklerItem> BUCKLER = ITEMS.register("buckler", () -> new BucklerItem((new Item.Properties()).durability(128)));
    public static final RegistryObject<TravellersCompassItem> TRAVELLERS_COMPASS = ITEMS.register("travellers_compass", () -> new TravellersCompassItem(new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> PIGLIN_ALCHEMIST_SPAWN_EGG = ITEMS.register("piglin_alchemist_spawn_egg", () -> new ForgeSpawnEggItem(PPEntityTypes.PIGLIN_ALCHEMIST, 10944611, 16380836, (new Item.Properties())));
    public static final RegistryObject<ForgeSpawnEggItem> PIGLIN_TRAVELLER_SPAWN_EGG = ITEMS.register("piglin_traveller_spawn_egg", () -> new ForgeSpawnEggItem(PPEntityTypes.PIGLIN_TRAVELLER, 3848107, 16380836, (new Item.Properties())));

    public static final Map<RegistryObject<Item>, RegistryObject<PiglinSkullBlock>> PIGLIN_HEADS = new HashMap<>();

    public static final RegistryObject<Item> PIGLIN_BRUTE_HEAD_ITEM = ITEMS.register("piglin_brute_head", () -> new StandingAndWallBlockItem(PPBlocks.PIGLIN_BRUTE_HEAD.get(), PPBlocks.PIGLIN_BRUTE_HEAD_WALL.get(), new Item.Properties().rarity(Rarity.UNCOMMON), Direction.DOWN));
    public static final RegistryObject<Item> ZOMBIFIED_PIGLIN_HEAD_ITEM = ITEMS.register("zombified_piglin_head", () -> new StandingAndWallBlockItem(PPBlocks.ZOMBIFIED_PIGLIN_HEAD.get(), PPBlocks.ZOMBIFIED_PIGLIN_HEAD_WALL.get(), new Item.Properties().rarity(Rarity.UNCOMMON), Direction.DOWN));
    public static final RegistryObject<Item> PIGLIN_ALCHEMIST_HEAD_ITEM = ITEMS.register("piglin_alchemist_head", () -> new StandingAndWallBlockItem(PPBlocks.PIGLIN_ALCHEMIST_HEAD.get(), PPBlocks.PIGLIN_ALCHEMIST_HEAD_WALL.get(), new Item.Properties().rarity(Rarity.UNCOMMON), Direction.DOWN));
    public static final RegistryObject<Item> PIGLIN_TRAVELLER_HEAD_ITEM = ITEMS.register("piglin_traveller_head", () -> new StandingAndWallBlockItem(PPBlocks.PIGLIN_TRAVELLER_HEAD.get(), PPBlocks.PIGLIN_TRAVELLER_HEAD_WALL.get(), new Item.Properties().rarity(Rarity.UNCOMMON), Direction.DOWN));
    
    public static final RegistryObject<Item> STONE_FIRE_RING_ITEM = ITEMS.register("stone_fire_ring", () -> new BlockItem(PPBlocks.STONE_FIRE_RING.get(), new Item.Properties()));
    public static final RegistryObject<Item> STONE_SOUL_FIRE_RING_ITEM = ITEMS.register("stone_soul_fire_ring", () -> new BlockItem(PPBlocks.STONE_SOUL_FIRE_RING.get(), new Item.Properties()));
    public static final RegistryObject<Item> DEEPSLATE_FIRE_RING_ITEM = ITEMS.register("deepslate_fire_ring", () -> new BlockItem(PPBlocks.DEEPSLATE_FIRE_RING.get(), new Item.Properties()));
    public static final RegistryObject<Item> DEEPSLATE_SOUL_FIRE_RING_ITEM = ITEMS.register("deepslate_soul_fire_ring", () -> new BlockItem(PPBlocks.DEEPSLATE_SOUL_FIRE_RING.get(), new Item.Properties()));
    public static final RegistryObject<Item> NETHERRACK_FIRE_RING_ITEM = ITEMS.register("netherrack_fire_ring", () -> new BlockItem(PPBlocks.NETHERRACK_FIRE_RING.get(), new Item.Properties()));
    public static final RegistryObject<Item> NETHERRACK_SOUL_FIRE_RING_ITEM = ITEMS.register("netherrack_soul_fire_ring", () -> new BlockItem(PPBlocks.NETHERRACK_SOUL_FIRE_RING.get(), new Item.Properties()));
    public static final RegistryObject<Item> BLACKSTONE_FIRE_RING_ITEM = ITEMS.register("blackstone_fire_ring", () -> new BlockItem(PPBlocks.BLACKSTONE_FIRE_RING.get(), new Item.Properties()));
    public static final RegistryObject<Item> BLACKSTONE_SOUL_FIRE_RING_ITEM = ITEMS.register("blackstone_soul_fire_ring", () -> new BlockItem(PPBlocks.BLACKSTONE_SOUL_FIRE_RING.get(), new Item.Properties()));
    public static final RegistryObject<Item> END_STONE_FIRE_RING_ITEM = ITEMS.register("end_stone_fire_ring", () -> new BlockItem(PPBlocks.END_STONE_FIRE_RING.get(), new Item.Properties()));
    public static final RegistryObject<Item> END_STONE_SOUL_FIRE_RING_ITEM = ITEMS.register("end_stone_soul_fire_ring", () -> new BlockItem(PPBlocks.END_STONE_SOUL_FIRE_RING.get(), new Item.Properties()));


    public static ItemStack checkEachHandForBuckler(LivingEntity entity) {
        InteractionHand hand = entity.getMainHandItem().getItem() instanceof BucklerItem ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        return entity.getItemInHand(hand);
    }

    //TODO do some dynamic block item thing in the future, but this works for now
    private static RegistryObject<Item> registerHead(RegistryObject<PiglinSkullBlock> head, RegistryObject<PiglinWallSkullBlock> wallHead) {
        RegistryObject<Item> registeredItem = ITEMS.register(Optional.ofNullable(head.getKey()).orElseThrow().location().getPath(), () -> new StandingAndWallBlockItem(head.get(), wallHead.get(), new Item.Properties().rarity(Rarity.UNCOMMON), Direction.DOWN));
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
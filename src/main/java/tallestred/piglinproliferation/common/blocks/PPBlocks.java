package tallestred.piglinproliferation.common.blocks;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PiglinWallSkullBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestred.piglinproliferation.PiglinProliferation;

import java.util.HashSet;
import java.util.Set;

import static tallestred.piglinproliferation.common.items.PPItems.ITEMS;

public class PPBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, PiglinProliferation.MODID);
    public static final BlockItemHolder<PiglinSkullBlock, StandingAndWallBlockItem> PIGLIN_BRUTE_HEAD = registerHead("piglin_brute", PiglinSkullBlock.Types.PIGLIN_BRUTE);
    public static final BlockItemHolder<PiglinSkullBlock, StandingAndWallBlockItem> ZOMBIFIED_PIGLIN_HEAD = registerHead("zombified_piglin", PiglinSkullBlock.Types.ZOMBIFIED_PIGLIN);
    public static final BlockItemHolder<PiglinSkullBlock, StandingAndWallBlockItem> PIGLIN_ALCHEMIST_HEAD = registerHead("piglin_alchemist_head", PiglinSkullBlock.Types.PIGLIN_ALCHEMIST);
    public static final BlockItemHolder<PiglinSkullBlock, StandingAndWallBlockItem> PIGLIN_TRAVELLER_HEAD = registerHead("piglin_traveller_head", PiglinSkullBlock.Types.PIGLIN_TRAVELLER);
    public static final BlockItemHolder<FireRingBlock, BlockItem> STONE_FIRE_RING = registerFireRing("stone_fire_ring", false, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE));
    public static final BlockItemHolder<FireRingBlock, BlockItem> STONE_SOUL_FIRE_RING = registerFireRing("stone_soul_fire_ring", true, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE));
    public static final BlockItemHolder<FireRingBlock, BlockItem> DEEPSLATE_FIRE_RING = registerFireRing("deepslate_fire_ring", false, BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundType.DEEPSLATE));
    public static final BlockItemHolder<FireRingBlock, BlockItem> DEEPSLATE_SOUL_FIRE_RING = registerFireRing("deepslate_soul_fire_ring", true, BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundType.DEEPSLATE));
    public static final BlockItemHolder<FireRingBlock, BlockItem> NETHERRACK_FIRE_RING = registerFireRing("netherrack_fire_ring", false, BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.4F).sound(SoundType.NETHERRACK));
    public static final BlockItemHolder<FireRingBlock, BlockItem> NETHERRACK_SOUL_FIRE_RING = registerFireRing("netherrack_soul_fire_ring", true, BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.4F).sound(SoundType.NETHERRACK));
    public static final BlockItemHolder<FireRingBlock, BlockItem> BLACKSTONE_FIRE_RING = registerFireRing("blackstone_fire_ring", false, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE));
    public static final BlockItemHolder<FireRingBlock, BlockItem> BLACKSTONE_SOUL_FIRE_RING = registerFireRing("blackstone_soul_fire_ring", true, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE));
    public static final BlockItemHolder<FireRingBlock, BlockItem> END_STONE_FIRE_RING = registerFireRing("end_stone_fire_ring", false, BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 9.0F));
    public static final BlockItemHolder<FireRingBlock, BlockItem> END_STONE_SOUL_FIRE_RING = registerFireRing("end_stone_soul_fire_ring", true, BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 9.0F));

    private static BlockItemHolder<FireRingBlock, BlockItem> registerFireRing(String name, boolean isSoulFire, BlockBehaviour.Properties properties) {
        BlockItemHolder<FireRingBlock, BlockItem> registeredBlock = BLOCKS.register(name, () -> new FireRingBlock(!isSoulFire, isSoulFire ? 2 : 1, isSoulFire ? 1200 : 600, properties.lightLevel(Blocks.litBlockEmission(isSoulFire ? 5 : 10)).noOcclusion().ignitedByLava()));
        FIRE_RINGS.add(registeredBlock);
        return registeredBlock;
    }

    // TODO find a better way to automate this; I'd do it right now but I have assignments to finish and it's 10:49 pm
    private static BlockItemHolder<PiglinSkullBlock, StandingAndWallBlockItem> registerHead(String name, PiglinSkullBlock.Types type) {
        var head = BLOCKS.register(name, () -> new PiglinSkullBlock(type, BlockBehaviour.Properties.of().strength(1.0F).pushReaction(PushReaction.DESTROY)));
        var wallHead = BLOCKS.register(name, () -> new PiglinWallSkullBlock(BlockBehaviour.Properties.of().strength(1.0F).pushReaction(PushReaction.DESTROY)));
        return new BlockItemHolder<>(head, ITEMS.register(name, () -> new StandingAndWallBlockItem(head.get(), wallHead.get(), new Item.Properties().rarity(Rarity.UNCOMMON), Direction.DOWN)));
    }
}

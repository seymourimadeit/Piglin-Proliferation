package tallestred.piglinproliferation.common.blocks;

import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tallestred.piglinproliferation.PiglinProliferation;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PiglinProliferation.MODID);

    public static final Set<RegistryObject<? extends AbstractSkullBlock>> PIGLIN_HEADS = new HashSet<>();
    public static final RegistryObject<PiglinSkullBlock> PIGLIN_BRUTE_HEAD = registerHead("piglin_brute_head", PiglinSkullBlock.Types.PIGLIN_BRUTE);
    public static final RegistryObject<PiglinWallSkullBlock> PIGLIN_BRUTE_HEAD_WALL = registerWallHead("piglin_brute_wall_head", PiglinSkullBlock.Types.PIGLIN_BRUTE);
    public static final RegistryObject<PiglinSkullBlock> ZOMBIFIED_PIGLIN_HEAD = registerHead("zombified_piglin_head", PiglinSkullBlock.Types.ZOMBIFIED_PIGLIN);
    public static final RegistryObject<PiglinWallSkullBlock> ZOMBIFIED_PIGLIN_HEAD_WALL = registerWallHead("zombified_piglin_wall_head", PiglinSkullBlock.Types.ZOMBIFIED_PIGLIN);
    public static final RegistryObject<PiglinSkullBlock> PIGLIN_ALCHEMIST_HEAD = registerHead("piglin_alchemist_head", PiglinSkullBlock.Types.PIGLIN_ALCHEMIST);
    public static final RegistryObject<PiglinWallSkullBlock> PIGLIN_ALCHEMIST_HEAD_WALL = registerWallHead("piglin_alchemist_wall_head", PiglinSkullBlock.Types.PIGLIN_ALCHEMIST);
    public static final RegistryObject<PiglinSkullBlock> PIGLIN_TRAVELER_HEAD = registerHead("piglin_traveler_head", PiglinSkullBlock.Types.PIGLIN_TRAVELER);
    public static final RegistryObject<PiglinWallSkullBlock> PIGLIN_TRAVELER_HEAD_WALL =  registerWallHead("piglin_traveler_wall_head", PiglinSkullBlock.Types.PIGLIN_TRAVELER);

    public static final Set<RegistryObject<FireRingBlock>> FIRE_RINGS = new HashSet<>();
    public static final RegistryObject<FireRingBlock> STONE_FIRE_RING = registerFireRing("stone_fire_ring", false, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE));
    public static final RegistryObject<FireRingBlock> STONE_SOUL_FIRE_RING = registerFireRing("stone_soul_fire_ring", true, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE));
    public static final RegistryObject<FireRingBlock> DEEPSLATE_FIRE_RING = registerFireRing("deepslate_fire_ring", false, BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundType.DEEPSLATE));
    public static final RegistryObject<FireRingBlock> DEEPSLATE_SOUL_FIRE_RING = registerFireRing("deepslate_soul_fire_ring", true, BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundType.DEEPSLATE));
    public static final RegistryObject<FireRingBlock> NETHERRACK_FIRE_RING = registerFireRing("netherrack_fire_ring", false, BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.4F).sound(SoundType.NETHERRACK));
    public static final RegistryObject<FireRingBlock> NETHERRACK_SOUL_FIRE_RING = registerFireRing("netherrack_soul_fire_ring", true, BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.4F).sound(SoundType.NETHERRACK));
    public static final RegistryObject<FireRingBlock> END_STONE_FIRE_RING = registerFireRing("end_stone_fire_ring", false, BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 9.0F) );
    public static final RegistryObject<FireRingBlock> END_STONE_SOUL_FIRE_RING = registerFireRing("end_stone_soul_fire_ring", true, BlockBehaviour.Properties.of().mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 9.0F));
    public static final RegistryObject<FireRingBlock> BLACKSTONE_FIRE_RING = registerFireRing("blackstone_fire_ring", false, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE));
    public static final RegistryObject<FireRingBlock> BLACKSTONE_SOUL_FIRE_RING = registerFireRing("blackstone_soul_fire_ring", true, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE));

    private static RegistryObject<FireRingBlock> registerFireRing(String name,  boolean isSoulFire, BlockBehaviour.Properties properties) {
        RegistryObject<FireRingBlock> registeredBlock = BLOCKS.register(name, () -> new FireRingBlock(!isSoulFire, isSoulFire ? 2 : 1, isSoulFire? 410 : 210, properties.lightLevel(Blocks.litBlockEmission(isSoulFire ? 5 : 10)).noOcclusion().ignitedByLava()));
        FIRE_RINGS.add(registeredBlock);
        return registeredBlock;
    }

    // TODO find a better way to automate this; I'd do it right now but I have assignments to finish and it's 10:49 pm
    private static RegistryObject<PiglinSkullBlock> registerHead(String name, PiglinSkullBlock.Types type) {
        RegistryObject<PiglinSkullBlock> registeredBlock = BLOCKS.register(name, () -> new PiglinSkullBlock(type, BlockBehaviour.Properties.of().strength(1.0F).pushReaction(PushReaction.DESTROY)));
        PIGLIN_HEADS.add(registeredBlock);
        return registeredBlock;
    }

    private static RegistryObject<PiglinWallSkullBlock> registerWallHead(String name, PiglinSkullBlock.Types type) {
        RegistryObject<PiglinWallSkullBlock> registeredBlock = BLOCKS.register(name, () -> new PiglinWallSkullBlock(type, BlockBehaviour.Properties.of().strength(1.0F).pushReaction(PushReaction.DESTROY)));
        PIGLIN_HEADS.add(registeredBlock);
        return registeredBlock;
    }
}

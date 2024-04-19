package tallestred.piglinproliferation.common.blockentities;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.common.blocks.FireRingBlock;
import tallestred.piglinproliferation.common.blocks.PPBlocks;
import tallestred.piglinproliferation.common.blocks.PiglinSkullBlock;

public class PPBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PiglinProliferation.MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PiglinSkullBlockEntity>> PIGLIN_SKULL = BLOCK_ENTITIES.register("piglin_head", () -> BlockEntityType.Builder.of(PiglinSkullBlockEntity::new, PiglinSkullBlock.PIGLIN_HEADS.values()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FireRingBlockEntity>> FIRE_RING = BLOCK_ENTITIES.register("fire_ring", () -> BlockEntityType.Builder.of(FireRingBlockEntity::new, FireRingBlock.FIRE_RINGS.values()).build(null));
}

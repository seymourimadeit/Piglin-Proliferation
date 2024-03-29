package tallestred.piglinproliferation.common.blockentities;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.common.blocks.PPBlocks;

public class PPBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PiglinProliferation.MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PiglinSkullBlockEntity>> PIGLIN_SKULL = BLOCK_ENTITIES.register("piglin_head", () -> BlockEntityType.Builder.of(PiglinSkullBlockEntity::new, PPBlocks.PIGLIN_HEADS.stream().map(DeferredHolder::get).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FireRingBlockEntity>> FIRE_RING = BLOCK_ENTITIES.register("fire_ring", () -> BlockEntityType.Builder.of(FireRingBlockEntity::new, PPBlocks.FIRE_RINGS.stream().map(DeferredHolder::get).toArray(Block[]::new)).build(null));
}

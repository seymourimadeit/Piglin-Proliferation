package tallestred.piglinproliferation.common.blockentities;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.common.blocks.PPBlocks;

public class PPBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, PiglinProliferation.MODID);
    public static final RegistryObject<BlockEntityType<PiglinSkullBlockEntity>> PIGLIN_SKULL = BLOCK_ENTITIES.register("piglin_head", () -> BlockEntityType.Builder.of(PiglinSkullBlockEntity::new, PPBlocks.PIGLIN_HEADS.stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));
    public static final RegistryObject<BlockEntityType<FireRingBlockEntity>> FIRE_RING = BLOCK_ENTITIES.register("fire_ring", () -> BlockEntityType.Builder.of(FireRingBlockEntity::new, PPBlocks.FIRE_RINGS.stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));
}

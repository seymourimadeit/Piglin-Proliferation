package tallestred.piglinproliferation.common.worldgen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestred.piglinproliferation.PiglinProliferation;

import java.util.ArrayList;
import java.util.List;

public class PPWorldgen {
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(
            Registries.PROCESSOR_LIST, new ResourceLocation("minecraft", "empty"));
    public static DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, PiglinProliferation.MODID);
    public static DeferredHolder<StructureType<?>, StructureType<CustomJigsawStructure>> CUSTOM_JIGSAW = STRUCTURE_TYPES.register("custom_jigsaw", () -> () -> CustomJigsawStructure.CODEC);

    /**
     * Adds the building to the targeted pool.
     * <p>
     * used from <a href="https://gist.github.com/TelepathicGrunt/4fdbc445ebcbcbeb43ac748f4b18f342">https://gist.github.com/TelepathicGrunt/4fdbc445ebcbcbeb43ac748f4b18f342</a>
     * Note: This is an additive operation which means multiple mods can do this and they stack with each other safely.
     */
    public static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry,
                                         Registry<StructureProcessorList> processorListRegistry,
                                         ResourceLocation poolRL,
                                         String nbtPieceRL,
                                         int weight) {

        // Grabs the processor list we want to use along with our piece.
        // This is a requirement as using the ProcessorLists.EMPTY field will cause the game to throw errors.
        // The reason why is the empty processor list in the world's registry is not the same instance as in that field once the world is started up.
        Holder<StructureProcessorList> emptyProcessorList = processorListRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);

        // Grab the pool we want to add to
        StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
        if (pool == null) return;

        // Grabs the nbt piece and creates a SinglePoolElement of it that we can add to a structure's pool.
        SinglePoolElement piece = SinglePoolElement.single(nbtPieceRL, emptyProcessorList).apply(StructureTemplatePool.Projection.RIGID);

        // Use AccessTransformer or Accessor Mixin to make StructureTemplatePool's templates field public for us to see.
        // Weight is handled by how many times the entry appears in this list.
        // We do not need to worry about immutability as this field is created using Lists.newArrayList(); which makes a mutable list.
        for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
        }

        // Use AccessTransformer or Accessor Mixin to make StructureTemplatePool's rawTemplates field public for us to see.
        // This list of pairs of pieces and weights is not used by vanilla by default but another mod may need it for efficiency.
        // So let's add to this list for completeness. We need to make a copy of the array as it can be an immutable list.
        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
        listOfPieceEntries.add(new Pair<>(piece, weight));
        pool.rawTemplates = listOfPieceEntries;
    }

    /**
     * @author telepathicgrunt
     *
     * <p>Finds the highest possible surface land in the nether, used to prevent structures from generating in solid terrain.</p>
     */
    public static BlockPos getHighestLand(ChunkGenerator chunkGenerator, RandomState randomState, BoundingBox boundingBox, LevelHeightAccessor heightLimitView) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(boundingBox.getCenter().getX(), (chunkGenerator.getMinY() + chunkGenerator.getGenDepth()) - 40, boundingBox.getCenter().getZ());
        NoiseColumn blockView = chunkGenerator.getBaseColumn(mutable.getX(), mutable.getZ(), heightLimitView, randomState);
        BlockState currentBlockstate;
        while (mutable.getY() > chunkGenerator.getSeaLevel()) {
            currentBlockstate = blockView.getBlock(mutable.getY());
            if (!currentBlockstate.canOcclude()) {
                mutable.move(Direction.DOWN);
                continue;
            } else if (blockView.getBlock(mutable.getY() + 3).isAir() && currentBlockstate.getFluidState().isEmpty()) {
                int oldX = mutable.getX();
                int oldZ = mutable.getZ();
                Direction direction = Direction.NORTH;
                boolean hasSolidNeighbours = true;
                for (int i = 0; i < 5; i++) {
                    mutable.move(direction, i < 2 ? 1 : 2);
                    direction = direction.getClockWise();
                    BlockState tempBlockState = chunkGenerator.getBaseColumn(mutable.getX(), mutable.getZ(), heightLimitView, randomState).getBlock(mutable.getY());
                    if (!(tempBlockState.canOcclude() && tempBlockState.getFluidState().isEmpty()))
                        hasSolidNeighbours = false;
                }
                mutable.setX(oldX);
                mutable.setZ(oldZ);
                if (hasSolidNeighbours)
                    return mutable;
            }
            mutable.move(Direction.DOWN);
        }
        return null;
    }
}

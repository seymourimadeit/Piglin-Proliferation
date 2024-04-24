package tallestred.piglinproliferation.common.items;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static tallestred.piglinproliferation.util.CodeUtilities.snakeCaseToEnglish;

public class TravelersCompassItem extends CompassItem {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TRANSLATION_PREFIX = "item.piglinproliferation.travelers_compass.desc.";

    public TravelersCompassItem(Properties pProperties) {
        super(pProperties);
    }

    private static Optional<ResourceKey<Level>> getDimension(CompoundTag pCompoundTag) {
        return Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, pCompoundTag.get("Dimension")).result();
    }

    @Nullable
    public static GlobalPos getPosition(CompoundTag tag) {
        boolean flag = tag.contains("Position");
        boolean flag1 = tag.contains("Dimension");
        if (flag && flag1) {
            Optional<ResourceKey<Level>> optional = getDimension(tag);
            if (optional.isPresent()) {
                BlockPos blockpos = NbtUtils.readBlockPos(tag.getCompound("Position"));
                return GlobalPos.of(optional.get(), blockpos);
            }
        }

        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag tooltip) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (!tag.getString("Destination").isEmpty()) {
                list.add((Component.translatable(TRANSLATION_PREFIX + "locked")).withStyle(ChatFormatting.BLUE));
                String raw = tag.getString("Destination");
                boolean hasBiome = tag.getBoolean("HasBiome");
                list.add(translatable(new ResourceLocation(raw), hasBiome).withStyle(ChatFormatting.GRAY));
                GlobalPos globalPos = getPosition(tag);
                if (globalPos != null) {
                    BlockPos pos = globalPos.pos();
                    list.add((Component.translatable(pos.getX() + ", " + (hasBiome ? String.valueOf(pos.getY()) : "~") + ", " + (pos.getZ())).withStyle(ChatFormatting.GRAY)));
                }
            }
        }
    }

    public void addTags(ResourceKey<Level> pLodestoneDimension, BlockPos pLodestonePos, CompoundTag pCompoundTag, ResourceLocation searchObjectLocation, boolean hasBiome) {
        pCompoundTag.put("Position", NbtUtils.writeBlockPos(pLodestonePos));
        pCompoundTag.putBoolean("HasBiome", hasBiome);
        pCompoundTag.putString("Destination", searchObjectLocation.toString());
        Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, pLodestoneDimension).resultOrPartial(LOGGER::error).ifPresent((p_40731_) -> {
            pCompoundTag.put("Dimension", p_40731_);
        });
    }

    public MutableComponent translatable(ResourceLocation location, boolean isBiome) {
        MutableComponent returnComponent;
        String objectKey = (isBiome ? "biome" : "structure") + "." + location.getNamespace() + "." + location.getPath();
        String manualKey = TRANSLATION_PREFIX + objectKey;
        returnComponent = Component.translatableWithFallback(manualKey, "");
        if (returnComponent.getString().isEmpty())
            returnComponent = Component.translatableWithFallback(objectKey, snakeCaseToEnglish(location.getPath()));
        //Biomes almost certainly use the same format as the object key, but structures have to rely on the fallback.
        return returnComponent;
    }

    public BlockPos search(Either<Holder<Biome>, Holder<Structure>> searchObject, BlockPos sourcePos, ServerLevel level) {
        Pair<BlockPos, ?> output;
        if (searchObject.left().isPresent())
            output = level.findClosestBiome3d(holder -> holder.equals(searchObject.left().orElseThrow()), sourcePos, 64000, 32, 64);
        else output = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(searchObject.right().orElseThrow()), sourcePos, 50, false);
        return output != null ? output.getFirst() : null;
    }

    public boolean entityAtSearchObject(Either<Holder<Biome>, Holder<Structure>> searchObject, LivingEntity entity) {
        BlockPos blockPos = entity.getOnPos();
        if (entity.level() instanceof ServerLevel level && level.isLoaded(blockPos)) {
            return searchObject.left().isPresent() ?
                    level.getBiome(blockPos).value().equals(searchObject.left().orElseThrow().value()) :
                    level.structureManager().getStructureWithPieceAt(blockPos, searchObject.right().orElseThrow().value()).isValid();
        }
        return false;
    }
}

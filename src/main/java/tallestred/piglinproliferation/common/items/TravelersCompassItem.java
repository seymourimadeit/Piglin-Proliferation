package tallestred.piglinproliferation.common.items;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import tallestred.piglinproliferation.common.items.component.PPComponents;
import tallestred.piglinproliferation.common.items.component.TravelersCompassTracker;

import java.util.List;
import java.util.Optional;

import static tallestred.piglinproliferation.util.CodeUtilities.snakeCaseToEnglish;

public class TravelersCompassItem extends CompassItem {
    private static final String TRANSLATION_PREFIX = "item.piglinproliferation.travelers_compass.desc.";

    public TravelersCompassItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag tooltip) {
        TravelersCompassTracker tracker = stack.get(PPComponents.TRAVELERS_COMPASS_TRACKER);
        if (tracker != null) {
            list.add((Component.translatable(TRANSLATION_PREFIX + "locked")).withStyle(ChatFormatting.BLUE));
            list.add(translatable(tracker.targetID(), tracker.isBiome()).withStyle(ChatFormatting.GRAY));
                BlockPos pos = tracker.target().pos();
                list.add((Component.translatable(pos.getX() + ", " + (tracker.isBiome() ? String.valueOf(pos.getY()) : "~") + ", " + (pos.getZ())).withStyle(ChatFormatting.GRAY)));
        }
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

    public Optional<BlockPos> search(Either<Holder<Biome>, Holder<Structure>> searchObject, BlockPos sourcePos, ServerLevel level) {
        Pair<BlockPos, ?> output;
        if (searchObject.left().isPresent())
            output = level.findClosestBiome3d(holder -> holder.equals(searchObject.left().orElseThrow()), sourcePos, 64000, 32, 64);
        else output = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(searchObject.right().orElseThrow()), sourcePos, 50, false);
        return output != null ? Optional.of(output.getFirst()) : Optional.empty();
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

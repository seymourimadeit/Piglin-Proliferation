package tallestred.piglinproliferation.common.items;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import tallestred.piglinproliferation.common.loot.CompassLocationMap;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class TravellersCompassItem extends CompassItem {
    private static final Logger LOGGER = LogUtils.getLogger();

    public TravellersCompassItem(Properties pProperties) {
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
                list.add((Component.translatable("item.piglinproliferation.travellers_compass.desc.locked")).withStyle(ChatFormatting.BLUE));
                String raw = tag.getString("Destination");
                boolean hasBiome = tag.getBoolean("HasBiome");
                list.add(new CompassLocationMap.SearchObject(hasBiome, raw).translatable().withStyle(ChatFormatting.GRAY));
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
}

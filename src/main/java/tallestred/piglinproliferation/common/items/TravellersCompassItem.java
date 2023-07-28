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
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

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

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag tooltip) {
        list.add((Component.translatable("Locked on to " + stack.getTag().getString("Destination"))).withStyle(ChatFormatting.BLUE));
    }

    @Nullable
    public static GlobalPos getPosition(CompoundTag p_220022_) {
        boolean flag = p_220022_.contains("Position");
        boolean flag1 = p_220022_.contains("Dimension");
        if (flag && flag1) {
            Optional<ResourceKey<Level>> optional = getDimension(p_220022_);
            if (optional.isPresent()) {
                BlockPos blockpos = NbtUtils.readBlockPos(p_220022_.getCompound("Position"));
                return GlobalPos.of(optional.get(), blockpos);
            }
        }

        return null;
    }

    public void addTags(ResourceKey<Level> pLodestoneDimension, BlockPos pLodestonePos, CompoundTag pCompoundTag, String point) {
        pCompoundTag.put("Position", NbtUtils.writeBlockPos(pLodestonePos));
        pCompoundTag.putString("Destination", point);
        Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, pLodestoneDimension).resultOrPartial(LOGGER::error).ifPresent((p_40731_) -> {
            pCompoundTag.put("Dimension", p_40731_);
        });
    }
}

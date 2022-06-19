package tallestred.piglinproliferation.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import tallestred.piglinproliferation.PiglinProliferation;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPLootTables {
    public static final ResourceLocation ALCHEMIST_BARTER = new ResourceLocation(PiglinProliferation.MODID,
            "gameplay/alchemist_bartering");
}

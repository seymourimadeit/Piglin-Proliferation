package tallestred.piglinproliferation.common;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PiglinProliferation.MODID);
    public static final RegistryObject<ForgeSpawnEggItem> PIGLIN_ALCHEMIST_SPAWN_EGG = ITEMS.register("piglin_alchemist_spawn_egg", () -> new ForgeSpawnEggItem(PPEntityTypes.PIGLIN_ALCHEMIST, 10944611, 16380836, (new Item.Properties()).tab(CreativeModeTab.TAB_MISC)));
}
package tallestred.piglinproliferation.common;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.common.blocks.PPBlocks;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PiglinProliferation.MODID);
    public static final RegistryObject<ForgeSpawnEggItem> PIGLIN_ALCHEMIST_SPAWN_EGG = ITEMS.register("piglin_alchemist_spawn_egg", () -> new ForgeSpawnEggItem(PPEntityTypes.PIGLIN_ALCHEMIST, 10944611, 16380836, (new Item.Properties()).tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> PIGLIN_HEAD_ITEM = ITEMS.register("piglin_head", () -> new StandingAndWallBlockItem(PPBlocks.PIGLIN_HEAD.get(), PPBlocks.PIGLIN_HEAD_WALL.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> PIGLIN_BRUTE_HEAD_ITEM = ITEMS.register("piglin_brute_head", () -> new StandingAndWallBlockItem(PPBlocks.PIGLIN_BRUTE_HEAD.get(), PPBlocks.PIGLIN_BRUTE_HEAD_WALL.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> ZOMBIFIED_PIGLIN_HEAD_ITEM = ITEMS.register("zombified_piglin_head", () -> new StandingAndWallBlockItem(PPBlocks.ZOMBIFIED_PIGLIN_HEAD.get(), PPBlocks.ZOMBIFIED_PIGLIN_HEAD_WALL.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> PIGLIN_ALCHEMIST_HEAD_ITEM = ITEMS.register("piglin_alchemist_head", () -> new StandingAndWallBlockItem(PPBlocks.PIGLIN_ALCHEMIST_HEAD.get(), PPBlocks.PIGLIN_ALCHEMIST_HEAD_WALL.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS).rarity(Rarity.UNCOMMON)));
}
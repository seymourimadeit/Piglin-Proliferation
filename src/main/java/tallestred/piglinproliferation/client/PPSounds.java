package tallestred.piglinproliferation.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tallestred.piglinproliferation.PiglinProliferation;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PiglinProliferation.MODID);
    public static final RegistryObject<SoundEvent> ALCHEMIST_ABOUT_TO_THROW_POTION = SOUNDS.register("entity.piglin_alchemist.throw_alert", () -> new SoundEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.throw_alert")));
}

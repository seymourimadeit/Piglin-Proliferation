package tallestred.piglinproliferation;

import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPActivities {
    public static final DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create(ForgeRegistries.ACTIVITIES, PiglinProliferation.MODID);
    public static final RegistryObject<Activity> THROW_POTION_ACTIVITY = ACTIVITIES.register("throw_potion_activity",() -> new Activity("throw_potion_activity"));
}

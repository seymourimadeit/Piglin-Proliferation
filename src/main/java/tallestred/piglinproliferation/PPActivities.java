package tallestred.piglinproliferation;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PPActivities {
    public static final DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create(Registries.ACTIVITY, PiglinProliferation.MODID);
    public static final DeferredHolder<Activity, Activity> THROW_POTION_ACTIVITY = ACTIVITIES.register("throw_potion_activity",() -> new Activity("throw_potion_activity"));
}

package tallestred.piglinproliferation.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tallestred.piglinproliferation.PiglinProliferation;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PiglinProliferation.MODID);
    public static final RegistryObject<SoundEvent> ALCHEMIST_ABOUT_TO_THROW_POTION = SOUNDS.register("entity.piglin_alchemist.throw_alert", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.throw_alert")));
    public static final RegistryObject<SoundEvent> ALCHEMIST_WALK = SOUNDS.register("entity.piglin_alchemist.walk", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.walk")));
    public static final RegistryObject<SoundEvent> ALCHEMIST_IDLE = SOUNDS.register("entity.piglin_alchemist.idle", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.idle")));
    public static final RegistryObject<SoundEvent> ALCHEMIST_HURT = SOUNDS.register("entity.piglin_alchemist.hurt", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.hurt")));
    public static final RegistryObject<SoundEvent> ALCHEMIST_ADMIRE = SOUNDS.register("entity.piglin_alchemist.admire", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.admire")));
    public static final RegistryObject<SoundEvent> ALCHEMIST_JEALOUS = SOUNDS.register("entity.piglin_alchemist.jealous", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.jealous")));
    public static final RegistryObject<SoundEvent> ALCHEMIST_RETREAT = SOUNDS.register("entity.piglin_alchemist.retreat", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.retreat")));
    public static final RegistryObject<SoundEvent> ALCHEMIST_DEATH = SOUNDS.register("entity.piglin_alchemist.death", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.death")));
    public static final RegistryObject<SoundEvent> ALCHEMIST_ANGRY = SOUNDS.register("entity.piglin_alchemist.angry", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.angry")));
    public static final RegistryObject<SoundEvent> ALCHEMIST_CELEBRATE = SOUNDS.register("entity.piglin_alchemist.celebrate", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.celebrate")));
    public static final RegistryObject<SoundEvent> ALCHEMIST_CONVERTED = SOUNDS.register("entity.piglin_alchemist.conversion", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.conversion")));
    public static final RegistryObject<SoundEvent> ALCHEMIST_STEP = SOUNDS.register("entity.piglin_alchemist.step", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.step")));
    public static final RegistryObject<SoundEvent> REGEN_HEALING_ARROW_HIT = SOUNDS.register("arrow.regenhealingarrowhit", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "arrow.regenhealingarrowhit")));
}

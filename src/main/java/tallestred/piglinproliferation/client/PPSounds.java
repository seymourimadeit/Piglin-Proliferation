package tallestred.piglinproliferation.client;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestred.piglinproliferation.PiglinProliferation;

public class PPSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, PiglinProliferation.MODID);
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_ABOUT_TO_THROW_POTION = SOUNDS.register("entity.piglin_alchemist.throw_alert", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.throw_alert")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_WALK = SOUNDS.register("entity.piglin_alchemist.walk", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.walk")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_IDLE = SOUNDS.register("entity.piglin_alchemist.idle", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.idle")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_HURT = SOUNDS.register("entity.piglin_alchemist.hurt", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_ADMIRE = SOUNDS.register("entity.piglin_alchemist.admire", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.admire")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_JEALOUS = SOUNDS.register("entity.piglin_alchemist.jealous", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.jealous")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_RETREAT = SOUNDS.register("entity.piglin_alchemist.retreat", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.retreat")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_DEATH = SOUNDS.register("entity.piglin_alchemist.death", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_ANGRY = SOUNDS.register("entity.piglin_alchemist.angry", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.angry")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_CELEBRATE = SOUNDS.register("entity.piglin_alchemist.celebrate", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.celebrate")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_CONVERTED = SOUNDS.register("entity.piglin_alchemist.conversion", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.conversion")));
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_STEP = SOUNDS.register("entity.piglin_alchemist.step", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_alchemist.step")));
    public static final DeferredHolder<SoundEvent, SoundEvent> REGEN_HEALING_ARROW_HIT = SOUNDS.register("arrow.regenhealingarrowhit", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "arrow.regenhealingarrowhit")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PIGLIN_BRUTE_CHARGE = SOUNDS.register("entity.piglin_brute.charge", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_brute.charge")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SHIELD_BASH = SOUNDS.register("item.buckler.bash", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "item.buckler.bash")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CRITICAL_ACTIVATE = SOUNDS.register("entity.critical_aura.activate", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.critical_aura.activate")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CRITICAL_DEACTIVATE = SOUNDS.register("entity.critical_aura.deactivate", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.critical_aura.deactivate")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MAKING_COMPASS = SOUNDS.register("entity.traveller.make_compass", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.traveller.make_compass")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELLER_IDLE = SOUNDS.register("entity.piglin_traveller.idle", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_traveller.idle")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELLER_HURT = SOUNDS.register("entity.piglin_traveller.hurt", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_traveller.hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELLER_ADMIRE = SOUNDS.register("entity.piglin_traveller.admire", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_traveller.admire")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELLER_JEALOUS = SOUNDS.register("entity.piglin_traveller.jealous", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_traveller.jealous")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELLER_RETREAT = SOUNDS.register("entity.piglin_traveller.retreat", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_traveller.retreat")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELLER_DEATH = SOUNDS.register("entity.piglin_traveller.death", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_traveller.death")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELLER_ANGRY = SOUNDS.register("entity.piglin_traveller.angry", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_traveller.angry")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELLER_CELEBRATE = SOUNDS.register("entity.piglin_traveller.celebrate", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_traveller.celebrate")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELLER_CONVERTED = SOUNDS.register("entity.piglin_traveller.conversion", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, "entity.piglin_traveller.conversion")));
}

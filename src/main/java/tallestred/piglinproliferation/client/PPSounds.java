package tallestred.piglinproliferation.client;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestred.piglinproliferation.PiglinProliferation;

public class PPSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, PiglinProliferation.MODID);
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_ABOUT_TO_THROW_POTION = createVariableRangeSound("entity.piglin_alchemist.throw_alert");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_WALK = createVariableRangeSound("entity.piglin_alchemist.walk");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_IDLE = createVariableRangeSound("entity.piglin_alchemist.idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_HURT = createVariableRangeSound("entity.piglin_alchemist.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_ADMIRE = createVariableRangeSound("entity.piglin_alchemist.admire");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_JEALOUS = createVariableRangeSound("entity.piglin_alchemist.jealous");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_RETREAT = createVariableRangeSound("entity.piglin_alchemist.retreat");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_DEATH = createVariableRangeSound("entity.piglin_alchemist.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_ANGRY = createVariableRangeSound("entity.piglin_alchemist.angry");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_CELEBRATE = createVariableRangeSound("entity.piglin_alchemist.celebrate");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_CONVERTED = createVariableRangeSound("entity.piglin_alchemist.conversion");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALCHEMIST_STEP = createVariableRangeSound("entity.piglin_alchemist.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> REGEN_HEALING_ARROW_HIT = createVariableRangeSound("arrow.regenhealingarrowhit");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIGLIN_BRUTE_CHARGE = createVariableRangeSound("entity.piglin_brute.charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHIELD_BASH = createVariableRangeSound("item.buckler.bash");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRITICAL_ACTIVATE = createVariableRangeSound("entity.critical_aura.activate");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRITICAL_APPLY = createVariableRangeSound("entity.critical_aura.apply");

    public static final DeferredHolder<SoundEvent, SoundEvent> CRITICAL_DEACTIVATE = createVariableRangeSound("entity.critical_aura.deactivate");
    public static final DeferredHolder<SoundEvent, SoundEvent> MAKE_COMPASS = createVariableRangeSound("entity.piglin_traveller.make_compass");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELLER_IDLE = createVariableRangeSound("entity.piglin_traveller.idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELLER_HURT = createVariableRangeSound("entity.piglin_traveller.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELLER_ADMIRE = createVariableRangeSound("entity.piglin_traveller.admire");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELLER_JEALOUS = createVariableRangeSound("entity.piglin_traveller.jealous");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELLER_RETREAT = createVariableRangeSound("entity.piglin_traveller.retreat");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELLER_DEATH = createVariableRangeSound("entity.piglin_traveller.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELLER_ANGRY = createVariableRangeSound("entity.piglin_traveller.angry");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELLER_CELEBRATE = createVariableRangeSound("entity.piglin_traveller.celebrate");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRAVELLER_CONVERTED = createVariableRangeSound("entity.piglin_traveller.conversion");

    public static DeferredHolder<SoundEvent, SoundEvent> createVariableRangeSound(String path) {
        return SOUNDS.register(path, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, path)));
    }
}

package tallestred.piglinproliferation.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tallestred.piglinproliferation.PiglinProliferation;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PiglinProliferation.MODID);
    public static final RegistryObject<SoundEvent> ALCHEMIST_ABOUT_TO_THROW_POTION = createVariableRangeSound("entity.piglin_alchemist.throw_alert");
    public static final RegistryObject<SoundEvent> ALCHEMIST_WALK = createVariableRangeSound("entity.piglin_alchemist.walk");
    public static final RegistryObject<SoundEvent> ALCHEMIST_IDLE = createVariableRangeSound("entity.piglin_alchemist.idle");
    public static final RegistryObject<SoundEvent> ALCHEMIST_HURT = createVariableRangeSound("entity.piglin_alchemist.hurt");
    public static final RegistryObject<SoundEvent> ALCHEMIST_ADMIRE = createVariableRangeSound("entity.piglin_alchemist.admire");
    public static final RegistryObject<SoundEvent> ALCHEMIST_JEALOUS = createVariableRangeSound("entity.piglin_alchemist.jealous");
    public static final RegistryObject<SoundEvent> ALCHEMIST_RETREAT = createVariableRangeSound("entity.piglin_alchemist.retreat");
    public static final RegistryObject<SoundEvent> ALCHEMIST_DEATH = createVariableRangeSound("entity.piglin_alchemist.death");
    public static final RegistryObject<SoundEvent> ALCHEMIST_ANGRY = createVariableRangeSound("entity.piglin_alchemist.angry");
    public static final RegistryObject<SoundEvent> ALCHEMIST_CELEBRATE = createVariableRangeSound("entity.piglin_alchemist.celebrate");
    public static final RegistryObject<SoundEvent> ALCHEMIST_CONVERTED = createVariableRangeSound("entity.piglin_alchemist.conversion");
    public static final RegistryObject<SoundEvent> ALCHEMIST_STEP = createVariableRangeSound("entity.piglin_alchemist.step");
    public static final RegistryObject<SoundEvent> REGEN_HEALING_ARROW_HIT = createVariableRangeSound("arrow.regenhealingarrowhit");
    public static final RegistryObject<SoundEvent> PIGLIN_BRUTE_CHARGE = createVariableRangeSound("entity.piglin_brute.charge");
    public static final RegistryObject<SoundEvent> SHIELD_BASH = createVariableRangeSound("item.buckler.bash");
    public static final RegistryObject<SoundEvent> CRITICAL_ACTIVATE = createVariableRangeSound("entity.critical_aura.activate");
    public static final RegistryObject<SoundEvent> CRITICAL_APPLY = createVariableRangeSound("entity.critical_aura.apply");
    public static final RegistryObject<SoundEvent> CRITICAL_DEACTIVATE = createVariableRangeSound("entity.critical_aura.deactivate");
    public static final RegistryObject<SoundEvent> MAKE_COMPASS = createVariableRangeSound("entity.piglin_traveler.make_compass");
    public static final RegistryObject<SoundEvent> TRAVELER_IDLE = createVariableRangeSound("entity.piglin_traveler.idle");
    public static final RegistryObject<SoundEvent> TRAVELER_HURT = createVariableRangeSound("entity.piglin_traveler.hurt");
    public static final RegistryObject<SoundEvent> TRAVELER_ADMIRE = createVariableRangeSound("entity.piglin_traveler.admire");
    public static final RegistryObject<SoundEvent> TRAVELER_JEALOUS = createVariableRangeSound("entity.piglin_traveler.jealous");
    public static final RegistryObject<SoundEvent> TRAVELER_RETREAT = createVariableRangeSound("entity.piglin_traveler.retreat");
    public static final RegistryObject<SoundEvent> TRAVELER_DEATH = createVariableRangeSound("entity.piglin_traveler.death");
    public static final RegistryObject<SoundEvent> TRAVELER_ANGRY = createVariableRangeSound("entity.piglin_traveler.angry");
    public static final RegistryObject<SoundEvent> TRAVELER_CELEBRATE = createVariableRangeSound("entity.piglin_traveler.celebrate");
    public static final RegistryObject<SoundEvent> TRAVELER_CONVERTED = createVariableRangeSound("entity.piglin_traveler.conversion");
    public static final RegistryObject<SoundEvent> NOTE_BLOCK_IMITATE_PIGLIN_BRUTE = createVariableRangeSound("block.note_block.imitate.piglin_brute");
    public static final RegistryObject<SoundEvent> NOTE_BLOCK_IMITATE_PIGLIN_ALCHEMIST = createVariableRangeSound("block.note_block.imitate.piglin_alchemist");
    public static final RegistryObject<SoundEvent> NOTE_BLOCK_IMITATE_PIGLIN_TRAVELER = createVariableRangeSound("block.note_block.imitate.piglin_traveler");
    public static final RegistryObject<SoundEvent> NOTE_BLOCK_IMITATE_ZOMBIFIED_PIGLIN = createVariableRangeSound("block.note_block.imitate.zombified_piglin");

    public static RegistryObject<SoundEvent> createVariableRangeSound(String path) {
        return SOUNDS.register(path, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PiglinProliferation.MODID, path)));
    }
}

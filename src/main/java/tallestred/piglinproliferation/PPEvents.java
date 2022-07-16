package tallestred.piglinproliferation;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import tallestred.piglinproliferation.capablities.PPCapablities;
import tallestred.piglinproliferation.capablities.TransformationSourceListener;
import tallestred.piglinproliferation.capablities.TransformationSourceProvider;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;
import tallestred.piglinproliferation.common.entities.ai.goals.PiglinCallForHelpGoal;
import tallestred.piglinproliferation.networking.PPNetworking;
import tallestred.piglinproliferation.networking.ZiglinCapablitySyncPacket;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID)
public class PPEvents {

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        final TransformationSourceProvider provider = new TransformationSourceProvider();
        if (event.getObject() instanceof ZombifiedPiglin) {
            event.addCapability(TransformationSourceProvider.IDENTIFIER, provider);
            event.addListener(provider::invalidate);
        }
    }

    @SubscribeEvent
    public static void entityJoin(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof AbstractPiglin piglin) {
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> piglin1.isOnFire() && !piglin1.hasEffect(MobEffects.FIRE_RESISTANCE), (alchemist -> alchemist.getItemShownOnOffhand() != null && PotionUtils.getPotion(alchemist.getItemShownOnOffhand()) == Potions.FIRE_RESISTANCE)));
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> piglin1.getHealth() < piglin1.getMaxHealth() && !piglin1.hasEffect(MobEffects.REGENERATION), (alchemist -> alchemist.getItemShownOnOffhand() != null && PotionUtils.getPotion(alchemist.getItemShownOnOffhand()) == Potions.STRONG_REGENERATION)));
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> piglin1.getHealth() < piglin1.getMaxHealth() && !piglin1.hasEffect(MobEffects.HEAL), (alchemist -> alchemist.getItemShownOnOffhand() != null && PotionUtils.getPotion(alchemist.getItemShownOnOffhand()) == Potions.STRONG_HEALING)));
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> piglin1.getHealth() < (piglin1.getMaxHealth() / 2) && piglin1.getTarget() != null && !piglin1.hasEffect(MobEffects.DAMAGE_BOOST), (alchemist -> alchemist.getItemShownOnOffhand() != null && PotionUtils.getPotion(alchemist.getItemShownOnOffhand()) == Potions.STRONG_STRENGTH)));
        }
    }

    @SubscribeEvent
    public static void onEffectApplied(PotionEvent.PotionAddedEvent event) {
        MobEffect mobEffect = event.getPotionEffect().getEffect();
        if (event.getEntityLiving() instanceof AbstractPiglin piglin) {
            if (mobEffect == MobEffects.FIRE_RESISTANCE) {
                piglin.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
                piglin.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
                piglin.setPathfindingMalus(BlockPathTypes.LAVA, 0.0F);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(PotionEvent.PotionRemoveEvent event) {
        MobEffect mobEffect = event.getPotionEffect().getEffect();
        if (event.getEntityLiving() instanceof AbstractPiglin piglin) {
            if (mobEffect == MobEffects.FIRE_RESISTANCE) {
                piglin.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
                piglin.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
                piglin.setPathfindingMalus(BlockPathTypes.LAVA, -1.0F);
            }
        }
    }

    @SubscribeEvent
    public static void hurtEntity(LivingHurtEvent event) {
        if (event.getSource().getDirectEntity() instanceof Arrow) {
            for (MobEffectInstance mobeffectinstance : ((Arrow) event.getSource().getDirectEntity()).potion.getEffects()) {
                if ((mobeffectinstance.getEffect() == MobEffects.REGENERATION || mobeffectinstance.getEffect() == MobEffects.HEAL)) {
                    if ((event.getEntity() instanceof Mob && ((Mob) event.getEntity()).isInvertedHealAndHarm()))
                        return;
                    event.setAmount(0.0F);
                    event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().multiply(-1.0D, -1.0D, -1.0D));
                    event.getEntity().invulnerableTime = 0;
                    if (event.getEntity() instanceof LivingEntity)
                        ((LivingEntity) event.getEntity()).hurtTime = 0;
                    event.setAmount(0.0F);
                }
            }
        }
    }

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ZombifiedPiglin ziglin) {
            if (!event.getTarget().level.isClientSide) {
                TransformationSourceListener transformationSource = getTransformationSourceListener(ziglin);
                if (transformationSource != null)
                    PPNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> ziglin), new ZiglinCapablitySyncPacket(ziglin.getId(), transformationSource.getTransformationSource()));
            }
        }
    }

    @SubscribeEvent
    public static void onConvert(LivingConversionEvent.Post event) {
        if (event.getEntity() instanceof AbstractPiglin piglin && event.getOutcome() instanceof ZombifiedPiglin ziglin) {
            if (piglin.level.isClientSide)
                return;
            TransformationSourceListener transformationSource = getTransformationSourceListener(ziglin);
            String piglinName = ForgeRegistries.ENTITIES.getKey(piglin.getType()).getPath();
            transformationSource.setTransformationSource(piglinName);
            PPNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> ziglin), new ZiglinCapablitySyncPacket(ziglin.getId(), piglinName));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBiomeLoad(BiomeLoadingEvent event) {
        if (event.getName().equals(Biomes.NETHER_WASTES.location()) || event.getName().equals(Biomes.CRIMSON_FOREST.location())) // TODO make this a config option
            event.getSpawns().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(PPEntityTypes.PIGLIN_ALCHEMIST.get(), 5, 3, 6));
    }

    public static TransformationSourceListener getTransformationSourceListener(LivingEntity entity) {
        LazyOptional<TransformationSourceListener> listener = entity.getCapability(PPCapablities.TRANSFORMATION_SOURCE_TRACKER);
        if (listener.isPresent())
            return listener.orElseThrow(() -> new IllegalStateException("Capability not found! Report this to the piglin proliferation github!"));
        return null;
    }
}


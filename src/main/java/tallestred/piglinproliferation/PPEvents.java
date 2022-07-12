package tallestred.piglinproliferation;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.Structures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import tallestred.piglinproliferation.capablities.PPCapablities;
import tallestred.piglinproliferation.capablities.TransformationSourceListener;
import tallestred.piglinproliferation.capablities.TransformationSourceProvider;
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
    public static void livingNaturalSpawn(LivingSpawnEvent.SpecialSpawn event) {
    }

    @SubscribeEvent
    public static void entityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ZombifiedPiglin ziglin) {
            if (!event.getEntity().level.isClientSide) {
                TransformationSourceListener transformationSource = getTransformationSourceListener(ziglin);
                if (transformationSource != null)
                    PPNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> ziglin), new ZiglinCapablitySyncPacket(ziglin.getId(), transformationSource.getTransformationSource()));
            }
        }
        if (event.getEntity() instanceof AbstractPiglin piglin) {
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> {
                return piglin1.isOnFire() && !piglin1.hasEffect(MobEffects.FIRE_RESISTANCE);
            }, (alchemist -> {
                return alchemist.getItemShownOnOffhand() != null && PotionUtils.getPotion(alchemist.getItemShownOnOffhand()) == Potions.FIRE_RESISTANCE;
            })));
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> {
                return piglin1.getHealth() < piglin1.getMaxHealth() && !piglin1.hasEffect(MobEffects.REGENERATION);
            }, (alchemist -> {
                return alchemist.getItemShownOnOffhand() != null && PotionUtils.getPotion(alchemist.getItemShownOnOffhand()) == Potions.STRONG_REGENERATION;
            })));
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> {
                return piglin1.getHealth() < piglin1.getMaxHealth() && !piglin1.hasEffect(MobEffects.HEAL);
            }, (alchemist -> {
                return alchemist.getItemShownOnOffhand() != null && PotionUtils.getPotion(alchemist.getItemShownOnOffhand()) == Potions.STRONG_HEALING;
            })));
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> {
                return piglin1.getHealth() < (piglin1.getMaxHealth() / 2) && piglin1.getTarget() != null && !piglin1.hasEffect(MobEffects.DAMAGE_BOOST);
            }, (alchemist -> {
                return alchemist.getItemShownOnOffhand() != null && PotionUtils.getPotion(alchemist.getItemShownOnOffhand()) == Potions.STRONG_STRENGTH;
            })));
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
    public static void onEffectApplied(MobEffectEvent.Added event) {
        MobEffect mobEffect = event.getEffectInstance().getEffect();
        if (event.getEntity() instanceof AbstractPiglin piglin) {
            if (mobEffect == MobEffects.FIRE_RESISTANCE) {
                piglin.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
                piglin.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
                piglin.setPathfindingMalus(BlockPathTypes.LAVA, 0.0F);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        MobEffect mobEffect = event.getEffectInstance().getEffect();
        if (event.getEntity() instanceof AbstractPiglin piglin) {
            if (mobEffect == MobEffects.FIRE_RESISTANCE) {
                piglin.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
                piglin.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
                piglin.setPathfindingMalus(BlockPathTypes.LAVA, -1.0F);
            }
        }
    }

    @SubscribeEvent
    public static void hurtEntity(LivingHurtEvent event) {
        if (event.getSource().getDirectEntity() instanceof Arrow arrow) {
            for (MobEffectInstance mobeffectinstance : arrow.potion.getEffects()) {
                if ((mobeffectinstance.getEffect() == MobEffects.REGENERATION || mobeffectinstance.getEffect() == MobEffects.HEAL)) {
                    if ((event.getEntity() instanceof Mob && ((Mob) event.getEntity()).isInvertedHealAndHarm()))
                        return;
                    event.setAmount(0.0F);
                    event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().multiply(-1.0D, -1.0D, -1.0D));
                    event.getEntity().invulnerableTime = 0;
                    event.getEntity().hurtTime = 0;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onConvert(LivingConversionEvent.Post event) {
        if (event.getEntity() instanceof AbstractPiglin piglin && event.getOutcome() instanceof ZombifiedPiglin ziglin) {
            if (piglin.level.isClientSide)
                return;
            TransformationSourceListener transformationSource = getTransformationSourceListener(ziglin);
            String piglinName = ForgeRegistries.ENTITY_TYPES.getKey(piglin.getType()).getPath();
            transformationSource.setTransformationSource(piglinName);
            PPNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> ziglin), new ZiglinCapablitySyncPacket(ziglin.getId(), piglinName));
        }
    }

    public static TransformationSourceListener getTransformationSourceListener(LivingEntity entity) {
        LazyOptional<TransformationSourceListener> listener = entity.getCapability(PPCapablities.TRANSFORMATION_SOURCE_TRACKER);
        if (listener.isPresent())
            return listener.orElseThrow(() -> new IllegalStateException("Capability not found! Report this to the piglin proliferation github!"));
        return null;
    }
}

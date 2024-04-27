package tallestred.piglinproliferation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import tallestred.piglinproliferation.capablities.PPDataAttachments;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.common.blockentities.PPBlockEntities;
import tallestred.piglinproliferation.common.blocks.PiglinSkullBlock;
import tallestred.piglinproliferation.common.enchantments.PPEnchantments;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;
import tallestred.piglinproliferation.common.entities.PiglinTraveler;
import tallestred.piglinproliferation.common.entities.ai.goals.DumbBowAttackGoal;
import tallestred.piglinproliferation.common.entities.ai.goals.DumbCrossbowAttackGoal;
import tallestred.piglinproliferation.common.entities.ai.goals.PiglinCallForHelpGoal;
import tallestred.piglinproliferation.common.entities.ai.goals.PiglinSwimInLavaGoal;
import tallestred.piglinproliferation.common.entities.spawns.TravelerSpawner;
import tallestred.piglinproliferation.common.items.BucklerItem;
import tallestred.piglinproliferation.common.items.PPItems;
import tallestred.piglinproliferation.configuration.PPConfig;
import tallestred.piglinproliferation.networking.CriticalCapabilityPacket;
import tallestred.piglinproliferation.networking.ZiglinCapabilitySyncPacket;

import java.util.*;

import static tallestred.piglinproliferation.util.CodeUtilities.castOrNull;
import static tallestred.piglinproliferation.util.CodeUtilities.potionContents;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = PiglinProliferation.MODID)
public class PPEvents {
    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (BucklerItem.getChargeTicks(PPItems.checkEachHandForBuckler(event.getEntity())) > 0) {
            event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().x(), 0.0D, event.getEntity().getDeltaMovement().z());
        }
    }

    @SubscribeEvent
    public static void entityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ZombifiedPiglin ziglin) {
            if (!event.getEntity().level().isClientSide) {
                PacketDistributor.sendToPlayersTrackingEntity(ziglin, new ZiglinCapabilitySyncPacket(ziglin.getId(), ziglin.getData(PPDataAttachments.TRANSFORMATION_TRACKER.get())));
            }
            ziglin.goalSelector.addGoal(2, new DumbBowAttackGoal<>(ziglin, 0.5D, 20, 15.0F));
            ziglin.goalSelector.addGoal(2, new DumbCrossbowAttackGoal<>(ziglin, 1.0D, 8.0F));
        }
        if (event.getEntity() instanceof AbstractPiglin piglin) {
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> piglin1.isOnFire() && !piglin1.hasEffect(MobEffects.FIRE_RESISTANCE), (alchemist -> alchemist.getItemShownOnOffhand() != null && potionContents(alchemist.getItemShownOnOffhand()).potion().orElse(Potions.WATER) == Potions.FIRE_RESISTANCE)));
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> piglin1.getHealth() < piglin1.getMaxHealth() && !piglin1.hasEffect(MobEffects.REGENERATION), (alchemist -> alchemist.getItemShownOnOffhand() != null && potionContents(alchemist.getItemShownOnOffhand()).potion().orElse(Potions.WATER) == Potions.STRONG_REGENERATION)));
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> piglin1.getHealth() < piglin1.getMaxHealth() && !piglin1.hasEffect(MobEffects.HEAL), (alchemist -> alchemist.getItemShownOnOffhand() != null && potionContents(alchemist.getItemShownOnOffhand()).potion().orElse(Potions.WATER) == Potions.STRONG_HEALING)));
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> piglin1.getHealth() < (piglin1.getMaxHealth() / 2) && piglin1.getTarget() != null && !piglin1.hasEffect(MobEffects.DAMAGE_BOOST), (alchemist -> alchemist.getItemShownOnOffhand() != null && potionContents(alchemist.getItemShownOnOffhand()).potion().orElse(Potions.WATER) == Potions.STRONG_STRENGTH)));
            piglin.goalSelector.addGoal(1, new PiglinSwimInLavaGoal(piglin));
        }
        if (event.getEntity() instanceof AreaEffectCloud lingeringCloud) {
            int centreX = (int) lingeringCloud.getX();
            int centreY = (int) lingeringCloud.getY();
            int centreZ = (int) lingeringCloud.getZ();
            int radius = (int) lingeringCloud.getRadius();
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(centreX, centreY, centreZ);
            for (int x = centreX - radius; x < centreX + radius; x++)
                for (int z = centreZ - radius; z < centreZ + radius; z++) {
                    mutable.setX(x);
                    mutable.setZ(z);
                    event.getLevel().getBlockEntity(mutable, PPBlockEntities.FIRE_RING.get()).ifPresent(fireRing -> {
                        fireRing.addEffects(castOrNull(lingeringCloud.getOwner(), Player.class), null, null, lingeringCloud.potionContents.getAllEffects());
                    });
                }
        }
    }

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ZombifiedPiglin ziglin) {
            if (!event.getTarget().level().isClientSide) {
                PacketDistributor.sendToPlayersTrackingEntity(ziglin, new ZiglinCapabilitySyncPacket(ziglin.getId(), ziglin.getData(PPDataAttachments.TRANSFORMATION_TRACKER.get())));

            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        ItemStack bucklerItemStack = PPItems.checkEachHandForBuckler(entity);
        boolean bucklerReadyToCharge = BucklerItem.isReady(bucklerItemStack);
        int bucklerChargeTicks = BucklerItem.getChargeTicks(bucklerItemStack);
        if (bucklerReadyToCharge) {
            BucklerItem.setChargeTicks(bucklerItemStack, bucklerChargeTicks - 1);
            if (bucklerChargeTicks > 0) {
                if (entity.horizontalCollision && PPEnchantments.hasBucklerEnchantsOnHands(entity, PPEnchantments.TURNING.get())) {
                    entity.setDeltaMovement(entity.getDeltaMovement().x, PPConfig.COMMON.turningBucklerLaunchStrength.get() * (EnchantmentHelper.getEnchantmentLevel(PPEnchantments.TURNING.get(), entity)), entity.getDeltaMovement().z);
                }
                BucklerItem.moveFowards(entity);
                BucklerItem.spawnRunningEffectsWhileCharging(entity);
                if (!entity.level().isClientSide()) BucklerItem.bucklerBash(entity);
            }
        }
        if (bucklerChargeTicks <= 0 && bucklerReadyToCharge || BucklerItem.CHARGE_SPEED_BOOST.hasModifier(entity)
                && (!(bucklerItemStack.getItem() instanceof BucklerItem) || !bucklerReadyToCharge)) {
            entity.setDeltaMovement(Vec3.ZERO);
            BucklerItem.TURNING_SPEED_REDUCTION.removeModifier(entity);
            BucklerItem.CHARGE_SPEED_BOOST.removeModifier(entity);
            BucklerItem.INCREASED_KNOCKBACK_RESISTANCE.removeModifier(entity);
            BucklerItem.setChargeTicks(bucklerItemStack, 0);
            BucklerItem.setReady(bucklerItemStack, false);
            entity.stopUsingItem();
            if (entity instanceof Player player) {
                for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                    if (player.getInventory().getItem(slot).getItem() instanceof BucklerItem) {
                        BucklerItem.setChargeTicks(player.getInventory().getItem(slot), 0);
                        BucklerItem.setReady(player.getInventory().getItem(slot), false);
                    }
                }
            }
        }
        boolean criticalAfterCharge = entity.getData(PPDataAttachments.CRITICAL.get());
        if (criticalAfterCharge) {
            if (entity.swingTime > 0) {
                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), PPSounds.CRITICAL_DEACTIVATE.get(), entity.getSoundSource(), 1.0F, 0.8F + entity.getRandom().nextFloat() * 0.4F);
                entity.setData(PPDataAttachments.CRITICAL.get(), false);
            }
            for (int i = 0; i < 2; ++i) {
                entity.level().addParticle(ParticleTypes.CRIT, entity.getRandomX(0.5D), entity.getRandomY(), entity.getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
            }
        }
        if (event.getEntity() instanceof ServerPlayer player)
            PacketDistributor.sendToPlayer(player, new CriticalCapabilityPacket(player.getId(), criticalAfterCharge));
    }

    @SubscribeEvent
    public static void onEffectApplied(MobEffectEvent.Added event) {
        if (event.getEffectInstance() != null) {
            Holder<MobEffect> mobEffect = event.getEffectInstance().getEffect();
            if (event.getEntity() instanceof AbstractPiglin piglin) {
                if (mobEffect == MobEffects.FIRE_RESISTANCE) {
                    piglin.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
                    piglin.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
                    piglin.setPathfindingMalus(PathType.LAVA, 0.0F);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffectInstance() == null)
            return;
        Holder<MobEffect> mobEffect = event.getEffectInstance().getEffect();
        if (event.getEntity() instanceof AbstractPiglin piglin) {
            if (mobEffect == MobEffects.FIRE_RESISTANCE) {
                piglin.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
                piglin.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
                piglin.setPathfindingMalus(PathType.LAVA, -1.0F);
            }
        }
    }

    @SubscribeEvent
    public static void hurtEntity(LivingHurtEvent event) {
        if (event.getSource().getDirectEntity() instanceof Arrow arrow && PPConfig.COMMON.healingArrowDamage.get()) {
            for (MobEffectInstance mobeffectinstance : arrow.getPotionContents().getAllEffects()) {
                if ((mobeffectinstance.getEffect() == MobEffects.REGENERATION || mobeffectinstance.getEffect() == MobEffects.HEAL)) {
                    if ((event.getEntity() instanceof Mob && event.getEntity().isInvertedHealAndHarm()))
                        return;
                    event.setAmount(0.0F);
                    arrow.level().playSound(null, arrow.blockPosition(), PPSounds.REGEN_HEALING_ARROW_HIT.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
                    event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().multiply(-1.0D, -1.0D, -1.0D));
                    event.getEntity().invulnerableTime = 0;
                    event.getEntity().hurtTime = 0;
                }
            }
        }
    }

    @SubscribeEvent
    public static void attackEntity(LivingAttackEvent event) {
        // Testing bygone nether compatibility led me to discover that alchemists healing piglin hunters leads to them attacking each other since the
        // horses they're riding on are considered undead, this should work as a quick fix for that, but further discussions with the mod creator is needed.
        if (event.getEntity() instanceof Mob mob) {
            for (Entity rider : mob.getPassengers()) {
                if (mob.isInvertedHealAndHarm() && event.getSource().getEntity() instanceof AbstractPiglin && rider instanceof AbstractPiglin piglin && event.getSource().is(DamageTypes.MAGIC)) {
                    if (event.getEntity().level().isClientSide)
                        return;
                    piglin.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void targetMob(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof AbstractPiglin) {
            if (event.getOriginalTarget() instanceof AbstractPiglin) {
                event.setCanceled(true);
            }
        }
        if (event.getEntity() instanceof ZombifiedPiglin) {
            if (event.getOriginalTarget() instanceof ZombifiedPiglin) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        if (player.getData(PPDataAttachments.CRITICAL.get())) {
            event.setResult(Event.Result.ALLOW);
            event.setDamageModifier(1.5F);
            Entity entity = event.getEntity();
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, entity.getSoundSource(), 1.0F, 1.0F);
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), PPSounds.CRITICAL_APPLY.get(), entity.getSoundSource(), 1.0F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
            player.setData(PPDataAttachments.CRITICAL.get(), false);
        }
    }

    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent event) {
        if (event.getEntity().getUseItem().getItem() instanceof BucklerItem)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void finalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        MobSpawnType spawnType = event.getSpawnType();
        RandomSource random = event.getLevel().getRandom();
        if (event.getEntity() instanceof Strider strider && random.nextInt(60) == 0 && !strider.isBaby()) {
            event.setCanceled(true);
            PiglinTraveler traveler = PPEntityTypes.PIGLIN_TRAVELER.get().create(strider.level());
            if (traveler != null) {
                traveler.copyPosition(strider);
                traveler.startRiding(strider);
                strider.equipSaddle(null);
                traveler.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
            }
        }
        if (event.getEntity() instanceof PiglinBrute piglinBrute) {
            if (!PPConfig.COMMON.BruteBuckler.get()) return;
            piglinBrute.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(PPItems.BUCKLER.get()));
            ItemStack itemstack = piglinBrute.getOffhandItem();
            if (itemstack.getItem() instanceof BucklerItem) {
                if (random.nextInt(300) == 0)
                    itemstack.enchant(PPEnchantments.TURNING.get(), 5);
                if (random.nextInt(500) == 0)
                    itemstack.enchant(PPEnchantments.BANG.get(), 1);
                piglinBrute.setItemSlot(EquipmentSlot.OFFHAND, itemstack);
            }
        }
        if (event.getEntity().getType() == EntityType.ZOMBIFIED_PIGLIN) { // Some mods have entities that extend zombified piglins in order to make their own ziglins have custom textures
            ZombifiedPiglin zombifiedPiglin = (ZombifiedPiglin) event.getEntity();
            if (spawnType != MobSpawnType.CONVERSION) {
                if (random.nextFloat() < PPConfig.COMMON.zombifiedPiglinDefaultChance.get().floatValue())
                    zombifiedPiglin.setData(PPDataAttachments.TRANSFORMATION_TRACKER.get(), "piglin");
                if (random.nextFloat() < PPConfig.COMMON.piglinVariantChances.get().floatValue()) {
                    List<? extends String> piglinTypes = PPConfig.COMMON.zombifiedPiglinTypeList.get();
                    if (!piglinTypes.isEmpty())
                        zombifiedPiglin.setData(PPDataAttachments.TRANSFORMATION_TRACKER.get(), piglinTypes.get(random.nextInt(piglinTypes.size())));
                }
                float bruteChance = PPConfig.COMMON.zombifiedBruteChance.get().floatValue();
                if (zombifiedPiglin.getData(PPDataAttachments.TRANSFORMATION_TRACKER.get()).equalsIgnoreCase("piglin")) {
                    if (random.nextFloat() < bruteChance) {
                        event.setCanceled(true);
                        zombifiedPiglin.setData(PPDataAttachments.TRANSFORMATION_TRACKER.get(), "piglin_brute");
                        zombifiedPiglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_AXE));
                        zombifiedPiglin.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(PPItems.BUCKLER.get()));
                    } else if (random.nextFloat() < PPConfig.COMMON.zombifiedAlchemistChance.get().floatValue()) {
                        event.setCanceled(true);
                        zombifiedPiglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                        zombifiedPiglin.setData(PPDataAttachments.TRANSFORMATION_TRACKER.get(), "piglin_alchemist");
                    } else if (random.nextFloat() < PPConfig.COMMON.crossbowChance.get().floatValue()) {
                        event.setCanceled(true);
                        zombifiedPiglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
                    }
                }
                if (spawnType == MobSpawnType.JOCKEY) {
                    event.setCanceled(true);
                    zombifiedPiglin.setData(PPDataAttachments.TRANSFORMATION_TRACKER.get(), "piglin_traveler");
                    zombifiedPiglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
                }
            }
        }
    }

    @SubscribeEvent
    public static void visionPercent(LivingEvent.LivingVisibilityEvent event) {
        if (event.getLookingEntity() != null) {
            ItemStack itemstack = event.getEntity().getItemBySlot(EquipmentSlot.HEAD);
            if (event.getLookingEntity() instanceof AbstractPiglin && PPItems.PIGLIN_HEADS.keySet().stream().anyMatch(h -> h.get() == itemstack.getItem()))
                event.modifyVisibility(0.5D);
        }
    }

    @SubscribeEvent
    public static void onConvert(LivingConversionEvent.Post event) {
        if (event.getEntity() instanceof AbstractPiglin piglin && event.getOutcome().getType() == EntityType.ZOMBIFIED_PIGLIN) { // Some mods have entities that extend zombified piglins in order to make their own ziglins have custom textures
            if (piglin.level().isClientSide)
                return;
            ZombifiedPiglin ziglin = (ZombifiedPiglin) event.getOutcome();
            Optional<Registry<EntityType<?>>> registryOptional = piglin.level().registryAccess().registry(Registries.ENTITY_TYPE);
            if (registryOptional.isPresent()) {
                ResourceLocation location = registryOptional.get().getKey(piglin.getType());
                if (location != null) {
                    String piglinName = location.getPath();
                    ziglin.setData(PPDataAttachments.TRANSFORMATION_TRACKER.get(), piglinName);
                    PacketDistributor.sendToPlayersTrackingEntity(ziglin, new ZiglinCapabilitySyncPacket(ziglin.getId(), piglinName));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLootDropEntity(LivingDropsEvent event) {
        PiglinSkullBlock.spawnSkullIfValidKill(event.getSource(), event.getEntity(), e -> e.getType() == EntityType.PIGLIN ? Items.PIGLIN_HEAD : PPItems.headItem(e.getType()));
        if (event.getEntity() instanceof PiglinBrute brute) {
            ItemStack itemstack = brute.getOffhandItem();
            if (itemstack.getItem() instanceof BucklerItem) {
                float f = 0.10F;
                if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack) && event.isRecentlyHit() && Math.max(brute.getRandom().nextFloat() - (float) event.getLootingLevel() * 0.01F, 0.0F) < f) {
                    if (itemstack.isDamageableItem()) {
                        int halvedMaxDurability = Math.abs(brute.getRandom().nextInt(Math.abs(itemstack.getMaxDamage() / 2)));
                        itemstack.setDamageValue(Math.abs(brute.getRandom().nextInt(halvedMaxDurability)));
                    }
                    brute.spawnAtLocation(itemstack);
                    brute.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }
            }
        }
    }

    @SubscribeEvent
    public static void noteBlockPlay(NoteBlockEvent.Play event) {
        BlockState stateAbove = event.getLevel().getBlockState(event.getPos().above());
        if (stateAbove.getBlock() instanceof PiglinSkullBlock skull) {
            event.setCanceled(true);
            event.getLevel().playSound(null, event.getPos(), skull.getType().getSoundEvent(), SoundSource.RECORDS);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END)
            if (event.level instanceof ServerLevel level && level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && level.dimension() == Level.NETHER)
                TravelerSpawner.tick(level, level.getDataStorage().computeIfAbsent(TravelerSpawner.SpawnDelay.factory(), "traveler_spawn_delay"));
    }
}

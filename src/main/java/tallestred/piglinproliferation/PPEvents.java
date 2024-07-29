package tallestred.piglinproliferation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.level.NoteBlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import tallestred.piglinproliferation.capablities.*;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.common.blockentities.PPBlockEntities;
import tallestred.piglinproliferation.common.blocks.PiglinSkullBlock;
import tallestred.piglinproliferation.common.enchantments.PPEnchantments;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;
import tallestred.piglinproliferation.common.entities.PiglinTraveler;
import tallestred.piglinproliferation.common.entities.ai.goals.*;
import tallestred.piglinproliferation.common.entities.spawns.TravelerSpawner;
import tallestred.piglinproliferation.common.items.BucklerItem;
import tallestred.piglinproliferation.common.items.PPItems;
import tallestred.piglinproliferation.common.loot.CompassCanFindLocationCondition;
import tallestred.piglinproliferation.configuration.PPConfig;
import tallestred.piglinproliferation.networking.CriticalCapabilityPacket;
import tallestred.piglinproliferation.networking.PPNetworking;
import tallestred.piglinproliferation.networking.ZiglinCapablitySyncPacket;

import java.util.List;
import java.util.Map;

import static tallestred.piglinproliferation.util.CodeUtilities.castOrNull;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID)
public class PPEvents {
    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        final GuranteedCritProvider critProvider = new GuranteedCritProvider();
        final TransformationSourceProvider provider = new TransformationSourceProvider();
        if (event.getObject() instanceof ZombifiedPiglin) {
            event.addCapability(TransformationSourceProvider.IDENTIFIER, provider);
            event.addListener(provider::invalidate);
        }
        if (event.getObject() instanceof Player) {
            event.addCapability(GuranteedCritProvider.IDENTIFIER, critProvider);
        }
    }

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
                TransformationSourceListener transformationSource = TransformationSourceListener.from(ziglin);
                if (transformationSource != null)
                    PPNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> ziglin), new ZiglinCapablitySyncPacket(ziglin.getId(), transformationSource.getTransformationSource()));
            }
            ziglin.goalSelector.addGoal(2, new DumbBowAttackGoal<>(ziglin, 0.5D, 20, 15.0F));
            ziglin.goalSelector.addGoal(2, new DumbCrossbowAttackGoal<>(ziglin, 1.0D, 8.0F));
        }
        if (event.getEntity() instanceof PathfinderMob mob && PPConfig.COMMON.mobsThatCanAlsoUseBuckler.get().contains(mob.getEncodeId())) {
            mob.goalSelector.addGoal(2, new UseBucklerGoal<>(mob));
        }
        if (event.getEntity() instanceof AbstractPiglin piglin) {
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> piglin1.isOnFire() && !piglin1.hasEffect(MobEffects.FIRE_RESISTANCE), (alchemist -> alchemist.getItemShownOnOffhand() != null && PotionUtils.getPotion(alchemist.getItemShownOnOffhand()) == Potions.FIRE_RESISTANCE)));
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> piglin1.getHealth() < piglin1.getMaxHealth() && !piglin1.hasEffect(MobEffects.REGENERATION), (alchemist -> alchemist.getItemShownOnOffhand() != null && PotionUtils.getPotion(alchemist.getItemShownOnOffhand()) == Potions.STRONG_REGENERATION)));
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> piglin1.getHealth() < piglin1.getMaxHealth() && !piglin1.hasEffect(MobEffects.HEAL), (alchemist -> alchemist.getItemShownOnOffhand() != null && PotionUtils.getPotion(alchemist.getItemShownOnOffhand()) == Potions.STRONG_HEALING)));
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> piglin1.getHealth() < (piglin1.getMaxHealth() / 2) && piglin1.getTarget() != null && !piglin1.hasEffect(MobEffects.DAMAGE_BOOST), (alchemist -> alchemist.getItemShownOnOffhand() != null && PotionUtils.getPotion(alchemist.getItemShownOnOffhand()) == Potions.STRONG_STRENGTH)));
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
                    if (!lingeringCloud.getPotion().getEffects().isEmpty()) {
                        try {
                            event.getLevel().getBlockEntity(mutable, PPBlockEntities.FIRE_RING.get()).ifPresent(fireRing -> {
                                fireRing.addEffects(castOrNull(lingeringCloud.getOwner(), Player.class), null, null, lingeringCloud.getPotion().getEffects());
                            });
                        } catch (ArrayIndexOutOfBoundsException execption) {
                            lingeringCloud.remove(Entity.RemovalReason.DISCARDED);
                            // I hope this fixes this issue, if not I will have to investigate further and see exactly how to recreate it
                        }
                    }
                }
        }
    }

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ZombifiedPiglin ziglin) {
            if (!event.getTarget().level().isClientSide) {
                TransformationSourceListener transformationSource = TransformationSourceListener.from(ziglin);
                if (transformationSource != null)
                    PPNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> ziglin), new ZiglinCapablitySyncPacket(ziglin.getId(), transformationSource.getTransformationSource()));
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
        CriticalAura criticalAura = PPCapablities.getGuaranteedCritical(entity);
        if (criticalAura != null) {
            if (criticalAura.isCritical()) {
                if (entity.swingTime > 0) {
                    entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), PPSounds.CRITICAL_DEACTIVATE.get(), entity.getSoundSource(), 1.0F, 0.8F + entity.getRandom().nextFloat() * 0.4F);
                    criticalAura.setCritical(false);
                }
                for (int i = 0; i < 2; ++i) {
                    entity.level().addParticle(ParticleTypes.CRIT, entity.getRandomX(0.5D), entity.getRandomY(), entity.getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
                }
            }
            if (event.getEntity() instanceof ServerPlayer player)
                PPNetworking.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new CriticalCapabilityPacket(player.getId(), criticalAura.isCritical()));
        }
    }

    @SubscribeEvent
    public static void onEffectApplied(MobEffectEvent.Added event) {
        if (event.getEntity() instanceof AbstractPiglin piglin) {
            if (event.getEffectInstance().getEffect() == MobEffects.FIRE_RESISTANCE) {
                piglin.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
                piglin.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
                piglin.setPathfindingMalus(BlockPathTypes.LAVA, 0.0F);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffectInstance() == null)
            return;
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
        if (event.getSource().getDirectEntity() instanceof Arrow arrow && PPConfig.COMMON.healingArrowDamage.get()) {
            for (MobEffectInstance mobeffectinstance : arrow.potion.getEffects()) {
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
        // Testing bygone nether compatibility lead me to discover that alchemists healing piglin hunters leads to them attacking each other since the
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
        CriticalAura criticalAura = PPCapablities.getGuaranteedCritical(player);
        if (criticalAura != null && criticalAura.isCritical()) {
            event.setResult(Event.Result.ALLOW);
            event.setDamageModifier(1.5F);
            Entity entity = event.getEntity();
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, entity.getSoundSource(), 1.0F, 1.0F);
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), PPSounds.CRITICAL_APPLY.get(), entity.getSoundSource(), 1.0F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
            criticalAura.setCritical(false);
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
            traveler.copyPosition(strider);
            traveler.startRiding(strider);
            strider.equipSaddle(null);
            traveler.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
        }
        if (event.getEntity() instanceof PiglinBrute piglinBrute) {
            if (!PPConfig.COMMON.BruteBuckler.get()) return;
            piglinBrute.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(PPItems.BUCKLER.get()));
            ItemStack itemstack = piglinBrute.getOffhandItem();
            if (itemstack.getItem() instanceof BucklerItem) {
                if (random.nextInt(300) == 0) {
                    Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack);
                    map.putIfAbsent(PPEnchantments.TURNING.get(), 1);
                    EnchantmentHelper.setEnchantments(map, itemstack);
                    piglinBrute.setItemSlot(EquipmentSlot.OFFHAND, itemstack);
                }
                if (random.nextInt(500) == 0) {
                    Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack);
                    map.putIfAbsent(PPEnchantments.BANG.get(), 1);
                    EnchantmentHelper.setEnchantments(map, itemstack);
                    piglinBrute.setItemSlot(EquipmentSlot.OFFHAND, itemstack);
                }
            }
        }
        if (event.getEntity().getType() == EntityType.ZOMBIFIED_PIGLIN) { // Some mods have entities that extend zombified piglins in order to make their own ziglins have custom textures
            ZombifiedPiglin zombifiedPiglin = (ZombifiedPiglin) event.getEntity();
            TransformationSourceListener transformationSourceListener = TransformationSourceListener.from(zombifiedPiglin);
            if (spawnType != MobSpawnType.CONVERSION) {
                if (random.nextFloat() < PPConfig.COMMON.zombifiedPiglinDefaultChance.get().floatValue())
                    transformationSourceListener.setTransformationSource("piglin");
                if (random.nextFloat() < PPConfig.COMMON.piglinVariantChances.get().floatValue()) {
                    List<? extends String> piglinTypes = PPConfig.COMMON.zombifiedPiglinTypeList.get();
                    if (!piglinTypes.isEmpty())
                        transformationSourceListener.setTransformationSource(piglinTypes.get(random.nextInt(piglinTypes.size())));
                }
                float bruteChance = PPConfig.COMMON.zombifiedBruteChance.get().floatValue();
                if (transformationSourceListener.getTransformationSource().equals("piglin")) {
                    if (random.nextFloat() < bruteChance) {
                        event.setCanceled(true);
                        transformationSourceListener.setTransformationSource("piglin_brute");
                        zombifiedPiglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_AXE));
                        zombifiedPiglin.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(PPItems.BUCKLER.get()));
                    } else if (random.nextFloat() < PPConfig.COMMON.zombifiedAlchemistChance.get().floatValue()) {
                        event.setCanceled(true);
                        zombifiedPiglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                        transformationSourceListener.setTransformationSource("piglin_alchemist");
                    } else {
                        if (random.nextFloat() < PPConfig.COMMON.zombifiedTravelerChance.get().floatValue()) {
                            transformationSourceListener.setTransformationSource("piglin_traveler");
                        }
                        if (random.nextFloat() < PPConfig.COMMON.crossbowChance.get().floatValue()) {
                            event.setCanceled(true);
                            zombifiedPiglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
                        }
                    }
                }
                if (spawnType == MobSpawnType.JOCKEY) {
                    event.setCanceled(true);
                    transformationSourceListener.setTransformationSource("piglin_traveler");
                    zombifiedPiglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
                }
            }
        }
    }

    @SubscribeEvent
    public static void visionPercent(LivingEvent.LivingVisibilityEvent event) {
        if (event.getLookingEntity() != null) {
            ItemStack itemstack = event.getEntity().getItemBySlot(EquipmentSlot.HEAD);
            if (event.getLookingEntity() instanceof AbstractPiglin && (itemstack.is(PPItems.PIGLIN_ALCHEMIST_HEAD_ITEM.get()) || itemstack.is(PPItems.PIGLIN_BRUTE_HEAD_ITEM.get())) || itemstack.is(PPItems.ZOMBIFIED_PIGLIN_HEAD_ITEM.get()) || itemstack.is(PPItems.PIGLIN_TRAVELER_HEAD_ITEM.get())) {
                event.modifyVisibility(0.5D);
            }
        }
    }

    @SubscribeEvent
    public static void onConvert(LivingConversionEvent.Post event) {
        if (event.getEntity() instanceof AbstractPiglin piglin && event.getOutcome().getType() == EntityType.ZOMBIFIED_PIGLIN) { // Some mods have entities that extend zombified piglins in order to make their own ziglins have custom textures
            if (piglin.level().isClientSide)
                return;
            ZombifiedPiglin ziglin = (ZombifiedPiglin) event.getOutcome();
            TransformationSourceListener transformationSource = TransformationSourceListener.from(ziglin);
            String piglinName = ForgeRegistries.ENTITY_TYPES.getKey(piglin.getType()).getPath();
            transformationSource.setTransformationSource(piglinName);
            PPNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> ziglin), new ZiglinCapablitySyncPacket(ziglin.getId(), piglinName));
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
                TravelerSpawner.tick(level, level.getDataStorage().computeIfAbsent(TravelerSpawner.SpawnDelay::load, TravelerSpawner.SpawnDelay::new, "traveler_spawn_delay"));
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        CompassCanFindLocationCondition.clearSearchCache();
    }
}

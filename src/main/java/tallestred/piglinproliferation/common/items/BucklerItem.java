package tallestred.piglinproliferation.common.items;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.Tags;
import tallestred.piglinproliferation.capablities.PPDataAttachments;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.client.particles.ParticleHelper;
import tallestred.piglinproliferation.client.renderers.BucklerRenderer;
import tallestred.piglinproliferation.common.attribute.AttributeModifierHolder;
import tallestred.piglinproliferation.common.attribute.PPAttributes;
import tallestred.piglinproliferation.common.attribute.RangedRandomAttributeModifierHolder;
import tallestred.piglinproliferation.common.enchantments.PPEnchantments;
import tallestred.piglinproliferation.common.items.component.PPComponents;
import tallestred.piglinproliferation.configuration.PPConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static tallestred.piglinproliferation.util.CodeUtilities.doubleToString;
import static tallestred.piglinproliferation.util.CodeUtilities.ticksToSeconds;

public class BucklerItem extends ShieldItem {
    public static final AttributeModifierHolder CHARGE_SPEED_BOOST = new AttributeModifierHolder(Attributes.MOVEMENT_SPEED, "charge_speed_boost", 9, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    public static final AttributeModifierHolder INCREASED_KNOCKBACK_RESISTANCE = new AttributeModifierHolder(Attributes.KNOCKBACK_RESISTANCE, "increased_knockback_resistance", 1, AttributeModifier.Operation.ADD_VALUE);
    public static final AttributeModifierHolder CHARGE_JUMP_PREVENTION = new AttributeModifierHolder(Attributes.JUMP_STRENGTH, "charge_jump_prevention", -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    public static final AttributeModifierHolder TURNING_SPEED_REDUCTION = new AttributeModifierHolder(PPAttributes.TURNING_SPEED, "turning_speed_reduction", -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    public static final RangedRandomAttributeModifierHolder ATTACK_DAMAGE = new RangedRandomAttributeModifierHolder(Attributes.ATTACK_DAMAGE, "attack_damage", 6, 8, AttributeModifier.Operation.ADD_VALUE);
    //This is stored as a modifier for easy localisation, even though it's not actually modifying anything

    public BucklerItem(Properties p_i48470_1_) {
        super(p_i48470_1_);
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    public static void moveFowards(LivingEntity entity) {
        if (entity.isAlive()) {
            Vec3 look = entity.getViewVector(1.0F);
            Vec3 motion = entity.getDeltaMovement();
            if (entity.level().isClientSide) {
                float yHeadRot = entity.yHeadRot + 180.0F;
                if (PPConfig.CLIENT.RenderAfterImage.get())
                    ParticleHelper.createAfterImage(entity, Vec3.directionFromRotation(0.0F, yHeadRot));
            }
            if (entity instanceof Player) {
                entity.setDeltaMovement(look.x * entity.getAttributeValue(Attributes.MOVEMENT_SPEED), motion.y,
                        look.z * entity.getAttributeValue(Attributes.MOVEMENT_SPEED));
            } else {
                // This is the only way to make the piglin brute go faster without having it
                // spazz out.
                entity.setDeltaMovement(look.x, motion.y, look.z);
            }
        }
    }

    public static boolean isReady(ItemStack stack) {
        return stack.getOrDefault(PPComponents.BUCKLER_IS_READY, false);
    }

    public static int startingChargeTicks(ItemStack stack, Level level) {
        int min = PPConfig.COMMON.minBucklerChargeTime.get();
        int max = PPConfig.COMMON.maxBucklerChargeTime.get();
        return min + (((max - min) * stack.getEnchantmentLevel(PPEnchantments.getEnchant(PPEnchantments.TURNING, level.registryAccess())) / 5));
    }

    public static int getChargeTicks(ItemStack stack) {
        return stack.getOrDefault(PPComponents.BUCKLER_CHARGE_TICKS, 0);
    }

    public static void setChargeTicks(ItemStack stack, Level level) {
        setChargeTicks(stack, startingChargeTicks(stack, level));
    }

    public static void setChargeTicks(ItemStack stack, int chargeTicks) {
        stack.set(PPComponents.BUCKLER_CHARGE_TICKS, chargeTicks);
    }

    public static void setReady(ItemStack stack, boolean ready) {
        stack.set(PPComponents.BUCKLER_IS_READY, ready);
    }

    public static void bucklerBash(LivingEntity entity) {
        List<LivingEntity> list = entity.level().getNearbyEntities(LivingEntity.class, TargetingConditions.forCombat(), entity, entity.getBoundingBox().inflate(1.5D));
        if (!list.isEmpty()) {
            LivingEntity entityHit = list.getFirst();
            entityHit.push(entity);
            if (entityHit.invulnerableTime <= 0) {
                int bangLevel = PPEnchantments.getBucklerEnchantsOnHands(PPEnchantments.BANG, entity);
                int turningLevel = PPEnchantments.getBucklerEnchantsOnHands(PPEnchantments.TURNING, entity);
                RangedRandomAttributeModifierHolder.Instance attackDamage = ATTACK_DAMAGE.getWithSummands(minDamageReduction(turningLevel), maxDamageReduction(turningLevel));
                float damage = (float) attackDamage.randomIntAmount();
                float knockbackStrength = 3.0F;
                for (int duration = 0; duration < 10; ++duration) {
                    double d0 = entity.getRandom().nextGaussian() * 0.02D;
                    double d1 = entity.getRandom().nextGaussian() * 0.02D;
                    double d2 = entity.getRandom().nextGaussian() * 0.02D;
                    SimpleParticleType type = entityHit instanceof WitherBoss || entityHit instanceof WitherSkeleton ? ParticleTypes.SMOKE : ParticleTypes.CLOUD;
                    // Collision is done on the server side, so a server side method must be used.
                    ((ServerLevel) entity.level()).sendParticles(type, entity.getRandomX(1.0D), entity.getRandomY() + 1.0D, entity.getRandomZ(1.0D), 1, d0, d1, d2, 1.0D);
                }
                if (bangLevel == 0) {
                    if (entityHit.hurt(entity.damageSources().mobAttack(entity), damage)) {
                        entityHit.knockback(knockbackStrength, Mth.sin(entity.getYRot() * ((float) Math.PI / 180F)), -Mth.cos(entity.getYRot() * ((float) Math.PI / 180F)));
                        entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                    }
                    if (!entity.isSilent())
                        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), PPSounds.SHIELD_BASH.get(), entity.getSoundSource(), 0.5F, 0.8F + entity.getRandom().nextFloat() * 0.4F);
                    if (entityHit instanceof Player && entityHit.getUseItem().canPerformAction(ItemAbilities.SHIELD_BLOCK))
                        ((Player) entityHit).disableShield();
                } else {
                    boolean isInMainHand = entity.getMainHandItem().getItem() instanceof BucklerItem;
                    ItemStack stack = entity.getItemInHand(isInMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
                    stack.hurtAndBreak(2 * bangLevel, entity, isInMainHand ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                    Level.ExplosionInteraction mode = PPConfig.COMMON.BangBlockDestruction.get() ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE;
                    entity.level().explode(null, entity.getX(), entity.getY(), entity.getZ(), (float) bangLevel, mode);
                    setChargeTicks(stack, 0);
                }
                entity.setLastHurtMob(entityHit);
                if (entity instanceof Player player && !PPEnchantments.hasBucklerEnchantsOnHands(player, PPEnchantments.BANG, PPEnchantments.TURNING)) {
                    player.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), PPSounds.CRITICAL_ACTIVATE.get(), entity.getSoundSource(), 1.0F, 1.0F);
                    player.setData(PPDataAttachments.CRITICAL.get(), true);
                }
            }
        }
    }

    public static void spawnRunningEffectsWhileCharging(LivingEntity entity) {
        int i = Mth.floor(entity.getX());
        int j = Mth.floor(entity.getY() - (double) 0.2F);
        int k = Mth.floor(entity.getZ());
        BlockPos blockpos = new BlockPos(i, j, k);
        BlockState blockstate = entity.level().getBlockState(blockpos);
        if (!blockstate.addRunningEffects(entity.level(), blockpos, entity))
            if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                Vec3 vec3 = entity.getDeltaMovement();
                entity.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate).setPos(blockpos), entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * (double) entity.getDimensions(entity.getPose()).height(), entity.getY() + 0.1D, entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * (double) entity.getDimensions(entity.getPose()).width(), vec3.x * -4.0D, 1.5D, vec3.z * -4.0D);
            }
    }
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entity) {
        ItemStack itemstack = super.finishUsingItem(stack, worldIn, entity);
        BucklerItem.setReady(stack, true);
        BucklerItem.setChargeTicks(stack, worldIn);
        CHARGE_SPEED_BOOST.get().resetTransientModifier(entity);
        CHARGE_JUMP_PREVENTION.get().resetTransientModifier(entity);
        INCREASED_KNOCKBACK_RESISTANCE.get().resetTransientModifier(entity);
        TURNING_SPEED_REDUCTION.getWithSummand(turningReduction(stack.getEnchantmentLevel(PPEnchantments.getEnchant(PPEnchantments.TURNING, entity.registryAccess())))).resetTransientModifier(entity);
        stack.hurtAndBreak(1, entity, EquipmentSlot.OFFHAND);
        if (entity instanceof Player)
            ((Player) entity).getCooldowns().addCooldown(this, PPConfig.COMMON.bucklerCooldown.get());
        entity.stopUsingItem();
        if (entity instanceof AbstractPiglin)
            entity.playSound(PPSounds.PIGLIN_BRUTE_CHARGE.get(), 2.0F, entity.isBaby()
                    ? (entity.getRandom().nextFloat() - entity.getRandom().nextFloat()) * 0.2F + 1.5F
                    : (entity.getRandom().nextFloat() - entity.getRandom().nextFloat()) * 0.2F + 1.0F);
        return itemstack;
    }

    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity entity) {
        return 10;
    }


    @SuppressWarnings("deprecation") //In Minecraft, deprecated methods can be overridden but not called.
    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        return !playerIn.isInWaterRainOrBubble() ? super.use(worldIn, playerIn, handIn)
                : InteractionResultHolder.pass(playerIn.getItemInHand(handIn));
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Tags.Items.INGOTS_GOLD);
    }

    public List<Component> getDescription(ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean isDetailed = InputConstants.isKeyDown(minecraft.getWindow().getWindow(), minecraft.options.keyShift.getKey().getValue());
        int turningLevel = stack.getEnchantmentLevel(PPEnchantments.getEnchant(PPEnchantments.TURNING, minecraft.player.registryAccess()));
        boolean isBang = stack.getEnchantmentLevel(PPEnchantments.getEnchant(PPEnchantments.BANG, minecraft.player.registryAccess())) > 0;
        ArrayList<Component> list = new ArrayList<>();
        list.add(Component.translatable("item.piglinproliferation.buckler.desc.on_use").withStyle(ChatFormatting.GRAY));
        list.add(Component.literal(" ").append(Component.translatable("item.piglinproliferation.buckler.desc.charge_ability", doubleToString(ticksToSeconds(startingChargeTicks(stack, minecraft.player.level())))).withStyle(ChatFormatting.DARK_GREEN)));
        if (!isDetailed)
            list.add(Component.literal(" ").append(Component.translatable("item.piglinproliferation.buckler.desc.details", minecraft.options.keyShift.getTranslatedKeyMessage()).withStyle(ChatFormatting.GREEN)));
        else {
            list.add(Component.literal(" ").append(Component.translatable("item.piglinproliferation.buckler.desc.while_charging").withStyle(ChatFormatting.GREEN)));
            list.add(Component.literal("  ").append(CHARGE_SPEED_BOOST.get().translatable()));
            list.add(Component.literal("  ").append(INCREASED_KNOCKBACK_RESISTANCE.get().translatable()));
            list.add(Component.literal("  ").append(Component.translatable("item.piglinproliferation.buckler.desc.shield_bash").withStyle(ChatFormatting.BLUE)));
            if (PPConfig.COMMON.turningBucklerLaunchStrength.get() > 0 && turningLevel > 0)
                list.add(Component.literal("  ").append(Component.translatable("item.piglinproliferation.buckler.desc.launch").withStyle(ChatFormatting.BLUE)));
            if (turningLevel != 5)
                list.add(Component.literal("  ").append(TURNING_SPEED_REDUCTION.getWithSummand(turningReduction(turningLevel)).translatable()));
            list.add(Component.literal("  ").append(CHARGE_JUMP_PREVENTION.get().translatable()));
            list.add(Component.literal("  ").append(Component.translatable("item.piglinproliferation.buckler.desc.water").withStyle(ChatFormatting.RED)));
            list.add(Component.translatable("item.piglinproliferation.buckler.desc.on_shield_bash").withStyle(ChatFormatting.GRAY));
            if (isBang)
                list.add(Component.literal(" ").append(Component.translatable("item.piglinproliferation.buckler.desc.explosion").withStyle(ChatFormatting.DARK_GREEN)));
            else {
                list.add(Component.literal(" ").append(ATTACK_DAMAGE.getWithSummands(minDamageReduction(turningLevel), maxDamageReduction(turningLevel)).translatable(0)));
                if (turningLevel <= 0) {
                    list.add(Component.literal(" ").append(Component.translatable("item.piglinproliferation.buckler.desc.critical_aura").withStyle(ChatFormatting.DARK_GREEN)));
                    list.add(Component.literal(" ").append(Component.translatable("item.piglinproliferation.buckler.desc.critical_aura_expires").withStyle(ChatFormatting.RED)));
                }
            }
        }
        return list;
    }

    public static double turningReduction(int turningLevel) {
        return 0.2 * turningLevel;
    }

    public static int minDamageReduction(int turningLevel) {
        return -1 * turningLevel;
    }

    public static int maxDamageReduction(int turningLevel) {
        return Math.round((float) (minDamageReduction(turningLevel) * 1.2));
    }
}
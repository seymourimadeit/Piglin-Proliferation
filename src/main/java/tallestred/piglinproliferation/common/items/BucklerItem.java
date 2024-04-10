package tallestred.piglinproliferation.common.items;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
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
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.ToolAction;
import net.neoforged.neoforge.common.ToolActions;
import net.neoforged.neoforge.event.EventHooks;
import tallestred.piglinproliferation.capablities.PPCapablities;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.client.renderers.BucklerRenderer;
import tallestred.piglinproliferation.common.attribute.AttributeModifierHolder;
import tallestred.piglinproliferation.common.attribute.RangedAttributeModifierHolder;
import tallestred.piglinproliferation.common.enchantments.PPEnchantments;
import tallestred.piglinproliferation.configuration.PPConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static tallestred.piglinproliferation.CodeUtilities.ticksToSeconds;

public class BucklerItem extends ShieldItem {
    public static final AttributeModifierHolder CHARGE_SPEED_BOOST = new AttributeModifierHolder(Attributes.MOVEMENT_SPEED, UUID.fromString("A2F995E8-B25A-4883-B9D0-93A676DC4045"), "Charge speed boost", 9.0D, AttributeModifier.Operation.MULTIPLY_BASE);
    public static final AttributeModifierHolder KNOCKBACK_RESISTANCE = new AttributeModifierHolder(Attributes.KNOCKBACK_RESISTANCE, UUID.fromString("93E74BB2-05A5-4AC0-8DF5-A55768208A95"), "Knockback reduction", 1.0D, AttributeModifier.Operation.ADDITION);
    public static final int MIN_DAMAGE = 6;
    public static final int MAX_DAMAGE = 8;
    public static final RangedAttributeModifierHolder ATTACK_DAMAGE = new RangedAttributeModifierHolder(Attributes.ATTACK_DAMAGE, UUID.fromString("1DDF2C1B-0279-440F-A919-D07479E60684"), "Attack damage", MIN_DAMAGE, MAX_DAMAGE, AttributeModifier.Operation.ADDITION);
    //This is stored as a modifier for easy localisation, even though it's not actually modifying anything

    public BucklerItem(Properties p_i48470_1_) {
        super(p_i48470_1_);
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    public static void moveFowards(LivingEntity entity) {
        if (entity.isAlive()) {
            Vec3 look = entity.getViewVector(1.0F);
            Vec3 motion = entity.getDeltaMovement();
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
        CompoundTag compoundnbt = stack.getTag();
        return compoundnbt != null && compoundnbt.getBoolean("Ready");
    }

    public static int startingChargeTicks(ItemStack stack) {
        boolean hasTurning = stack.getEnchantmentLevel(PPEnchantments.TURNING.get()) > 0;
        return hasTurning ? PPConfig.COMMON.BucklerTurningRunTime.get() : PPConfig.COMMON.BucklerRunTime.get();
    }

    public static int getChargeTicks(ItemStack stack) {
        CompoundTag compoundnbt = stack.getTag();
        if (compoundnbt != null)
            return compoundnbt.getInt("ChargeTicks");
        else
            return 0;
    }

    public static void setChargeTicks(ItemStack stack) {
        setChargeTicks(stack, startingChargeTicks(stack));
    }

    public static void setChargeTicks(ItemStack stack, int chargeTicks) {
        CompoundTag compoundnbt = stack.getOrCreateTag();
        compoundnbt.putInt("ChargeTicks", chargeTicks);
    }

    public static void setReady(ItemStack stack, boolean ready) {
        CompoundTag compoundnbt = stack.getOrCreateTag();
        compoundnbt.putBoolean("Ready", ready);
    }

    public static void bucklerBash(LivingEntity entity) {
        List<LivingEntity> list = entity.level().getNearbyEntities(LivingEntity.class, TargetingConditions.forCombat(), entity, entity.getBoundingBox().inflate(1.5D));
        if (!list.isEmpty()) {
            LivingEntity entityHit = list.get(0);
            entityHit.push(entity);
            int bangLevel = PPEnchantments.getBucklerEnchantsOnHands(PPEnchantments.BANG.get(), entity);
            float damage = entity.getRandom().nextIntBetweenInclusive(MIN_DAMAGE, MAX_DAMAGE);
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
                if (entityHit instanceof Player && entityHit.getUseItem().canPerformAction(ToolActions.SHIELD_BLOCK))
                    ((Player) entityHit).disableShield(true);
            } else {
                InteractionHand hand = entity.getMainHandItem().getItem() instanceof BucklerItem ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                ItemStack stack = entity.getItemInHand(hand);
                stack.hurtAndBreak(2 * bangLevel, entity, (player1) -> {
                    player1.broadcastBreakEvent(hand);
                    if (entity instanceof Player)
                        EventHooks.onPlayerDestroyItem((Player) entity, entity.getUseItem(), hand);
                });
                Level.ExplosionInteraction mode = PPConfig.COMMON.BangBlockDestruction.get()? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE;
                entity.level().explode(null, entity.getX(), entity.getY(), entity.getZ(), (float) bangLevel, mode);
                setChargeTicks(stack, 0);
            }
            entity.setLastHurtMob(entityHit);
            if (entity instanceof Player player && PPEnchantments.getBucklerEnchantsOnHands(PPEnchantments.BANG.get(), player) == 0) {
                player.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), PPSounds.CRITICAL_ACTIVATE.get(), entity.getSoundSource(), 1.0F, 1.0F);
                player.setData(PPCapablities.CRITICAL.get(), true);
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
                entity.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate).setPos(blockpos), entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * (double) entity.getDimensions(entity.getPose()).height, entity.getY() + 0.1D, entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * (double) entity.getDimensions(entity.getPose()).width, vec3.x * -4.0D, 1.5D, vec3.z * -4.0D);
            }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new BucklerRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
            }
        });
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
        ItemStack itemstack = super.finishUsingItem(stack, worldIn, entityLiving);
        BucklerItem.setReady(stack, true);
        BucklerItem.setChargeTicks(stack);
        AttributeInstance speed = entityLiving.getAttribute(CHARGE_SPEED_BOOST.attribute());
        AttributeInstance knockback = entityLiving.getAttribute(KNOCKBACK_RESISTANCE.attribute());
        if (speed != null && knockback != null) {
            speed.removeModifier(CHARGE_SPEED_BOOST.modifier().getId());
            speed.addTransientModifier(CHARGE_SPEED_BOOST.modifier());
            knockback.removeModifier(KNOCKBACK_RESISTANCE.modifier().getId());
            knockback.addTransientModifier(KNOCKBACK_RESISTANCE.modifier());
        }
        stack.hurtAndBreak(1, entityLiving, (entityLiving1) -> entityLiving1.broadcastBreakEvent(EquipmentSlot.OFFHAND));
        if (entityLiving instanceof Player)
            ((Player) entityLiving).getCooldowns().addCooldown(this, PPConfig.COMMON.BucklerCooldown.get());
        entityLiving.stopUsingItem();
        if (entityLiving instanceof AbstractPiglin)
            entityLiving.playSound(PPSounds.PIGLIN_BRUTE_CHARGE.get(), 2.0F, entityLiving.isBaby()
                    ? (entityLiving.getRandom().nextFloat() - entityLiving.getRandom().nextFloat()) * 0.2F + 1.5F
                    : (entityLiving.getRandom().nextFloat() - entityLiving.getRandom().nextFloat()) * 0.2F + 1.0F);
        return itemstack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
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


    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.DEFAULT_SHIELD_ACTIONS.contains(toolAction);
    }

    public static List<Component> getDescription(Minecraft minecraft, ItemStack stack){
        boolean isDetailed = InputConstants.isKeyDown(minecraft.getWindow().getWindow(), minecraft.options.keyShift.getKey().getValue());
        boolean isTurning = stack.getEnchantmentLevel(PPEnchantments.TURNING.get()) > 0;
        boolean isBang = stack.getEnchantmentLevel(PPEnchantments.BANG.get()) > 0;

        ArrayList<Component> list = new ArrayList<>();
        list.add(Component.translatable("item.piglinproliferation.buckler.desc.on_use").withStyle(ChatFormatting.GRAY));
        list.add(Component.literal(" ").append(Component.translatable("item.piglinproliferation.buckler.desc.charge_ability", ticksToSeconds(startingChargeTicks(stack))).withStyle(ChatFormatting.DARK_GREEN)));
        if(!isDetailed)
            list.add(Component.literal(" ").append(Component.translatable("item.piglinproliferation.buckler.desc.details", minecraft.options.keyShift.getTranslatedKeyMessage()).withStyle(ChatFormatting.GREEN)));
        else {
            list.add(Component.literal(" ").append(Component.translatable("item.piglinproliferation.buckler.desc.while_charging").withStyle(ChatFormatting.GREEN)));
            list.add(Component.literal("  ").append(BucklerItem.CHARGE_SPEED_BOOST.translatable()));
            list.add(Component.literal("  ").append(BucklerItem.KNOCKBACK_RESISTANCE.translatable()));
            if (!isTurning)
                list.add(Component.literal("  ").append(Component.translatable("item.piglinproliferation.buckler.desc.shield_bash").withStyle(ChatFormatting.BLUE)));
            list.add(Component.literal("  ").append(Component.translatable("item.piglinproliferation.buckler.desc.cannot_jump").withStyle(ChatFormatting.RED)));
            if (!isTurning)
                list.add(Component.literal("  ").append(Component.translatable("item.piglinproliferation.buckler.desc.turn_speed").withStyle(ChatFormatting.RED)));
            list.add(Component.literal("  ").append(Component.translatable("item.piglinproliferation.buckler.desc.water").withStyle(ChatFormatting.RED)));
            if (!isTurning) {
                list.add(Component.translatable("item.piglinproliferation.buckler.desc.on_shield_bash").withStyle(ChatFormatting.GRAY));
                if (isBang)
                    list.add(Component.literal(" ").append(Component.translatable("item.piglinproliferation.buckler.desc.explosion").withStyle(ChatFormatting.DARK_GREEN)));
                else {
                    list.add(Component.literal(" ").append(BucklerItem.ATTACK_DAMAGE.translatable(0)));
                    list.add(Component.literal(" ").append(Component.translatable("item.piglinproliferation.buckler.desc.critical_aura").withStyle(ChatFormatting.DARK_GREEN)));
                    list.add(Component.literal(" ").append(Component.translatable("item.piglinproliferation.buckler.desc.critical_aura_expires").withStyle(ChatFormatting.RED)));
                }
            }
        }
        return list;
    }
}

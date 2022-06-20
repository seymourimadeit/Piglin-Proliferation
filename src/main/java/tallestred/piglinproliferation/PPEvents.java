package tallestred.piglinproliferation;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestred.piglinproliferation.common.entities.ai.goals.PiglinCallForHelpGoal;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID)
public class PPEvents {
    @SubscribeEvent
    public static void entityJoin(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof AbstractPiglin piglin) {
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> {
                return piglin1.isOnFire() && !piglin1.hasEffect(MobEffects.FIRE_RESISTANCE);
            }, (alchemist -> {
                return alchemist.getItemShownOnOffhand() != null && PotionUtils.getPotion(alchemist.getItemShownOnOffhand()) == Potions.LONG_FIRE_RESISTANCE;
            })));
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> {
                return piglin1.getHealth() < piglin1.getMaxHealth() && !piglin1.hasEffect(MobEffects.REGENERATION);
            }, (alchemist -> {
                return alchemist.getItemShownOnOffhand() != null && PotionUtils.getPotion(alchemist.getItemShownOnOffhand()) == Potions.LONG_REGENERATION;
            })));
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> {
                return piglin1.getHealth() < (piglin1.getMaxHealth() / 2) && piglin1.getTarget() != null && !piglin1.hasEffect(MobEffects.DAMAGE_BOOST);
            }, (alchemist -> {
                return alchemist.getItemShownOnOffhand() != null && PotionUtils.getPotion(alchemist.getItemShownOnOffhand()) == Potions.LONG_STRENGTH;
            })));
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
                    ((Arrow)event.getSource().getDirectEntity()).setKnockback(0);
                    event.getEntity().invulnerableTime = 0;
                    if (event.getEntity() instanceof LivingEntity)
                        ((LivingEntity) event.getEntity()).hurtTime = 0;
                }
            }
        }
    }

    @SubscribeEvent
    public static void knockBackEntity(LivingKnockBackEvent event) {
        if (event.getEntityLiving().getLastDamageSource() != null && event.getEntityLiving().getLastDamageSource().getDirectEntity() != null) {
            Entity directEntity = event.getEntityLiving().getLastDamageSource().getDirectEntity();
            if (directEntity instanceof Arrow) {
                for (MobEffectInstance mobeffectinstance : ((Arrow) directEntity).potion.getEffects()) {
                    if ((mobeffectinstance.getEffect() == MobEffects.REGENERATION || mobeffectinstance.getEffect() == MobEffects.HEAL)) {
                        if ((event.getEntity() instanceof Mob && ((Mob) event.getEntity()).isInvertedHealAndHarm()))
                            return;
                        event.setCanceled(true);
                        ((Arrow)directEntity).setKnockback(0);
                    }
                }
            }
        }
    }
}

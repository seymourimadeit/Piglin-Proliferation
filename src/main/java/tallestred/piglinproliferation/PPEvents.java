package tallestred.piglinproliferation;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestred.piglinproliferation.common.entities.ai.goals.PiglinCallForHelpGoal;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID)
public class PPEvents {
    @SubscribeEvent
    public static void entityJoin(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof AbstractPiglin piglin) {
            piglin.setImmuneToZombification(true);
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> {
                return piglin1.isOnFire() && !piglin1.hasEffect(MobEffects.FIRE_RESISTANCE);
            }, (alchemist -> {
                return alchemist.getPotionAboutToThrown() != null && PotionUtils.getPotion(alchemist.getPotionAboutToThrown()) == Potions.LONG_FIRE_RESISTANCE;
            })));
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> {
                return piglin1.getHealth() < piglin1.getMaxHealth() && !piglin1.hasEffect(MobEffects.REGENERATION);
            }, (alchemist -> {
                return alchemist.getPotionAboutToThrown() != null && PotionUtils.getPotion(alchemist.getPotionAboutToThrown()) == Potions.LONG_REGENERATION;
            })));
            piglin.goalSelector.addGoal(0, new PiglinCallForHelpGoal(piglin, (piglin1) -> {
                return piglin1.getHealth() < 15 && piglin1.getTarget() != null && !piglin1.hasEffect(MobEffects.DAMAGE_BOOST);
            }, (alchemist -> {
                return alchemist.getPotionAboutToThrown() != null && PotionUtils.getPotion(alchemist.getPotionAboutToThrown()) == Potions.LONG_STRENGTH;
            })));
        }
    }
}

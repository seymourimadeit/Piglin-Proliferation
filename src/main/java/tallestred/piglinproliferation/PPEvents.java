package tallestred.piglinproliferation;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;
import tallestred.piglinproliferation.common.entities.ai.goals.PiglinCallForHelpGoal;

import java.util.List;

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
                List<AbstractPiglin> list = piglin1.level.getEntitiesOfClass(AbstractPiglin.class, piglin1.getBoundingBox().inflate(15.0D, 3.0D, 15.0D));
                if (!list.isEmpty()) {
                    for (AbstractPiglin otherPiglins : list) {
                        if (otherPiglins.getHealth() < 15 && list.size() > 3) {
                            return piglin1.getHealth() < 15 && piglin1.getTarget() != null;
                        }
                    }
                }
                return false;
            }, (alchemist -> {
                return alchemist.getPotionAboutToThrown() != null && PotionUtils.getPotion(alchemist.getPotionAboutToThrown()) == Potions.LONG_STRENGTH;
            })));
        }
    }
}

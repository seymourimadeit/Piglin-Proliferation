package tallestred.piglinproliferation.common.entities.ai.behaviors;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.monster.piglin.Piglin;
import tallestred.piglinproliferation.common.entities.PiglinTraveller;

public class KickWhenClose {
    public static OneShot<Piglin> create(int p_259782_) {
        return BehaviorBuilder.create(
                p_260278_ -> p_260278_.group(
                                p_260278_.absent(MemoryModuleType.WALK_TARGET),
                                p_260278_.registered(MemoryModuleType.LOOK_TARGET),
                                p_260278_.present(MemoryModuleType.ATTACK_TARGET),
                                p_260278_.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                        )
                        .apply(p_260278_, (p_260206_, p_259953_, p_259993_, p_259209_) -> (p_259617_, mob, p_259374_) -> {
                            LivingEntity livingentity = p_260278_.get(p_259993_);
                            if (livingentity.closerThan(mob, (double) p_259782_) && p_260278_.<NearestVisibleLivingEntities>get(p_259209_).contains(livingentity) && ((PiglinTraveller) mob).getKickCoolDown() <= 0) {
                                p_259953_.set(new EntityTracker(livingentity, true));
                                if (mob.doHurtTarget(livingentity)) {
                                    ((PiglinTraveller) mob).setKickTicks(10);
                                    livingentity.knockback(1.0F, Mth.sin(mob.getYRot() * ((float) Math.PI / 180F)), (-Mth.cos(mob.getYRot() * ((float) Math.PI / 180F))));
                                    mob.lookAt(mob, 90.0F, 90.0F);
                                }
                                ((PiglinTraveller) mob).setKickCoolDown(100);
                                mob.setYRot(Mth.rotateIfNecessary(mob.getYRot(), mob.yHeadRot, 0.0F));
                                return true;
                            } else {
                                return false;
                            }
                        })
        );
    }
}

package tallestred.piglinproliferation.common.attribute;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestred.piglinproliferation.PiglinProliferation;

public class PPAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, PiglinProliferation.MODID);
    public static final DeferredHolder<Attribute, Attribute> TURNING_SPEED = ATTRIBUTES.register("turning_speed", () -> new RangedAttribute("attribute.piglinproliferation.name.generic.turning_speed", 1, 0, 10).setSyncable(true));

    public static double turningValue(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(PPAttributes.TURNING_SPEED.get());
        return instance != null ? instance.getValue() : 1;
    }

    public static boolean aiFailsTurningChance(LivingEntity entity) {
        double value = turningValue(entity);
        return value != 1 && entity.getRandom().nextDouble() < value;
    }
}
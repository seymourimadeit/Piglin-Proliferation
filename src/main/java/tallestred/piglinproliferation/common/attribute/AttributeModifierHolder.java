package tallestred.piglinproliferation.common.attribute;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import tallestred.piglinproliferation.PiglinProliferation;

import java.util.UUID;

public class AttributeModifierHolder extends AbstractAttributeModifierHolder {
    public final double defaultAmount;
    public final AttributeModifier.Operation defaultOperation;

    protected final Instance defaultInstance;

    public AttributeModifierHolder(Holder<Attribute> attribute, String name, double defaultAmount, AttributeModifier.Operation defaultOperation) {
        super(attribute, name);
        this.defaultAmount = defaultAmount;
        this.defaultOperation = defaultOperation;
        this.defaultInstance = new Instance(defaultAmount, defaultOperation);
    }

    @Override
    public Instance get() {
        return defaultInstance;
    }

    @Override
    public Instance getWithMultiplier(double multiplier) {
        return new Instance(defaultAmount * multiplier, defaultOperation);
    }

    @Override
    public Instance getWithSummand(double summand) {
        return new Instance(defaultAmount + summand, defaultOperation);
    }

    public Instance getWithNewAmount(double newAmount) {
        return new Instance(newAmount, defaultOperation);
    }

    public Instance getWithNewAmountAndOperation(double newAmount, AttributeModifier.Operation operation) {
        return new Instance(newAmount, operation);
    }

    public class Instance extends AbstractAttributeModifierHolder.Instance {
        public AttributeModifier modifier;

        protected Instance(double amount, AttributeModifier.Operation operation) {
            this.modifier = new AttributeModifier(ResourceLocation.fromNamespaceAndPath(PiglinProliferation.MODID, name), amount, operation);
        }

        public void addTransientModifier(LivingEntity entity) {
            addTransientInternal(modifier, entity);
        }

        public void addPermanentModifier(LivingEntity entity) {
            addPermanentInternal(modifier, entity);
        }

        public MutableComponent translatable() {
            return translatableInternal(this.modifier.amount(), this.modifier.operation(), false, -1);
        }

        public MutableComponent translatable(double baseAmount) {
            return translatableInternal(this.modifier.amount(), this.modifier.operation(), true, baseAmount);
        }
    }
}

package tallestred.piglinproliferation.common.attribute;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

public class RangedAttributeModifierHolder extends AbstractAttributeModifierHolder {
    public final double defaultMinAmount;
    public final double defaultMaxAmount;
    public final AttributeModifier.Operation defaultOperation;
    protected final Instance defaultInstance;

    public RangedAttributeModifierHolder(Attribute attribute, UUID uuid, String name, double defaultMinAmount, double defaultMaxAmount, AttributeModifier.Operation defaultOperation) {
        super(attribute, uuid, name);
        this.defaultMinAmount = defaultMinAmount;
        this.defaultMaxAmount = defaultMaxAmount;
        this.defaultOperation = defaultOperation;
        this.defaultInstance = new Instance(defaultMinAmount, defaultMaxAmount, defaultOperation);
    }

    public Instance get() {
        return defaultInstance;
    }

    public Instance getWithMultiplier(double multiplier) {
        return getWithMultipliers(multiplier, multiplier);
    }

    public Instance getWithMultipliers(double minMultiplier, double maxMultiplier) {
        return new Instance(defaultMinAmount * minMultiplier, defaultMaxAmount * maxMultiplier, defaultOperation);
    }

    public Instance getWithSummand(double summand) {
        return getWithSummands(summand, summand);
    }

    public Instance getWithSummands(double minSummand, double maxSummand) {
        return new Instance(defaultMinAmount + minSummand, defaultMaxAmount + maxSummand, defaultOperation);
    }

    public Instance getWithNewAmounts(double newMinAmount, double newMaxAmount) {
        return new Instance(newMinAmount, newMaxAmount, defaultOperation);
    }

    public Instance getWithNewAmountAndOperation(double newMinAmount, double newMaxAmount, AttributeModifier.Operation operation) {
        return new Instance(newMinAmount, newMaxAmount, operation);
    }

    public class Instance extends AbstractAttributeModifierHolder.Instance {
        public final double minAmount;
        public final double maxAmount;
        public final AttributeModifier.Operation operation;

        protected Instance(double minAmount, double maxAmount, AttributeModifier.Operation operation) {
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.operation = operation;
        }

        public void resetTransientModifierRandom(RandomSource random, LivingEntity entity) {
            resetTransientModifier(randomAmount(random), entity);
        }

        public void resetTransientModifier(double amount, LivingEntity entity) {
            removeModifier(entity);
            addTransientModifier(amount, entity);
        }

        public void resetPermanentModifierRandom(RandomSource random, LivingEntity entity) {
            resetPermanentModifier(randomAmount(random), entity);
        }

        public void resetPermanentModifier(double amount, LivingEntity entity) {
            removeModifier(entity);
            addPermanentModifier(amount, entity);
        }


        public void addTransientModifier(double amount, LivingEntity entity) {
            addTransientInternal(createModifier(amount), entity);
        }

        public void addPermanentModifier(double amount, LivingEntity entity) {
            addPermanentInternal(createModifier(amount), entity);
        }

        public MutableComponent translatable() {
            return translatableInternal(maxAmount, operation, false,-1);
        }

        public MutableComponent translatable(double baseAmount) {
            return translatableInternal(maxAmount, operation, true, baseAmount);
        }

        @Override
        protected MutableComponent translatableInternal(double maxAmount, AttributeModifier.Operation operation, boolean displaysBase, double baseValue) {
            MutableComponent result = super.translatableInternal(maxAmount, operation, displaysBase, baseValue);
            if (result.getContents() instanceof TranslatableContents contents) {
                String newKey = contents.getKey().replace("modifier", "piglinproliferation.ranged_modifier");
                Object[] oldArgs = contents.getArgs();
                Object[] newArgs = new Object[contents.getArgs().length+1];
                newArgs[0] = ATTRIBUTE_MODIFIER_FORMAT.format(formattedAmount(minAmount, operation, displaysBase, baseValue));
                System.arraycopy(oldArgs, 0, newArgs, 1, oldArgs.length);
                result = Component.translatable(newKey, newArgs).withStyle(result.getStyle());
            }
            return result;
        }

        protected AttributeModifier createModifier(double amount) {
            return new AttributeModifier(uuid, name, amount, operation);
        }

        public double randomAmount(RandomSource source) {
            return minAmount + ((maxAmount-minAmount) * source.nextDouble());
        }
    }
}

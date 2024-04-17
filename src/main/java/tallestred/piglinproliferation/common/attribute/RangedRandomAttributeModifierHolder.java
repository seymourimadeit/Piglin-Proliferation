package tallestred.piglinproliferation.common.attribute;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;
import java.util.random.RandomGenerator;

import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;
import static tallestred.piglinproliferation.util.CodeUtilities.capToRange;

public class RangedRandomAttributeModifierHolder extends AbstractAttributeModifierHolder {
    public final double defaultMinAmount;
    public final double defaultMaxAmount;
    public final AttributeModifier.Operation defaultOperation;
    protected final Instance defaultInstance;

    public RangedRandomAttributeModifierHolder(Attribute attribute, UUID uuid, String name, double defaultMinAmount, double defaultMaxAmount, AttributeModifier.Operation defaultOperation) {
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
        protected RandomGenerator random;


        protected Instance(double minAmount, double maxAmount, AttributeModifier.Operation operation) {
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.operation = operation;
        }

        public AttributeModifier modifier() {
            return modifier(randomAmount());
        }

        public AttributeModifier modifier(double amount) {
            return new AttributeModifier(uuid, name, capToRange(amount, minAmount, maxAmount), operation);
        }

        public void addTransientModifier(LivingEntity entity) {
            addTransientInternal(modifier(), entity);
        }

        public void addPermanentModifier(LivingEntity entity) {
            addPermanentInternal(modifier(), entity);
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

        public double randomAmount() {
            return random().nextDouble(minAmount, maxAmount);
        }

        public int randomIntAmount() {
            return random().nextInt(Math.round((float) minAmount), Math.round((float) maxAmount) + 1);
        }

        protected RandomGenerator random() {
           if (random == null)
               random = RandomGenerator.getDefault();
           return random;
        }
    }
}

package tallestred.piglinproliferation.common.attribute;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import tallestred.piglinproliferation.PiglinProliferation;

import java.util.UUID;

import static net.minecraft.world.item.component.ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT;

public abstract class AbstractAttributeModifierHolder {
    public final Holder<Attribute> attribute;
    public final ResourceLocation resourceLocation;
    public final String name;

    public AbstractAttributeModifierHolder(Holder<Attribute> attribute, String name) {
        this.attribute = attribute;
        this.name = name;
        this.resourceLocation = ResourceLocation.fromNamespaceAndPath(PiglinProliferation.MODID, name);
    }

    public abstract Instance get();

    public abstract Instance getWithMultiplier(double multiplier);

    public abstract Instance getWithSummand(double summand);

    public boolean hasModifier(LivingEntity entity) {
        return entity.getAttributes().hasModifier(attribute, resourceLocation);
    }

    public void removeModifier(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null)
            instance.removeModifier(resourceLocation);
    }

    public abstract class Instance {

        public void resetTransientModifier(LivingEntity entity) {
            removeModifier(entity);
            addTransientModifier(entity);
        }

        public void resetPermanentModifier(LivingEntity entity) {
            removeModifier(entity);
            addPermanentModifier(entity);
        }

        public abstract void addTransientModifier(LivingEntity entity);

        public abstract void addPermanentModifier(LivingEntity entity);

        protected void addTransientInternal(AttributeModifier modifier, LivingEntity entity) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance != null)
                instance.addTransientModifier(modifier);
        }

        protected void addPermanentInternal(AttributeModifier modifier, LivingEntity entity) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance != null)
                instance.addPermanentModifier(modifier);
        }

        public abstract MutableComponent translatable();

        public abstract MutableComponent translatable(double baseAmount);

        protected MutableComponent translatableInternal(double amount, AttributeModifier.Operation operation, boolean displaysBase, double base) {
            amount = formattedAmount(amount, operation, displaysBase, base);
            String key = "attribute.modifier.equals.";
            ChatFormatting style = ChatFormatting.DARK_GREEN;
            if (!displaysBase) {
                if (amount > 0) {
                    key = "attribute.modifier.plus.";
                    style = ChatFormatting.BLUE;
                }
                if (amount < 0) {
                    amount *= -1.0;
                    key = "attribute.modifier.take.";
                    style = ChatFormatting.RED;
                }
            }
            return Component.translatable(key + operation.id(), ATTRIBUTE_MODIFIER_FORMAT.format(amount), Component.translatable(attribute.value().getDescriptionId())).withStyle(style);
        }

        protected double formattedAmount(double amount, AttributeModifier.Operation operation, boolean displaysBase, double base) {
            if (displaysBase)
                amount += base;
            switch (operation) {
                case ADD_VALUE -> amount *= attribute.equals(Attributes.KNOCKBACK_RESISTANCE) ? 10 : 1;
                case ADD_MULTIPLIED_BASE, ADD_MULTIPLIED_TOTAL -> amount *= 100.0;
            }
            return amount;
        }
    }
}

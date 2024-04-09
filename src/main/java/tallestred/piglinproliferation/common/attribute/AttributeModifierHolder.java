package tallestred.piglinproliferation.common.attribute;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

public class AttributeModifierHolder {
    protected final Attribute attribute;
    protected final AttributeModifier modifier;

    public AttributeModifierHolder(Attribute attribute, AttributeModifier modifier) {
        this.attribute = attribute;
        this.modifier = modifier;
    }

    public AttributeModifierHolder(Attribute attribute, UUID uuid, String name, double amount, AttributeModifier.Operation operation) {
        this(attribute, new AttributeModifier(uuid, name, amount, operation));
    }

    public Attribute attribute() {
        return this.attribute;
    }

    public AttributeModifier modifier() {
        return this.modifier;
    }

    public MutableComponent translatable() {
        return translatableInternal(false,-1);
    }

    public MutableComponent translatable(double baseValue) {
        return translatableInternal(true,baseValue);
    }

    protected MutableComponent translatableInternal(boolean displaysBase, double base) {
        double amount = formattedAmount(modifier, displaysBase, base);
        String key = "attribute.modifier.equals.";
        ChatFormatting style = ChatFormatting.DARK_GREEN;
        if (!displaysBase) {
            if (amount > 0) {
                key = "attribute.modifier.plus.";
                style = ChatFormatting.BLUE;
            }
            if (amount < 0) {
                key = "attribute.modifier.take.";
                style = ChatFormatting.RED;
            }
        }
        return Component.translatable(key + modifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(amount), Component.translatable(attribute.getDescriptionId())).withStyle(style);
    }

    protected double formattedAmount(AttributeModifier modifier, boolean displaysBase, double base) {
        double amount = modifier.getAmount();
        if (displaysBase)
            amount += base;
        switch (modifier.getOperation()) {
            case ADDITION -> amount *= attribute.equals(Attributes.KNOCKBACK_RESISTANCE) ? 10 : 1;
            case MULTIPLY_BASE, MULTIPLY_TOTAL -> amount *= 100.0;
        }
        if (amount < 0)
            amount *= -1.0;
        return amount;
    }
}
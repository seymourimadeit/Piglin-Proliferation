package tallestred.piglinproliferation.common.attribute;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.*;

import static net.minecraft.world.item.ItemStack.ATTRIBUTE_MODIFIER_FORMAT;

public class RangedAttributeModifierHolder extends AttributeModifierHolder{
    private final AttributeModifier min;

    public RangedAttributeModifierHolder(Attribute attribute, AttributeModifier min, AttributeModifier max) {
        super(attribute, max);
        this.min = min;
    }
    public RangedAttributeModifierHolder(Attribute attribute, UUID uuid, String name, double minAmount, double maxAmount, AttributeModifier.Operation operation) {
        this(attribute, new AttributeModifier(uuid, name, minAmount, operation), new AttributeModifier(uuid, name, maxAmount, operation));
    }

    public AttributeModifier min() {
        return this.min;
    }

    public AttributeModifier max() {
        return this.modifier();
    }
    @Override
    public MutableComponent translatableInternal(boolean displaysBase, double baseValue) {
        MutableComponent result = super.translatableInternal(displaysBase, baseValue);
        if (result.getContents() instanceof TranslatableContents contents) {
            String newKey = contents.getKey().replace("modifier", "piglinproliferation.ranged_modifier");
            Object[] oldArgs = contents.getArgs();
            Object[] newArgs = new Object[contents.getArgs().length+1];
            newArgs[0] = ATTRIBUTE_MODIFIER_FORMAT.format(formattedAmount(min(), displaysBase, baseValue));
            System.arraycopy(oldArgs, 0, newArgs, 1, oldArgs.length);
            result = Component.translatable(newKey, newArgs).withStyle(result.getStyle());
        }
        return result;
    }
}

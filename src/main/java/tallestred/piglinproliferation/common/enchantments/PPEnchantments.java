package tallestred.piglinproliferation.common.enchantments;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.common.items.BucklerItem;
import tallestred.piglinproliferation.common.tags.PPTags;

import java.util.List;
import java.util.function.UnaryOperator;

public class PPEnchantments {
    public static final DeferredRegister<DataComponentType<?>> ENCHANTMENTS = DeferredRegister.create(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, PiglinProliferation.MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>> TURNING = registerComponentType("turning", builder ->
           builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ITEM).listOf()));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>> BANG = registerComponentType("turning", builder ->
            builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ITEM).listOf()));

    public static boolean hasBucklerEnchantsOnHands(LivingEntity player, Enchantment... enchantments) {
        InteractionHand hand = player.getMainHandItem().getItem() instanceof BucklerItem ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack stack = player.getItemInHand(hand);
        for (Enchantment enchantment : enchantments)
            if (stack.getEnchantmentLevel(enchantment) > 0)
                return true;
        return false;
    }

    public static int getBucklerEnchantsOnHands(Enchantment enchantment, LivingEntity player) {
        InteractionHand hand = player.getMainHandItem().getItem() instanceof BucklerItem ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack stack = player.getItemInHand(hand);
        return stack.getEnchantmentLevel(enchantment);
    }

    public static <D> DeferredHolder<DataComponentType<?>, DataComponentType<D>> registerComponentType(String name, UnaryOperator<DataComponentType.Builder<D>> builder) {
        return ENCHANTMENTS.register(name, () -> builder.apply(DataComponentType.builder()).build());
    }
}

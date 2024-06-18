package tallestred.piglinproliferation.common.enchantments;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.common.items.BucklerItem;

public class PPEnchantments {
    public static final ResourceKey<Enchantment> BANG = createResourceKey("bang");
    public static final ResourceKey<Enchantment> TURNING = createResourceKey("turning");

    public static boolean hasBucklerEnchantsOnHands(LivingEntity player, ResourceKey<Enchantment>... enchantment) {
        InteractionHand hand = player.getMainHandItem().getItem() instanceof BucklerItem ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack stack = player.getItemInHand(hand);
        for (ResourceKey<Enchantment> enchantments : enchantment)
            if (stack.getEnchantmentLevel(getEnchant(enchantments, player.registryAccess())) > 0)
                return true;
        return false;
    }

    public static int getBucklerEnchantsOnHands(ResourceKey<Enchantment> enchantment, LivingEntity player) {
        InteractionHand hand = player.getMainHandItem().getItem() instanceof BucklerItem ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack stack = player.getItemInHand(hand);
        return stack.getEnchantmentLevel(getEnchant(enchantment, player.registryAccess()));
    }

    public static Holder<Enchantment> getEnchant(ResourceKey<Enchantment> enchantment, RegistryAccess access) {
        return access.registry(Registries.ENCHANTMENT).get().getHolderOrThrow(enchantment);
    }

    public static ResourceKey<Enchantment> createResourceKey(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(PiglinProliferation.MODID, path));
    }
}

package tallestred.piglinproliferation.common.enchantments;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tallestred.piglinproliferation.PiglinProliferation;
import tallestred.piglinproliferation.common.items.BucklerItem;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, PiglinProliferation.MODID);
    public static final RegistryObject<Enchantment> TURNING = ENCHANTMENTS.register("turning", () -> new TurningEnchantment(Enchantment.Rarity.COMMON, EquipmentSlot.MAINHAND));
    public static final RegistryObject<Enchantment> BANG = ENCHANTMENTS.register("bang", () -> new BangEnchantment(Enchantment.Rarity.COMMON, EquipmentSlot.MAINHAND));

    public static final EnchantmentCategory BUCKLER = EnchantmentCategory.create("buckler", (item) -> (item instanceof BucklerItem));

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
}

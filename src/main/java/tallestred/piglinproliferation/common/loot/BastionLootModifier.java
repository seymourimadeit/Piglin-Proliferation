package tallestred.piglinproliferation.common.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import tallestred.piglinproliferation.configuration.PPConfig;
import tallestred.piglinproliferation.common.items.PPItems;

import java.util.function.Supplier;

public class BastionLootModifier extends LootModifier {
    public static final Supplier<MapCodec<BastionLootModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, BastionLootModifier::new)));

    public BastionLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getRandom().nextFloat() < PPConfig.COMMON.bucklerChance.get()) {
            generatedLoot.add(new ItemStack(PPItems.BUCKLER.get(), 1));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}

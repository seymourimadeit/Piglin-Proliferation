package tallestred.piglinproliferation.configuration;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;
import tallestred.piglinproliferation.PiglinProliferation;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ClientConfig CLIENT;

    static {
        {
            final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
            COMMON = specPair.getLeft();
            COMMON_SPEC = specPair.getRight();
        }
        {
            final Pair<ClientConfig, ForgeConfigSpec> specPair1 = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
            CLIENT = specPair1.getLeft();
            CLIENT_SPEC = specPair1.getRight();
        }
    }


    public static class CommonConfig {
        public final ForgeConfigSpec.BooleanValue healingArrowDamage;
        public final ForgeConfigSpec.IntValue alchemistWeightInBastions;
        public final ForgeConfigSpec.DoubleValue healingArrowChances;
        public final ForgeConfigSpec.DoubleValue piglinVariantChances;
        public final ForgeConfigSpec.DoubleValue zombifiedPiglinDefaultChance;
        public final ForgeConfigSpec.DoubleValue zombifiedBruteChance;
        public final ForgeConfigSpec.DoubleValue zombifiedAlchemistChance;
        public final ForgeConfigSpec.DoubleValue alchemistPotionChance;
        public final ForgeConfigSpec.IntValue healingArrowMinStackSize;
        public final ForgeConfigSpec.IntValue healingArrowMaxStackSize;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> zombifiedPiglinTypeList;

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            builder.push("Vanilla Changes");
            healingArrowDamage = builder.define("Allow healing and Regeneration arrows to not do damage?", true);
            alchemistWeightInBastions = builder.comment("Weight is calculated by dividing the current value by the sum of all weights combined.",
                    "Use https://minecraft.fandom.com/wiki/Bastion_Remnant?so=search#cite_ref-piglin_group_1-39 as a guide.",
                    "The default weight for alchemists spawning is 4, giving them a 28% chance of spawning in bastions",
                    "(Due to this, regular piglins have a spawn rate of 28% aswell, and brutes have a spawn rate of 7% spawn rate, changed values will have to account for this).", "To change the natural spawn rates, use a datapack that overrides add_alchemist.json file, located in data/piglinproliferation/data/forge/biome_modifier").defineInRange("Alchemist spawnrate weight in bastions", 4, Integer.MIN_VALUE, Integer.MAX_VALUE);
            zombifiedPiglinDefaultChance = builder.defineInRange("Chance of zombified piglins spawning with regular piglin clothing", 0.90F, 0.0F, 9000.0F);
            zombifiedBruteChance = builder.defineInRange("Chance of zombified piglins spawning with brute clothing (including items)", 0.015F, 0.0F, 9000.0F);
            zombifiedAlchemistChance = builder.defineInRange("Chance of zombified piglins spawning with alchemist clothing (including items", 0.10F, 0.0F, 9000.0F);
            piglinVariantChances = builder.defineInRange("Chance of zombified piglin variants spawning from the list below", 0.50F, 0.0F, 9000.0F);
            zombifiedPiglinTypeList = builder.comment("List of variants a zombified piglin has a 50% of turning into when spawning.", "Keep in mind they will still spawn with golden swords, this will be changed in the future.").defineList("List of piglin variants to use", ImmutableList.of(), obj -> true);
            builder.pop();
            builder.push("Piglin Alchemist");
            alchemistPotionChance = builder.defineInRange("Chance of alchemist potions not getting broken when killed", 0.20F, 0.0F, 9000.0F);
            builder.push("Healing Arrow Chances");
            healingArrowChances = builder.defineInRange("Chances of an alchemist getting healing arrows", 0.30F, 0.0F, 100.0F);
            healingArrowMinStackSize = builder.defineInRange("Minmium healing arrow stack size", 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            healingArrowMaxStackSize = builder.defineInRange("Maximium healing arrow stack size", 6, Integer.MIN_VALUE, Integer.MAX_VALUE);
            builder.pop();
            builder.pop();
        }
    }

    public static class ClientConfig {
        public final ForgeConfigSpec.BooleanValue ziglinTextures;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.push("vanilla changes");
            ziglinTextures = builder.define("Allow Zombified Piglins to render consistent clothing?", true);
            builder.pop();
        }
    }
}

package tallestred.piglinproliferation.configuration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class PPConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ClientConfig CLIENT;

    static {
        {
            final Pair<CommonConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
            COMMON = specPair.getLeft();
            COMMON_SPEC = specPair.getRight();
        }
        {
            final Pair<ClientConfig, ModConfigSpec> specPair1 = new ModConfigSpec.Builder().configure(ClientConfig::new);
            CLIENT = specPair1.getLeft();
            CLIENT_SPEC = specPair1.getRight();
        }
    }


    public static class CommonConfig {
        public final ModConfigSpec.BooleanValue healingArrowDamage;
        public final ModConfigSpec.BooleanValue BruteBuckler;
        public final ModConfigSpec.BooleanValue BangBlockDestruction;
        public final ModConfigSpec.IntValue alchemistWeightInBastions;
        public final ModConfigSpec.DoubleValue healingArrowChances;
        public final ModConfigSpec.DoubleValue piglinVariantChances;
        public final ModConfigSpec.DoubleValue zombifiedPiglinDefaultChance;
        public final ModConfigSpec.DoubleValue zombifiedBruteChance;
        public final ModConfigSpec.DoubleValue zombifiedAlchemistChance;
        public final ModConfigSpec.DoubleValue alchemistPotionChance;
        public final ModConfigSpec.DoubleValue bucklerChance;
        public final ModConfigSpec.DoubleValue crossbowChance;
        public final ModConfigSpec.DoubleValue crossbowChanceTraveler;
        public final ModConfigSpec.IntValue healingArrowMinStackSize;
        public final ModConfigSpec.IntValue healingArrowMaxStackSize;
        public final ModConfigSpec.IntValue bucklerCooldown;
        public final ModConfigSpec.IntValue minBucklerChargeTime;
        public final ModConfigSpec.IntValue maxBucklerChargeTime;
        public final ModConfigSpec.DoubleValue turningBucklerLaunchStrength;
        public final ModConfigSpec.ConfigValue<List<? extends String>> zombifiedPiglinTypeList;
        public final ModConfigSpec.ConfigValue<List<? extends String>> mobsThatCanAlsoUseBuckler;
        public CommonConfig(ModConfigSpec.Builder builder) {
            builder.push("Vanilla Changes");
            healingArrowDamage = builder.define("Allow healing and Regeneration arrows to not do damage?", true);
            alchemistWeightInBastions = builder.comment("""
                    Weight is calculated by dividing the current value by the sum of all weights combined.
                    Use https://minecraft.fandom.com/wiki/Bastion_Remnant?so=search#cite_ref-piglin_group_1-39 as a guide.
                    The default weight for alchemists spawning is 4, giving them a 28% chance of spawning in bastions.
                    (Due to this, regular piglins have a spawn rate of 28% aswell, and brutes have a spawn rate of 7% spawn rate, changed values will have to account for this).
                    To change the natural spawn rates, use a datapack that overrides add_alchemist.json file, located in data/piglinproliferation/data/forge/biome_modifier""").defineInRange("Alchemist spawnrate weight in bastions", 4, Integer.MIN_VALUE, Integer.MAX_VALUE);
            zombifiedPiglinDefaultChance = builder.defineInRange("Chance of zombified piglins spawning with regular piglin clothing", 0.90F, 0.0F, 9000.0F);
            zombifiedBruteChance = builder.defineInRange("Chance of zombified piglins spawning with brute clothing (including items)", 0.015F, 0.0F, 9000.0F);
            crossbowChance = builder.defineInRange("Chance of zombified piglins spawning with crossbows", 0.50F, 0.0F, 9000.0F);
            zombifiedAlchemistChance = builder.defineInRange("Chance of zombified piglins spawning with alchemist clothing (including items", 0.10F, 0.0F, 9000.0F);
            piglinVariantChances = builder.defineInRange("Chance of zombified piglin variants spawning from the list below", 0.50F, 0.0F, 9000.0F);
            zombifiedPiglinTypeList = builder.comment("List of variants a zombified piglin has a 50% of turning into when spawning.", "Keep in mind they will still spawn with golden swords, this will be changed in the future.").defineList("List of piglin variants to use", ImmutableList.of(), obj -> true);
            builder.pop();
            builder.push("Buckler");
            mobsThatCanAlsoUseBuckler = builder.define("Mobs that can also use the buckler", Lists.newArrayList("guardvillagers:guard"));
            BangBlockDestruction = builder.define("Have the explosion spawned while using the Bang! enchant destroy blocks?", false);
            BruteBuckler = builder.define("Have brutes spawn with bucklers?", true);
            bucklerCooldown = builder.defineInRange("How long should the buckler's cooldown be in ticks?", 240, Integer.MIN_VALUE, Integer.MAX_VALUE);
            minBucklerChargeTime = builder.defineInRange("How long should the buckler's charge move be in ticks?", 15, Integer.MIN_VALUE, Integer.MAX_VALUE); // Thinking of removing this in 1.17.
            maxBucklerChargeTime = builder.defineInRange("How long should the buckler's charge move if you have the max level of turning enchant be in ticks?", 40, Integer.MIN_VALUE, Integer.MAX_VALUE);
            bucklerChance = builder.defineInRange("Chance of buckler appearing in bastion loot", 0.25F, 0.0F, 9999999.0F);
            turningBucklerLaunchStrength = builder.comment("Experimental: Bucklers enchanted with Turning will allow the player to launch off of blocks they collide with. By default this is disabled, but if you want to test it out try 0.15").defineInRange("Launch strength ", 0.0D, 0.0D, 999999.0D);
            builder.pop();
            builder.push("Piglin Alchemist");
            alchemistPotionChance = builder.defineInRange("Chance of alchemist potions not getting broken when killed", 0.20F, 0.0F, 9000.0F);
            builder.push("Healing Arrow Chances");
            healingArrowChances = builder.defineInRange("Chances of an alchemist getting healing arrows", 0.30F, 0.0F, 100.0F);
            healingArrowMinStackSize = builder.defineInRange("Minmium healing arrow stack size", 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            healingArrowMaxStackSize = builder.defineInRange("Maximium healing arrow stack size", 6, Integer.MIN_VALUE, Integer.MAX_VALUE);
            builder.pop();
            builder.pop();
            builder.push("Piglin Traveler");
            crossbowChanceTraveler = builder.defineInRange("Chance of travelers equipping crossbows", 0.20F, 0.0F, 9000.0F);
            builder.pop();
        }
    }

    public static class ClientConfig {
        public final ModConfigSpec.BooleanValue ziglinTextures;
        public final ModConfigSpec.BooleanValue RenderAfterImage;
        public final ModConfigSpec.BooleanValue RenderAfterImageLayers;
        public ClientConfig(ModConfigSpec.Builder builder) {
            builder.push("vanilla changes");
            ziglinTextures = builder.define("Allow Zombified Piglins to render consistent clothing?", true);
            builder.pop();
            builder.push("after image");
            RenderAfterImage = builder.define("Render an after image while an entity is charging with a buckler?", true);
            RenderAfterImageLayers = builder.define("Render the layers of an entity when charging? (this includes things like items and armor, be warned that the alpha transparencies may glitch out)", false);
            builder.pop();
        }
    }
}

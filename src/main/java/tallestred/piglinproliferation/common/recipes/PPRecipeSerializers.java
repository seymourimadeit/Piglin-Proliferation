package tallestred.piglinproliferation.common.recipes;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tallestred.piglinproliferation.PiglinProliferation;

public class PPRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, PiglinProliferation.MODID);

    public static final RegistryObject<RecipeSerializer<?>> CRAFTING_SPECIAL_COMPASS_CLONING = RECIPE_SERIALIZERS.register("crafting_special_compass_cloning", () -> new SimpleCraftingRecipeSerializer<>(TravellersCompassCloningRecipe::new));
}
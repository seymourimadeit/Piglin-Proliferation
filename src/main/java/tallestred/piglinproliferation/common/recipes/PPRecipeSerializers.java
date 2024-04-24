package tallestred.piglinproliferation.common.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tallestred.piglinproliferation.PiglinProliferation;

public class PPRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, PiglinProliferation.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TravelersCompassCloningRecipe>> CRAFTING_SPECIAL_COMPASS_CLONING = RECIPE_SERIALIZERS.register("crafting_special_compass_cloning", () -> new SimpleCraftingRecipeSerializer<>(TravelersCompassCloningRecipe::new));
}

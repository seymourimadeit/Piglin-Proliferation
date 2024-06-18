package tallestred.piglinproliferation.common.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import tallestred.piglinproliferation.common.items.PPItems;
import tallestred.piglinproliferation.common.items.component.PPComponents;

/*matches and assemble are heavily based on the corresponding methods in BookCloningRecipe*/
public class TravelersCompassCloningRecipe extends CustomRecipe {
    public TravelersCompassCloningRecipe(CraftingBookCategory p_249010_) {
        super(p_249010_);
    }

    @Override
    public boolean matches(CraftingInput input, Level p_345375_) {
        ItemStack compassToCopy = ItemStack.EMPTY;
        boolean hasWriteableCompasses = false;
        for (int j = 0; j < input.size(); j++) {
            ItemStack itemStack = input.getItem(j);
            if (!itemStack.isEmpty())
                if (itemStack.is(PPItems.TRAVELERS_COMPASS.get())) {
                    if (!compassToCopy.isEmpty())
                        return false;
                    compassToCopy = itemStack;
                } else if (itemStack.is(Items.COMPASS))
                    hasWriteableCompasses = true;
                else return false;
        }
        return !compassToCopy.isEmpty() && compassToCopy.has(PPComponents.TRAVELERS_COMPASS_TRACKER) && hasWriteableCompasses;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider p_346030_) {
        ItemStack compassToCopy = ItemStack.EMPTY;
        int compassesToCreate = 1;

        for (int j = 0; j < input.size(); j++) {
            ItemStack itemStack = input.getItem(j);
            if (!itemStack.isEmpty())
                if (itemStack.is(PPItems.TRAVELERS_COMPASS.get())) {
                    if (!compassToCopy.isEmpty())
                        return ItemStack.EMPTY;
                    compassToCopy = itemStack;
                } else if (itemStack.is(Items.COMPASS))
                    compassesToCreate++;
                else return ItemStack.EMPTY;
        }

        if (!compassToCopy.isEmpty() && compassToCopy.has(PPComponents.TRAVELERS_COMPASS_TRACKER) && compassesToCreate >= 1) {
            ItemStack result = new ItemStack(PPItems.TRAVELERS_COMPASS.get(), compassesToCreate);
            result.set(PPComponents.TRAVELERS_COMPASS_TRACKER, compassToCopy.get(PPComponents.TRAVELERS_COMPASS_TRACKER));
            return result;
        } else return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth >= 3 && pHeight >= 3;
    }

    @Override
    public RecipeSerializer<TravelersCompassCloningRecipe> getSerializer() {
        return PPRecipeSerializers.CRAFTING_SPECIAL_COMPASS_CLONING.get();
    }
}

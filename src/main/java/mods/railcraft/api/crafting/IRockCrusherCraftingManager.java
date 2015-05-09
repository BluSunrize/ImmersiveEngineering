/*
 * ******************************************************************************
 *  Copyright 2011-2015 CovertJaguar
 *
 *  This work (the API) is licensed under the "MIT" License, see LICENSE.md for details.
 * ***************************************************************************
 */

package mods.railcraft.api.crafting;

import java.util.List;
import net.minecraft.item.ItemStack;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public interface IRockCrusherCraftingManager {

    IRockCrusherRecipe createNewRecipe(ItemStack input, boolean matchDamage, boolean matchNBT);

    IRockCrusherRecipe getRecipe(ItemStack input);

    List<? extends IRockCrusherRecipe> getRecipes();
}

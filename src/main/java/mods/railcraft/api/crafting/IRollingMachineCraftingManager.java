/*
 * ******************************************************************************
 *  Copyright 2011-2015 CovertJaguar
 *
 *  This work (the API) is licensed under the "MIT" License, see LICENSE.md for details.
 * ***************************************************************************
 */

package mods.railcraft.api.crafting;

import java.util.List;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public interface IRollingMachineCraftingManager
{

    void addRecipe(ItemStack output, Object... components);

    void addShapelessRecipe(ItemStack output, Object... compenents);

    ItemStack findMatchingRecipe(InventoryCrafting inventorycrafting, World world);

    List<IRecipe> getRecipeList();

}

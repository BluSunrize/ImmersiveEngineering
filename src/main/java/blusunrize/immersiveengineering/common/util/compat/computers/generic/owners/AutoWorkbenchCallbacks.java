/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.AutoWorkbenchBlockEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.IndexArgument;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.PoweredMBCallbacks;
import net.minecraft.world.item.ItemStack;

public class AutoWorkbenchCallbacks extends MultiblockCallbackOwner<AutoWorkbenchBlockEntity>
{
	public AutoWorkbenchCallbacks()
	{
		super(AutoWorkbenchBlockEntity.class, "auto_workbench");
		addAdditional(PoweredMBCallbacks.INSTANCE);
	}

	@ComputerCallable
	public void selectRecipe(CallbackEnvironment<AutoWorkbenchBlockEntity> env, @IndexArgument int selected)
	{
		BlueprintCraftingRecipe[] availableRecipes = env.object().getAvailableRecipes();
		if(selected < 0||selected >= availableRecipes.length)
			throw new RuntimeException("Only "+availableRecipes.length+" recipes are available");
		env.object().selectedRecipe = selected;
	}

	@ComputerCallable
	public void unselectRecipe(CallbackEnvironment<AutoWorkbenchBlockEntity> env)
	{
		env.object().selectedRecipe = -1;
	}

	@ComputerCallable
	public ItemStack[] getAvailableRecipes(CallbackEnvironment<AutoWorkbenchBlockEntity> env)
	{
		BlueprintCraftingRecipe[] availableRecipes = env.object().getAvailableRecipes();
		ItemStack[] outputs = new ItemStack[availableRecipes.length];
		for(int i = 0; i < availableRecipes.length; ++i)
			outputs[i] = availableRecipes[i].output.get();
		return outputs;
	}

	@ComputerCallable
	public int getSelectedRecipe(CallbackEnvironment<AutoWorkbenchBlockEntity> env)
	{
		return env.object().selectedRecipe+1;
	}
}

/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.AutoWorkbenchLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.AutoWorkbenchLogic.State;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.IndexArgument;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.MBEnergyCallbacks;
import net.minecraft.world.item.ItemStack;

public class AutoWorkbenchCallbacks extends Callback<State>
{
	public AutoWorkbenchCallbacks()
	{
		addAdditional(MBEnergyCallbacks.INSTANCE, State::getEnergy);
	}

	@ComputerCallable
	public boolean isRunning(CallbackEnvironment<State> env)
	{
		return env.object().active;
	}

	@ComputerCallable
	public void selectRecipe(CallbackEnvironment<State> env, @IndexArgument int selected)
	{
		BlueprintCraftingRecipe[] availableRecipes = AutoWorkbenchLogic.getAvailableRecipes(env.level(), env.object());
		if(selected < 0||selected >= availableRecipes.length)
			throw new RuntimeException("Only "+availableRecipes.length+" recipes are available");
		env.object().selectedRecipe = selected;
	}

	@ComputerCallable
	public void unselectRecipe(CallbackEnvironment<State> env)
	{
		env.object().selectedRecipe = -1;
	}

	@ComputerCallable
	public ItemStack[] getAvailableRecipes(CallbackEnvironment<State> env)
	{
		BlueprintCraftingRecipe[] availableRecipes = AutoWorkbenchLogic.getAvailableRecipes(env.level(), env.object());
		ItemStack[] outputs = new ItemStack[availableRecipes.length];
		for(int i = 0; i < availableRecipes.length; ++i)
			outputs[i] = availableRecipes[i].output.get();
		return outputs;
	}

	@ComputerCallable
	public int getSelectedRecipe(CallbackEnvironment<State> env)
	{
		return env.object().selectedRecipe+1;
	}
}

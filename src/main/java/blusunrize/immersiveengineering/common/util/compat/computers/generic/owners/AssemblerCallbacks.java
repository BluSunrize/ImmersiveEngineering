/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.AssemblerLogic.State;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.IndexArgument;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.InventoryCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.MBEnergyCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.TankCallbacks;
import net.minecraft.world.item.ItemStack;

public class AssemblerCallbacks extends Callback<State>
{
	public AssemblerCallbacks()
	{
		addAdditional(MBEnergyCallbacks.INSTANCE, s -> s.energy);
		addAdditional(InventoryCallbacks.fromHandler(State::getInventory, 0, 18, "input"));
		addAdditional(InventoryCallbacks.fromHandler(State::getInventory, 18, 3, "buffer"));
		for(int i = 0; i < 3; ++i)
		{
			final int finalI = i;
			addAdditional(new TankCallbacks<>(te -> te.tanks[finalI], "tank "+(i+1)));
		}
	}

	@ComputerCallable
	public boolean isValidRecipe(CallbackEnvironment<State> env, @IndexArgument int recipe)
	{
		checkRecipeIndex(recipe);
		return !env.object().patterns[recipe].inv.get(9).isEmpty();
	}

	//TODO
	/*@ComputerCallable
	public void setRecipeEnabled(CallbackEnvironment<State> env, @IndexArgument int recipe, boolean enabled)
	{
		checkRecipeIndex(recipe);
		env.object().computerControlByRecipe[recipe].setEnabled(enabled);
	}*/

	@ComputerCallable
	public ItemStack getRecipeInputStack(
			CallbackEnvironment<State> env, @IndexArgument int recipe, @IndexArgument int slot
	)
	{
		checkRecipeIndex(recipe);
		if(slot < 0||slot >= 9)
			throw new IllegalArgumentException("Recipe input stacks are 1-9");
		return env.object().patterns[recipe].inv.get(slot);
	}

	private void checkRecipeIndex(int javaIndex)
	{
		if(javaIndex >= 3||javaIndex < 0)
			throw new IllegalArgumentException("Only recipes 1-3 are available");
	}
}

/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.AssemblerBlockEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerControlState;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.IndexArgument;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.EnergyCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.InventoryCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.TankCallbacks;
import net.minecraft.world.item.ItemStack;

public class AssemblerCallbacks extends MultiblockCallbackOwner<AssemblerBlockEntity>
{
	public AssemblerCallbacks()
	{
		super(AssemblerBlockEntity.class, "assembler");
		addAdditional(EnergyCallbacks.INSTANCE);
		addAdditional(new InventoryCallbacks<>(te -> te.inventory, 0, 18, "input"));
		addAdditional(new InventoryCallbacks<>(te -> te.inventory, 18, 3, "buffer"));
		for(int i = 0; i < 3; ++i)
		{
			final int finalI = i;
			addAdditional(new TankCallbacks<>(te -> te.tanks[finalI], "tank "+(i+1)));
		}
	}

	@ComputerCallable
	public boolean isValidRecipe(CallbackEnvironment<AssemblerBlockEntity> env, @IndexArgument int recipe)
	{
		checkRecipeIndex(recipe);
		return !env.getObject().patterns[recipe].inv.get(9).isEmpty();
	}

	@ComputerCallable
	public void setRecipeEnabled(CallbackEnvironment<AssemblerBlockEntity> env, @IndexArgument int recipe, boolean enabled)
	{
		checkRecipeIndex(recipe);
		env.getObject().computerControlByRecipe[recipe] = new ComputerControlState(env.getIsAttached(), enabled);
	}

	@ComputerCallable
	public ItemStack getRecipeInputStack(
			CallbackEnvironment<AssemblerBlockEntity> env, @IndexArgument int recipe, @IndexArgument int slot
	)
	{
		checkRecipeIndex(recipe);
		if(slot < 0||slot >= 9)
			throw new IllegalArgumentException("Recipe input stacks are 1-9");
		return env.getObject().patterns[recipe].inv.get(slot);
	}

	private void checkRecipeIndex(int javaIndex)
	{
		if(javaIndex >= 3||javaIndex < 0)
			throw new IllegalArgumentException("Only recipes 1-3 are available");
	}
}

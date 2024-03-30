/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions;

import blusunrize.immersiveengineering.api.Lib;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.bracket.CommandStringDisplayable;
import com.blamejared.crafttweaker.api.fluid.MCFluidStackMutable;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.fluids.FluidStack;

public class ActionAddRecipeCustomOutput<T extends Recipe<?>> extends ActionAddRecipe<T>
{
	private final String output;

	public ActionAddRecipeCustomOutput(IRecipeManager<T> recipeManager, T recipe, FluidStack output)
	{
		this(recipeManager, recipe, new MCFluidStackMutable(output).getCommandString());
	}

	public ActionAddRecipeCustomOutput(IRecipeManager<T> recipeManager, T recipe, CommandStringDisplayable output)
	{
		this(recipeManager, recipe, output.getCommandString());
	}

	public ActionAddRecipeCustomOutput(IRecipeManager<T> recipeManager, T recipe, String output)
	{
		super(recipeManager, recipe, null);
		this.output = output;
	}

//	@Override
//	protected String describeOutputs()
//	{
//		return output;
//	}
}

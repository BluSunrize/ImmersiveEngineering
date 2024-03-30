/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.crafting.GeneratedListRecipe;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.base.IRuntimeAction;
import com.blamejared.crafttweaker.api.bracket.CommandStringDisplayable;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker.natives.fluid.ExpandFluid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluid;

import java.util.Iterator;
import java.util.Map;

public abstract class AbstractActionGenericRemoveRecipe<T extends Recipe<?>> implements IRuntimeAction
{

	private final IRecipeManager<T> manager;
	private final String output;

	public AbstractActionGenericRemoveRecipe(IRecipeManager<T> manager, CommandStringDisplayable output)
	{
		this(manager, output.getCommandString());
	}

	public AbstractActionGenericRemoveRecipe(IRecipeManager<T> manager, Fluid fluid)
	{
		this(manager, ExpandFluid.getCommandString(fluid));
	}

	public AbstractActionGenericRemoveRecipe(IRecipeManager<T> manager, String output)
	{
		this.manager = manager;
		this.output = output;
	}

	@Override
	public String systemName()
	{
		return Lib.MODID;
	}

	@Override
	public void apply()
	{
		int count = 0;
		final Iterator<Map.Entry<ResourceLocation, T>> iterator = manager.getRecipes()
				.entrySet()
				.iterator();

		try
		{
			while(iterator.hasNext())
			{
				final T recipe = iterator.next().getValue();
				if(recipe instanceof GeneratedListRecipe)
				{
					logger().debug("Skipping GeneratedListRecipe '{}'", recipe.getId());
					continue;
				}

				if(shouldRemove(recipe))
				{
					iterator.remove();
					count++;
				}
			}
		} catch(ClassCastException exception)
		{
			logger().error(
					"There is an illegal entry in "+manager.getCommandString()+" that caused an exception: ", exception
			);
		}

		logger().info("Removed {} \"{}\" recipes", count, manager.getCommandString());
	}

	public abstract boolean shouldRemove(T recipe);

	@Override
	public String describe()
	{
		return "Removing all \""+manager.getCommandString()+"\" recipes, that output: "+output;
	}
}

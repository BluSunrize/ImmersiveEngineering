/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions;

import blusunrize.immersiveengineering.common.crafting.GeneratedListRecipe;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.actions.IRuntimeAction;
import com.blamejared.crafttweaker.api.brackets.CommandStringDisplayable;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl_native.fluid.ExpandFluid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluid;

import java.util.Iterator;
import java.util.Map;

public abstract class AbstractActionGenericRemoveRecipe<T extends Recipe<?>> implements IRuntimeAction
{

	private final IRecipeManager manager;
	private final String output;

	public AbstractActionGenericRemoveRecipe(IRecipeManager manager, CommandStringDisplayable output)
	{
		this(manager, output.getCommandString());
	}

	public AbstractActionGenericRemoveRecipe(IRecipeManager manager, Fluid fluid)
	{
		this(manager, ExpandFluid.getCommandString(fluid));
	}

	public AbstractActionGenericRemoveRecipe(IRecipeManager manager, String output)
	{
		this.manager = manager;
		this.output = output;
	}

	@Override
	public void apply()
	{
		int count = 0;
		final Iterator<Map.Entry<ResourceLocation, Recipe<?>>> iterator = manager.getRecipes()
				.entrySet()
				.iterator();

		try
		{
			while(iterator.hasNext())
			{
				final Recipe<?> recipe = iterator.next().getValue();
				if(recipe instanceof GeneratedListRecipe)
				{
					CraftTweakerAPI.logDebug("Skipping GeneratedListRecipe '%s'", recipe.getId());
					continue;
				}

				//noinspection unchecked
				if(shouldRemove((T)recipe))
				{
					iterator.remove();
					count++;
				}
			}
		} catch(ClassCastException exception)
		{
			CraftTweakerAPI.logThrowing("There is an illegal entry in %s that caused an exception: ", exception, manager
					.getCommandString());
		}

		CraftTweakerAPI.logInfo("Removed %s \"%s\" recipes", count, manager.getCommandString());
	}

	public abstract boolean shouldRemove(T recipe);

	@Override
	public String describe()
	{
		return "Removing all \""+manager.getCommandString()+"\" recipes, that output: "+output;
	}
}

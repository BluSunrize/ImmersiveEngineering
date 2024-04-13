/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */
package blusunrize.immersiveengineering.common.util.compat.crafttweaker.actions;

import blusunrize.immersiveengineering.api.Lib;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.MCItemStackMutable;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

public abstract class AbstractActionRemoveMultipleOutputs<T extends Recipe<?>> extends AbstractActionGenericRemoveRecipe<T>
{

	private final IIngredient output;

	public AbstractActionRemoveMultipleOutputs(IRecipeManager<T> manager, IIngredient output)
	{
		super(manager, output);
		this.output = output;
	}

	@Override
	public String systemName()
	{
		return Lib.MODID;
	}

	@Override
	public boolean shouldRemove(T recipe)
	{
		return getAllOutputs(recipe).stream().map(MCItemStackMutable::new).anyMatch(output::matches);
	}

	public abstract List<ItemStack> getAllOutputs(T recipe);
}

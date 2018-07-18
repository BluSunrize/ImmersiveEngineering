/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class BlastFurnaceRecipeWrapper implements IRecipeWrapper
{
	private final List<ItemStack> inputs;
	private final ItemStack output;
	private final ItemStack slag;

	public BlastFurnaceRecipeWrapper(BlastFurnaceRecipe recipe)
	{
		this.inputs = (List<ItemStack>)(recipe.input instanceof List?recipe.input: Arrays.asList((ItemStack)recipe.input));
		this.output = recipe.output;
		this.slag = recipe.slag;
	}


	@Override
	public void getIngredients(IIngredients ingredients)
	{
		ingredients.setInputs(ItemStack.class, inputs);
		ingredients.setOutputs(ItemStack.class, Arrays.asList(output, slag));
	}

	public ItemStack getSmeltingOutput()
	{
		return output;
	}

	public ItemStack getSlagOutput()
	{
		return slag;
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
	}
}